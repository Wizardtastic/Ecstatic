/**
 * Uses Minecraft's density function used in chunk generation in a vertical march + a binary refine to find the surface height
 * highly version specific, density function literally doesn't exist pre 1.18 
 * returns a SurfaceSample object, contains a height, BiomeRawID, a color, and a contains-trees bool
 *
 * Could potentially be made faster by basing the march off of neighbor hint, low priority
 */

package com.angryalchemist.ecstatic.sample;

import com.angryalchemist.ecstatic.Constants;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


public final class SurfaceSampler { 
    private static final int MARCH_STEP = 8; 
    public static final int NO_HEIGHT_HINT = Integer.MIN_VALUE; // no neighboring point to check against for floating points
    private static final int FLOATING_OUTLIER_THRESHOLD_BLOCKS = 24; // maximum height diff between neighboring points before being treated as a floating block/blob
    private static final int GRASS_COLOR_BLEND_RADIUS = 2; // radius to blend terrain color
    private static final Map<Biome, Boolean> hasTreesCache = new ConcurrentHashMap<>(); //caches biome tree data. computed once per biome instance. 
    private static final Set<ResourceLocation> BADLANDS_BIOMES = Set.of(
        Biomes.BADLANDS.location(), Biomes.ERODED_BADLANDS.location(), Biomes.WOODED_BADLANDS.location()
    ); // badlands biomes, unfortunately must be hardcoded
    private static final Method SURFACE_SYSTEM_GET_BAND = resolveGetBandMethod(); // vanilla's terracotta band gen

    private SurfaceSampler() {
    }

    private static Method resolveGetBandMethod() {
        try {
            Method method = SurfaceSystem.class.getDeclaredMethod("getBand", int.class, int.class, int.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            Constants.LOG
                .warn("Ecstatic: could not resolve SurfaceSystem#getBand via reflection - badlands terrain will fall back to plain sampled grass color", e);
            return null;
        }
    }

    public static SurfaceSample sample(ServerLevel level, int blockX, int blockZ) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        return sample(generator, randomState, biomeRegistry, level, blockX, blockZ, Integer.MIN_VALUE);
    }

    public static SurfaceSample sample(
        ChunkGenerator generator, RandomState randomState, Registry<Biome> biomeRegistry, LevelHeightAccessor heightAccessor, int blockX, int blockZ
    ) {
        return sample(generator, randomState, biomeRegistry, heightAccessor, blockX, blockZ, Integer.MIN_VALUE);
    }

    public static SurfaceSample sample(
        ChunkGenerator generator,
        RandomState randomState,
        Registry<Biome> biomeRegistry,
        LevelHeightAccessor heightAccessor,
        int blockX,
        int blockZ,
        int expectedHeightHint
    ) {
        DensityFunction finalDensity = randomState.router().finalDensity();
        int height = findSurfaceHeight(finalDensity, heightAccessor, blockX, blockZ, expectedHeightHint);
        BiomeSource biomeSource = generator.getBiomeSource();
        Holder<Biome> biomeHolder = biomeSource.getNoiseBiome(
            QuartPos.fromBlock(blockX), QuartPos.fromBlock(height), QuartPos.fromBlock(blockZ), randomState.sampler()
        );
        Biome biome = (Biome)biomeHolder.value();
        int biomeRawId = biomeRegistry.getId(biome);
        ResourceLocation biomeKey = biomeRegistry.getKey(biome);
        int colorRgb = biomeKey != null && BADLANDS_BIOMES.contains(biomeKey)
            ? badlandsColor(randomState, biome, blockX, height, blockZ)
            : averagedGrassColor(biome, blockX, blockZ);
        boolean hasTrees = hasTreesCache.computeIfAbsent(biome, SurfaceSampler::biomeHasTrees);
        return new SurfaceSample(height, biomeRawId, colorRgb, hasTrees);
    }

