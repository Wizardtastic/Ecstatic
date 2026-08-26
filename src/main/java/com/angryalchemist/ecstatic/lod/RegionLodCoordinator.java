package com.angryalchemist.ecstatic.lod;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.render.LodSettingsConfig;
import com.angryalchemist.ecstatic.sample.SurfaceSample;
import com.angryalchemist.ecstatic.sample.SurfaceSampler;
import com.angryalchemist.ecstatic.storage.LodRegionFile;
import com.angryalchemist.ecstatic.storage.LodStoragePaths;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

public final class RegionLodCoordinator {
    public static final int BOOTSTRAP_LEVEL = 0;
    private static final int BOOTSTRAP_SAMPLE_SPACING_BLOCKS = LodLevel.sampleSpacingBlocks(4);
    private static final int BOOTSTRAP_PRIORITY = -1;
    private static final double RESCAN_THRESHOLD_CHUNKS = 4.0;
    private static final long PAUSED_RESCAN_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(500L);
    private static final long WORKER_IDLE_POLL_MILLIS = 250L;
    private final ChunkGenerator generator;
    private final RandomState randomState;
    private final Registry<Biome> biomeRegistry;
    private final LevelHeightAccessor heightAccessor;
    private final int lod1Width;
    private final int lod2Width;
    private final int lod3Width;
    private final int lod4Width;
    private final int lod5Width;
    private final int hysteresisChunks;
    private final Map<Integer, LodRegionFile> filesByLevel = new HashMap<>();
    private final Map<RegionCoord, Integer> currentLevelByRegion = new HashMap<>();
    private final Set<RegionCoord> inFlight = ConcurrentHashMap.newKeySet();
    private final ConcurrentLinkedQueue<RegionLodCoordinator.RegionReadyResult> ready = new ConcurrentLinkedQueue<>();
    private LodRegionFile bootstrapFile;
    private final Set<RegionCoord> bootstrapInFlight = ConcurrentHashMap.newKeySet();
    private final ConcurrentLinkedQueue<RegionCoord> bootstrapReady = new ConcurrentLinkedQueue<>();
    private final PriorityBlockingQueue<RegionLodCoordinator.SampleTask> pendingTasks = new PriorityBlockingQueue<>();
    private final List<RegionLodCoordinator.Worker> workers = new CopyOnWriteArrayList<>();
    private int nextWorkerIndex;
    private volatile boolean shuttingDown;
    private boolean hasLastScanPos;
    private double lastScanChunkX;
    private double lastScanChunkZ;
    private long lastPausedRescanNanos;

    public RegionLodCoordinator(
        ChunkGenerator generator,
        RandomState randomState,
        Registry<Biome> biomeRegistry,
        LevelHeightAccessor heightAccessor,
        Path storageDir,
        int lod1Width,
        int lod2Width,
        int lod3Width,
        int lod4Width,
        int lod5Width,
        int hysteresisChunks,
        int workerThreadCount
        
    ) throws IOException {
        this.generator = generator;
        this.randomState = randomState;
        this.biomeRegistry = biomeRegistry;
        this.heightAccessor = heightAccessor;
        this.lod1Width = lod1Width;
        this.lod2Width = lod2Width;
        this.lod3Width = lod3Width;
        this.lod4Width = lod4Width;
        this.lod5Width = lod5Width;
        this.hysteresisChunks = hysteresisChunks;

        for (int level = 1; level <= 5; level++) {
            Path file = LodStoragePaths.regionFile(storageDir, level);
            this.filesByLevel.put(level, LodRegionFile.open(file, level, LodLevel.sampleSpacingBlocks(level)));
        }

        Path bootstrapPath = LodStoragePaths.regionFile(storageDir, 0);
        this.bootstrapFile = LodRegionFile.open(bootstrapPath, 0, BOOTSTRAP_SAMPLE_SPACING_BLOCKS);
        int threadCount = Math.max(1, workerThreadCount);

        for (int i = 0; i < threadCount; i++) {
            this.workers.add(this.spawnWorker());
        }
    }

