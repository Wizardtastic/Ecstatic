package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.lod.StructureChunkLocator;
import com.angryalchemist.ecstatic.storage.HeightmapColumn;
import com.angryalchemist.ecstatic.storage.SavedChunkAccess;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class LodStructureIslands {
    private static final Set<String> EXCLUDED_STRUCTURE_IDS = Set.of(
        "minecraft:mineshaft",
        "minecraft:mineshaft_mesa",
        "minecraft:stronghold",
        "minecraft:ancient_city",
        "minecraft:buried_treasure",
        "minecraft:trail_ruins"
    );
    private static final int BAND_BEYOND_RENDER_DISTANCE_CHUNKS = 12;
    private static final int MAX_CHUNKS_PER_STRUCTURE = 64;
    private static final int SCAN_TOP_Y = 192;
    private static final int Y_MARGIN_BLOCKS = 4;
    private static final LodRegionMesh.FadeParams NO_FADE = new LodRegionMesh.FadeParams(-1073741824, -1073741824, 0.0F, 1.0F);
    private static volatile VertexBuffer buffer;
    private static int builtChunkX = Integer.MIN_VALUE;
    private static int builtChunkZ = Integer.MIN_VALUE;
    private static final AtomicBoolean buildInFlight = new AtomicBoolean(false);
    private static volatile LodRegionMesh.RecordedPart pendingUpload;
    private static volatile Set<Long> renderedChunks = Set.of();
    private static final int SCAN_MISS = Integer.MIN_VALUE;

    private LodStructureIslands() {
    }

    static void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ClientLevel level, Vec3 cameraPos) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            int chunkX = Math.floorDiv((int)Math.floor(cameraPos.x), 16);
            int chunkZ = Math.floorDiv((int)Math.floor(cameraPos.z), 16);
            if ((chunkX != builtChunkX || chunkZ != builtChunkZ) && buildInFlight.compareAndSet(false, true)) {
                builtChunkX = chunkX;
                builtChunkZ = chunkZ;
                dispatchBuild(chunkX, chunkZ, Minecraft.getInstance().options.getEffectiveRenderDistance(), server, level);
            }

            LodRegionMesh.RecordedPart finished = pendingUpload;
            if (finished != null) {
                pendingUpload = null;
                VertexBuffer old = buffer;
                buffer = LodRegionMesh.uploadPart(finished);
                if (old != null) {
                    old.close();
                }
            }

            VertexBuffer current = buffer;
            if (current != null) {
                boolean fogReady = LodFogShader.getTexturedOrNull() != null;
                RenderType renderType = fogReady
                    ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL
                    : LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL;
                renderType.setupRenderState();
                RenderSystem.setShader(fogReady ? LodFogShader::getTexturedOrNull : GameRenderer::getPositionTexColorShader);
                current.bind();
                current.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                VertexBuffer.unbind();
                renderType.clearRenderState();
            }
        }
    }

    static Set<Long> renderedChunks() {
        return renderedChunks;
    }

    private static void dispatchBuild(int centerChunkX, int centerChunkZ, int renderDistanceChunks, MinecraftServer server, ClientLevel clientLevel) {
        Thread worker = new Thread(() -> {
            try {
                ServerLevel level = server.getLevel(clientLevel.dimension());
                if (level != null) {
                    LodRegionMesh.RecordedPart part = buildGeometry(centerChunkX, centerChunkZ, renderDistanceChunks, server, level);
                    pendingUpload = part;
                    return;
                }
            } catch (RuntimeException e) {
                Constants.LOG.warn("Ecstatic: structure island build failed", e);
                return;
            } finally {
                buildInFlight.set(false);
            }
        }, "Ecstatic-StructureIsland-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    private static LodRegionMesh.RecordedPart buildGeometry(
        int centerChunkX, int centerChunkZ, int renderDistanceChunks, MinecraftServer server, ServerLevel level
    ) {
        RecordedVertexSink sink = new RecordedVertexSink();
        int[] vertexCount = new int[]{0};
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);

        try (SavedChunkAccess savedChunks = new SavedChunkAccess(server.getWorldPath(LevelResource.ROOT), biomeRegistry)) {
            int outerRadiusChunks = renderDistanceChunks + 12;
            int minChunkX = centerChunkX - outerRadiusChunks;
            int maxChunkX = centerChunkX + outerRadiusChunks;
            int minChunkZ = centerChunkZ - outerRadiusChunks;
            int maxChunkZ = centerChunkZ + outerRadiusChunks;
            List<ChunkPos> candidates = StructureChunkLocator.candidateStartChunks(level, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
            Set<Long> chunksToRender = new LinkedHashSet<>();
            Map<Long, int[]> yRangeByChunk = new HashMap<>();
            Set<String> foundStructureIds = new TreeSet<>();

            for (ChunkPos candidate : candidates) {
                SavedChunkAccess.StructureFootprint footprint = savedChunks.structureFootprint(candidate.x, candidate.z);
                if (footprint != null) {
                    Set<String> ids = new HashSet<>(footprint.ids());
                    ids.removeAll(EXCLUDED_STRUCTURE_IDS);
                    if (!ids.isEmpty()) {
                        foundStructureIds.addAll(ids);
                        int footprintMinY = Math.max(level.getMinBuildHeight(), footprint.minY() - 4);
                        int footprintMaxY = Math.min(192, footprint.maxY() + 4);
                        int fromChunkX = footprint.minX() >> 4;
                        int toChunkX = footprint.maxX() >> 4;
                        int fromChunkZ = footprint.minZ() >> 4;
                        int toChunkZ = footprint.maxZ() >> 4;
                        int added = 0;

                        for (int cx = fromChunkX; cx <= toChunkX && added < 64; cx++) {
                            for (int cz = fromChunkZ; cz <= toChunkZ && added < 64; cz++) {
                                if (cx >= minChunkX
                                    && cx <= maxChunkX
                                    && cz >= minChunkZ
                                    && cz <= maxChunkZ
                                    && !withinRenderDistance(cx, cz, centerChunkX, centerChunkZ, renderDistanceChunks)) {
                                    long packed = ChunkPos.asLong(cx, cz);
                                    chunksToRender.add(packed);
                                    yRangeByChunk.merge(
                                        packed, new int[]{footprintMinY, footprintMaxY}, (a, b) -> new int[]{Math.min(a[0], b[0]), Math.max(a[1], b[1])}
                                    );
                                    added++;
                                }
                            }
                        }
                    }
                }
            }

            for (long packed : chunksToRender) {
                int[] range = yRangeByChunk.get(packed);
                emitChunk(sink, savedChunks, ChunkPos.getX(packed), ChunkPos.getZ(packed), range[0], range[1], vertexCount);
            }

            int structureChunkCount = chunksToRender.size();
            renderedChunks = Set.copyOf(chunksToRender);
//            Constants.LOG
//                .info(
//                    "Ecstatic structure islands: center chunk ({}, {}), ring {}-{} chunks, {} placement candidate(s) -> {} chunk(s) rendered ({}), {} vertices",
//                    new Object[]{
//                        centerChunkX,
//                        centerChunkZ,
//                        renderDistanceChunks,
//                        outerRadiusChunks,
//                        candidates.size(),
//                        structureChunkCount,
//                        foundStructureIds.isEmpty() ? "none" : String.join(", ", foundStructureIds),
//                        vertexCount[0]
//                    }
//                );
        }

        return new LodRegionMesh.RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    private static boolean withinRenderDistance(int chunkX, int chunkZ, int centerChunkX, int centerChunkZ, int renderDistanceChunks) {
        int inner = Math.max(0, renderDistanceChunks - 4);
        int dx = chunkX - centerChunkX;
        int dz = chunkZ - centerChunkZ;
        return dx * dx + dz * dz <= inner * inner;
    }

    private static void emitChunk(VertexSink sink, SavedChunkAccess savedChunks, int chunkX, int chunkZ, int minY, int maxY, int[] vertexCount) {
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        int span = 18;
        int[][] heights = new int[span][span];
        int heightScanTop = Math.min(192, maxY);

        for (int lx = 0; lx < span; lx++) {
            for (int lz = 0; lz < span; lz++) {
                int worldX = originX - 1 + lx;
                int worldZ = originZ - 1 + lz;
                int foundY = Integer.MIN_VALUE;

                for (int y = heightScanTop; y >= minY; y--) {
                    if (isSolid(savedChunks, worldX, y, worldZ)) {
                        foundY = y;
                        break;
                    }
                }

                heights[lx][lz] = foundY;
            }
        }

        for (int lx = 1; lx < span - 1; lx++) {
            for (int lz = 1; lz < span - 1; lz++) {
                if (heights[lx][lz] != Integer.MIN_VALUE) {
                    int worldX = originX - 1 + lx;
                    int worldZ = originZ - 1 + lz;
                    HeightmapColumn west = neighborColumn(heights, lx - 1, lz);
                    HeightmapColumn east = neighborColumn(heights, lx + 1, lz);
                    HeightmapColumn south = neighborColumn(heights, lx, lz - 1);
                    HeightmapColumn north = neighborColumn(heights, lx, lz + 1);
                    HeightmapColumn center = neighborColumn(heights, lx, lz);
                    float slopeShade = LodRegionMesh.bakedSurfaceBrightness(
                        LodRegionMesh.centralDifference(west, east, center, 1),
                        LodRegionMesh.centralDifference(south, north, center, 1),
                        LodSettingsConfig.get().structureSlopeShadingFloor()
                    );

                    for (int y = heightScanTop; y >= minY; y--) {
                        BlockState state = savedChunks.blockAt(worldX, y, worldZ);
                        if (!state.isAir() && state.getFluidState().isEmpty()) {
                            emitVoxel(sink, savedChunks, worldX, y, worldZ, state, slopeShade, vertexCount);
                        }
                    }
                }
            }
        }
    }

    private static boolean isSolid(SavedChunkAccess savedChunks, int x, int y, int z) {
        BlockState state = savedChunks.blockAt(x, y, z);
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    private static void emitVoxel(VertexSink sink, SavedChunkAccess savedChunks, int x, int y, int z, BlockState state, float slopeShade, int[] vertexCount) {
        boolean upExposed = !isSolid(savedChunks, x, y + 1, z);
        boolean downExposed = !isSolid(savedChunks, x, y - 1, z);
        boolean northExposed = !isSolid(savedChunks, x, y, z - 1);
        boolean southExposed = !isSolid(savedChunks, x, y, z + 1);
        boolean westExposed = !isSolid(savedChunks, x - 1, y, z);
        boolean eastExposed = !isSolid(savedChunks, x + 1, y, z);
        if (upExposed || downExposed || northExposed || southExposed || westExposed || eastExposed) {
            float x0 = x - 0.5F;
            float x1 = x + 0.5F;
            float z0 = z - 0.5F;
            float z1 = z + 0.5F;
            float yBottom = y;
            float yTop = y + 1.0F;
            if (upExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.UP);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                float shade = 1.0F * slopeShade;
                LodRegionMesh.emitBoxTop(sink, x0, z0, x1, z1, yTop, yTop, yTop, yTop, sprite, tint, shade, shade, shade, shade, NO_FADE, false, vertexCount);
            }

            if (downExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.DOWN);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                LodRegionMesh.emitBoxBottom(sink, x0, z0, x1, z1, yBottom, sprite, tint, 0.5F * slopeShade, NO_FADE, false, vertexCount);
            }

            if (westExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.WEST);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                LodRegionMesh.emitSkirtQuad(
                    sink, x0, z0, x0, z1, yTop, yTop, yBottom, sprite, tint, 0.6F * slopeShade, NO_FADE, -1.0F, 0.0F, 0.0F, false, vertexCount
                );
            }

            if (eastExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.EAST);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                LodRegionMesh.emitSkirtQuad(
                    sink, x1, z0, x1, z1, yTop, yTop, yBottom, sprite, tint, 0.6F * slopeShade, NO_FADE, 1.0F, 0.0F, 0.0F, false, vertexCount
                );
            }

            if (northExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.NORTH);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                LodRegionMesh.emitSkirtQuad(
                    sink, x0, z0, x1, z0, yTop, yTop, yBottom, sprite, tint, 0.8F * slopeShade, NO_FADE, 0.0F, 0.0F, -1.0F, false, vertexCount
                );
            }

            if (southExposed) {
                SurfaceMaterial.Sprite sprite = SurfaceMaterial.resolveSprite(state, Direction.SOUTH);
                int tint = resolveTint(savedChunks, sprite, x, y, z);
                LodRegionMesh.emitSkirtQuad(
                    sink, x0, z1, x1, z1, yTop, yTop, yBottom, sprite, tint, 0.8F * slopeShade, NO_FADE, 0.0F, 0.0F, 1.0F, false, vertexCount
                );
            }
        }
    }

    private static int resolveTint(SavedChunkAccess savedChunks, SurfaceMaterial.Sprite sprite, int x, int y, int z) {
        if (!sprite.tinted()) {
            return 16777215;
        }

        Holder<Biome> biome = savedChunks.biomeAt(x, y, z);
        return biome != null ? ((Biome)biome.value()).getGrassColor(x, z) : 16777215;
    }

    private static HeightmapColumn neighborColumn(int[][] heights, int lx, int lz) {
        if (lx >= 0 && lz >= 0 && lx < heights.length && lz < heights[0].length) {
            int height = heights[lx][lz];
            return height == Integer.MIN_VALUE ? null : new HeightmapColumn(height, 0, 16777215, false);
        } else {
            return null;
        }
    }
}
