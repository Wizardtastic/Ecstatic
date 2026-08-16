package com.angryalchemist.ecstatic.debug;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.sample.SurfaceSample;
import com.angryalchemist.ecstatic.sample.SurfaceSampler;
import com.angryalchemist.ecstatic.storage.SavedChunkAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.LevelResource;

public final class LodDebugCompare {
    private LodDebugCompare() {
    }

    public static void compareAtPlayer(Minecraft client) {
        MinecraftServer server = client.getSingleplayerServer();
        if (server != null && client.level != null && client.player != null) {
            ServerLevel level = server.getLevel(client.level.dimension());
            if (level == null) {
                LodDebugCommon.sendMessage(client, "compare: no matching server level");
            } else {
                int blockX = client.player.getBlockX();
                int blockZ = client.player.getBlockZ();
                SurfaceSample sampled = SurfaceSampler.sample(level, blockX, blockZ);
                int realHeight = level.getHeight(Types.OCEAN_FLOOR, blockX, blockZ);
                Holder<Biome> realBiomeHolder = level.getBiome(new BlockPos(blockX, realHeight, blockZ));
                int realBiomeId = level.registryAccess().registryOrThrow(Registries.BIOME).getId((Biome)realBiomeHolder.value());
                String message = String.format(
                    "compare @ (%d, %d): sampled height=%d biome=%d | real height=%d biome=%d | delta=%d",
                    blockX,
                    blockZ,
                    sampled.height(),
                    sampled.biomeRawId(),
                    realHeight,
                    realBiomeId,
                    sampled.height() - realHeight
                );
                Constants.LOG.info("Ecstatic debug {}", message);
                LodDebugCommon.sendMessage(client, message);
                LodDebugCommon.sendMessage(client, savedChunkMessage(server, level, blockX, blockZ, realHeight));
            }
        } else {
            LodDebugCommon.sendMessage(client, "compare: not in a singleplayer world");
        }
    }

    private static String savedChunkMessage(MinecraftServer server, ServerLevel level, int blockX, int blockZ, int realHeight) {
        try (SavedChunkAccess savedChunks = new SavedChunkAccess(
                server.getWorldPath(LevelResource.ROOT), level.registryAccess().registryOrThrow(Registries.BIOME)
            )) {
            if (!savedChunks.hasChunk(blockX >> 4, blockZ >> 4)) {
                return "compare (saved): chunk not found on disk (unexpected - player is standing in it)";
            }

            int minY = level.getMinBuildHeight();
            int scanFrom = Math.min(realHeight + 4, level.getMaxBuildHeight() - 1);

            for (int y = scanFrom; y >= minY; y--) {
                BlockState state = savedChunks.blockAt(blockX, y, blockZ);
                if (!state.isAir()) {
                    return String.format("compare (saved) @ (%d, %d): real top block=%s at y=%d", blockX, blockZ, state.getBlock().getDescriptionId(), y);
                }
            }

            return String.format("compare (saved) @ (%d, %d): all air from y=%d down to %d", blockX, blockZ, scanFrom, minY);
        } catch (RuntimeException e) {
            Constants.LOG.warn("Ecstatic debug: saved-chunk read failed", e);
            return "compare (saved): read failed, see log";
        }
    }
}
