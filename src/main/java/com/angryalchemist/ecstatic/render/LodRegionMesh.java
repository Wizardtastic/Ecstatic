package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.debug.LodDebugState;
import com.angryalchemist.ecstatic.lod.LodLevel;
import com.angryalchemist.ecstatic.lod.RegionCoord;
import com.angryalchemist.ecstatic.storage.HeightmapColumn;
import com.angryalchemist.ecstatic.storage.LodRegionFile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;

public final class LodRegionMesh implements AutoCloseable {
    private static final int SEA_LEVEL_Y = 63;
    private static final float WATER_SURFACE_Y = 62.8F;
    private static final float ICE_ALPHA = 0.92F;
    private static final int FALLBACK_MIN_BUILD_HEIGHT = -64;
    private static final int FALLBACK_MAX_BUILD_HEIGHT = 320;
    private static final float WATER_DEPTH_FOR_FULL_TINT = 20.0F;
    private static final float MIN_SLOPE_BRIGHTNESS = 0.72F;
    private static final float DIRT_SLOPE_START = 0.3F;
    private static final float DIRT_SLOPE_FULL = 0.9F;
    private static final float STONE_SLOPE_START = 0.9F;
    private static final float STONE_SLOPE_FULL = 1.6F;
    private static final float BEVEL_MAX_HEIGHT_BLOCKS = 3.0F;
    private static final float SUBDIVIDE_MIN_RANGE_BLOCKS = 1.0F;
    private static final float[] AO_BRIGHTNESS_BY_OCCLUDER_COUNT = new float[]{1.0F, 0.92F, 0.82F, 0.72F};
    private static final int SNOW_COLOR = 15789021;
    private static final float SNOW_MAX_BLEND = 0.8F;
    private static final int WATER_TINT_COLOR = 3103882;
    private static final int DIRT_COLOR = 7165246;
    private static final int STONE_COLOR = 9079426;
    private static final int SAND_COLOR = 14274464;
    static final float UP_SHADE = 1.0F;
    static final float NORTH_SOUTH_SHADE = 0.8F;
    static final float WEST_EAST_SHADE = 0.6F;
    static final float DOWN_SHADE = 0.5F;
    private static final float SUN_DIR_X = -0.4082F;
    private static final float SUN_DIR_Y = 0.8165F;
    private static final float SUN_DIR_Z = -0.4082F;
    private static final int BLOCKY_COLOR_QUANTIZE_STEP = 16;
    private static final float MAX_BAKED_BRIGHTNESS = 1.35F;
    private static final float TEMPERATURE_MAX_CHANNEL_SHIFT = 0.2F;
    static final float FLAT_UV = 0.5F;
    private static final int MAX_TREE_LOD_LEVEL = 3;
    private static final int MAX_BOX_TREE_LOD_LEVEL = 2;
    static final float TREE_DENSITY_PER_BLOCK_AREA = 0.006666667F;
    private static final float TREE_DENSITY_MULTIPLIER = 2.0F;
    private static final float TREE_MAX_SLOPE = 0.3F;
    private static final float TRUNK_HALF_WIDTH = 0.5F;
    private static final float CANOPY_BASE_HEIGHT_FRACTION = 0.35F;
    private static final int FULL_ALPHA = 255;
    private static final SurfaceMaterial.Sprite FULL_SPRITE = new SurfaceMaterial.Sprite(0.0F, 1.0F, 0.0F, 1.0F, false);
    private final boolean terrainTextured;
    private final VertexBuffer terrainBufferOpaque;
    private final VertexBuffer terrainBufferFade;
    private final boolean terrainLit;
    private final VertexBuffer terrainBufferWater;
    private final boolean waterLit;
    private final List<LodRegionMesh.TreeMeshPart> treeParts;
    private static SurfaceMaterial.Sprite cachedWaterSprite;
    private static final int SHALLOW_WATER_COLOR = 6469340;
    private static final float WATER_DEPTH_FOR_FULL_COLOR = 40.0F;
    private static final float SEAFLOOR_DEPTH_BELOW_SURFACE = 3.0F;

    private LodRegionMesh(
        VertexBuffer terrainBufferOpaque,
        VertexBuffer terrainBufferFade,
        boolean terrainTextured,
        boolean terrainLit,
        VertexBuffer terrainBufferWater,
        boolean waterLit,
        List<LodRegionMesh.TreeMeshPart> treeParts
    ) {
        this.terrainBufferOpaque = terrainBufferOpaque;
        this.terrainBufferFade = terrainBufferFade;
        this.terrainTextured = terrainTextured;
        this.terrainLit = terrainLit;
        this.terrainBufferWater = terrainBufferWater;
        this.waterLit = waterLit;
        this.treeParts = treeParts;
    }

    public static LodRegionMesh build(LodRegionFile regionFile, RegionCoord region, LodRegionMesh.FadeParams fade, Registry<Biome> biomeRegistry) {
        return upload(buildGeometry(regionFile, region, fade, biomeRegistry));
    }

    public static LodRegionMesh.RecordedRegionMesh buildGeometry(
        LodRegionFile regionFile, RegionCoord region, LodRegionMesh.FadeParams fade, Registry<Biome> biomeRegistry
    ) {
        boolean textured = regionFile.lodLevel <= 2;
        boolean litFormat = resolveLitFormat();
        LodSettingsConfig config = LodSettingsConfig.get();
        boolean cullSubmergedTerrain = config.oceanPlaneEnabled() && config.opaqueWaterEnabled();
        int[] opaqueVertexCount = new int[]{0};
        int[] fadeVertexCount = new int[]{0};
        boolean beyondFadeBand = regionBeyondFadeBand(region, regionFile.sampleSpacingBlocks, fade);
        LodRegionMesh.RecordedPart terrainOpaque;
        LodRegionMesh.RecordedPart terrainFade;
        if (textured) {
            terrainOpaque = recordSteppedTerrain(regionFile, region, fade, biomeRegistry, litFormat, true, cullSubmergedTerrain, opaqueVertexCount);
            terrainFade = beyondFadeBand
                ? null
                : recordSteppedTerrain(regionFile, region, fade, biomeRegistry, litFormat, false, cullSubmergedTerrain, fadeVertexCount);
        } else {
            terrainOpaque = recordFacetedTerrain(regionFile, region, fade, biomeRegistry, litFormat, true, cullSubmergedTerrain, opaqueVertexCount);
            terrainFade = beyondFadeBand
                ? null
                : recordFacetedTerrain(regionFile, region, fade, biomeRegistry, litFormat, false, cullSubmergedTerrain, fadeVertexCount);
        }

        List<LodRegionMesh.RecordedTreePart> treeParts = List.of();
        int[] treeVertexCount = new int[]{0};
        if (regionFile.lodLevel >= 1 && regionFile.lodLevel <= 3) {
            treeParts = recordTrees(regionFile, region, fade, biomeRegistry, treeVertexCount, regionFile.lodLevel);
        }

        LodRegionMesh.RecordedPart water = null;
        boolean waterLit = false;
        int[] waterVertexCount = new int[]{0};
        if (config.oceanPlaneEnabled()) {
            waterLit = resolveWaterLitFormat();
            water = recordWater(regionFile, region, fade, waterLit, config.opaqueWaterEnabled(), biomeRegistry, waterVertexCount);
        }

        Constants.LOG
            .debug(
                "Ecstatic mesh: region ({}, {}) level {} origin ({}, {}) recorded {} opaque + {} fade-band + {} tree + {} water vertices",
                new Object[]{
                    region.x(),
                    region.z(),
                    regionFile.lodLevel,
                    region.originBlockX(),
                    region.originBlockZ(),
                    opaqueVertexCount[0],
                    fadeVertexCount[0],
                    treeVertexCount[0],
                    waterVertexCount[0]
                }
            );
        return new LodRegionMesh.RecordedRegionMesh(terrainOpaque, terrainFade, textured, litFormat, water, waterLit, treeParts);
    }

    public static LodRegionMesh upload(LodRegionMesh.RecordedRegionMesh recorded) {
        VertexBuffer terrainBufferOpaque = uploadPart(recorded.terrainOpaque());
        VertexBuffer terrainBufferFade = recorded.terrainFade() == null ? null : uploadPart(recorded.terrainFade());
        VertexBuffer terrainBufferWater = recorded.water() == null ? null : uploadPart(recorded.water());
        List<LodRegionMesh.TreeMeshPart> treeParts = new ArrayList<>(recorded.treeParts().size());

        for (LodRegionMesh.RecordedTreePart treePart : recorded.treeParts()) {
            VertexBuffer buffer = uploadPart(treePart.part());
            if (buffer != null) {
                treeParts.add(new LodRegionMesh.TreeMeshPart(treePart.renderType(), buffer));
            }
        }

        return new LodRegionMesh(
            terrainBufferOpaque, terrainBufferFade, recorded.terrainTextured(), recorded.terrainLit(), terrainBufferWater, recorded.waterLit(), treeParts
        );
    }

    static VertexBuffer uploadPart(LodRegionMesh.RecordedPart part) {
        BufferBuilder builder = Tesselator.getInstance().begin(Mode.TRIANGLES, part.format());
        BufferBuilderVertexSink glSink = new BufferBuilderVertexSink(builder);
        part.sink().replayInto(glSink, part.kind());
        return upload(builder, part.sink().count());
    }

    private static boolean regionBeyondFadeBand(RegionCoord region, int spacing, LodRegionMesh.FadeParams fade) {
        int minX = region.originBlockX() - spacing;
        int maxX = region.originBlockX() + 512 + spacing;
        int minZ = region.originBlockZ() - spacing;
        int maxZ = region.originBlockZ() + 512 + spacing;
        float closestX = Math.max(minX, Math.min((float)fade.spawnBlockX(), (float)maxX));
        float closestZ = Math.max(minZ, Math.min((float)fade.spawnBlockZ(), (float)maxZ));
        float dx = fade.spawnBlockX() - closestX;
        float dz = fade.spawnBlockZ() - closestZ;
        float minDistance = (float)Math.sqrt(dx * dx + dz * dz);
        return minDistance >= fade.fadeEndBlocks();
    }

    private static boolean resolveLitFormat() {
        int override = LodDebugState.vertexFormatOverride();
        if (override == 1) {
            return true;
        } else {
            return override == 2 ? false : LodSettingsConfig.get().useLitVertexFormat();
        }
    }

    private static boolean resolveWaterLitFormat() {
        int override = LodDebugState.vertexFormatOverride();
        if (override == 1) {
            return true;
        } else if (override == 2) {
            return false;
        } else {
            return LodSettingsConfig.get().shaderWaterEnabled() && IrisCompat.isShaderPackActive();
        }
    }

    private static LodRegionMesh.RecordedPart recordFacetedTerrain(
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.FadeParams fade,
        Registry<Biome> biomeRegistry,
        boolean litFormat,
        boolean opaquePass,
        boolean cullSubmergedTerrain,
        int[] vertexCount
    ) {
        int samplesPerAxis = regionFile.samplesPerAxis;
        int spacing = regionFile.sampleSpacingBlocks;
        VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.POSITION_COLOR;
        RecordedVertexSink sink = new RecordedVertexSink();
        RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT : RecordedVertexSink.Kind.PLAIN;
        int last = samplesPerAxis - 1;

        for (int lz = 0; lz < last; lz++) {
            for (int lx = 0; lx < last; lx++) {
                emitFacetedCell(
                    sink,
                    regionFile,
                    spacing,
                    samplesPerAxis,
                    vertexCount,
                    fade,
                    biomeRegistry,
                    litFormat,
                    opaquePass,
                    cullSubmergedTerrain,
                    region,
                    lx,
                    lz,
                    region,
                    lx + 1,
                    lz,
                    region,
                    lx,
                    lz + 1,
                    region,
                    lx + 1,
                    lz + 1
                );
            }
        }

        RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());

        for (int lz = 0; lz < last; lz++) {
            emitFacetedCell(
                sink,
                regionFile,
                spacing,
                samplesPerAxis,
                vertexCount,
                fade,
                biomeRegistry,
                litFormat,
                opaquePass,
                cullSubmergedTerrain,
                region,
                last,
                lz,
                plusX,
                0,
                lz,
                region,
                last,
                lz + 1,
                plusX,
                0,
                lz + 1
            );
        }

        RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);

        for (int lx = 0; lx < last; lx++) {
            emitFacetedCell(
                sink,
                regionFile,
                spacing,
                samplesPerAxis,
                vertexCount,
                fade,
                biomeRegistry,
                litFormat,
                opaquePass,
                cullSubmergedTerrain,
                region,
                lx,
                last,
                region,
                lx + 1,
                last,
                plusZ,
                lx,
                0,
                plusZ,
                lx + 1,
                0
            );
        }

        RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
        emitFacetedCell(
            sink,
            regionFile,
            spacing,
            samplesPerAxis,
            vertexCount,
            fade,
            biomeRegistry,
            litFormat,
            opaquePass,
            cullSubmergedTerrain,
            region,
            last,
            last,
            plusX,
            0,
            last,
            plusZ,
            last,
            0,
            plusXZ,
            0,
            0
        );
        return new LodRegionMesh.RecordedPart(sink, kind, format);
    }

    private static LodRegionMesh.RecordedPart recordWater(
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        boolean opaqueWaterEnabled,
        Registry<Biome> biomeRegistry,
        int[] vertexCount
    ) {
        int spacing = regionFile.sampleSpacingBlocks;
        int samplesPerAxis = regionFile.samplesPerAxis;
        int waterColor = BiomeStyleConfig.get().waterColor();
        float waterAlpha = opaqueWaterEnabled ? 1.0F : BiomeStyleConfig.get().waterAlpha();
        int iceColor = BiomeStyleConfig.get().iceColor();
        int subdivisions = waterTextureSubdivisions(regionFile.lodLevel);
        SurfaceMaterial.Sprite waterSprite = waterSprite();
        VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.POSITION_TEX_COLOR;
        RecordedVertexSink sink = new RecordedVertexSink();
        RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT_TEXTURED : RecordedVertexSink.Kind.TEXTURED;
        int last = samplesPerAxis - 1;

        for (int lz = 0; lz < last; lz++) {
            for (int lx = 0; lx < last; lx++) {
                emitWaterCell(
                    sink,
                    regionFile,
                    spacing,
                    vertexCount,
                    fade,
                    litFormat,
                    waterColor,
                    waterAlpha,
                    iceColor,
                    biomeRegistry,
                    subdivisions,
                    waterSprite,
                    opaqueWaterEnabled,
                    region,
                    lx,
                    lz,
                    region,
                    lx + 1,
                    lz,
                    region,
                    lx,
                    lz + 1,
                    region,
                    lx + 1,
                    lz + 1
                );
            }
        }

        RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());

        for (int lz = 0; lz < last; lz++) {
            emitWaterCell(
                sink,
                regionFile,
                spacing,
                vertexCount,
                fade,
                litFormat,
                waterColor,
                waterAlpha,
                iceColor,
                biomeRegistry,
                subdivisions,
                waterSprite,
                opaqueWaterEnabled,
                region,
                last,
                lz,
                plusX,
                0,
                lz,
                region,
                last,
                lz + 1,
                plusX,
                0,
                lz + 1
            );
        }

        RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);

        for (int lx = 0; lx < last; lx++) {
            emitWaterCell(
                sink,
                regionFile,
                spacing,
                vertexCount,
                fade,
                litFormat,
                waterColor,
                waterAlpha,
                iceColor,
                biomeRegistry,
                subdivisions,
                waterSprite,
                opaqueWaterEnabled,
                region,
                lx,
                last,
                region,
                lx + 1,
                last,
                plusZ,
                lx,
                0,
                plusZ,
                lx + 1,
                0
            );
        }

        RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
        emitWaterCell(
            sink,
            regionFile,
            spacing,
            vertexCount,
            fade,
            litFormat,
            waterColor,
            waterAlpha,
            iceColor,
            biomeRegistry,
            subdivisions,
            waterSprite,
            opaqueWaterEnabled,
            region,
            last,
            last,
            plusX,
            0,
            last,
            plusZ,
            last,
            0,
            plusXZ,
            0,
            0
        );
        return new LodRegionMesh.RecordedPart(sink, kind, format);
    }

    private static int waterTextureSubdivisions(int lodLevel) {
        return switch (LodLevel.sampleSpacingBlocks(Math.max(1, lodLevel))) {
            case 8 -> 2;
            case 16 -> 4;
            case 32 -> 8;
            default -> 1;
        };
    }

    private static SurfaceMaterial.Sprite waterSprite() {
        if (cachedWaterSprite == null) {
            cachedWaterSprite = SurfaceMaterial.resolveSprite(Blocks.WATER.defaultBlockState(), Direction.UP);
        }

        return cachedWaterSprite;
    }

    private static void emitWaterCell(
        VertexSink sink,
        LodRegionFile regionFile,
        int spacing,
        int[] vertexCount,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int waterColor,
        float baseAlpha,
        int iceColor,
        Registry<Biome> biomeRegistry,
        int subdivisions,
        SurfaceMaterial.Sprite sprite,
        boolean opaqueWaterEnabled,
        RegionCoord r00,
        int lx00,
        int lz00,
        RegionCoord r10,
        int lx10,
        int lz10,
        RegionCoord r01,
        int lx01,
        int lz01,
        RegionCoord r11,
        int lx11,
        int lz11
    ) {
        HeightmapColumn c00 = regionFile.readColumn(r00, lx00, lz00);
        HeightmapColumn c10 = regionFile.readColumn(r10, lx10, lz10);
        HeightmapColumn c01 = regionFile.readColumn(r01, lx01, lz01);
        HeightmapColumn c11 = regionFile.readColumn(r11, lx11, lz11);
        if (c00 != null && c10 != null && c01 != null && c11 != null) {
            if (c00.height() < 63 || c10.height() < 63 || c01.height() < 63 || c11.height() < 63) {
                float x0 = r00.originBlockX() + lx00 * spacing;
                float z0 = r00.originBlockZ() + lz00 * spacing;
                float x1 = r10.originBlockX() + lx10 * spacing;
                float z1 = r01.originBlockZ() + lz01 * spacing;
                boolean frozen00 = isFrozenWater(biomeRegistry, c00, (int)x0, (int)z0);
                boolean frozen10 = isFrozenWater(biomeRegistry, c10, (int)x1, (int)z0);
                boolean frozen01 = isFrozenWater(biomeRegistry, c01, (int)x0, (int)z1);
                boolean frozen11 = isFrozenWater(biomeRegistry, c11, (int)x1, (int)z1);
                int color00 = frozen00 ? iceColor : waterColorAt(waterColor, c00);
                int color10 = frozen10 ? iceColor : waterColorAt(waterColor, c10);
                int color01 = frozen01 ? iceColor : waterColorAt(waterColor, c01);
                int color11 = frozen11 ? iceColor : waterColorAt(waterColor, c11);
                int a00 = fadedWaterAlpha(fade, x0, 63.0F, z0, frozen00 ? 0.92F : baseAlpha);
                int a10 = fadedWaterAlpha(fade, x1, 63.0F, z0, frozen10 ? 0.92F : baseAlpha);
                int a01 = fadedWaterAlpha(fade, x0, 63.0F, z1, frozen01 ? 0.92F : baseAlpha);
                int a11 = fadedWaterAlpha(fade, x1, 63.0F, z1, frozen11 ? 0.92F : baseAlpha);
                if (a00 != 0 || a10 != 0 || a01 != 0 || a11 != 0) {
                    if (!opaqueWaterEnabled) {
                        emitSeafloorTile(sink, litFormat, sprite, fade, waterColor, x0, z0, x1, z1, c00, c10, c01, c11, vertexCount);
                    }

                    for (int i = 0; i < subdivisions; i++) {
                        float u0 = (float)i / subdivisions;
                        float u1 = (float)(i + 1) / subdivisions;
                        float sx0 = x0 + (x1 - x0) * u0;
                        float sx1 = x0 + (x1 - x0) * u1;

                        for (int j = 0; j < subdivisions; j++) {
                            float v0 = (float)j / subdivisions;
                            float v1 = (float)(j + 1) / subdivisions;
                            float sz0 = z0 + (z1 - z0) * v0;
                            float sz1 = z0 + (z1 - z0) * v1;
                            int sa00 = bilinearAlpha(a00, a10, a01, a11, u0, v0);
                            int sa10 = bilinearAlpha(a00, a10, a01, a11, u1, v0);
                            int sa01 = bilinearAlpha(a00, a10, a01, a11, u0, v1);
                            int sa11 = bilinearAlpha(a00, a10, a01, a11, u1, v1);
                            if (sa00 != 0 || sa10 != 0 || sa01 != 0 || sa11 != 0) {
                                int sc00 = bilinearColor(color00, color10, color01, color11, u0, v0);
                                int sc10 = bilinearColor(color00, color10, color01, color11, u1, v0);
                                int sc01 = bilinearColor(color00, color10, color01, color11, u0, v1);
                                int sc11 = bilinearColor(color00, color10, color01, color11, u1, v1);
                                emitWaterTile(sink, litFormat, sprite, sx0, sz0, sx1, sz1, sc00, sc10, sc01, sc11, sa00, sa10, sa01, sa11, vertexCount);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void emitWaterTile(
        VertexSink sink,
        boolean litFormat,
        SurfaceMaterial.Sprite sprite,
        float x0,
        float z0,
        float x1,
        float z1,
        int color00,
        int color10,
        int color01,
        int color11,
        int a00,
        int a10,
        int a01,
        int a11,
        int[] vertexCount
    ) {
        if (litFormat) {
            sink.litTexturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00, 0.0F, 1.0F, 0.0F);
            sink.litTexturedVertex(x1, 62.8F, z0, sprite.u1(), sprite.v1(), color10, a10, 0.0F, 1.0F, 0.0F);
            sink.litTexturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11, 0.0F, 1.0F, 0.0F);
            sink.litTexturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00, 0.0F, 1.0F, 0.0F);
            sink.litTexturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11, 0.0F, 1.0F, 0.0F);
            sink.litTexturedVertex(x0, 62.8F, z1, sprite.u0(), sprite.v0(), color01, a01, 0.0F, 1.0F, 0.0F);
        } else {
            sink.texturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00);
            sink.texturedVertex(x1, 62.8F, z0, sprite.u1(), sprite.v1(), color10, a10);
            sink.texturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11);
            sink.texturedVertex(x0, 62.8F, z0, sprite.u0(), sprite.v1(), color00, a00);
            sink.texturedVertex(x1, 62.8F, z1, sprite.u1(), sprite.v0(), color11, a11);
            sink.texturedVertex(x0, 62.8F, z1, sprite.u0(), sprite.v0(), color01, a01);
        }

        vertexCount[0] += 6;
    }

    private static int waterColorAt(int configuredDeepColor, HeightmapColumn column) {
        float depthFactor = clamp01((63 - column.height()) / 40.0F);
        return blend(6469340, configuredDeepColor, depthFactor);
    }

    private static void emitSeafloorTile(
        VertexSink sink,
        boolean litFormat,
        SurfaceMaterial.Sprite sprite,
        LodRegionMesh.FadeParams fade,
        int waterColor,
        float x0,
        float z0,
        float x1,
        float z1,
        HeightmapColumn c00,
        HeightmapColumn c10,
        HeightmapColumn c01,
        HeightmapColumn c11,
        int[] vertexCount
    ) {
        int color00 = waterColorAt(waterColor, c00);
        int color10 = waterColorAt(waterColor, c10);
        int color01 = waterColorAt(waterColor, c01);
        int color11 = waterColorAt(waterColor, c11);
        int a00 = fadedWaterAlpha(fade, x0, 63.0F, z0, 1.0F);
        int a10 = fadedWaterAlpha(fade, x1, 63.0F, z0, 1.0F);
        int a01 = fadedWaterAlpha(fade, x0, 63.0F, z1, 1.0F);
        int a11 = fadedWaterAlpha(fade, x1, 63.0F, z1, 1.0F);
        if (a00 != 0 || a10 != 0 || a01 != 0 || a11 != 0) {
            float u = (sprite.u0() + sprite.u1()) * 0.5F;
            float v = (sprite.v0() + sprite.v1()) * 0.5F;
            float y = 59.8F;
            if (litFormat) {
                sink.litTexturedVertex(x0, y, z0, u, v, color00, a00, 0.0F, 1.0F, 0.0F);
                sink.litTexturedVertex(x1, y, z0, u, v, color10, a10, 0.0F, 1.0F, 0.0F);
                sink.litTexturedVertex(x1, y, z1, u, v, color11, a11, 0.0F, 1.0F, 0.0F);
                sink.litTexturedVertex(x0, y, z0, u, v, color00, a00, 0.0F, 1.0F, 0.0F);
                sink.litTexturedVertex(x1, y, z1, u, v, color11, a11, 0.0F, 1.0F, 0.0F);
                sink.litTexturedVertex(x0, y, z1, u, v, color01, a01, 0.0F, 1.0F, 0.0F);
            } else {
                sink.texturedVertex(x0, y, z0, u, v, color00, a00);
                sink.texturedVertex(x1, y, z0, u, v, color10, a10);
                sink.texturedVertex(x1, y, z1, u, v, color11, a11);
                sink.texturedVertex(x0, y, z0, u, v, color00, a00);
                sink.texturedVertex(x1, y, z1, u, v, color11, a11);
                sink.texturedVertex(x0, y, z1, u, v, color01, a01);
            }

            vertexCount[0] += 6;
        }
    }

    private static int bilinearColor(int c00, int c10, int c01, int c11, float u, float v) {
        int top = blend(c00, c10, u);
        int bottom = blend(c01, c11, u);
        return blend(top, bottom, v);
    }

    private static int bilinearAlpha(int a00, int a10, int a01, int a11, float u, float v) {
        float top = a00 + (a10 - a00) * u;
        float bottom = a01 + (a11 - a01) * u;
        return Math.round(top + (bottom - top) * v);
    }

    private static boolean isFrozenWater(Registry<Biome> biomeRegistry, HeightmapColumn column, int blockX, int blockZ) {
        if (biomeRegistry == null) {
            return false;
        }

        Biome biome = (Biome)biomeRegistry.byId(column.biomeRawId());
        return biome != null && biome.coldEnoughToSnow(new BlockPos(blockX, 62, blockZ));
    }

    private static int fadedWaterAlpha(LodRegionMesh.FadeParams fade, float worldX, float worldY, float worldZ, float baseAlpha) {
        return Math.round(baseAlpha * fade.alphaAt(worldX, worldY, worldZ));
    }

    private static boolean allSubmerged(HeightmapColumn... columns) {
        for (HeightmapColumn column : columns) {
            if (column == null || column.height() >= 63) {
                return false;
            }
        }

        return true;
    }

    private static LodRegionMesh.RecordedPart recordSteppedTerrain(
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.FadeParams fade,
        Registry<Biome> biomeRegistry,
        boolean litFormat,
        boolean opaquePass,
        boolean cullSubmergedTerrain,
        int[] vertexCount
    ) {
        int samplesPerAxis = regionFile.samplesPerAxis;
        int spacing = regionFile.sampleSpacingBlocks;
        float half = spacing / 2.0F;
        int subStep = LodSettingsConfig.get().lod1SubStepBlocks();
        int sub = subStep >= 2 ? Math.max(1, spacing / 2) : spacing;
        boolean blockyTops = spacing <= 2;
        VertexFormat format = litFormat ? LodTerrainRenderType.BLOCK_SAFE : DefaultVertexFormat.POSITION_TEX_COLOR;
        RecordedVertexSink sink = new RecordedVertexSink();
        RecordedVertexSink.Kind kind = litFormat ? RecordedVertexSink.Kind.LIT_TEXTURED : RecordedVertexSink.Kind.TEXTURED;
        LodRegionMesh.EdgeColumn[][] edgeGrid = new LodRegionMesh.EdgeColumn[samplesPerAxis][samplesPerAxis];

        for (int lz = 0; lz < samplesPerAxis; lz++) {
            for (int lx = 0; lx < samplesPerAxis; lx++) {
                HeightmapColumn column = regionFile.readColumn(region, lx, lz);
                if (column != null) {
                    float cx = region.originBlockX() + lx * spacing;
                    float cz = region.originBlockZ() + lz * spacing;
                    long structureChunk = ChunkPos.asLong(Math.floorDiv((int)cx, 16), Math.floorDiv((int)cz, 16));
                    if (!LodStructureIslands.renderedChunks().contains(structureChunk)) {
                        boolean columnFullyOpaque = fade.alphaAt(cx, column.height(), cz) == 255;
                        if (columnFullyOpaque == opaquePass) {
                            HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
                            HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
                            HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
                            HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
                            float dhdx = centralDifference(west, east, column, spacing);
                            float dhdz = centralDifference(south, north, column, spacing);
                            float slope = (float)Math.sqrt(dhdx * dhdx + dhdz * dhdz);
                            SurfaceMaterial material = SurfaceMaterial.classify(column, biomeRegistry, slope, (int)cx, (int)cz);
                            int tintColor = applyGroundTint(column, biomeRegistry, column.colorRgb());
                            tintColor = multiplyColor(tintColor, LodSettingsConfig.get().nearTerrainTint(material.kind()));
                            float x0 = cx - half;
                            float x1 = cx + half;
                            float z0 = cz - half;
                            float z1 = cz + half;
                            HeightmapColumn northwest = columnAt(regionFile, region, lx - 1, lz + 1, samplesPerAxis);
                            HeightmapColumn northeast = columnAt(regionFile, region, lx + 1, lz + 1, samplesPerAxis);
                            HeightmapColumn southwest = columnAt(regionFile, region, lx - 1, lz - 1, samplesPerAxis);
                            HeightmapColumn southeast = columnAt(regionFile, region, lx + 1, lz - 1, samplesPerAxis);
                            if (!cullSubmergedTerrain
                                || !allSubmerged(column, east, north, northeast)
                                || !allSubmerged(west, column, northwest, north)
                                || !allSubmerged(south, southeast, column, east)
                                || !allSubmerged(southwest, south, west, column)) {
                                float skirtBottom = lowestNeighborHeight(column.height(), west, east, south, north, northwest, northeast, southwest, southeast);
                                float ySW;
                                float ySE;
                                float yNE;
                                float yNW;
                                if (blockyTops) {
                                    ySW = ySE = yNE = yNW = column.height();
                                } else {
                                    ySW = blendedCornerHeight(column.height(), west, south, southwest);
                                    ySE = blendedCornerHeight(column.height(), east, south, southeast);
                                    yNE = blendedCornerHeight(column.height(), east, north, northeast);
                                    yNW = blendedCornerHeight(column.height(), west, north, northwest);
                                }

                                float minCorner = Math.min(Math.min(ySW, ySE), Math.min(yNE, yNW));
                                float maxCorner = Math.max(Math.max(ySW, ySE), Math.max(yNE, yNW));
                                float slopeShade = bakedSurfaceBrightness(
                                    west, east, south, north, column, spacing, LodSettingsConfig.get().nearSlopeShadingFloor()
                                );
                                float aoSW = cornerAo(column.height(), west, south, southwest) * slopeShade;
                                float aoSE = cornerAo(column.height(), east, south, southeast) * slopeShade;
                                float aoNE = cornerAo(column.height(), east, north, northeast) * slopeShade;
                                float aoNW = cornerAo(column.height(), west, north, northwest) * slopeShade;
                                if (sub > 1 && maxCorner - minCorner >= 1.0F) {
                                    emitSubdividedColumn(
                                        sink,
                                        x0,
                                        z0,
                                        x1,
                                        z1,
                                        ySW,
                                        ySE,
                                        yNE,
                                        yNW,
                                        column.height(),
                                        west,
                                        east,
                                        south,
                                        north,
                                        skirtBottom,
                                        material,
                                        tintColor,
                                        sub,
                                        fade,
                                        litFormat,
                                        vertexCount,
                                        aoSW,
                                        aoSE,
                                        aoNE,
                                        aoNW
                                    );
                                } else {
                                    emitBoxTop(
                                        sink,
                                        x0,
                                        z0,
                                        x1,
                                        z1,
                                        ySW,
                                        ySE,
                                        yNE,
                                        yNW,
                                        material.topSprite(),
                                        tintColor,
                                        1.0F * aoSW,
                                        1.0F * aoSE,
                                        1.0F * aoNE,
                                        1.0F * aoNW,
                                        fade,
                                        litFormat,
                                        vertexCount
                                    );
                                    edgeGrid[lx][lz] = new LodRegionMesh.EdgeColumn(
                                        column.height(), ySW, ySE, yNE, yNW, skirtBottom, tintColor, material.sideSprite()
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        emitMergedSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
        return new LodRegionMesh.RecordedPart(sink, kind, format);
    }

    private static int quantizeColor(int colorRgb) {
        int r = quantizeChannel(colorRgb >> 16 & 0xFF);
        int g = quantizeChannel(colorRgb >> 8 & 0xFF);
        int b = quantizeChannel(colorRgb & 0xFF);
        return r << 16 | g << 8 | b;
    }

    private static int quantizeChannel(int channel) {
        int step = 16;
        return clampByte(Math.round((float)channel / step) * step);
    }

    private static void emitMergedSkirts(
        VertexSink sink,
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.EdgeColumn[][] edgeGrid,
        int samplesPerAxis,
        int spacing,
        float half,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        emitMergedWestSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
        emitMergedEastSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
        emitMergedSouthSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
        emitMergedNorthSkirts(sink, regionFile, region, edgeGrid, samplesPerAxis, spacing, half, fade, litFormat, vertexCount);
    }

    private static void emitMergedWestSkirts(
        VertexSink sink,
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.EdgeColumn[][] edgeGrid,
        int samplesPerAxis,
        int spacing,
        float half,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        for (int lx = 0; lx < samplesPerAxis; lx++) {
            float x0 = region.originBlockX() + lx * spacing - half;
            boolean runActive = false;
            int runStart = 0;
            float runHeight = 0.0F;
            float runBottom = 0.0F;
            int runTint = 0;
            SurfaceMaterial.Sprite runSideSprite = null;

            for (int lz = 0; lz < samplesPerAxis; lz++) {
                LodRegionMesh.EdgeColumn col = edgeGrid[lx][lz];
                boolean needed = false;
                boolean flat = false;
                if (col != null) {
                    HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
                    needed = west != null && west.height() < col.height();
                    flat = needed && col.ySW() == col.yNW();
                }

                if (runActive) {
                    boolean canExtend = flat
                        && col.ySW() == runHeight
                        && col.skirtBottom() == runBottom
                        && col.tintColor() == runTint
                        && col.sideSprite() == runSideSprite;
                    if (canExtend) {
                        continue;
                    }

                    float zStart = region.originBlockZ() + runStart * spacing - half;
                    float zEnd = region.originBlockZ() + (lz - 1) * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x0,
                        zStart,
                        x0,
                        zEnd,
                        runHeight,
                        runHeight,
                        runBottom,
                        runSideSprite,
                        runTint,
                        0.6F,
                        fade,
                        -1.0F,
                        0.0F,
                        0.0F,
                        litFormat,
                        vertexCount
                    );
                    runActive = false;
                }

                if (needed && flat) {
                    runActive = true;
                    runStart = lz;
                    runHeight = col.ySW();
                    runBottom = col.skirtBottom();
                    runTint = col.tintColor();
                    runSideSprite = col.sideSprite();
                } else if (needed) {
                    float z0 = region.originBlockZ() + lz * spacing - half;
                    float z1 = region.originBlockZ() + lz * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x0,
                        z0,
                        x0,
                        z1,
                        col.ySW(),
                        col.yNW(),
                        col.skirtBottom(),
                        col.sideSprite(),
                        col.tintColor(),
                        0.6F,
                        fade,
                        -1.0F,
                        0.0F,
                        0.0F,
                        litFormat,
                        vertexCount
                    );
                }
            }

            if (runActive) {
                float zStart = region.originBlockZ() + runStart * spacing - half;
                float zEnd = region.originBlockZ() + (samplesPerAxis - 1) * spacing + half;
                emitSkirtQuad(
                    sink, x0, zStart, x0, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount
                );
            }
        }
    }

    private static void emitMergedEastSkirts(
        VertexSink sink,
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.EdgeColumn[][] edgeGrid,
        int samplesPerAxis,
        int spacing,
        float half,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        for (int lx = 0; lx < samplesPerAxis; lx++) {
            float x1 = region.originBlockX() + lx * spacing + half;
            boolean runActive = false;
            int runStart = 0;
            float runHeight = 0.0F;
            float runBottom = 0.0F;
            int runTint = 0;
            SurfaceMaterial.Sprite runSideSprite = null;

            for (int lz = 0; lz < samplesPerAxis; lz++) {
                LodRegionMesh.EdgeColumn col = edgeGrid[lx][lz];
                boolean needed = false;
                boolean flat = false;
                if (col != null) {
                    HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
                    needed = east != null && east.height() < col.height();
                    flat = needed && col.ySE() == col.yNE();
                }

                if (runActive) {
                    boolean canExtend = flat
                        && col.ySE() == runHeight
                        && col.skirtBottom() == runBottom
                        && col.tintColor() == runTint
                        && col.sideSprite() == runSideSprite;
                    if (canExtend) {
                        continue;
                    }

                    float zStart = region.originBlockZ() + runStart * spacing - half;
                    float zEnd = region.originBlockZ() + (lz - 1) * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x1,
                        zStart,
                        x1,
                        zEnd,
                        runHeight,
                        runHeight,
                        runBottom,
                        runSideSprite,
                        runTint,
                        0.6F,
                        fade,
                        1.0F,
                        0.0F,
                        0.0F,
                        litFormat,
                        vertexCount
                    );
                    runActive = false;
                }

                if (needed && flat) {
                    runActive = true;
                    runStart = lz;
                    runHeight = col.ySE();
                    runBottom = col.skirtBottom();
                    runTint = col.tintColor();
                    runSideSprite = col.sideSprite();
                } else if (needed) {
                    float z0 = region.originBlockZ() + lz * spacing - half;
                    float z1 = region.originBlockZ() + lz * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x1,
                        z0,
                        x1,
                        z1,
                        col.ySE(),
                        col.yNE(),
                        col.skirtBottom(),
                        col.sideSprite(),
                        col.tintColor(),
                        0.6F,
                        fade,
                        1.0F,
                        0.0F,
                        0.0F,
                        litFormat,
                        vertexCount
                    );
                }
            }

            if (runActive) {
                float zStart = region.originBlockZ() + runStart * spacing - half;
                float zEnd = region.originBlockZ() + (samplesPerAxis - 1) * spacing + half;
                emitSkirtQuad(
                    sink, x1, zStart, x1, zEnd, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount
                );
            }
        }
    }

    private static void emitMergedSouthSkirts(
        VertexSink sink,
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.EdgeColumn[][] edgeGrid,
        int samplesPerAxis,
        int spacing,
        float half,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        for (int lz = 0; lz < samplesPerAxis; lz++) {
            float z0 = region.originBlockZ() + lz * spacing - half;
            boolean runActive = false;
            int runStart = 0;
            float runHeight = 0.0F;
            float runBottom = 0.0F;
            int runTint = 0;
            SurfaceMaterial.Sprite runSideSprite = null;

            for (int lx = 0; lx < samplesPerAxis; lx++) {
                LodRegionMesh.EdgeColumn col = edgeGrid[lx][lz];
                boolean needed = false;
                boolean flat = false;
                if (col != null) {
                    HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
                    needed = south != null && south.height() < col.height();
                    flat = needed && col.ySW() == col.ySE();
                }

                if (runActive) {
                    boolean canExtend = flat
                        && col.ySW() == runHeight
                        && col.skirtBottom() == runBottom
                        && col.tintColor() == runTint
                        && col.sideSprite() == runSideSprite;
                    if (canExtend) {
                        continue;
                    }

                    float xStart = region.originBlockX() + runStart * spacing - half;
                    float xEnd = region.originBlockX() + (lx - 1) * spacing + half;
                    emitSkirtQuad(
                        sink,
                        xStart,
                        z0,
                        xEnd,
                        z0,
                        runHeight,
                        runHeight,
                        runBottom,
                        runSideSprite,
                        runTint,
                        0.8F,
                        fade,
                        0.0F,
                        0.0F,
                        -1.0F,
                        litFormat,
                        vertexCount
                    );
                    runActive = false;
                }

                if (needed && flat) {
                    runActive = true;
                    runStart = lx;
                    runHeight = col.ySW();
                    runBottom = col.skirtBottom();
                    runTint = col.tintColor();
                    runSideSprite = col.sideSprite();
                } else if (needed) {
                    float x0 = region.originBlockX() + lx * spacing - half;
                    float x1 = region.originBlockX() + lx * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x0,
                        z0,
                        x1,
                        z0,
                        col.ySW(),
                        col.ySE(),
                        col.skirtBottom(),
                        col.sideSprite(),
                        col.tintColor(),
                        0.8F,
                        fade,
                        0.0F,
                        0.0F,
                        -1.0F,
                        litFormat,
                        vertexCount
                    );
                }
            }

            if (runActive) {
                float xStart = region.originBlockX() + runStart * spacing - half;
                float xEnd = region.originBlockX() + (samplesPerAxis - 1) * spacing + half;
                emitSkirtQuad(
                    sink, xStart, z0, xEnd, z0, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount
                );
            }
        }
    }

    private static void emitMergedNorthSkirts(
        VertexSink sink,
        LodRegionFile regionFile,
        RegionCoord region,
        LodRegionMesh.EdgeColumn[][] edgeGrid,
        int samplesPerAxis,
        int spacing,
        float half,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        for (int lz = 0; lz < samplesPerAxis; lz++) {
            float z1 = region.originBlockZ() + lz * spacing + half;
            boolean runActive = false;
            int runStart = 0;
            float runHeight = 0.0F;
            float runBottom = 0.0F;
            int runTint = 0;
            SurfaceMaterial.Sprite runSideSprite = null;

            for (int lx = 0; lx < samplesPerAxis; lx++) {
                LodRegionMesh.EdgeColumn col = edgeGrid[lx][lz];
                boolean needed = false;
                boolean flat = false;
                if (col != null) {
                    HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
                    needed = north != null && north.height() < col.height();
                    flat = needed && col.yNW() == col.yNE();
                }

                if (runActive) {
                    boolean canExtend = flat
                        && col.yNW() == runHeight
                        && col.skirtBottom() == runBottom
                        && col.tintColor() == runTint
                        && col.sideSprite() == runSideSprite;
                    if (canExtend) {
                        continue;
                    }

                    float xStart = region.originBlockX() + runStart * spacing - half;
                    float xEnd = region.originBlockX() + (lx - 1) * spacing + half;
                    emitSkirtQuad(
                        sink,
                        xStart,
                        z1,
                        xEnd,
                        z1,
                        runHeight,
                        runHeight,
                        runBottom,
                        runSideSprite,
                        runTint,
                        0.8F,
                        fade,
                        0.0F,
                        0.0F,
                        1.0F,
                        litFormat,
                        vertexCount
                    );
                    runActive = false;
                }

                if (needed && flat) {
                    runActive = true;
                    runStart = lx;
                    runHeight = col.yNW();
                    runBottom = col.skirtBottom();
                    runTint = col.tintColor();
                    runSideSprite = col.sideSprite();
                } else if (needed) {
                    float x0 = region.originBlockX() + lx * spacing - half;
                    float x1 = region.originBlockX() + lx * spacing + half;
                    emitSkirtQuad(
                        sink,
                        x0,
                        z1,
                        x1,
                        z1,
                        col.yNW(),
                        col.yNE(),
                        col.skirtBottom(),
                        col.sideSprite(),
                        col.tintColor(),
                        0.8F,
                        fade,
                        0.0F,
                        0.0F,
                        1.0F,
                        litFormat,
                        vertexCount
                    );
                }
            }

            if (runActive) {
                float xStart = region.originBlockX() + runStart * spacing - half;
                float xEnd = region.originBlockX() + (samplesPerAxis - 1) * spacing + half;
                emitSkirtQuad(
                    sink, xStart, z1, xEnd, z1, runHeight, runHeight, runBottom, runSideSprite, runTint, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount
                );
            }
        }
    }

    private static void emitSubdividedColumn(
        VertexSink sink,
        float x0,
        float z0,
        float x1,
        float z1,
        float ySW,
        float ySE,
        float yNE,
        float yNW,
        int columnHeight,
        HeightmapColumn west,
        HeightmapColumn east,
        HeightmapColumn south,
        HeightmapColumn north,
        float skirtBottom,
        SurfaceMaterial material,
        int tintColor,
        int sub,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount,
        float aoSW,
        float aoSE,
        float aoNE,
        float aoNW
    ) {
        float subSize = (x1 - x0) / sub;
        SurfaceMaterial.Sprite top = material.topSprite();
        SurfaceMaterial.Sprite side = material.sideSprite();
        int[][] subH = new int[sub][sub];

        for (int sz = 0; sz < sub; sz++) {
            for (int sx = 0; sx < sub; sx++) {
                float u = (sx + 0.5F) / sub;
                float v = (sz + 0.5F) / sub;
                subH[sx][sz] = Math.round(bilinearHeight(ySW, ySE, yNE, yNW, u, v));
            }
        }

        boolean westLower = west != null && west.height() < columnHeight;
        boolean eastLower = east != null && east.height() < columnHeight;
        boolean southLower = south != null && south.height() < columnHeight;
        boolean northLower = north != null && north.height() < columnHeight;

        for (int sz = 0; sz < sub; sz++) {
            for (int sx = 0; sx < sub; sx++) {
                float sx0 = x0 + sx * subSize;
                float sx1 = sx0 + subSize;
                float sz0 = z0 + sz * subSize;
                float sz1 = sz0 + subSize;
                float h = subH[sx][sz];
                float ao = bilinearHeight(aoSW, aoSE, aoNE, aoNW, (sx + 0.5F) / sub, (sz + 0.5F) / sub);
                emitBoxTop(sink, sx0, sz0, sx1, sz1, h, h, h, h, top, tintColor, 1.0F * ao, 1.0F * ao, 1.0F * ao, 1.0F * ao, fade, litFormat, vertexCount);
                if (sx > 0) {
                    if (subH[sx - 1][sz] < h) {
                        emitSkirtQuad(sink, sx0, sz0, sx0, sz1, h, h, subH[sx - 1][sz], side, tintColor, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
                    }
                } else if (westLower) {
                    emitSkirtQuad(sink, sx0, sz0, sx0, sz1, h, h, skirtBottom, side, tintColor, 0.6F, fade, -1.0F, 0.0F, 0.0F, litFormat, vertexCount);
                }

                if (sx < sub - 1) {
                    if (subH[sx + 1][sz] < h) {
                        emitSkirtQuad(sink, sx1, sz0, sx1, sz1, h, h, subH[sx + 1][sz], side, tintColor, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
                    }
                } else if (eastLower) {
                    emitSkirtQuad(sink, sx1, sz0, sx1, sz1, h, h, skirtBottom, side, tintColor, 0.6F, fade, 1.0F, 0.0F, 0.0F, litFormat, vertexCount);
                }

                if (sz > 0) {
                    if (subH[sx][sz - 1] < h) {
                        emitSkirtQuad(sink, sx0, sz0, sx1, sz0, h, h, subH[sx][sz - 1], side, tintColor, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
                    }
                } else if (southLower) {
                    emitSkirtQuad(sink, sx0, sz0, sx1, sz0, h, h, skirtBottom, side, tintColor, 0.8F, fade, 0.0F, 0.0F, -1.0F, litFormat, vertexCount);
                }

                if (sz < sub - 1) {
                    if (subH[sx][sz + 1] < h) {
                        emitSkirtQuad(sink, sx0, sz1, sx1, sz1, h, h, subH[sx][sz + 1], side, tintColor, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
                    }
                } else if (northLower) {
                    emitSkirtQuad(sink, sx0, sz1, sx1, sz1, h, h, skirtBottom, side, tintColor, 0.8F, fade, 0.0F, 0.0F, 1.0F, litFormat, vertexCount);
                }
            }
        }
    }

    private static float bilinearHeight(float ySW, float ySE, float yNE, float yNW, float u, float v) {
        float southEdge = ySW + (ySE - ySW) * u;
        float northEdge = yNW + (yNE - yNW) * u;
        return southEdge + (northEdge - southEdge) * v;
    }

    private static float blendedCornerHeight(int centerHeight, HeightmapColumn... cornerNeighbors) {
        float sum = centerHeight;
        int count = 1;

        for (HeightmapColumn neighbor : cornerNeighbors) {
            if (neighbor != null && Math.abs(neighbor.height() - centerHeight) <= 3.0F) {
                sum += neighbor.height();
                count++;
            }
        }

        return Math.round(sum / count);
    }

    static float cornerAo(int centerHeight, HeightmapColumn cardinal1, HeightmapColumn cardinal2, HeightmapColumn diagonal) {
        boolean side1 = cardinal1 != null && cardinal1.height() > centerHeight;
        boolean side2 = cardinal2 != null && cardinal2.height() > centerHeight;
        int occluderCount;
        if (side1 && side2) {
            occluderCount = 3;
        } else {
            boolean corner = diagonal != null && diagonal.height() > centerHeight;
            occluderCount = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        }

        return AO_BRIGHTNESS_BY_OCCLUDER_COUNT[occluderCount];
    }

    private static float lowestNeighborHeight(int columnHeight, HeightmapColumn... neighbors) {
        int lowest = columnHeight;

        for (HeightmapColumn neighbor : neighbors) {
            if (neighbor != null && neighbor.height() < lowest) {
                lowest = neighbor.height();
            }
        }

        return lowest;
    }

    private static List<LodRegionMesh.RecordedTreePart> recordTrees(
        LodRegionFile regionFile, RegionCoord region, LodRegionMesh.FadeParams fade, Registry<Biome> biomeRegistry, int[] vertexCount, int lodLevel
    ) {
        List<LodRegionMesh.TreePlacement> placements = findTreePlacements(
            regionFile, region, regionFile.sampleSpacingBlocks, regionFile.samplesPerAxis, biomeRegistry, lodLevel
        );
        if (placements.isEmpty()) {
            return List.of();
        }

        BiomeStyleConfig config = BiomeStyleConfig.get();
        List<LodRegionMesh.RecordedTreePart> parts = new ArrayList<>();

        for (TreeStyle.Group group : TreeStyle.Group.values()) {
            ResourceLocation trunkTexture = config.trunkTextureId(group);
            if (trunkTexture != null) {
                recordTexturedTreePart(parts, placements, fade, group, LodRegionMesh.TreePart.TRUNK, trunkTexture, config.trunkTint(group), vertexCount);
            }

            ResourceLocation foliageTexture = config.foliageTextureId(group);
            if (foliageTexture != null) {
                recordTexturedTreePart(parts, placements, fade, group, LodRegionMesh.TreePart.FOLIAGE, foliageTexture, config.foliageTint(group), vertexCount);
            }
        }

        recordDefaultTreePart(parts, placements, fade, config, vertexCount);
        return parts;
    }

    private static void recordTexturedTreePart(
        List<LodRegionMesh.RecordedTreePart> parts,
        List<LodRegionMesh.TreePlacement> placements,
        LodRegionMesh.FadeParams fade,
        TreeStyle.Group group,
        LodRegionMesh.TreePart part,
        ResourceLocation texture,
        int tintColor,
        int[] vertexCount
    ) {
        RecordedVertexSink sink = new RecordedVertexSink();
        int[] partVertexCount = new int[]{0};

        for (LodRegionMesh.TreePlacement placement : placements) {
            if (placement.style().group == group && fade.visibleAt(placement.blockX(), placement.groundY(), placement.blockZ())) {
                if (part == LodRegionMesh.TreePart.TRUNK) {
                    emitTexturedTrunk(sink, placement, tintColor, 255, partVertexCount);
                } else {
                    emitTexturedFoliage(sink, placement, tintColor, 255, partVertexCount);
                }
            }
        }

        parts.add(
            new LodRegionMesh.RecordedTreePart(
                LodTreeRenderType.forTexture(texture),
                new LodRegionMesh.RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.POSITION_TEX_COLOR)
            )
        );
        vertexCount[0] += partVertexCount[0];
    }

    private static void recordDefaultTreePart(
        List<LodRegionMesh.RecordedTreePart> parts,
        List<LodRegionMesh.TreePlacement> placements,
        LodRegionMesh.FadeParams fade,
        BiomeStyleConfig config,
        int[] vertexCount
    ) {
        RecordedVertexSink sink = new RecordedVertexSink();
        int[] partVertexCount = new int[]{0};

        for (LodRegionMesh.TreePlacement placement : placements) {
            if (fade.visibleAt(placement.blockX(), placement.groundY(), placement.blockZ())) {
                TreeStyle.Group group = placement.style().group;
                if (config.trunkTextureId(group) == null) {
                    emitBlockTrunk(sink, placement, config.trunkTint(group), 255, partVertexCount);
                }

                if (config.foliageTextureId(group) == null) {
                    emitBlockFoliage(sink, placement, config.foliageTint(group), 255, partVertexCount);
                }
            }
        }

        parts.add(
            new LodRegionMesh.RecordedTreePart(
                LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL,
                new LodRegionMesh.RecordedPart(sink, RecordedVertexSink.Kind.TEXTURED, DefaultVertexFormat.POSITION_TEX_COLOR)
            )
        );
        vertexCount[0] += partVertexCount[0];
    }

    private static VertexBuffer upload(BufferBuilder builder, int vertexCount) {
        if (vertexCount == 0) {
            return null;
        }

        MeshData rendered = builder.buildOrThrow();
        VertexBuffer vertexBuffer = new VertexBuffer(Usage.STATIC);
        vertexBuffer.bind();
        vertexBuffer.upload(rendered);
        VertexBuffer.unbind();
        return vertexBuffer;
    }

    static void emitBoxTop(
        VertexSink sink,
        float x0,
        float z0,
        float x1,
        float z1,
        float ySW,
        float ySE,
        float yNE,
        float yNW,
        SurfaceMaterial.Sprite sprite,
        int tintColor,
        float shadeSW,
        float shadeSE,
        float shadeNE,
        float shadeNW,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        int alpha00 = fade.alphaAt(x0, ySW, z0);
        int alpha10 = fade.alphaAt(x1, ySE, z0);
        int alpha11 = fade.alphaAt(x1, yNE, z1);
        int alpha01 = fade.alphaAt(x0, yNW, z1);
        int baseColor = applyLightTemperature(sprite != null && !sprite.tinted() ? 16777215 : tintColor);
        int color00 = shade(baseColor, shadeSW);
        int color10 = shade(baseColor, shadeSE);
        int color11 = shade(baseColor, shadeNE);
        int color01 = shade(baseColor, shadeNW);
        if (litFormat) {
            float[] crossA = triangleCross(x0, ySW, z0, x1, ySE, z0, x1, yNE, z1);
            float[] crossB = triangleCross(x0, ySW, z0, x1, yNE, z1, x0, yNW, z1);
            float[] n00 = normalizeSum(crossA, crossB);
            float[] n10 = normalize(crossA);
            float[] n11 = normalizeSum(crossA, crossB);
            float[] n01 = normalize(crossB);
            if (sprite == null) {
                sink.litVertex(x0, ySW, z0, color00, alpha00, n00[0], n00[1], n00[2]);
                sink.litVertex(x1, ySE, z0, color10, alpha10, n10[0], n10[1], n10[2]);
                sink.litVertex(x1, yNE, z1, color11, alpha11, n11[0], n11[1], n11[2]);
                sink.litVertex(x0, ySW, z0, color00, alpha00, n00[0], n00[1], n00[2]);
                sink.litVertex(x1, yNE, z1, color11, alpha11, n11[0], n11[1], n11[2]);
                sink.litVertex(x0, yNW, z1, color01, alpha01, n01[0], n01[1], n01[2]);
            } else {
                sink.litTexturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00, n00[0], n00[1], n00[2]);
                sink.litTexturedVertex(x1, ySE, z0, sprite.u1(), sprite.v0(), color10, alpha10, n10[0], n10[1], n10[2]);
                sink.litTexturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11, n11[0], n11[1], n11[2]);
                sink.litTexturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00, n00[0], n00[1], n00[2]);
                sink.litTexturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11, n11[0], n11[1], n11[2]);
                sink.litTexturedVertex(x0, yNW, z1, sprite.u0(), sprite.v1(), color01, alpha01, n01[0], n01[1], n01[2]);
            }
        } else if (sprite == null) {
            sink.vertex(x0, ySW, z0, color00, alpha00);
            sink.vertex(x1, ySE, z0, color10, alpha10);
            sink.vertex(x1, yNE, z1, color11, alpha11);
            sink.vertex(x0, ySW, z0, color00, alpha00);
            sink.vertex(x1, yNE, z1, color11, alpha11);
            sink.vertex(x0, yNW, z1, color01, alpha01);
        } else {
            sink.texturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00);
            sink.texturedVertex(x1, ySE, z0, sprite.u1(), sprite.v0(), color10, alpha10);
            sink.texturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11);
            sink.texturedVertex(x0, ySW, z0, sprite.u0(), sprite.v0(), color00, alpha00);
            sink.texturedVertex(x1, yNE, z1, sprite.u1(), sprite.v1(), color11, alpha11);
            sink.texturedVertex(x0, yNW, z1, sprite.u0(), sprite.v1(), color01, alpha01);
        }

        vertexCount[0] += 6;
    }

    static void emitBoxBottom(
        VertexSink sink,
        float x0,
        float z0,
        float x1,
        float z1,
        float y,
        SurfaceMaterial.Sprite sprite,
        int tintColor,
        float shadeFactor,
        LodRegionMesh.FadeParams fade,
        boolean litFormat,
        int[] vertexCount
    ) {
        int alpha00 = fade.alphaAt(x0, y, z0);
        int alpha10 = fade.alphaAt(x1, y, z0);
        int alpha11 = fade.alphaAt(x1, y, z1);
        int alpha01 = fade.alphaAt(x0, y, z1);
        int color = shade(applyLightTemperature(sprite.tinted() ? tintColor : 16777215), shadeFactor);
        if (litFormat) {
            sink.litTexturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00, 0.0F, -1.0F, 0.0F);
            sink.litTexturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11, 0.0F, -1.0F, 0.0F);
            sink.litTexturedVertex(x1, y, z0, sprite.u1(), sprite.v0(), color, alpha10, 0.0F, -1.0F, 0.0F);
            sink.litTexturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00, 0.0F, -1.0F, 0.0F);
            sink.litTexturedVertex(x0, y, z1, sprite.u0(), sprite.v1(), color, alpha01, 0.0F, -1.0F, 0.0F);
            sink.litTexturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11, 0.0F, -1.0F, 0.0F);
        } else {
            sink.texturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00);
            sink.texturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11);
            sink.texturedVertex(x1, y, z0, sprite.u1(), sprite.v0(), color, alpha10);
            sink.texturedVertex(x0, y, z0, sprite.u0(), sprite.v0(), color, alpha00);
            sink.texturedVertex(x0, y, z1, sprite.u0(), sprite.v1(), color, alpha01);
            sink.texturedVertex(x1, y, z1, sprite.u1(), sprite.v1(), color, alpha11);
        }

        vertexCount[0] += 6;
    }

    static void emitSkirtQuad(
        VertexSink sink,
        float x0,
        float z0,
        float x1,
        float z1,
        float yTop0,
        float yTop1,
        float yBottom,
        SurfaceMaterial.Sprite sprite,
        int tintColor,
        float shadeFactor,
        LodRegionMesh.FadeParams fade,
        float nx,
        float ny,
        float nz,
        boolean litFormat,
        int[] vertexCount
    ) {
        int alpha0 = fade.alphaAt(x0, yTop0, z0);
        int alpha1 = fade.alphaAt(x1, yTop1, z1);
        int color = shade(applyLightTemperature(sprite != null && !sprite.tinted() ? 16777215 : tintColor), shadeFactor * wallSunRelief(nx, nz));
        boolean forward = (z1 - z0) * nx - (x1 - x0) * nz < 0.0F;
        if (litFormat) {
            if (sprite == null) {
                if (forward) {
                    sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
                    sink.litVertex(x1, yTop1, z1, color, alpha1, nx, ny, nz);
                    sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
                    sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
                    sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
                    sink.litVertex(x0, yBottom, z0, color, alpha0, nx, ny, nz);
                } else {
                    sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
                    sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
                    sink.litVertex(x1, yTop1, z1, color, alpha1, nx, ny, nz);
                    sink.litVertex(x0, yTop0, z0, color, alpha0, nx, ny, nz);
                    sink.litVertex(x0, yBottom, z0, color, alpha0, nx, ny, nz);
                    sink.litVertex(x1, yBottom, z1, color, alpha1, nx, ny, nz);
                }
            } else if (forward) {
                sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
                sink.litTexturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1, nx, ny, nz);
                sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
                sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
                sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
                sink.litTexturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0, nx, ny, nz);
            } else {
                sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
                sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
                sink.litTexturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1, nx, ny, nz);
                sink.litTexturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0, nx, ny, nz);
                sink.litTexturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0, nx, ny, nz);
                sink.litTexturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1, nx, ny, nz);
            }
        } else if (sprite == null) {
            if (forward) {
                sink.vertex(x0, yTop0, z0, color, alpha0);
                sink.vertex(x1, yTop1, z1, color, alpha1);
                sink.vertex(x1, yBottom, z1, color, alpha1);
                sink.vertex(x0, yTop0, z0, color, alpha0);
                sink.vertex(x1, yBottom, z1, color, alpha1);
                sink.vertex(x0, yBottom, z0, color, alpha0);
            } else {
                sink.vertex(x0, yTop0, z0, color, alpha0);
                sink.vertex(x1, yBottom, z1, color, alpha1);
                sink.vertex(x1, yTop1, z1, color, alpha1);
                sink.vertex(x0, yTop0, z0, color, alpha0);
                sink.vertex(x0, yBottom, z0, color, alpha0);
                sink.vertex(x1, yBottom, z1, color, alpha1);
            }
        } else if (forward) {
            sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
            sink.texturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1);
            sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
            sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
            sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
            sink.texturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0);
        } else {
            sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
            sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
            sink.texturedVertex(x1, yTop1, z1, sprite.u1(), sprite.v0(), color, alpha1);
            sink.texturedVertex(x0, yTop0, z0, sprite.u0(), sprite.v0(), color, alpha0);
            sink.texturedVertex(x0, yBottom, z0, sprite.u0(), sprite.v1(), color, alpha0);
            sink.texturedVertex(x1, yBottom, z1, sprite.u1(), sprite.v1(), color, alpha1);
        }

        vertexCount[0] += 6;
    }

    private static float[] triangleCross(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {
        float e1x = bx - ax;
        float e1y = by - ay;
        float e1z = bz - az;
        float e2x = cx - ax;
        float e2y = cy - ay;
        float e2z = cz - az;
        float nx = e1y * e2z - e1z * e2y;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        if (ny < 0.0F) {
            nx = -nx;
            ny = -ny;
            nz = -nz;
        }

        return new float[]{nx, ny, nz};
    }

    private static float[] normalize(float[] v) {
        float len = (float)Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return len < 1.0E-6F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{v[0] / len, v[1] / len, v[2] / len};
    }

    private static float[] normalizeSum(float[] a, float[] b) {
        return normalize(new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]});
    }

    private static void emitFacetedCell(
        VertexSink sink,
        LodRegionFile regionFile,
        int spacing,
        int samplesPerAxis,
        int[] vertexCount,
        LodRegionMesh.FadeParams fade,
        Registry<Biome> biomeRegistry,
        boolean litFormat,
        boolean opaquePass,
        boolean cullSubmergedTerrain,
        RegionCoord r00,
        int lx00,
        int lz00,
        RegionCoord r10,
        int lx10,
        int lz10,
        RegionCoord r01,
        int lx01,
        int lz01,
        RegionCoord r11,
        int lx11,
        int lz11
    ) {
        HeightmapColumn c00 = regionFile.readColumn(r00, lx00, lz00);
        HeightmapColumn c10 = regionFile.readColumn(r10, lx10, lz10);
        HeightmapColumn c01 = regionFile.readColumn(r01, lx01, lz01);
        HeightmapColumn c11 = regionFile.readColumn(r11, lx11, lz11);
        if (c00 != null && c10 != null && c01 != null && c11 != null) {
            if (!cullSubmergedTerrain || !allSubmerged(c00, c10, c01, c11)) {
                float x0 = r00.originBlockX() + lx00 * spacing;
                float z0 = r00.originBlockZ() + lz00 * spacing;
                float x1 = r10.originBlockX() + lx10 * spacing;
                float z1 = r01.originBlockZ() + lz01 * spacing;
                int alpha00 = fade.alphaAt(x0, c00.height(), z0);
                int alpha10 = fade.alphaAt(x1, c10.height(), z0);
                int alpha01 = fade.alphaAt(x0, c01.height(), z1);
                int alpha11 = fade.alphaAt(x1, c11.height(), z1);
                boolean cellFullyOpaque = alpha00 == 255 && alpha10 == 255 && alpha01 == 255 && alpha11 == 255;
                if (cellFullyOpaque == opaquePass) {
                    float h00 = c00.height();
                    float h10 = c10.height();
                    float h01 = c01.height();
                    float h11 = c11.height();
                    int color00 = vertexColor(regionFile, r00, lx00, lz00, c00, samplesPerAxis, spacing, biomeRegistry);
                    int color10 = vertexColor(regionFile, r10, lx10, lz10, c10, samplesPerAxis, spacing, biomeRegistry);
                    int color01 = vertexColor(regionFile, r01, lx01, lz01, c01, samplesPerAxis, spacing, biomeRegistry);
                    int color11 = vertexColor(regionFile, r11, lx11, lz11, c11, samplesPerAxis, spacing, biomeRegistry);
                    int flatColor = quantizeColor(bilinearColor(color00, color10, color01, color11, 0.5F, 0.5F));
                    if (litFormat) {
                        float[] crossA = triangleCross(x0, h00, z0, x1, h10, z0, x1, h11, z1);
                        float[] crossB = triangleCross(x0, h00, z0, x1, h11, z1, x0, h01, z1);
                        float[] faceNormal = normalizeSum(crossA, crossB);
                        sink.litVertex(x0, h00, z0, flatColor, alpha00, faceNormal[0], faceNormal[1], faceNormal[2]);
                        sink.litVertex(x1, h10, z0, flatColor, alpha10, faceNormal[0], faceNormal[1], faceNormal[2]);
                        sink.litVertex(x1, h11, z1, flatColor, alpha11, faceNormal[0], faceNormal[1], faceNormal[2]);
                        sink.litVertex(x0, h00, z0, flatColor, alpha00, faceNormal[0], faceNormal[1], faceNormal[2]);
                        sink.litVertex(x1, h11, z1, flatColor, alpha11, faceNormal[0], faceNormal[1], faceNormal[2]);
                        sink.litVertex(x0, h01, z1, flatColor, alpha01, faceNormal[0], faceNormal[1], faceNormal[2]);
                    } else {
                        sink.vertex(x0, h00, z0, flatColor, alpha00);
                        sink.vertex(x1, h10, z0, flatColor, alpha10);
                        sink.vertex(x1, h11, z1, flatColor, alpha11);
                        sink.vertex(x0, h00, z0, flatColor, alpha00);
                        sink.vertex(x1, h11, z1, flatColor, alpha11);
                        sink.vertex(x0, h01, z1, flatColor, alpha01);
                    }

                    vertexCount[0] += 6;
                }
            }
        }
    }

    private static HeightmapColumn columnAt(LodRegionFile regionFile, RegionCoord region, int lx, int lz, int samplesPerAxis) {
        int rx = region.x();
        int rz = region.z();
        if (lx < 0) {
            rx--;
            lx += samplesPerAxis;
        } else if (lx >= samplesPerAxis) {
            rx++;
            lx -= samplesPerAxis;
        }

        if (lz < 0) {
            rz--;
            lz += samplesPerAxis;
        } else if (lz >= samplesPerAxis) {
            rz++;
            lz -= samplesPerAxis;
        }

        return regionFile.readColumn(new RegionCoord(rx, rz), lx, lz);
    }

    private static List<LodRegionMesh.TreePlacement> findTreePlacements(
        LodRegionFile regionFile, RegionCoord region, int spacing, int samplesPerAxis, Registry<Biome> biomeRegistry, int lodLevel
    ) {
        List<LodRegionMesh.TreePlacement> placements = new ArrayList<>();
        float positionRandomRange = spacing <= 4 ? spacing * 0.2F : spacing * 0.4F;
        float snowStart = snowStartHeightBlocks();
        boolean boxGeometry = lodLevel <= 2;

        for (int lz = 0; lz < samplesPerAxis; lz++) {
            for (int lx = 0; lx < samplesPerAxis; lx++) {
                HeightmapColumn column = regionFile.readColumn(region, lx, lz);
                if (column != null && column.height() > 63 && !(column.height() > snowStart) && column.hasTrees()) {
                    float perBlockAreaDensity = TreeDensityEstimator.densityPerBlockArea(biomeRegistry, column.biomeRawId()) * 2.0F;
                    float density = clamp01(spacing * spacing * perBlockAreaDensity);
                    int blockX = region.originBlockX() + lx * spacing;
                    int blockZ = region.originBlockZ() + lz * spacing;
                    if (!(hashRandom01(blockX, blockZ) >= density)) {
                        HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
                        HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
                        HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
                        HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
                        float dhdx = centralDifference(west, east, column, spacing);
                        float dhdz = centralDifference(south, north, column, spacing);
                        float slope = (float)Math.sqrt(dhdx * dhdx + dhdz * dhdz);
                        if (!(slope > 0.3F)) {
                            float offsetX = (hashRandom01(blockX ^ 305419896, blockZ) - 0.5F) * 2.0F * positionRandomRange;
                            float offsetZ = (hashRandom01(blockX, blockZ ^ -2023406815) - 0.5F) * 2.0F * positionRandomRange;
                            int finalBlockX = Math.round(blockX + offsetX);
                            int finalBlockZ = Math.round(blockZ + offsetZ);
                            TreeStyle style = TreeStyle.forBiome(biomeRegistry, column.biomeRawId());
                            placements.add(new LodRegionMesh.TreePlacement(finalBlockX, finalBlockZ, column.height(), style, boxGeometry));
                        }
                    }
                }
            }
        }

        return placements;
    }

    private static void emitTexturedTrunk(VertexSink sink, LodRegionMesh.TreePlacement placement, int trunkTint, int alpha, int[] vertexCount) {
        float cx = placement.blockX() + 0.5F;
        float cz = placement.blockZ() + 0.5F;
        float trunkTop = placement.groundY() + placement.style().trunkHeight;
        if (placement.boxGeometry()) {
            emitBox(sink, cx - 0.5F, cz - 0.5F, cx + 0.5F, cz + 0.5F, placement.groundY(), trunkTop, FULL_SPRITE, trunkTint, alpha, false, vertexCount);
        } else {
            emitCrossedQuadsAny(sink, cx, cz, placement.groundY(), trunkTop, 0.5F, trunkTint, alpha, FULL_SPRITE, vertexCount);
        }
    }

    private static void emitBlockTrunk(VertexSink sink, LodRegionMesh.TreePlacement placement, int trunkTint, int alpha, int[] vertexCount) {
        float cx = placement.blockX() + 0.5F;
        float cz = placement.blockZ() + 0.5F;
        float trunkTop = placement.groundY() + placement.style().trunkHeight;
        SurfaceMaterial.Sprite sprite = placement.style().trunkSprite();
        int color = sprite.tinted() ? trunkTint : 16777215;
        if (placement.boxGeometry()) {
            emitBox(sink, cx - 0.5F, cz - 0.5F, cx + 0.5F, cz + 0.5F, placement.groundY(), trunkTop, sprite, color, alpha, false, vertexCount);
        } else {
            emitCrossedQuadsAny(sink, cx, cz, placement.groundY(), trunkTop, 0.5F, color, alpha, sprite, vertexCount);
        }
    }

    private static void emitTexturedFoliage(VertexSink sink, LodRegionMesh.TreePlacement placement, int foliageTint, int alpha, int[] vertexCount) {
        float cx = placement.blockX() + 0.5F;
        float cz = placement.blockZ() + 0.5F;
        emitCanopy(sink, cx, cz, canopyBaseY(placement), placement.style(), foliageTint, alpha, FULL_SPRITE, placement.boxGeometry(), vertexCount);
    }

    private static void emitBlockFoliage(VertexSink sink, LodRegionMesh.TreePlacement placement, int foliageTint, int alpha, int[] vertexCount) {
        float cx = placement.blockX() + 0.5F;
        float cz = placement.blockZ() + 0.5F;
        SurfaceMaterial.Sprite sprite = placement.style().foliageSprite();
        int color = sprite.tinted() ? foliageTint : 16777215;
        emitCanopy(sink, cx, cz, canopyBaseY(placement), placement.style(), color, alpha, sprite, placement.boxGeometry(), vertexCount);
    }

    private static float canopyBaseY(LodRegionMesh.TreePlacement placement) {
        return placement.groundY() + placement.style().trunkHeight * 0.35F;
    }

    private static void emitCanopy(
        VertexSink sink,
        float cx,
        float cz,
        float baseY,
        TreeStyle style,
        int colorRgb,
        int alpha,
        SurfaceMaterial.Sprite sprite,
        boolean boxGeometry,
        int[] vertexCount
    ) {
        switch (style.shape) {
            case CONICAL:
                float[] heightsx = new float[]{0.0F, 0.4F, 0.75F, 1.0F};
                float[] radiix = new float[]{1.0F, 0.65F, 0.3F};
                if (boxGeometry) {
                    emitCanopyBoxTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heightsx, radiix, vertexCount);
                } else {
                    emitCanopyTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heightsx, radiix, vertexCount);
                }
                break;
            case FLAT_TOP:
                if (boxGeometry) {
                    float r = style.canopyRadius;
                    emitBox(sink, cx - r, cz - r, cx + r, cz + r, baseY, baseY + style.canopyHeight, sprite, colorRgb, alpha, true, vertexCount);
                } else {
                    emitCrossedQuadsAny(sink, cx, cz, baseY, baseY + style.canopyHeight, style.canopyRadius, colorRgb, alpha, sprite, vertexCount);
                }
                break;
            default:
                float[] heights = new float[]{0.0F, 0.5F, 1.0F};
                float[] radii = new float[]{1.0F, 0.6F};
                if (boxGeometry) {
                    emitCanopyBoxTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
                } else {
                    emitCanopyTiers(sink, cx, cz, baseY, style, colorRgb, alpha, sprite, heights, radii, vertexCount);
                }
        }
    }

    private static void emitCanopyTiers(
        VertexSink sink,
        float cx,
        float cz,
        float baseY,
        TreeStyle style,
        int colorRgb,
        int alpha,
        SurfaceMaterial.Sprite sprite,
        float[] heightFractions,
        float[] radiusFractions,
        int[] vertexCount
    ) {
        for (int i = 0; i < radiusFractions.length; i++) {
            float yBottom = baseY + heightFractions[i] * style.canopyHeight;
            float yTop = baseY + heightFractions[i + 1] * style.canopyHeight;
            float radius = radiusFractions[i] * style.canopyRadius;
            emitCrossedQuadsAny(sink, cx, cz, yBottom, yTop, radius, colorRgb, alpha, sprite, vertexCount);
        }
    }

    private static void emitCanopyBoxTiers(
        VertexSink sink,
        float cx,
        float cz,
        float baseY,
        TreeStyle style,
        int colorRgb,
        int alpha,
        SurfaceMaterial.Sprite sprite,
        float[] heightFractions,
        float[] radiusFractions,
        int[] vertexCount
    ) {
        for (int i = 0; i < radiusFractions.length; i++) {
            float yBottom = baseY + heightFractions[i] * style.canopyHeight;
            float yTop = baseY + heightFractions[i + 1] * style.canopyHeight;
            float radius = radiusFractions[i] * style.canopyRadius;
            emitBox(sink, cx - radius, cz - radius, cx + radius, cz + radius, yBottom, yTop, sprite, colorRgb, alpha, true, vertexCount);
        }
    }

    private static void emitBox(
        VertexSink sink,
        float x0,
        float z0,
        float x1,
        float z1,
        float yBottom,
        float yTop,
        SurfaceMaterial.Sprite sprite,
        int tintColor,
        int alpha,
        boolean withTop,
        int[] vertexCount
    ) {
        int southNorthColor = shade(tintColor, 0.8F);
        int westEastColor = shade(tintColor, 0.6F);
        emitQuadTextured(sink, x0, yBottom, z0, x1, yBottom, z0, x1, yTop, z0, x0, yTop, z0, sprite, southNorthColor, alpha);
        emitQuadTextured(sink, x1, yBottom, z1, x0, yBottom, z1, x0, yTop, z1, x1, yTop, z1, sprite, southNorthColor, alpha);
        emitQuadTextured(sink, x0, yBottom, z1, x0, yBottom, z0, x0, yTop, z0, x0, yTop, z1, sprite, westEastColor, alpha);
        emitQuadTextured(sink, x1, yBottom, z0, x1, yBottom, z1, x1, yTop, z1, x1, yTop, z0, sprite, westEastColor, alpha);
        int quads = 4;
        if (withTop) {
            int topColor = shade(tintColor, 1.0F);
            emitQuadTextured(sink, x0, yTop, z0, x1, yTop, z0, x1, yTop, z1, x0, yTop, z1, sprite, topColor, alpha);
            quads = 5;
        }

        vertexCount[0] += quads * 6;
    }

    private static void emitCrossedQuadsAny(
        VertexSink sink,
        float cx,
        float cz,
        float yBottom,
        float yTop,
        float halfWidth,
        int colorRgb,
        int alpha,
        SurfaceMaterial.Sprite sprite,
        int[] vertexCount
    ) {
        if (sprite != null) {
            emitCrossedQuadsTextured(sink, cx, cz, yBottom, yTop, halfWidth, sprite, colorRgb, alpha, vertexCount);
        } else {
            emitCrossedQuads(sink, cx, cz, yBottom, yTop, halfWidth, colorRgb, alpha, vertexCount);
        }
    }

    private static void emitCrossedQuads(
        VertexSink sink, float cx, float cz, float yBottom, float yTop, float halfWidth, int colorRgb, int alpha, int[] vertexCount
    ) {
        emitQuad(sink, cx - halfWidth, yBottom, cz, cx + halfWidth, yBottom, cz, cx + halfWidth, yTop, cz, cx - halfWidth, yTop, cz, colorRgb, alpha);
        emitQuad(sink, cx, yBottom, cz - halfWidth, cx, yBottom, cz + halfWidth, cx, yTop, cz + halfWidth, cx, yTop, cz - halfWidth, colorRgb, alpha);
        vertexCount[0] += 12;
    }

    private static void emitQuad(
        VertexSink sink,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float x3,
        float y3,
        float z3,
        int colorRgb,
        int alpha
    ) {
        sink.vertex(x0, y0, z0, colorRgb, alpha);
        sink.vertex(x1, y1, z1, colorRgb, alpha);
        sink.vertex(x2, y2, z2, colorRgb, alpha);
        sink.vertex(x0, y0, z0, colorRgb, alpha);
        sink.vertex(x2, y2, z2, colorRgb, alpha);
        sink.vertex(x3, y3, z3, colorRgb, alpha);
    }

    private static void emitCrossedQuadsTextured(
        VertexSink sink,
        float cx,
        float cz,
        float yBottom,
        float yTop,
        float halfWidth,
        SurfaceMaterial.Sprite sprite,
        int tintColor,
        int alpha,
        int[] vertexCount
    ) {
        emitQuadTextured(
            sink, cx - halfWidth, yBottom, cz, cx + halfWidth, yBottom, cz, cx + halfWidth, yTop, cz, cx - halfWidth, yTop, cz, sprite, tintColor, alpha
        );
        emitQuadTextured(
            sink, cx, yBottom, cz - halfWidth, cx, yBottom, cz + halfWidth, cx, yTop, cz + halfWidth, cx, yTop, cz - halfWidth, sprite, tintColor, alpha
        );
        vertexCount[0] += 12;
    }

    private static void emitQuadTextured(
        VertexSink sink,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float x3,
        float y3,
        float z3,
        SurfaceMaterial.Sprite sprite,
        int colorRgb,
        int alpha
    ) {
        sink.texturedVertex(x0, y0, z0, sprite.u0(), sprite.v1(), colorRgb, alpha);
        sink.texturedVertex(x1, y1, z1, sprite.u1(), sprite.v1(), colorRgb, alpha);
        sink.texturedVertex(x2, y2, z2, sprite.u1(), sprite.v0(), colorRgb, alpha);
        sink.texturedVertex(x0, y0, z0, sprite.u0(), sprite.v1(), colorRgb, alpha);
        sink.texturedVertex(x2, y2, z2, sprite.u1(), sprite.v0(), colorRgb, alpha);
        sink.texturedVertex(x3, y3, z3, sprite.u0(), sprite.v0(), colorRgb, alpha);
    }

    private static float hashRandom01(int x, int z) {
        long h = x * -7046029254386353131L ^ z * -4417276706812531889L;
        h ^= h >>> 33;
        h *= -49064778989728563L;
        h ^= h >>> 33;
        return (float)(h >>> 40) / 1.6777216E7F;
    }

    private static int vertexColor(
        LodRegionFile regionFile, RegionCoord region, int lx, int lz, HeightmapColumn column, int samplesPerAxis, int spacing, Registry<Biome> biomeRegistry
    ) {
        HeightmapColumn west = columnAt(regionFile, region, lx - 1, lz, samplesPerAxis);
        HeightmapColumn east = columnAt(regionFile, region, lx + 1, lz, samplesPerAxis);
        HeightmapColumn south = columnAt(regionFile, region, lx, lz - 1, samplesPerAxis);
        HeightmapColumn north = columnAt(regionFile, region, lx, lz + 1, samplesPerAxis);
        float dhdx = centralDifference(west, east, column, spacing);
        float dhdz = centralDifference(south, north, column, spacing);
        float brightness = bakedSurfaceBrightness(west, east, south, north, column, spacing, LodSettingsConfig.get().slopeShadingFloor());
        float slope = (float)Math.sqrt(dhdx * dhdx + dhdz * dhdz);
        float dirtBlend = smoothstep(0.3F, 0.9F, slope);
        float stoneBlend = smoothstep(0.9F, 1.6F, slope);
        int blockX = region.originBlockX() + lx * spacing;
        int blockZ = region.originBlockZ() + lz * spacing;
        Biome biome = biomeRegistry != null ? (Biome)biomeRegistry.byId(column.biomeRawId()) : null;
        ResourceLocation biomeKey = biome != null && biomeRegistry != null ? biomeRegistry.getKey(biome) : null;
        boolean coldEnoughToSnow = biome != null && biome.coldEnoughToSnow(new BlockPos(blockX, column.height(), blockZ));
        boolean biomeSand = SurfaceMaterial.isSandBiome(biomeKey);
        float sandBlend = biomeSand ? 1.0F - smoothstep(0.0F, 0.3F, slope) : 0.0F;
        int color = terrainColor(column, biomeSand, sandBlend, dirtBlend, stoneBlend, coldEnoughToSnow);
        color = applyGroundTint(column, biomeRegistry, color);
        color = desaturate(color, LodSettingsConfig.get().saturationReduction());
        color = applyLightTemperature(color);
        return shade(color, brightness);
    }

    private static int multiplyColor(int baseColor, int tint) {
        if (tint == 16777215) {
            return baseColor;
        }

        int r = (baseColor >> 16 & 0xFF) * (tint >> 16 & 0xFF) / 255;
        int g = (baseColor >> 8 & 0xFF) * (tint >> 8 & 0xFF) / 255;
        int b = (baseColor & 0xFF) * (tint & 0xFF) / 255;
        return r << 16 | g << 8 | b;
    }

    private static int applyGroundTint(HeightmapColumn column, Registry<Biome> biomeRegistry, int baseColor) {
        TreeStyle.Group group = TreeStyle.forBiome(biomeRegistry, column.biomeRawId()).group;
        return BiomeStyleConfig.get().applyGroundTint(group, baseColor);
    }

    static float centralDifference(HeightmapColumn negative, HeightmapColumn positive, HeightmapColumn center, int spacing) {
        if (negative != null && positive != null) {
            return (positive.height() - negative.height()) / (2.0F * spacing);
        } else if (positive != null) {
            return (float)(positive.height() - center.height()) / spacing;
        } else {
            return negative != null ? (float)(center.height() - negative.height()) / spacing : 0.0F;
        }
    }

    private static float bakedSurfaceBrightness(
        HeightmapColumn west, HeightmapColumn east, HeightmapColumn south, HeightmapColumn north, HeightmapColumn center, int spacing, float slopeFloor
    ) {
        return bakedSurfaceBrightness(centralDifference(west, east, center, spacing), centralDifference(south, north, center, spacing), slopeFloor);
    }

    static float bakedSurfaceBrightness(float dhdx, float dhdz, float slopeFloor) {
        float normalLength = (float)Math.sqrt(dhdx * dhdx + 1.0F + dhdz * dhdz);
        float brightness = slopeFloor + (1.0F - slopeFloor) / normalLength;
        float ndotl = (-dhdx * -0.4082F + 0.8165F - dhdz * -0.4082F) / normalLength;
        float relief = ndotl - 0.8165F;
        brightness *= 1.0F + relief * LodSettingsConfig.get().sunReliefStrength();
        return Math.max(0.0F, Math.min(1.35F, brightness));
    }

    static float wallSunRelief(float nx, float nz) {
        float ndotl = nx * -0.4082F + nz * -0.4082F;
        float relief = ndotl - 0.8165F;
        return Math.max(0.0F, Math.min(1.35F, 1.0F + relief * LodSettingsConfig.get().sunReliefStrength()));
    }

    private static int applyLightTemperature(int colorRgb) {
        float temperature = LodSettingsConfig.get().lightTemperature();
        if (temperature == 0.5F) {
            return colorRgb;
        }

        float warmth = (temperature - 0.5F) * 2.0F;
        float redScale = 1.0F + warmth * 0.2F;
        float blueScale = 1.0F - warmth * 0.2F;
        int r = clampByte(Math.round((colorRgb >> 16 & 0xFF) * redScale));
        int g = colorRgb >> 8 & 0xFF;
        int b = clampByte(Math.round((colorRgb & 0xFF) * blueScale));
        return r << 16 | g << 8 | b;
    }

    static float snowStartHeightBlocks() { //deprecated
        ClientLevel level = Minecraft.getInstance().level;
        int minHeight = level != null ? level.getMinBuildHeight() : -64;
        int maxHeight = level != null ? level.getMaxBuildHeight() : 320;
        float percent = BiomeStyleConfig.get().snowHeightPercent();
        return minHeight + (maxHeight - minHeight) * (percent / 100.0F);
    }

    private static int terrainColor(HeightmapColumn column, boolean biomeSand, float sandBlend, float dirtBlend, float stoneBlend, boolean coldEnoughToSnow) {
        int color = column.colorRgb();
        if (biomeSand) {
            int tintedSand = multiplyColor(14274464, LodSettingsConfig.get().nearTerrainTint(SurfaceMaterial.Kind.SAND));
            color = blend(color, tintedSand, sandBlend);
        }

        color = blend(color, 7165246, dirtBlend);
        color = blend(color, 9079426, stoneBlend);
        int height = column.height();
        if (height < 63) {
            float depthFactor = clamp01((63 - height) / 20.0F);
            color = blend(color, 3103882, depthFactor);
        } else if (coldEnoughToSnow) {
            float snowFactor = 0.8F * (1.0F - stoneBlend);
            color = blend(color, 15789021, snowFactor);
        }

        return color;
    }

    private static int desaturate(int colorRgb, float amount) {
        int r = colorRgb >> 16 & 0xFF;
        int g = colorRgb >> 8 & 0xFF;
        int b = colorRgb & 0xFF;
        int luminance = Math.round(0.299F * r + 0.587F * g + 0.114F * b);
        int nr = clampByte(Math.round(r + (luminance - r) * amount));
        int ng = clampByte(Math.round(g + (luminance - g) * amount));
        int nb = clampByte(Math.round(b + (luminance - b) * amount));
        return nr << 16 | ng << 8 | nb;
    }

    private static int shade(int colorRgb, float brightness) {
        int r = clampByte(Math.round((colorRgb >> 16 & 0xFF) * brightness));
        int g = clampByte(Math.round((colorRgb >> 8 & 0xFF) * brightness));
        int b = clampByte(Math.round((colorRgb & 0xFF) * brightness));
        return r << 16 | g << 8 | b;
    }

    private static int blend(int colorA, int colorB, float t) {
        t = clamp01(t);
        int ar = colorA >> 16 & 0xFF;
        int ag = colorA >> 8 & 0xFF;
        int ab = colorA & 0xFF;
        int br = colorB >> 16 & 0xFF;
        int bg = colorB >> 8 & 0xFF;
        int bb = colorB & 0xFF;
        int r = clampByte(Math.round(ar + (br - ar) * t));
        int g = clampByte(Math.round(ag + (bg - ag) * t));
        int b = clampByte(Math.round(ab + (bb - ab) * t));
        return r << 16 | g << 8 | b;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0F - 2.0F * t);
    }

    private static float clamp01(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }

    private static int clampByte(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static void renderTerrainLitOpaque(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        List<VertexBuffer> plainLitOpaque = new ArrayList<>();
        List<VertexBuffer> texturedLitOpaque = new ArrayList<>();

        for (LodRegionMesh mesh : meshes) {
            if (mesh.terrainLit && mesh.terrainBufferOpaque != null) {
                (mesh.terrainTextured ? texturedLitOpaque : plainLitOpaque).add(mesh.terrainBufferOpaque);
            }
        }

        boolean shaderPackActive = IrisCompat.isShaderPackActive();
        boolean parallaxShaderReady = !shaderPackActive && LodParallaxShader.getOrNull() != null;
        boolean litShaderReady = !shaderPackActive && LodLitShader.getOrNull() != null;
        boolean backfaceCullingEnabled = LodSettingsConfig.get().backfaceCullingEnabled();
        Supplier<ShaderInstance> litShader = litShaderReady ? LodLitShader::getOrNull : GameRenderer::getRendertypeSolidShader;
        RenderType plainLitOpaqueRenderType = parallaxShaderReady
                ? LodTerrainRenderType.TERRAIN_PARALLAX
                : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_OPAQUE : LodTerrainRenderType.TERRAIN_LIT_OPAQUE_NOCULL);
        Supplier<ShaderInstance> plainLitShader = parallaxShaderReady ? LodParallaxShader::getOrNull : litShader;
        renderBucket(plainLitOpaque, plainLitOpaqueRenderType, plainLitShader, modelViewMatrix, projectionMatrix);
        renderBucket(
                texturedLitOpaque,
                backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE_NOCULL,
                litShader,
                modelViewMatrix,
                projectionMatrix
        );
    }

    public static void renderTerrainLitFade(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        List<VertexBuffer> plainLitFade = new ArrayList<>();
        List<VertexBuffer> texturedLitFade = new ArrayList<>();

        for (LodRegionMesh mesh : meshes) {
            if (mesh.terrainLit) {
                if (mesh.terrainBufferFade != null) {
                    (mesh.terrainTextured ? texturedLitFade : plainLitFade).add(mesh.terrainBufferFade);
                }
            }
        }

        if (IrisCompat.isShaderPackActive()) {
            // Shader packs fully replace the translucent fragment shader with their own water/glass material
            // So basically our little fade thing with alpha doesn't work as far as I'm aware
            // I don't think there's an (easy) way around this, so for now we're just going to have to settle with a hard cutoff
            // We can fully deal with this later, 1.4.0 does not support shaders
            boolean backfaceCullingEnabled = LodSettingsConfig.get().backfaceCullingEnabled();
            renderBucket(
                    plainLitFade,
                    backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_OPAQUE : LodTerrainRenderType.TERRAIN_LIT_OPAQUE_NOCULL,
                    GameRenderer::getRendertypeSolidShader,
                    modelViewMatrix,
                    projectionMatrix
            );
            renderBucket(
                    texturedLitFade,
                    backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_LIT_TEXTURED_OPAQUE_NOCULL,
                    GameRenderer::getRendertypeSolidShader,
                    modelViewMatrix,
                    projectionMatrix
            );
        } else {
            Supplier<ShaderInstance> litShader = LodLitShader.getOrNull() != null ? LodLitShader::getOrNull : GameRenderer::getRendertypeTranslucentShader;
            renderBucket(plainLitFade, LodTerrainRenderType.TERRAIN_LIT, litShader, modelViewMatrix, projectionMatrix);
            renderBucket(texturedLitFade, LodTerrainRenderType.TERRAIN_LIT_TEXTURED, litShader, modelViewMatrix, projectionMatrix);
        }
    }

    public static void renderTerrainCheap(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        List<VertexBuffer> plainCheapOpaque = new ArrayList<>();
        List<VertexBuffer> plainCheapFade = new ArrayList<>();
        List<VertexBuffer> texturedCheapOpaque = new ArrayList<>();
        List<VertexBuffer> texturedCheapFade = new ArrayList<>();

        for (LodRegionMesh mesh : meshes) {
            if (!mesh.terrainLit) {
                if (mesh.terrainBufferOpaque != null) {
                    (mesh.terrainTextured ? texturedCheapOpaque : plainCheapOpaque).add(mesh.terrainBufferOpaque);
                }

                if (mesh.terrainBufferFade != null) {
                    (mesh.terrainTextured ? texturedCheapFade : plainCheapFade).add(mesh.terrainBufferFade);
                }
            }
        }

        boolean backfaceCullingEnabled = LodSettingsConfig.get().backfaceCullingEnabled();
        boolean plainFogReady = LodFogShader.getPlainOrNull() != null;
        boolean texturedFogReady = LodFogShader.getTexturedOrNull() != null;
        RenderType plainOpaqueType = plainFogReady
            ? (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_FOG_OPAQUE : LodTerrainRenderType.TERRAIN_FOG_OPAQUE_NOCULL)
            : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_OPAQUE : LodTerrainRenderType.TERRAIN_OPAQUE_NOCULL);
        Supplier<ShaderInstance> plainOpaqueShader = plainFogReady ? LodFogShader::getPlainOrNull : GameRenderer::getPositionColorShader;
        renderBucket(plainCheapOpaque, plainOpaqueType, plainOpaqueShader, modelViewMatrix, projectionMatrix);
        RenderType plainFadeType = plainFogReady ? LodTerrainRenderType.TERRAIN_FOG : LodTerrainRenderType.TERRAIN;
        Supplier<ShaderInstance> plainFadeShader = plainFogReady ? LodFogShader::getPlainOrNull : GameRenderer::getPositionColorShader;
        renderBucket(plainCheapFade, plainFadeType, plainFadeShader, modelViewMatrix, projectionMatrix);
        RenderType texturedOpaqueType = texturedFogReady
            ? (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_FOG_TEXTURED_OPAQUE_NOCULL)
            : (backfaceCullingEnabled ? LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE : LodTerrainRenderType.TERRAIN_TEXTURED_OPAQUE_NOCULL);
        Supplier<ShaderInstance> texturedOpaqueShader = texturedFogReady ? LodFogShader::getTexturedOrNull : GameRenderer::getPositionTexColorShader;
        renderBucket(texturedCheapOpaque, texturedOpaqueType, texturedOpaqueShader, modelViewMatrix, projectionMatrix);
        RenderType texturedFadeType = texturedFogReady ? LodTerrainRenderType.TERRAIN_FOG_TEXTURED : LodTerrainRenderType.TERRAIN_TEXTURED;
        Supplier<ShaderInstance> texturedFadeShader = texturedFogReady ? LodFogShader::getTexturedOrNull : GameRenderer::getPositionTexColorShader;
        renderBucket(texturedCheapFade, texturedFadeType, texturedFadeShader, modelViewMatrix, projectionMatrix);
    }

    public static void renderWaterLit(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        List<VertexBuffer> litWater = new ArrayList<>();

        for (LodRegionMesh mesh : meshes) {
            if (mesh.waterLit && mesh.terrainBufferWater != null) {
                litWater.add(mesh.terrainBufferWater);
            }
        }

        renderBucket(litWater, LodTerrainRenderType.WATER_LIT_TEXTURED, GameRenderer::getRendertypeSolidShader, modelViewMatrix, projectionMatrix);
    }

    public static void renderWaterCheap(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        List<VertexBuffer> cheapWater = new ArrayList<>();

        for (LodRegionMesh mesh : meshes) {
            if (!mesh.waterLit && mesh.terrainBufferWater != null) {
                cheapWater.add(mesh.terrainBufferWater);
            }
        }

        if (!cheapWater.isEmpty()) {
            boolean waterShaderReady = LodWaterShader.getTexturedOrNull() != null;
            Supplier<ShaderInstance> shader = waterShaderReady ? LodWaterShader::getTexturedOrNull : GameRenderer::getPositionTexColorShader;
            renderBucket(cheapWater, LodTerrainRenderType.WATER_TEXTURED, shader, modelViewMatrix, projectionMatrix);
        }
    }

    public static void renderTrees(List<LodRegionMesh> meshes, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        Map<RenderType, List<VertexBuffer>> treeBuckets = new LinkedHashMap<>();

        for (LodRegionMesh mesh : meshes) {
            for (LodRegionMesh.TreeMeshPart part : mesh.treeParts) {
                treeBuckets.computeIfAbsent(part.renderType(), key -> new ArrayList<>()).add(part.buffer());
            }
        }

        if (!treeBuckets.isEmpty()) {
            boolean treeShaderReady = LodTreeShader.getOrNull() != null;
            Supplier<ShaderInstance> shader = treeShaderReady ? LodTreeShader::getOrNull : GameRenderer::getPositionTexColorShader;

            for (Entry<RenderType, List<VertexBuffer>> entry : treeBuckets.entrySet()) {
                renderBucket(entry.getValue(), entry.getKey(), shader, modelViewMatrix, projectionMatrix);
            }
        }
    }

    private static void renderBucket(
        List<VertexBuffer> buffers, RenderType renderType, Supplier<ShaderInstance> shader, Matrix4f modelViewMatrix, Matrix4f projectionMatrix
    ) {
        if (!buffers.isEmpty()) {
            renderType.setupRenderState();
            RenderSystem.setShader(shader);

            for (VertexBuffer buffer : buffers) {
                buffer.bind();
                buffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                VertexBuffer.unbind();
            }

            renderType.clearRenderState();
        }
    }

    @Override
    public void close() {
        if (this.terrainBufferOpaque != null) {
            this.terrainBufferOpaque.close();
        }

        if (this.terrainBufferFade != null) {
            this.terrainBufferFade.close();
        }

        if (this.terrainBufferWater != null) {
            this.terrainBufferWater.close();
        }

        for (LodRegionMesh.TreeMeshPart part : this.treeParts) {
            part.buffer().close();
        }
    }

    private record EdgeColumn(int height, float ySW, float ySE, float yNE, float yNW, float skirtBottom, int tintColor, SurfaceMaterial.Sprite sideSprite) {
    }

    public record FadeParams(int spawnBlockX, int spawnBlockZ, int playerY, float fadeStartBlocks, float fadeEndBlocks) {
        int alphaAt(float worldX, float worldY, float worldZ) {
            float dx = worldX - this.spawnBlockX;
            float dy = worldY - this.playerY;
            float dz = worldZ - this.spawnBlockZ;
            float distanceBlocks = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            float t = LodRegionMesh.smoothstep(this.fadeStartBlocks, this.fadeEndBlocks, distanceBlocks);
            return Math.round(t * 255.0F);
        }

        boolean visibleAt(float worldX, float worldY, float worldZ) {
            float dx = worldX - this.spawnBlockX;
            float dy = worldY - this.playerY;
            float dz = worldZ - this.spawnBlockZ;
            float distanceBlocks = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            return distanceBlocks >= this.fadeEndBlocks;
        }
    }

    record RecordedPart(RecordedVertexSink sink, RecordedVertexSink.Kind kind, VertexFormat format) {
    }

    record RecordedRegionMesh(
        LodRegionMesh.RecordedPart terrainOpaque,
        LodRegionMesh.RecordedPart terrainFade,
        boolean terrainTextured,
        boolean terrainLit,
        LodRegionMesh.RecordedPart water,
        boolean waterLit,
        List<LodRegionMesh.RecordedTreePart> treeParts
    ) {
    }

    private record RecordedTreePart(RenderType renderType, LodRegionMesh.RecordedPart part) {
    }

    private record TreeMeshPart(RenderType renderType, VertexBuffer buffer) {
    }

    private enum TreePart {
        TRUNK,
        FOLIAGE;
    }

    private record TreePlacement(int blockX, int blockZ, int groundY, TreeStyle style, boolean boxGeometry) {
    }
}
