package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.Ecstatic;
import com.angryalchemist.ecstatic.debug.LodDebugState;
import com.angryalchemist.ecstatic.lod.RegionCoord;
import com.angryalchemist.ecstatic.lod.RegionLodCoordinator;
import com.angryalchemist.ecstatic.lod.RingConfig;
import com.angryalchemist.ecstatic.storage.LodRegionFile;
import com.angryalchemist.ecstatic.storage.LodStoragePaths;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LodRenderer {
    private static final int[] LOD_LEVELS = new int[]{0, 1, 2, 3, 4, 5};
    private static final int ALPHA_FADE_WIDTH_CHUNKS = 12;
    static final int EDGE_SAFETY_MARGIN_CHUNKS = 4;
    private static final int FADE_REFRESH_THRESHOLD_CHUNKS = 2;
    private static final long FADE_REFRESH_TIME_BUDGET_NANOS = TimeUnit.MICROSECONDS.toNanos(3000L);
    private static final long COORDINATOR_BUILD_TIME_BUDGET_NANOS = TimeUnit.MICROSECONDS.toNanos(4500L);
    private static final int SHADER_ACTIVE_BUDGET_DIVISOR = 3;
    private static final float SKY_DARKEN_MIN = 0.2F;
    private static final float SKY_DARKEN_MAX = 1.0F;
    private static final float MAX_FOG_AT_FAR_EDGE = 0.45F;
    private static final float PLACEHOLDER_FOG_GAP_SCALE = 0.5F;
    private static volatile Matrix4f capturedTrueRotation;
    private static MinecraftServer loadedServer;
    private static ResourceKey<Level> loadedDimension;
    private static Path loadedStorageDir;
    private static final Map<Integer, Map<RegionCoord, LodRegionMesh>> meshesByLevel = new HashMap<>();
    private static final Map<RegionCoord, Integer> meshLevelByRegion = new HashMap<>();
    private static final Map<RegionCoord, Integer> latestDispatchedLevel = new HashMap<>();
    private static final ConcurrentLinkedQueue<LodRenderer.GeometryBuildResult> pendingCoordinatorBuilds = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<LodRenderer.GeometryBuildResult> pendingFadeBuilds = new ConcurrentLinkedQueue<>();
    private static Vec3 lastFadeRefreshOrigin;
    private static final Set<LodRenderer.PendingMeshRefresh> pendingFadeRefreshes = ConcurrentHashMap.newKeySet();

    private static long frameBudgetNanos(long normalBudgetNanos) {
        return !IrisCompat.isShaderPackActive() ? normalBudgetNanos : normalBudgetNanos / 3L;
    }

    private LodRenderer() {
    }

    public static void captureTrueRotation(Matrix4f matrix) {
        capturedTrueRotation = matrix;
    }

    private static Matrix4f trueRotationMatrix(Camera camera) {
        Matrix4f captured = capturedTrueRotation;
        if (captured != null) {
            return captured;
        }

        Matrix4f fallback = new Matrix4f();
        fallback.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
        fallback.rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        return fallback;
    }

    public static void render(Matrix4f projectionMatrix, Camera camera) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (server != null && clientLevel != null) {
            ResourceKey<Level> dimension = clientLevel.dimension();
            if (dimension.equals(Level.OVERWORLD)) {
                if (server != loadedServer || !dimension.equals(loadedDimension)) {
                    reload(server, dimension, clientLevel);
                }

                Vec3 cameraPos = camera.getPosition();
                Registry<Biome> biomeRegistry = clientLevel.registryAccess().registryOrThrow(Registries.BIOME);
                tickCoordinator(cameraPos, biomeRegistry);
                tickBootstrap(cameraPos, biomeRegistry);
                drainPendingCoordinatorBuilds();
                maybeRefreshFade(cameraPos, biomeRegistry);
                drainPendingFadeBuilds();
                if (LodDebugState.isReferenceQuadEnabled()) {
                    LodDebugReferenceQuad.render(camera, projectionMatrix);
                }

                float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
                Matrix4f modelViewMatrix = new Matrix4f();
                applyVanillaCameraBob(modelViewMatrix, partialTick);
                modelViewMatrix.mul(trueRotationMatrix(camera));
                Matrix4f rotationOnlyMatrix = new Matrix4f(modelViewMatrix);
                modelViewMatrix.translate(-((float) cameraPos.x), -((float) cameraPos.y), -((float) cameraPos.z));
                float dayFraction = clamp01((clientLevel.getSkyDarken(partialTick) - 0.2F) / 0.8F);
                float night = LodSettingsConfig.get().nightBrightness();
                float day = LodSettingsConfig.get().dayBrightness();
                float brightness = night + (day - night) * dayFraction;
                RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
                int clientRenderDistanceChunks = Minecraft.getInstance().options.getEffectiveRenderDistance();
                Matrix4f lodProjectionMatrix = LodFarPlaneProjection.withExtendedFarPlane(projectionMatrix, extendedFarPlaneBlocks(clientRenderDistanceChunks));
                Matrix4f viewProjectionMatrix = new Matrix4f(lodProjectionMatrix).mul(modelViewMatrix);
                FrustumIntersection frustum = new FrustumIntersection(viewProjectionMatrix);
                boolean cullingEnabled = LodSettingsConfig.get().frustumCullingEnabled();
                float minBuildHeight = clientLevel.getMinBuildHeight();
                float maxBuildHeight = clientLevel.getMaxBuildHeight();
                int forcedLevel = LodDebugState.forcedLevel();
                List<LodRegionMesh> visibleMeshes = new ArrayList<>();
                List<LodRegionMesh> visiblePlaceholderMeshes = new ArrayList<>();

                for (int level : LOD_LEVELS) {
                    if (forcedLevel == 0 || level == forcedLevel) {
                        Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(level);
                        if (meshes != null) {
                            List<LodRegionMesh> target = level == 0 ? visiblePlaceholderMeshes : visibleMeshes;
                            if (!cullingEnabled) {
                                target.addAll(meshes.values());
                            } else {
                                for (Entry<RegionCoord, LodRegionMesh> entry : meshes.entrySet()) {
                                    if (isRegionVisible(frustum, entry.getKey(), minBuildHeight, maxBuildHeight)) {
                                        target.add(entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }

                Vector3f sunDirection = LodOceanPlane.sunDirection(clientLevel.getTimeOfDay(partialTick) * 360.0F);
                LodParallaxShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
                LodWaterShader.setSunDirection(sunDirection.x, sunDirection.y, sunDirection.z);
                LodWaterShader.setGameTime(RenderSystem.getShaderGameTime());
                float savedFogStart = RenderSystem.getShaderFogStart();
                float savedFogEnd = RenderSystem.getShaderFogEnd();
                RingConfig ringConfigForFog = Ecstatic.currentRingConfig(clientRenderDistanceChunks);
                float fogStartBlocks = clientRenderDistanceChunks * 16.0F;
                float lod4EdgeBlocks = ringConfigForFog.outerBoundary(5) * 16.0F;
                float fogGapBlocks = (lod4EdgeBlocks - fogStartBlocks) / 0.45F * LodSettingsConfig.get().fogFalloffScale();
                float fogEndBlocks = fogStartBlocks + fogGapBlocks;
                float placeholderFogEndBlocks = fogStartBlocks + fogGapBlocks * 0.5F;
                RenderSystem.setShaderFogStart(fogStartBlocks);
                RenderSystem.setShaderFogEnd(fogEndBlocks);
                float fogIntensity = LodSettingsConfig.get().fogIntensity();
                LodFogShader.setFogIntensity(fogIntensity);
                LodTreeShader.setFogIntensity(fogIntensity);
                LodWaterShader.setFogIntensity(fogIntensity);
                LodFogShader.setSaturation(LodSettingsConfig.get().saturationReduction());
                boolean cameraUnderwater = camera.getFluidInCamera() == FogType.WATER;
                if (!cameraUnderwater) {
                    Object terrainPhaseToken = IrisCompat.beginTerrainPhase();
                    LodRegionMesh.renderTerrainLit(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
                    RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
                    LodRegionMesh.renderTerrainLit(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
                    RenderSystem.setShaderFogEnd(fogEndBlocks);
                    IrisCompat.endPhase(terrainPhaseToken);
                }

                Object neutralPhaseToken = IrisCompat.beginNeutralPhase();
                if (!cameraUnderwater) {
                    LodRegionMesh.renderTerrainCheap(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
                    LodRegionMesh.renderTrees(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
                    RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
                    LodRegionMesh.renderTerrainCheap(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
                    RenderSystem.setShaderFogEnd(fogEndBlocks);
                }

                LodRegionMesh.renderWaterCheap(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
                RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
                LodRegionMesh.renderWaterCheap(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
                RenderSystem.setShaderFogEnd(fogEndBlocks);
                IrisCompat.endPhase(neutralPhaseToken);
                Object translucentPhaseToken = IrisCompat.beginTranslucentPhase();
                LodRegionMesh.renderWaterLit(visibleMeshes, modelViewMatrix, lodProjectionMatrix);
                RenderSystem.setShaderFogEnd(placeholderFogEndBlocks);
                LodRegionMesh.renderWaterLit(visiblePlaceholderMeshes, modelViewMatrix, lodProjectionMatrix);
                RenderSystem.setShaderFogEnd(fogEndBlocks);
                IrisCompat.endPhase(translucentPhaseToken);
                RenderSystem.setShaderFogStart(savedFogStart);
                RenderSystem.setShaderFogEnd(savedFogEnd);
                int lodCloudRadiusBlocks = ringConfigForFog.outerBoundary(5) * 16;
                LodCloudExtension.render(rotationOnlyMatrix, lodProjectionMatrix, clientLevel, camera, partialTick, lodCloudRadiusBlocks);
                LodStructureIslands.render(modelViewMatrix, lodProjectionMatrix, clientLevel, cameraPos);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private static boolean isRegionVisible(FrustumIntersection frustum, RegionCoord region, float minBuildHeight, float maxBuildHeight) {
        float minX = region.originBlockX();
        float minZ = region.originBlockZ();
        float maxX = minX + 512.0F;
        float maxZ = minZ + 512.0F;
        return frustum.testAab(minX, minBuildHeight, minZ, maxX, maxBuildHeight, maxZ);
    }

    private static float clamp01(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }

    private static void applyVanillaCameraBob(Matrix4f modelViewMatrix, float partialTick) {
        applyHurtBob(modelViewMatrix, partialTick);
        if (Minecraft.getInstance().options.bobView().get()) {
            applyViewBob(modelViewMatrix, partialTick);
        }
    }

    private static void applyHurtBob(Matrix4f modelViewMatrix, float partialTick) {
        if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.isDeadOrDying()) {
                float deathTime = Math.min(livingEntity.deathTime + partialTick, 20.0F);
                modelViewMatrix.rotate(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (deathTime + 200.0F)));
            }

            float hurtTime = livingEntity.hurtTime - partialTick;
            if (!(hurtTime < 0.0F)) {
                hurtTime /= livingEntity.hurtDuration;
                hurtTime = Mth.sin(hurtTime * hurtTime * hurtTime * hurtTime * (float) Math.PI);
                float hurtDir = livingEntity.getHurtDir();
                modelViewMatrix.rotate(Axis.YP.rotationDegrees(-hurtDir));
                modelViewMatrix.rotate(Axis.ZP.rotationDegrees(-hurtTime * 14.0F));
                modelViewMatrix.rotate(Axis.YP.rotationDegrees(hurtDir));
            }
        }
    }

    private static void applyViewBob(Matrix4f modelViewMatrix, float partialTick) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player player) {
            float var6 = player.walkDist - player.walkDistO;
            float bobPhase = -(player.walkDist + var6 * partialTick);
            float bobAmount = Mth.lerp(partialTick, player.oBob, player.bob);
            modelViewMatrix.translate(Mth.sin(bobPhase * (float) Math.PI) * bobAmount * 0.5F, -Math.abs(Mth.cos(bobPhase * (float) Math.PI) * bobAmount), 0.0F);
            modelViewMatrix.rotate(Axis.ZP.rotationDegrees(Mth.sin(bobPhase * (float) Math.PI) * bobAmount * 3.0F));
            modelViewMatrix.rotate(Axis.XP.rotationDegrees(Math.abs(Mth.cos(bobPhase * (float) Math.PI - 0.2F) * bobAmount) * 5.0F));
        }
    }

    private static void reload(MinecraftServer server, ResourceKey<Level> dimension, ClientLevel clientLevel) {
        unload();
        loadedServer = server;
        loadedDimension = dimension;
        loadedStorageDir = LodStoragePaths.dimensionStorageDir(server.getWorldPath(LevelResource.ROOT), dimension);
        int renderDistanceChunks = Minecraft.getInstance().options.getEffectiveRenderDistance();
        float farPlaneBlocks = renderDistanceChunks * 16.0F * 4.0F;
        Constants.LOG
                .info(
                        "Ecstatic renderer: client render distance={} chunks, graphics mode={}, vanilla far clip plane ~= {} blocks (LOD rings beyond this are frustum-clipped)",
                        renderDistanceChunks, Minecraft.getInstance().options.graphicsMode().get(), farPlaneBlocks);
    }

    private static float extendedFarPlaneBlocks(int clientRenderDistanceChunks) {
        RingConfig ringConfig = Ecstatic.currentRingConfig(clientRenderDistanceChunks);
        float ringFarBlocks = (ringConfig.outerBoundary(5) + ringConfig.hysteresisChunks) * 16.0F;
        return Math.max(ringFarBlocks, 4096.0F) * 1.05F;
    }

    private static LodRegionMesh.FadeParams fadeParamsAt(Vec3 origin) {
        int clientRenderDistanceChunks = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int ring1StartChunks = RingConfig.ring1StartChunks(clientRenderDistanceChunks);
        float fadeStartBlocks = ring1StartChunks * 16.0F;
        float fadeEndBlocks = Math.min(fadeStartBlocks + 192.0F, Math.max(fadeStartBlocks + 16.0F, (clientRenderDistanceChunks - 4) * 16.0F));
        return new LodRegionMesh.FadeParams((int) origin.x, (int) origin.z, (int) origin.y, fadeStartBlocks, fadeEndBlocks);
    }

    private static void tickBootstrap(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
        RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
        if (coordinator != null) {
            List<RegionCoord> readyRegions = coordinator.drainBootstrapReady();
            if (!readyRegions.isEmpty()) {
                LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);
                LodRegionFile file = coordinator.bootstrapFile();
                double playerChunkX = cameraPos.x / 16.0;
                double playerChunkZ = cameraPos.z / 16.0;

                for (RegionCoord region : readyRegions) {
                    latestDispatchedLevel.put(region, 0);
                    double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
                    coordinator.submitBackgroundTask(0, distanceChunks, () -> {
                        try {
                            LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
                            pendingCoordinatorBuilds.add(new LodRenderer.GeometryBuildResult(region, 0, recorded));
                        } catch (Exception e) {
                            Constants.LOG.error("Ecstatic failed to build bootstrap geometry for region ({}, {})", region.x(), region.z(), e);
                        }
                    });
                }
            }
        }
    }

    private static void tickCoordinator(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
        RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
        if (coordinator != null) {
            double playerChunkX = cameraPos.x / 16.0;
            double playerChunkZ = cameraPos.z / 16.0;
            coordinator.tick(playerChunkX, playerChunkZ, cameraPos.y);
            List<RegionLodCoordinator.RegionReadyResult> readyResults = coordinator.drainReady();
            if (!readyResults.isEmpty()) {
                LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);

                for (RegionLodCoordinator.RegionReadyResult result : readyResults) {
                    RegionCoord region = result.region();
                    if (result.level() == -1) {
                        latestDispatchedLevel.remove(region);
                        evictMesh(region);
                    } else {
                        int level = result.level();
                        latestDispatchedLevel.put(region, level);
                        LodRegionFile file = coordinator.fileForLevel(level);
                        double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
                        coordinator.submitBackgroundTask(
                                level,
                                distanceChunks,
                                () -> {
                                    try {
                                        LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
                                        pendingCoordinatorBuilds.add(new LodRenderer.GeometryBuildResult(region, level, recorded));
                                    } catch (Exception e) {
                                        Constants.LOG.error("Ecstatic failed to build bootstrap geometry for region ({}, {})", region.x(), region.z(), e);
                                    }
                                }
                        );
                    }
                }
            }
        }
    }

    private static void drainPendingCoordinatorBuilds() {
        if (!pendingCoordinatorBuilds.isEmpty()) {
            long deadlineNanos = System.nanoTime() + frameBudgetNanos(COORDINATOR_BUILD_TIME_BUDGET_NANOS);
            int processed = 0;

            while (!pendingCoordinatorBuilds.isEmpty() && (processed == 0 || System.nanoTime() < deadlineNanos)) {
                processed++;
                applyGeometryBuildResult(pendingCoordinatorBuilds.poll());
            }
        }
    }

    private static void evictMesh(RegionCoord region) {
        Integer oldLevel = meshLevelByRegion.remove(region);
        if (oldLevel != null) {
            Map<RegionCoord, LodRegionMesh> oldMeshes = meshesByLevel.get(oldLevel);
            if (oldMeshes != null) {
                LodRegionMesh oldMesh = oldMeshes.remove(region);
                if (oldMesh != null) {
                    oldMesh.close();
                }
            }
        }
    }

    private static void applyGeometryBuildResult(LodRenderer.GeometryBuildResult result) {
        Integer expectedLevel = latestDispatchedLevel.get(result.region());
        if (expectedLevel != null && expectedLevel == result.level()) {
            latestDispatchedLevel.remove(result.region());
            evictMesh(result.region());
            LodRegionMesh mesh = LodRegionMesh.upload(result.recorded());
            meshesByLevel.computeIfAbsent(result.level(), k -> new HashMap<>()).put(result.region(), mesh);
            meshLevelByRegion.put(result.region(), result.level());
        }
    }

    private static void maybeRefreshFade(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
        if (lastFadeRefreshOrigin == null) {
            lastFadeRefreshOrigin = cameraPos;
        } else {
            double thresholdBlocks = 32.0;
            if (cameraPos.distanceToSqr(lastFadeRefreshOrigin) >= thresholdBlocks * thresholdBlocks) {
                lastFadeRefreshOrigin = cameraPos;
                queuePendingFadeRefresh(0);
                queuePendingFadeRefresh(1);
            }
        }

        drainPendingFadeRefreshes(cameraPos, biomeRegistry);
    }

    private static void queuePendingFadeRefresh(int level) {
        Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(level);
        if (meshes != null && !meshes.isEmpty()) {
            for (RegionCoord region : meshes.keySet()) {
                pendingFadeRefreshes.add(new LodRenderer.PendingMeshRefresh(level, region));
            }
        }
    }

    private static void drainPendingFadeRefreshes(Vec3 cameraPos, Registry<Biome> biomeRegistry) {
        if (!pendingFadeRefreshes.isEmpty()) {
            RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
            LodRegionMesh.FadeParams fade = fadeParamsAt(cameraPos);
            double playerChunkX = cameraPos.x / 16.0;
            double playerChunkZ = cameraPos.z / 16.0;
            Iterator<LodRenderer.PendingMeshRefresh> iterator = pendingFadeRefreshes.iterator();

            while (iterator.hasNext()) {
                LodRenderer.PendingMeshRefresh pending = iterator.next();
                iterator.remove();
                LodRegionFile file = levelFile(pending.level());
                if (file != null) {
                    Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(pending.level());
                    if (meshes != null && meshes.containsKey(pending.region())) {
                        RegionCoord region = pending.region();
                        int level = pending.level();
                        latestDispatchedLevel.put(region, level);
                        double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
                        Runnable buildTask = () -> {
                            try {
                                LodRegionMesh.RecordedRegionMesh recorded = LodRegionMesh.buildGeometry(file, region, fade, biomeRegistry);
                                pendingFadeBuilds.add(new LodRenderer.GeometryBuildResult(region, level, recorded));
                            } catch (Exception e) {
                                Constants.LOG
                                        .error(
                                                "Ecstatic failed to build fade-refresh geometry for region ({}, {}) at LOD{}", region.x(), region.z(), level, e
                                        );
                            }
                        };
                        if (coordinator != null) {
                            coordinator.submitBackgroundTask(level, distanceChunks, buildTask);
                        } else {
                            buildTask.run();
                        }
                    }
                }
            }
        }
    }

    private static void drainPendingFadeBuilds() {
        if (!pendingFadeBuilds.isEmpty()) {
            long deadlineNanos = System.nanoTime() + frameBudgetNanos(FADE_REFRESH_TIME_BUDGET_NANOS);
            int processed = 0;

            while (!pendingFadeBuilds.isEmpty() && (processed == 0 || System.nanoTime() < deadlineNanos)) {
                processed++;
                applyGeometryBuildResult(pendingFadeBuilds.poll());
            }
        }
    }

    private static LodRegionFile levelFile(int level) {
        RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
        if (coordinator == null) {
            return null;
        } else {
            return level == 0 ? coordinator.bootstrapFile() : coordinator.fileForLevel(level);
        }
    }

    public static boolean isOverworldLoaded() {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        return clientLevel != null && clientLevel.dimension().equals(Level.OVERWORLD);
    }

    public static void rebuildAllMeshes() {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel != null) {
            Registry<Biome> biomeRegistry = clientLevel.registryAccess().registryOrThrow(Registries.BIOME);
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            LodRegionMesh.FadeParams fade = fadeParamsAt(camera.getPosition());
            RegionLodCoordinator coordinator = Ecstatic.activeCoordinator();
            if (coordinator != null) {
                refreshLevelMeshes(0, coordinator.bootstrapFile(), fade, biomeRegistry);

                for (int level = 1; level <= 5; level++) {
                    refreshLevelMeshes(level, coordinator.fileForLevel(level), fade, biomeRegistry);
                }
            }
        }
    }

    private static void refreshLevelMeshes(int level, LodRegionFile file, LodRegionMesh.FadeParams fade, Registry<Biome> biomeRegistry) {
        if (file == null) return;

        Map<RegionCoord, LodRegionMesh> meshes = meshesByLevel.get(level);
        if (meshes == null || meshes.isEmpty()) return;

        Map<RegionCoord, LodRegionMesh> updatedMeshes = new HashMap<>();

        for (Entry<RegionCoord, LodRegionMesh> entry : meshes.entrySet()) {
            RegionCoord region = entry.getKey();
            LodRegionMesh oldMesh = entry.getValue();
            LodRegionMesh newMesh = null;

            try {
                // try and build + upload a new mesh
                newMesh = LodRegionMesh.build(file, region, fade, biomeRegistry);

                // if successful, close the old mesh and store the new one
                if (oldMesh != null) {
                    oldMesh.close();
                }
                updatedMeshes.put(region, newMesh);
            } catch (Exception e) {
                Constants.LOG.error("Failed to rebuild mesh for region ({}, {}) at LOD{}", region.x(), region.z(), level, e);

                if (newMesh != null) {
                    try {
                        newMesh.close();
                    } catch (Exception closeEx) {
                        // suppress nested cleanup exception
                    }
                }
                updatedMeshes.put(region, oldMesh);
            }
        }
        meshes.putAll(updatedMeshes);
    }

    private static void unload() {
        for (Map<RegionCoord, LodRegionMesh> meshes : meshesByLevel.values()) {
            for (LodRegionMesh mesh : meshes.values()) {
                mesh.close();
            }
        }

        meshesByLevel.clear();
        meshLevelByRegion.clear();
        loadedDimension = null;
        loadedStorageDir = null;
        lastFadeRefreshOrigin = null;
        pendingFadeRefreshes.clear();
        pendingCoordinatorBuilds.clear();
        pendingFadeBuilds.clear();
        latestDispatchedLevel.clear();
    }

    private record GeometryBuildResult(RegionCoord region, int level, LodRegionMesh.RecordedRegionMesh recorded) {
    }

    private record PendingMeshRefresh(int level, RegionCoord region) {
    }
}
