package com.angryalchemist.ecstatic;

import com.angryalchemist.ecstatic.lod.RegionLodCoordinator;
import com.angryalchemist.ecstatic.lod.RingConfig;
import com.angryalchemist.ecstatic.platform.Services;
import com.angryalchemist.ecstatic.render.LodSettingsConfig;
import com.angryalchemist.ecstatic.sample.FixedHeightAccessor;
import com.angryalchemist.ecstatic.storage.LodStoragePaths;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelResource;

public class Ecstatic {
    private static final int LOD1_RING_WIDTH_CHUNKS = 28;
    private static final int LOD2_RING_WIDTH_CHUNKS = 32;
    private static final int LOD3_RING_WIDTH_CHUNKS = 64;
    private static final int LOD4_RING_WIDTH_CHUNKS = 12;
    private static final int LOD5_RING_WIDTH_CHUNKS = 24;
    private static final int RING_HYSTERESIS_CHUNKS = 4;
    private static volatile MinecraftServer startingServer;
    private static volatile RegionLodCoordinator activeCoordinator;

    public static RingConfig currentRingConfig(int clientRenderDistanceChunks) {
        int ring1StartChunks = RingConfig.ring1StartChunks(clientRenderDistanceChunks);
        return RingConfig.scaled(ring1StartChunks, 28, 32, 64, 12, 24, 4, LodSettingsConfig.get().lodRenderDistanceScale());
    }

    public static RegionLodCoordinator activeCoordinator() {
        return activeCoordinator;
    }

    public static void init() {
        Constants.LOG.info("Ecstatic initializing on {} ({})", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static synchronized void onServerStarted(ServerLevel overworld) {
        MinecraftServer server = overworld.getServer();
        if (startingServer == server) {
            Constants.LOG.warn("Ecstatic dynamic coordinator already starting for this server, skipping duplicate run");
        } else {
            startingServer = server;
            Path storageDir = LodStoragePaths.dimensionStorageDir(overworld.getServer().getWorldPath(LevelResource.ROOT), overworld.dimension());
            int workerThreadCount = LodSettingsConfig.get().workerThreadCount();
            ChunkGenerator generator = overworld.getChunkSource().getGenerator();
            RandomState randomState = overworld.getChunkSource().randomState();
            Registry<Biome> biomeRegistry = overworld.registryAccess().registryOrThrow(Registries.BIOME);
            LevelHeightAccessor heightAccessor = new FixedHeightAccessor(overworld.getMinBuildHeight(), overworld.getMaxBuildHeight());
            Thread startupThread = new Thread(
                () -> {
                    try {
                        activeCoordinator = new RegionLodCoordinator(
                            generator, randomState, biomeRegistry, heightAccessor, storageDir, 28, 32, 64, 12, 24, 4, workerThreadCount
                        );
                    } catch (IOException e) {
                        Constants.LOG.error("Ecstatic failed to start the dynamic region coordinator", e);
                    } finally {
                        startingServer = null;
                    }
                },
                "Ecstatic-CoordinatorStartup"
            );
            startupThread.setDaemon(true);
            startupThread.start();
        }
    }

    public static synchronized void onServerStopping(MinecraftServer server) {
        RegionLodCoordinator coordinator = activeCoordinator;
        if (coordinator != null) {
            activeCoordinator = null;
            coordinator.shutdown();
        }
    }
}