    private void runWorkerLoop(RegionLodCoordinator.Worker self) {
        while (!this.shuttingDown && !self.retire) {
            RegionLodCoordinator.SampleTask task;
            try {
                task = this.pendingTasks.poll(WORKER_IDLE_POLL_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (task != null) {
            
                if (!task.isValid().getAsBoolean()) {
               
                    continue;
                }

                try {
                    task.action().run();
                } catch (Throwable t) {
                    Constants.LOG.error("Ecstatic region worker task failed unexpectedly", t);
                }
            }
        }
    }

    private RegionLodCoordinator.Worker spawnWorker() {
        RegionLodCoordinator.Worker worker = new RegionLodCoordinator.Worker();
        int index = this.nextWorkerIndex++;
        worker.thread = new Thread(() -> this.runWorkerLoop(worker), "Ecstatic-Region-Worker-" + index);
        worker.thread.setDaemon(true);
        worker.thread.start();
        return worker;
    }

    private synchronized void adjustWorkerThreadCount(int desiredCount) {
        int target = Math.max(1, desiredCount);

        while (this.workers.size() < target) {
            this.workers.add(this.spawnWorker());
        }

        while (this.workers.size() > target) {
            Worker worker = this.workers.remove(this.workers.size() - 1);
            worker.retire = true;
        }
    }
    
    public LodRegionFile fileForLevel(int level) {
        return this.filesByLevel.get(level);
    }

    public LodRegionFile bootstrapFile() {
        return this.bootstrapFile;
    }

    public List<RegionCoord> drainBootstrapReady() {
        List<RegionCoord> result = new ArrayList<>();

        RegionCoord next;
        while ((next = this.bootstrapReady.poll()) != null) {
            result.add(next);
        }

        return result;
    }

    public void tick(double playerChunkX, double playerChunkZ) {
        this.adjustWorkerThreadCount(LodSettingsConfig.get().workerThreadCount());
        if (this.hasLastScanPos) {
            double dx = playerChunkX - this.lastScanChunkX;
            double dz = playerChunkZ - this.lastScanChunkZ;
            if (Math.sqrt(dx * dx + dz * dz) < 4.0) {
                if (!Minecraft.getInstance().isPaused()) {
                    return;
                }

                long now = System.nanoTime();
                if (now - this.lastPausedRescanNanos < PAUSED_RESCAN_INTERVAL_NANOS) {
                    return;
                }

                this.lastPausedRescanNanos = now;
            }
        }

        this.hasLastScanPos = true;
        this.lastScanChunkX = playerChunkX;
        this.lastScanChunkZ = playerChunkZ;
        int clientRenderDistanceChunks = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int ring1StartChunks = RingConfig.ring1StartChunks(clientRenderDistanceChunks);
        RingConfig ringConfig = RingConfig.scaled(
                ring1StartChunks,
                this.lod1Width,
                this.lod2Width,
                this.lod3Width,
                this.lod4Width,
                this.lod5Width,
                this.hysteresisChunks,
                LodSettingsConfig.get().lodRenderDistanceScale()
        );
        int maxOuterChunks = ringConfig.outerBoundary(5) + this.hysteresisChunks;
        int minRegionX = Math.floorDiv((int) Math.floor(playerChunkX - maxOuterChunks), 32);
        int maxRegionX = Math.floorDiv((int) Math.ceil(playerChunkX + maxOuterChunks), 32);
        int minRegionZ = Math.floorDiv((int) Math.floor(playerChunkZ - maxOuterChunks), 32);
        int maxRegionZ = Math.floorDiv((int) Math.ceil(playerChunkZ + maxOuterChunks), 32);

        for (int rx = minRegionX; rx <= maxRegionX; rx++) {
            for (int rz = minRegionZ; rz <= maxRegionZ; rz++) {
                RegionCoord region = new RegionCoord(rx, rz);
                double distanceChunks = region.distanceChunksTo(playerChunkX, playerChunkZ);
                Integer previous = this.currentLevelByRegion.get(region);
                int previousLevel = previous == null ? -1 : previous;
                if (distanceChunks > maxOuterChunks) {
                    if (previous != null && !this.inFlight.contains(region)) {
                        this.ready.add(new RegionLodCoordinator.RegionReadyResult(region, -1));
                    }
                } else {
                    int targetLevel = previousLevel == -1 ? ringConfig.classify(distanceChunks) : ringConfig.resolveLevel(distanceChunks, previousLevel);
                    if (targetLevel == 0) {
                        double farthestDistanceChunks = region.farthestDistanceChunksTo(playerChunkX, playerChunkZ);
                        if (farthestDistanceChunks <= ringConfig.outerBoundary(0)) {
                            if (previous != null && !this.inFlight.contains(region) && regionHasRealTerrain(region)) {
                                this.ready.add(new RegionLodCoordinator.RegionReadyResult(region, -1));
                            }
                            continue;
                        }

                        targetLevel = Math.max(1, ringConfig.classify(farthestDistanceChunks));
                        if (targetLevel != -1 && targetLevel != previousLevel && !this.inFlight.contains(region)) {
                            LodRegionFile targetFile = this.filesByLevel.get(targetLevel);
                            if (targetFile.isFullySampled(region)) {
                                this.ready.add(new RegionLodCoordinator.RegionReadyResult(region, targetLevel));
                            } else {
                                if (targetLevel <= 3 && !this.bootstrapInFlight.contains(region) && !this.bootstrapFile.isFullySampled(region)) {
                                    this.bootstrapInFlight.add(region);
                                }
                                this.pendingTasks.put(new RegionLodCoordinator.SampleTask(
                                        BOOTSTRAP_PRIORITY,
                                        distanceChunks,
                                        region,
                                        () -> this.bootstrapInFlight.contains(region),
                                        () -> this.sampleBootstrapRegion(region)
                                ));
                            }
                            this.inFlight.add(region);
                            int finalTargetLevel = targetLevel;
                            this.pendingTasks.put(new RegionLodCoordinator.SampleTask(
                                    finalTargetLevel,
                                    distanceChunks,
                                    region,
                                    () -> this.inFlight.contains(region),
                                    () -> this.sampleRegion(region, finalTargetLevel)
                            ));
                        }
                    }
                }
            }
        }
    }

    private static boolean regionHasRealTerrain(RegionCoord region) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }

        int minChunkX = region.originChunkX();
        int minChunkZ = region.originChunkZ();
        int maxChunkX = minChunkX + 32 - 1;
        int maxChunkZ = minChunkZ + 32 - 1;
        int midChunkX = (minChunkX + maxChunkX) / 2;
        int midChunkZ = (minChunkZ + maxChunkZ) / 2;
        return level.hasChunk(midChunkX, midChunkZ)
            && level.hasChunk(minChunkX, minChunkZ)
            && level.hasChunk(minChunkX, maxChunkZ)
            && level.hasChunk(maxChunkX, minChunkZ)
            && level.hasChunk(maxChunkX, maxChunkZ);
    }

