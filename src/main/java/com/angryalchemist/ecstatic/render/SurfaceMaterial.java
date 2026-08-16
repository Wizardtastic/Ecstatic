package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.storage.HeightmapColumn;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class SurfaceMaterial {
    private static final float DIRT_SLOPE_START = 0.6F;
    private static final float STONE_SLOPE_START = 1.25F;
    private static final Set<ResourceLocation> SAND_BIOMES = Set.of(
        Biomes.DESERT.location(),
        Biomes.BEACH.location(),
        Biomes.SNOWY_BEACH.location(),
        Biomes.WARM_OCEAN.location(),
        Biomes.LUKEWARM_OCEAN.location(),
        Biomes.DEEP_LUKEWARM_OCEAN.location()
    );
    private static final Set<ResourceLocation> BADLANDS_BIOMES = Set.of(
        Biomes.BADLANDS.location(), Biomes.ERODED_BADLANDS.location(), Biomes.WOODED_BADLANDS.location()
    );
    static final SurfaceMaterial GRASS = new SurfaceMaterial(
        SurfaceMaterial.Kind.GRASS, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState()
    );
    static final SurfaceMaterial DIRT = new SurfaceMaterial(SurfaceMaterial.Kind.DIRT, Blocks.DIRT.defaultBlockState(), Blocks.DIRT.defaultBlockState());
    static final SurfaceMaterial STONE = new SurfaceMaterial(SurfaceMaterial.Kind.STONE, Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState());
    static final SurfaceMaterial SAND = new SurfaceMaterial(SurfaceMaterial.Kind.SAND, Blocks.SAND.defaultBlockState(), Blocks.SAND.defaultBlockState());
    static final SurfaceMaterial SNOW = new SurfaceMaterial(
        SurfaceMaterial.Kind.SNOW, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.SNOW_BLOCK.defaultBlockState()
    );
    static final SurfaceMaterial RED_SAND = new SurfaceMaterial(
        SurfaceMaterial.Kind.RED_SAND, Blocks.RED_SAND.defaultBlockState(), Blocks.RED_SAND.defaultBlockState()
    );
    static final SurfaceMaterial TERRACOTTA = new SurfaceMaterial(
        SurfaceMaterial.Kind.TERRACOTTA, Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState()
    );
    private final SurfaceMaterial.Kind kind;
    private final BlockState topState;
    private final BlockState sideState;
    private SurfaceMaterial.Sprite topSprite;
    private SurfaceMaterial.Sprite sideSprite;

    private SurfaceMaterial(SurfaceMaterial.Kind kind, BlockState topState, BlockState sideState) {
        this.kind = kind;
        this.topState = topState;
        this.sideState = sideState;
    }

    SurfaceMaterial.Kind kind() {
        return this.kind;
    }

    SurfaceMaterial.Sprite topSprite() {
        if (this.topSprite == null) {
            this.topSprite = resolveSprite(this.topState, Direction.UP);
        }

        return this.topSprite;
    }

    SurfaceMaterial.Sprite sideSprite() {
        if (this.sideSprite == null) {
            this.sideSprite = resolveSprite(this.sideState, Direction.NORTH);
        }

        return this.sideSprite;
    }

    static SurfaceMaterial.Sprite resolveSprite(BlockState state, Direction face) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create());
        if (quads.isEmpty()) {
            quads = model.getQuads(state, null, RandomSource.create());
        }

        if (quads.isEmpty()) {
            TextureAtlasSprite particle = model.getParticleIcon();
            return new SurfaceMaterial.Sprite(particle.getU0(), particle.getU1(), particle.getV0(), particle.getV1(), false);
        } else {
            BakedQuad quad = quads.get(0);
            TextureAtlasSprite sprite = quad.getSprite();
            return new SurfaceMaterial.Sprite(sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), quad.getTintIndex() >= 0);
        }
    }

    static SurfaceMaterial classify(HeightmapColumn column, Registry<Biome> biomeRegistry, float slope, int blockX, int blockZ) {
        Biome biome = biomeOf(biomeRegistry, column.biomeRawId());
        ResourceLocation biomeKey = biome != null ? biomeRegistry.getKey(biome) : null;
        boolean tooSteepForSnow = slope >= 1.25F;
        boolean coldEnoughToSnow = biome != null && biome.coldEnoughToSnow(new BlockPos(blockX, column.height(), blockZ));
        if (!tooSteepForSnow && coldEnoughToSnow) {
            return SNOW;
        } else if (biomeKey != null && BADLANDS_BIOMES.contains(biomeKey)) {
            return slope >= 0.6F ? TERRACOTTA : RED_SAND;
        } else if (isSandBiome(biomeKey) && slope < 0.6F) {
            return SAND;
        } else if (slope >= 1.25F) {
            return STONE;
        } else {
            return slope >= 0.6F ? DIRT : GRASS;
        }
    }

    static boolean isSandBiome(ResourceLocation biomeKey) {
        return biomeKey != null && SAND_BIOMES.contains(biomeKey);
    }

    private static Biome biomeOf(Registry<Biome> biomeRegistry, int biomeRawId) {
        return biomeRegistry != null ? (Biome)biomeRegistry.byId(biomeRawId) : null;
    }

    enum Kind {
        GRASS,
        DIRT,
        STONE,
        SAND,
        SNOW,
        RED_SAND,
        TERRACOTTA;
    }

    record Sprite(float u0, float u1, float v0, float v1, boolean tinted) {
    }
}
