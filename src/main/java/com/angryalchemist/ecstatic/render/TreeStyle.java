package com.angryalchemist.ecstatic.render;

import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class TreeStyle {
    final TreeStyle.Group group;
    final int trunkColor;
    final int foliageColor;
    final float trunkHeight;
    final float canopyHeight;
    final float canopyRadius;
    final TreeStyle.CanopyShape shape;
    private final BlockState trunkBlockState;
    private final BlockState foliageBlockState;
    private SurfaceMaterial.Sprite trunkSprite;
    private SurfaceMaterial.Sprite foliageSprite;
    private static final TreeStyle DEFAULT = new TreeStyle(
        TreeStyle.Group.DEFAULT,
        4861719,
        6985529,
        6.0F,
        4.0F,
        2.5F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.OAK_LOG.defaultBlockState(),
        Blocks.OAK_LEAVES.defaultBlockState()
    );
    private static final TreeStyle CONIFER = new TreeStyle(
        TreeStyle.Group.CONIFER,
        3877404,
        6127969,
        6.0F,
        6.5F,
        2.9F,
        TreeStyle.CanopyShape.CONICAL,
        Blocks.SPRUCE_LOG.defaultBlockState(),
        Blocks.SPRUCE_LEAVES.defaultBlockState()
    );
    private static final TreeStyle BIRCH = new TreeStyle(
        TreeStyle.Group.BIRCH,
        11778739,
        9219676,
        5.5F,
        4.0F,
        3.1F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.BIRCH_LOG.defaultBlockState(),
        Blocks.BIRCH_LEAVES.defaultBlockState()
    );
    private static final TreeStyle JUNGLE = new TreeStyle(
        TreeStyle.Group.JUNGLE,
        4862745,
        5220410,
        8.0F,
        6.0F,
        4.4F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.JUNGLE_LOG.defaultBlockState(),
        Blocks.JUNGLE_LEAVES.defaultBlockState()
    );
    private static final TreeStyle SAVANNA = new TreeStyle(
        TreeStyle.Group.SAVANNA,
        6048302,
        10391114,
        6.5F,
        2.2F,
        4.7F,
        TreeStyle.CanopyShape.FLAT_TOP,
        Blocks.ACACIA_LOG.defaultBlockState(),
        Blocks.ACACIA_LEAVES.defaultBlockState()
    );
    private static final TreeStyle DARK_FOREST = new TreeStyle(
        TreeStyle.Group.DARK_FOREST,
        3811352,
        5208634,
        6.0F,
        5.5F,
        4.7F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.DARK_OAK_LOG.defaultBlockState(),
        Blocks.DARK_OAK_LEAVES.defaultBlockState()
    );
    private static final TreeStyle SWAMP = new TreeStyle(
        TreeStyle.Group.SWAMP,
        4865322,
        6057538,
        5.0F,
        4.0F,
        3.8F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.OAK_LOG.defaultBlockState(),
        Blocks.OAK_LEAVES.defaultBlockState()
    );
    private static final TreeStyle CHERRY = new TreeStyle(
        TreeStyle.Group.CHERRY,
        6046776,
        15180996,
        4.5F,
        4.0F,
        3.5F,
        TreeStyle.CanopyShape.ROUND,
        Blocks.CHERRY_LOG.defaultBlockState(),
        Blocks.CHERRY_LEAVES.defaultBlockState()
    );
    private static final TreeStyle BEACH = new TreeStyle(
        TreeStyle.Group.BEACH,
        9073493,
        12757352,
        3.5F,
        3.0F,
        2.6F,
        TreeStyle.CanopyShape.FLAT_TOP,
        Blocks.OAK_LOG.defaultBlockState(),
        Blocks.OAK_LEAVES.defaultBlockState()
    );
    private static final Set<ResourceLocation> CONIFER_BIOMES = Set.of(
        Biomes.TAIGA.location(),
        Biomes.OLD_GROWTH_PINE_TAIGA.location(),
        Biomes.OLD_GROWTH_SPRUCE_TAIGA.location(),
        Biomes.SNOWY_TAIGA.location(),
        Biomes.GROVE.location()
    );
    private static final Set<ResourceLocation> BIRCH_BIOMES = Set.of(Biomes.BIRCH_FOREST.location(), Biomes.OLD_GROWTH_BIRCH_FOREST.location());
    private static final Set<ResourceLocation> JUNGLE_BIOMES = Set.of(
        Biomes.JUNGLE.location(), Biomes.BAMBOO_JUNGLE.location(), Biomes.SPARSE_JUNGLE.location()
    );
    private static final Set<ResourceLocation> SAVANNA_BIOMES = Set.of(
        Biomes.SAVANNA.location(), Biomes.SAVANNA_PLATEAU.location(), Biomes.WINDSWEPT_SAVANNA.location()
    );
    private static final Set<ResourceLocation> DARK_FOREST_BIOMES = Set.of(Biomes.DARK_FOREST.location());
    private static final Set<ResourceLocation> SWAMP_BIOMES = Set.of(Biomes.SWAMP.location(), Biomes.MANGROVE_SWAMP.location());
    private static final Set<ResourceLocation> CHERRY_BIOMES = Set.of(Biomes.CHERRY_GROVE.location());
    private static final Set<ResourceLocation> BEACH_BIOMES = Set.of(Biomes.BEACH.location(), Biomes.SNOWY_BEACH.location());

    private TreeStyle(
        TreeStyle.Group group,
        int trunkColor,
        int foliageColor,
        float trunkHeight,
        float canopyHeight,
        float canopyRadius,
        TreeStyle.CanopyShape shape,
        BlockState trunkBlockState,
        BlockState foliageBlockState
    ) {
        this.group = group;
        this.trunkColor = trunkColor;
        this.foliageColor = foliageColor;
        this.trunkHeight = trunkHeight;
        this.canopyHeight = canopyHeight;
        this.canopyRadius = canopyRadius;
        this.shape = shape;
        this.trunkBlockState = trunkBlockState;
        this.foliageBlockState = foliageBlockState;
    }

    SurfaceMaterial.Sprite trunkSprite() {
        if (this.trunkSprite == null) {
            this.trunkSprite = SurfaceMaterial.resolveSprite(this.trunkBlockState, Direction.NORTH);
        }

        return this.trunkSprite;
    }

    SurfaceMaterial.Sprite foliageSprite() {
        if (this.foliageSprite == null) {
            this.foliageSprite = SurfaceMaterial.resolveSprite(this.foliageBlockState, Direction.NORTH);
        }

        return this.foliageSprite;
    }

    static TreeStyle forBiome(Registry<Biome> biomeRegistry, int biomeRawId) {
        if (biomeRegistry == null) {
            return DEFAULT;
        } else {
            Biome biome = (Biome)biomeRegistry.byId(biomeRawId);
            if (biome == null) {
                return DEFAULT;
            } else {
                ResourceLocation key = biomeRegistry.getKey(biome);
                if (key == null) {
                    return DEFAULT;
                } else if (CONIFER_BIOMES.contains(key)) {
                    return CONIFER;
                } else if (BIRCH_BIOMES.contains(key)) {
                    return BIRCH;
                } else if (JUNGLE_BIOMES.contains(key)) {
                    return JUNGLE;
                } else if (SAVANNA_BIOMES.contains(key)) {
                    return SAVANNA;
                } else if (DARK_FOREST_BIOMES.contains(key)) {
                    return DARK_FOREST;
                } else if (SWAMP_BIOMES.contains(key)) {
                    return SWAMP;
                } else if (CHERRY_BIOMES.contains(key)) {
                    return CHERRY;
                } else {
                    return BEACH_BIOMES.contains(key) ? BEACH : DEFAULT;
                }
            }
        }
    }

    static TreeStyle forGroup(TreeStyle.Group group) {
        return switch (group) {
            case CONIFER -> CONIFER;
            case BIRCH -> BIRCH;
            case JUNGLE -> JUNGLE;
            case SAVANNA -> SAVANNA;
            case DARK_FOREST -> DARK_FOREST;
            case SWAMP -> SWAMP;
            case CHERRY -> CHERRY;
            case BEACH -> BEACH;
            case DEFAULT -> DEFAULT;
        };
    }

    enum CanopyShape {
        ROUND,
        CONICAL,
        FLAT_TOP;
    }

    enum Group {
        DEFAULT,
        CONIFER,
        BIRCH,
        JUNGLE,
        SAVANNA,
        DARK_FOREST,
        SWAMP,
        CHERRY,
        BEACH;
    }
}