    private static boolean biomeHasTrees(Biome biome) {
        List<HolderSet<PlacedFeature>> steps = biome.getGenerationSettings().features();
        int stepIndex = Decoration.VEGETAL_DECORATION.ordinal();
        if (stepIndex >= steps.size()) {
            return false;
        }

        for (Holder<PlacedFeature> placedHolder : steps.get(stepIndex)) {
            boolean isTree = ((PlacedFeature)placedHolder.value()).getFeatures().anyMatch(configured -> configured.feature() == Feature.TREE);
            if (isTree) {
                return true;
            }
        }

        return false;
    }

    private static int badlandsColor(RandomState randomState, Biome biome, int blockX, int height, int blockZ) {
        BlockState band = badlandsBand(randomState, blockX, height, blockZ);
        return band != null ? terracottaColor(band) : averagedGrassColor(biome, blockX, blockZ);
    }

    private static BlockState badlandsBand(RandomState randomState, int blockX, int blockY, int blockZ) {
        if (SURFACE_SYSTEM_GET_BAND == null) {
            return null;
        }

        try {
            return (BlockState)SURFACE_SYSTEM_GET_BAND.invoke(randomState.surfaceSystem(), blockX, blockY, blockZ);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static int terracottaColor(BlockState band) {
        if (band.is(Blocks.ORANGE_TERRACOTTA)) {
            return 10703913;
        } else if (band.is(Blocks.YELLOW_TERRACOTTA)) {
            return 12099634;
        } else if (band.is(Blocks.BROWN_TERRACOTTA)) {
            return 5059364;
        } else if (band.is(Blocks.RED_TERRACOTTA)) {
            return 9387310;
        } else if (band.is(Blocks.WHITE_TERRACOTTA)) {
            return 13743267;
        } else {
            return band.is(Blocks.LIGHT_GRAY_TERRACOTTA) ? 10257528 : 10117462;
        }
    }

    private static int averagedGrassColor(Biome biome, int blockX, int blockZ) {
        int rSum = 0;
        int gSum = 0;
        int bSum = 0;
        int count = 0;

        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                int sampleColor = biome.getGrassColor(blockX + dx, blockZ + dz);
                rSum += sampleColor >> 16 & 0xFF;
                gSum += sampleColor >> 8 & 0xFF;
                bSum += sampleColor & 0xFF;
                count++;
            }
        }

        return rSum / count << 16 | gSum / count << 8 | bSum / count;
    }

    private static int findSurfaceHeight(DensityFunction finalDensity, LevelHeightAccessor heightAccessor, int blockX, int blockZ, int expectedHeightHint) {
        int top = heightAccessor.getMaxBuildHeight() - 1;
        int bottom = heightAccessor.getMinBuildHeight();
        int prevY = top;
        int y = top - 8;
        int fallback = bottom;

        while (y >= bottom) {
            if (isSolid(finalDensity, blockX, y, blockZ)) {
                int candidate = binaryRefine(finalDensity, blockX, blockZ, prevY, y) + 1;
                if (expectedHeightHint == Integer.MIN_VALUE || Math.abs(candidate - expectedHeightHint) <= 24) {
                    return candidate;
                }

                fallback = candidate;
                prevY = candidate - 1;
                y = prevY - 8;
            } else {
                prevY = y;
                y -= 8;
            }
        }

        return fallback;
    }

    private static int binaryRefine(DensityFunction finalDensity, int blockX, int blockZ, int highNonSolidY, int lowSolidY) {
        while (highNonSolidY - lowSolidY > 1) {
            int mid = (highNonSolidY + lowSolidY) / 2;
            if (isSolid(finalDensity, blockX, mid, blockZ)) {
                lowSolidY = mid;
            } else {
                highNonSolidY = mid;
            }
        }

        return lowSolidY;
    }

    private static boolean isSolid(DensityFunction finalDensity, int blockX, int blockY, int blockZ) {
        return finalDensity.compute(new SinglePointContext(blockX, blockY, blockZ)) > 0.0;
    }
}