    public void submitBackgroundTask(int level, double distanceChunks, Runnable action) {
        this.pendingTasks.put(new RegionLodCoordinator.SampleTask(
            level, 
            distanceChunks, 
            null, 
            () -> true, 
            action
        ));
    }

    public List<RegionLodCoordinator.RegionReadyResult> drainReady() {
        List<RegionLodCoordinator.RegionReadyResult> result = new ArrayList<>();

        RegionLodCoordinator.RegionReadyResult next;
        while ((next = this.ready.poll()) != null) {
            if (next.level() == -1) {
                this.currentLevelByRegion.remove(next.region());
            } else {
                this.currentLevelByRegion.put(next.region(), next.level());
            }

            result.add(next);
        }

        return result;
    }

    private void sampleColumns(RegionCoord region, LodRegionFile file, int spacing) throws IOException {
        int last = file.samplesPerAxis - 1;

        for (int lz = 0; lz <= last; lz++) {
            int prevHeightInRow = Integer.MIN_VALUE;

            for (int lx = 0; lx <= last; lx++) {
                int blockX = region.originBlockX() + lx * spacing;
                int blockZ = region.originBlockZ() + lz * spacing;
                SurfaceSample sample = SurfaceSampler.sample(
                    this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, blockX, blockZ, prevHeightInRow
                );
                prevHeightInRow = sample.height();
                writeSample(file, region, lx, lz, sample);
            }
        }

        RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());
        int plusXBlockX = plusX.originBlockX();
        int prevHeightPlusX = Integer.MIN_VALUE;

