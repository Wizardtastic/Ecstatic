package com.angryalchemist.ecstatic.render;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

final class TreeDensityEstimator {
    private static final float BLOCKS_PER_CHUNK_AREA = 256.0F;
    private static final float MIN_DENSITY_PER_BLOCK_AREA = 6.6666666E-4F;
    private static final float MAX_DENSITY_PER_BLOCK_AREA = 0.06666667F;
    private static final Field COUNT_PLACEMENT_COUNT_FIELD = resolveField(CountPlacement.class, "count");
    private static final Field RARITY_FILTER_CHANCE_FIELD = resolveField(RarityFilter.class, "chance");
    private static final Map<Biome, Float> densityCache = new ConcurrentHashMap<>();

    private TreeDensityEstimator() {
    }

    static float densityPerBlockArea(Registry<Biome> biomeRegistry, int biomeRawId) {
        if (biomeRegistry == null) {
            return 0.006666667F;
        }

        Biome biome = (Biome)biomeRegistry.byId(biomeRawId);
        return biome == null ? 0.006666667F : densityCache.computeIfAbsent(biome, TreeDensityEstimator::estimate);
    }

    private static float estimate(Biome biome) {
        List<HolderSet<PlacedFeature>> steps = biome.getGenerationSettings().features();
        int stepIndex = Decoration.VEGETAL_DECORATION.ordinal();
        if (stepIndex >= steps.size()) {
            return 0.006666667F;
        }

        float expectedTreesPerChunk = 0.0F;
        boolean anyTree = false;

        for (Holder<PlacedFeature> placedHolder : steps.get(stepIndex)) {
            PlacedFeature placedFeature = (PlacedFeature)placedHolder.value();
            boolean isTree = placedFeature.getFeatures().anyMatch(configured -> configured.feature() == Feature.TREE);
            if (isTree) {
                anyTree = true;
                expectedTreesPerChunk += expectedCount(placedFeature);
            }
        }

        if (anyTree && !(expectedTreesPerChunk <= 0.0F)) {
            float densityPerBlockArea = expectedTreesPerChunk / 256.0F;
            return Mth.clamp(densityPerBlockArea, 6.6666666E-4F, 0.06666667F);
        } else {
            return 0.006666667F;
        }
    }

    private static float expectedCount(PlacedFeature placedFeature) {
        float expected = 1.0F;

        for (PlacementModifier modifier : placedFeature.placement()) {
            if (COUNT_PLACEMENT_COUNT_FIELD != null && modifier instanceof CountPlacement) {
                try {
                    IntProvider provider = (IntProvider)COUNT_PLACEMENT_COUNT_FIELD.get(modifier);
                    expected *= (provider.getMinValue() + provider.getMaxValue()) / 2.0F;
                } catch (ReflectiveOperationException | ClassCastException var5) {
                }
            } else if (RARITY_FILTER_CHANCE_FIELD != null && modifier instanceof RarityFilter) {
                try {
                    int chance = (Integer)RARITY_FILTER_CHANCE_FIELD.get(modifier);
                    if (chance > 0) {
                        expected /= chance;
                    }
                } catch (ReflectiveOperationException | ClassCastException var6) {
                }
            }
        }

        return expected;
    }

    private static Field resolveField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | SecurityException e) {
            return null;
        }
    }
}
