package com.angryalchemist.ecstatic.sample;

import com.angryalchemist.ecstatic.Constants;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeTags;
import net.minecraft.world.level.biome.BiomeSource;
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

/**
 * Uses Minecraft's density function used in chunk generation in a vertical march + a binary refine to find the surface height
 * highly version specific, density function literally doesn't exist pre 1.18, keep any divergence contained here
 * returns a SurfaceSample object, contains a height, BiomeRawID, a color, and a contains-trees bool
 *
 * Could potentially be made faster by basing the march off of neighbor hint, low priority
 */
public final class SurfaceSampler { 
    private static final int MARCH_STEP = 8; 
    public static final int NO_HEIGHT_HINT = Integer.MIN_VALUE; // no neighboring point to check against for floating points
    private static final int FLOATING_OUTLIER_THRESHOLD_BLOCKS = 24; // maximum height diff between neighboring points before being treated as a floating block/blob
    private static final int GRASS_COLOR_BLEND_RADIUS = 2; // radius to blend terrain color
    private static final Map<Biome, Boolean> hasTreesCache = new ConcurrentHashMap<>(); //caches biome tree data. computed once per biome instance. 
    private static final Method SURFACE_SYSTEM_GET_BAND = resolveGetBandMethod(); // vanilla's terracotta band gen

    private SurfaceSampler() {
    }
    /**
     * Minecraft's {@code SurfaceRules} is not public, so reflection is the only (Sorta) future proof way of 
     * calling the real thing opposed to rederiving the code myself, which is vulnerable to any future tweak
     * by the big Moj 
     * 
     * Resloved one time, then cached. IF it fails for some reason, it reverts back to the default grass color 
     * (not pretty but it's something) rather than just throwing.
     * 
     * I can't seem to get it to work for some reason
     */
    private static Method resolveGetBandMethod() {
        try {
            for (Method method : SurfaceSystem.class.getDeclaredMethods()) {
                if (method.getReturnType() == BlockState.class) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 3 && params[0] == int.class && params[1] == int.class && params[2] == int.class) {
                        method.trySetAccessible();
                        return method;
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.warn("Ecstatic: Exception while scanning SurfaceSystem methods", e);
        }
    
        Constants.LOG.warn("Ecstatic: Could not resolve SurfaceSystem#getBand via reflection; badlands terrain will fall back to plain sampled grass color");
        return null;
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
        int colorRgb = biomeHolder.is(BiomeTags.IS_BADLANDS)
            ? badlandsColor(randomState, biome, blockX, height, blockZ)
            : averagedGrassColor(biome, blockX, blockZ);
        boolean hasTrees = hasTreesCache.computeIfAbsent(biome, SurfaceSampler::biomeHasTrees);
        return new SurfaceSample(height, biomeRawId, colorRgb, hasTrees);
    }
    
    /**
     * Checks if a biome places a {@link Feature#TREE} in the vegetation step.
     * Works for modded biomes too since it doesn't pull from a hardcoded list.
     */
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

        for (int dz = -GRASS_COLOR_BLEND_RADIUS; dz <= GRASS_COLOR_BLEND_RADIUS; dz++) {
            for (int dx = -GRASS_COLOR_BLEND_RADIUS; dx <= GRASS_COLOR_BLEND_RADIUS; dx++) {
                int sampleColor = biome.getGrassColor(blockX + dx, blockZ + dz);
                rSum += (sampleColor >> 16) & 0xFF;
                gSum += (sampleColor >> 8) & 0xFF;
                bSum += sampleColor & 0xFF;
                count++;
            }
        }
        
        int rAvg = rSum / count;
        int gAvg = gSum / count;
        int bAvg = bSum / count;

        return (rAvg << 16) | (gAvg << 8) | bAvg;
    }

    private static int findSurfaceHeight(DensityFunction finalDensity, LevelHeightAccessor heightAccessor, int blockX, int blockZ, int expectedHeightHint) {
        int top = heightAccessor.getMaxBuildHeight() - 1;
        int bottom = heightAccessor.getMinBuildHeight();
        int prevY = top;
        int y = top - MARCH_STEP;
        int fallback = bottom;

        while (y >= bottom) {
            if (isSolid(finalDensity, blockX, y, blockZ)) {
                int candidate = binaryRefine(finalDensity, blockX, blockZ, prevY, y) + 1;
                if (expectedHeightHint == NO_HEIGHT_HINT || Math.abs(candidate - expectedHeightHint) <= FLOATING_OUTLIER_THRESHOLD_BLOCKS) {
                    return candidate;
                }

                fallback = candidate;
                prevY = candidate - 1;
                y = prevY - MARCH_STEP;
            } else {
                prevY = y;
                y -= MARCH_STEP;
            }
        }

        return fallback;
    }

    private static int binaryRefine(DensityFunction finalDensity, int blockX, int blockZ, int highNonSolidY, int lowSolidY) {
        while (highNonSolidY - lowSolidY > 1) {
            int mid = (highNonSolidY + lowSolidY) >>> 1;
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