        for (int lz = 0; lz <= last; lz++) {
            int blockZ = region.originBlockZ() + lz * spacing;
            SurfaceSample sample = SurfaceSampler.sample(
                this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, plusXBlockX, blockZ, prevHeightPlusX
            );
            prevHeightPlusX = sample.height();
            writeSample(file, plusX, 0, lz, sample);
        }

        RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);
        int plusZBlockZ = plusZ.originBlockZ();
        int prevHeightPlusZ = Integer.MIN_VALUE;

        for (int lx = 0; lx <= last; lx++) {
            int blockX = region.originBlockX() + lx * spacing;
            SurfaceSample sample = SurfaceSampler.sample(
                this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, blockX, plusZBlockZ, prevHeightPlusZ
            );
            prevHeightPlusZ = sample.height();
            writeSample(file, plusZ, lx, 0, sample);
        }

        RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
        SurfaceSample cornerSample = SurfaceSampler.sample(
            this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, plusXBlockX, plusZBlockZ, Integer.MIN_VALUE
        );
        writeSample(file, plusXZ, 0, 0, cornerSample);
        file.flush();
    }

    private void sampleRegion(RegionCoord region, int level) {
        try {
            this.sampleColumns(region, this.filesByLevel.get(level), LodLevel.sampleSpacingBlocks(level));
            this.ready.add(new RegionLodCoordinator.RegionReadyResult(region, level));
        } catch (Exception e) {
            Constants.LOG.error("Ecstatic failed to sample region ({}, {}) at LOD{}",region.x(), region.z(), level, e);
        } finally {
            this.inFlight.remove(region);
        }
    }

    private void sampleBootstrapRegion(RegionCoord region) {
        try {
            this.sampleColumns(region, this.bootstrapFile, BOOTSTRAP_SAMPLE_SPACING_BLOCKS);
            this.bootstrapReady.add(region);
        } catch (Exception e) {
            Constants.LOG
                .error("Ecstatic region coordinator failed to sample bootstrap placeholder for region ({}, {})", region.x(), region.z(), e);
        } finally {
            this.bootstrapInFlight.remove(region);
        }
    }

    private static void writeSample(LodRegionFile file, RegionCoord region, int localX, int localZ, SurfaceSample sample) throws IOException {
        file.writeColumn(region, localX, localZ, sample.height(), sample.biomeRawId(), sample.colorRgb(), sample.hasTrees());
    }

public synchronized void shutdown() {
        this.shuttingDown = true;

        for (RegionLodCoordinator.Worker worker : this.workers) {
            worker.thread.interrupt();
        }

        for (RegionLodCoordinator.Worker worker : this.workers) {
            try {
                worker.thread.join(TimeUnit.SECONDS.toMillis(10L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (LodRegionFile file : this.filesByLevel.values()) {
            try {
                file.close();
            } catch (IOException e) {
                Constants.LOG.error("Ecstatic region coordinator failed to close a region file", e);
            }
        }

        try {
            this.bootstrapFile.close();
        } catch (IOException e) {
            Constants.LOG.error("Ecstatic region coordinator failed to close the bootstrap region file", e);
        }
    }

    public record RegionReadyResult(RegionCoord region, int level) {
    }

    private record SampleTask(
        int level, 
        double distanceChunks, 
        RegionCoord region, 
        BooleanSupplier isValid, 
        Runnable action
    ) implements Comparable<RegionLodCoordinator.SampleTask> {
        @Override
        public int compareTo(RegionLodCoordinator.SampleTask other) {
            int cmp = Integer.compare(this.level, other.level);
            return cmp != 0 ? cmp : Double.compare(this.distanceChunks, other.distanceChunks);
        }
    }

    private static final class Worker {
        private Thread thread;
        private volatile boolean retire;
    }
}
