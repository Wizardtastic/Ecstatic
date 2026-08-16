/*     */ package com.angryalchemist.ecstatic.lod;
/*     */ 
/*     */ import com.angryalchemist.ecstatic.Constants;
/*     */ import com.angryalchemist.ecstatic.sample.SurfaceSample;
/*     */ import com.angryalchemist.ecstatic.sample.SurfaceSampler;
/*     */ import com.angryalchemist.ecstatic.storage.LodRegionFile;
/*     */ import com.angryalchemist.ecstatic.storage.LodStoragePaths;
/*     */ import java.io.IOException;
/*     */ import java.nio.file.Path;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ConcurrentLinkedQueue;
/*     */ import java.util.concurrent.PriorityBlockingQueue;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.multiplayer.ClientLevel;
/*     */ import net.minecraft.core.Registry;
/*     */ import net.minecraft.world.level.LevelHeightAccessor;
/*     */ import net.minecraft.world.level.biome.Biome;
/*     */ import net.minecraft.world.level.chunk.ChunkGenerator;
/*     */ import net.minecraft.world.level.levelgen.RandomState;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class RegionLodCoordinator
/*     */ {
/*     */   public static final int BOOTSTRAP_LEVEL = 0;
/*  85 */   private static final int BOOTSTRAP_SAMPLE_SPACING_BLOCKS = LodLevel.sampleSpacingBlocks(4);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static final int BOOTSTRAP_PRIORITY = -1;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static final double RESCAN_THRESHOLD_CHUNKS = 4.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 114 */   private static final long PAUSED_RESCAN_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(500L);
/*     */ 
/*     */   
/*     */   private static final long WORKER_IDLE_POLL_MILLIS = 250L;
/*     */ 
/*     */   
/*     */   private final ChunkGenerator generator;
/*     */ 
/*     */   
/*     */   private final RandomState randomState;
/*     */   
/*     */   private final Registry<Biome> biomeRegistry;
/*     */   
/*     */   private final LevelHeightAccessor heightAccessor;
/*     */   
/*     */   private final int lod1Width;
/*     */   
/*     */   private final int lod2Width;
/*     */   
/*     */   private final int lod3Width;
/*     */   
/*     */   private final int lod4Width;
/*     */   
/*     */   private final int lod5Width;
/*     */   
/*     */   private final int hysteresisChunks;
/*     */   
/* 141 */   private final Map<Integer, LodRegionFile> filesByLevel = new HashMap<>();
/* 142 */   private final Map<RegionCoord, Integer> currentLevelByRegion = new HashMap<>();
/*     */   
/* 144 */   private final Set<RegionCoord> inFlight = ConcurrentHashMap.newKeySet();
/*     */   
/* 146 */   private final ConcurrentLinkedQueue<RegionReadyResult> ready = new ConcurrentLinkedQueue<>();
/*     */   
/*     */   private LodRegionFile bootstrapFile;
/*     */   
/* 150 */   private final Set<RegionCoord> bootstrapInFlight = ConcurrentHashMap.newKeySet();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 160 */   private final ConcurrentLinkedQueue<RegionCoord> bootstrapReady = new ConcurrentLinkedQueue<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 179 */   private final PriorityBlockingQueue<SampleTask> pendingTasks = new PriorityBlockingQueue<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 187 */   private final List<Worker> workers = new ArrayList<>();
/*     */   
/*     */   private int nextWorkerIndex;
/*     */   
/*     */   private volatile boolean shuttingDown;
/*     */   
/*     */   private boolean hasLastScanPos;
/*     */   
/*     */   private double lastScanChunkX;
/*     */   private double lastScanChunkZ;
/*     */   private long lastPausedRescanNanos;
/*     */   
/*     */   public RegionLodCoordinator(ChunkGenerator generator, RandomState randomState, Registry<Biome> biomeRegistry, LevelHeightAccessor heightAccessor, Path storageDir, int lod1Width, int lod2Width, int lod3Width, int lod4Width, int lod5Width, int hysteresisChunks, int workerThreadCount) throws IOException {
/* 200 */     this.generator = generator;
/* 201 */     this.randomState = randomState;
/* 202 */     this.biomeRegistry = biomeRegistry;
/* 203 */     this.heightAccessor = heightAccessor;
/* 204 */     this.lod1Width = lod1Width;
/* 205 */     this.lod2Width = lod2Width;
/* 206 */     this.lod3Width = lod3Width;
/* 207 */     this.lod4Width = lod4Width;
/* 208 */     this.lod5Width = lod5Width;
/* 209 */     this.hysteresisChunks = hysteresisChunks;
/*     */     
/* 211 */     for (int level = 1; level <= 5; level++) {
/* 212 */       Path file = LodStoragePaths.regionFile(storageDir, level);
/* 213 */       this.filesByLevel.put(Integer.valueOf(level), LodRegionFile.open(file, level, LodLevel.sampleSpacingBlocks(level)));
/*     */     } 
/* 215 */     Path bootstrapPath = LodStoragePaths.regionFile(storageDir, 0);
/* 216 */     this.bootstrapFile = LodRegionFile.open(bootstrapPath, 0, BOOTSTRAP_SAMPLE_SPACING_BLOCKS);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 222 */     int threadCount = Math.max(1, workerThreadCount);
/* 223 */     for (int i = 0; i < threadCount; i++) {
/* 224 */       this.workers.add(spawnWorker());
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void runWorkerLoop(Worker self) {
/* 243 */     while (!this.shuttingDown && !self.retire) {
/*     */       SampleTask task;
/*     */       try {
/* 246 */         task = this.pendingTasks.poll(250L, TimeUnit.MILLISECONDS);
/* 247 */       } catch (InterruptedException e) {
/* 248 */         Thread.currentThread().interrupt();
/*     */         return;
/*     */       } 
/* 251 */       if (task == null) {
/*     */         continue;
/*     */       }
/*     */       
/*     */       try {
/* 256 */         task.action().run();
/* 257 */       } catch (Throwable t) {
/* 258 */         Constants.LOG.error("Ecstatic region worker task failed unexpectedly", t);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private static final class Worker
/*     */   {
/*     */     private Thread thread;
/*     */     private volatile boolean retire;
/*     */   }
/*     */   
/*     */   private Worker spawnWorker() {
/* 270 */     Worker worker = new Worker();
/* 271 */     int index = this.nextWorkerIndex++;
/* 272 */     worker.thread = new Thread(() -> runWorkerLoop(worker), "Ecstatic-Region-Worker-" + index);
/* 273 */     worker.thread.setDaemon(true);
/* 274 */     worker.thread.start();
/* 275 */     return worker;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void adjustWorkerThreadCount(int desiredCount) {
/* 294 */     int target = Math.max(1, desiredCount);
/* 295 */     while (this.workers.size() < target) {
/* 296 */       this.workers.add(spawnWorker());
/*     */     }
/* 298 */     while (this.workers.size() > target) {
/* 299 */       ((Worker)this.workers.remove(this.workers.size() - 1)).retire = true;
/*     */     }
/*     */   }
/*     */   
/*     */   public LodRegionFile fileForLevel(int level) {
/* 304 */     return this.filesByLevel.get(Integer.valueOf(level));
/*     */   }
/*     */ 
/*     */   
/*     */   public LodRegionFile bootstrapFile() {
/* 309 */     return this.bootstrapFile;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<RegionCoord> drainBootstrapReady() {
/* 320 */     List<RegionCoord> result = new ArrayList<>();
/*     */     RegionCoord next;
/* 322 */     while ((next = this.bootstrapReady.poll()) != null) {
/* 323 */       result.add(next);
/*     */     }
/* 325 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void tick(double playerChunkX, double playerChunkZ) {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: invokestatic get : ()Lcom/angryalchemist/ecstatic/render/LodSettingsConfig;
/*     */     //   4: invokevirtual workerThreadCount : ()I
/*     */     //   7: invokevirtual adjustWorkerThreadCount : (I)V
/*     */     //   10: aload_0
/*     */     //   11: getfield hasLastScanPos : Z
/*     */     //   14: ifeq -> 90
/*     */     //   17: dload_1
/*     */     //   18: aload_0
/*     */     //   19: getfield lastScanChunkX : D
/*     */     //   22: dsub
/*     */     //   23: dstore #5
/*     */     //   25: dload_3
/*     */     //   26: aload_0
/*     */     //   27: getfield lastScanChunkZ : D
/*     */     //   30: dsub
/*     */     //   31: dstore #7
/*     */     //   33: dload #5
/*     */     //   35: dload #5
/*     */     //   37: dmul
/*     */     //   38: dload #7
/*     */     //   40: dload #7
/*     */     //   42: dmul
/*     */     //   43: dadd
/*     */     //   44: invokestatic sqrt : (D)D
/*     */     //   47: ldc2_w 4.0
/*     */     //   50: dcmpg
/*     */     //   51: ifge -> 90
/*     */     //   54: invokestatic m_91087_ : ()Lnet/minecraft/client/Minecraft;
/*     */     //   57: invokevirtual m_91104_ : ()Z
/*     */     //   60: ifne -> 64
/*     */     //   63: return
/*     */     //   64: invokestatic nanoTime : ()J
/*     */     //   67: lstore #9
/*     */     //   69: lload #9
/*     */     //   71: aload_0
/*     */     //   72: getfield lastPausedRescanNanos : J
/*     */     //   75: lsub
/*     */     //   76: getstatic com/angryalchemist/ecstatic/lod/RegionLodCoordinator.PAUSED_RESCAN_INTERVAL_NANOS : J
/*     */     //   79: lcmp
/*     */     //   80: ifge -> 84
/*     */     //   83: return
/*     */     //   84: aload_0
/*     */     //   85: lload #9
/*     */     //   87: putfield lastPausedRescanNanos : J
/*     */     //   90: aload_0
/*     */     //   91: iconst_1
/*     */     //   92: putfield hasLastScanPos : Z
/*     */     //   95: aload_0
/*     */     //   96: dload_1
/*     */     //   97: putfield lastScanChunkX : D
/*     */     //   100: aload_0
/*     */     //   101: dload_3
/*     */     //   102: putfield lastScanChunkZ : D
/*     */     //   105: invokestatic m_91087_ : ()Lnet/minecraft/client/Minecraft;
/*     */     //   108: getfield f_91066_ : Lnet/minecraft/client/Options;
/*     */     //   111: invokevirtual m_193772_ : ()I
/*     */     //   114: istore #5
/*     */     //   116: iload #5
/*     */     //   118: invokestatic ring1StartChunks : (I)I
/*     */     //   121: istore #6
/*     */     //   123: iload #6
/*     */     //   125: aload_0
/*     */     //   126: getfield lod1Width : I
/*     */     //   129: aload_0
/*     */     //   130: getfield lod2Width : I
/*     */     //   133: aload_0
/*     */     //   134: getfield lod3Width : I
/*     */     //   137: aload_0
/*     */     //   138: getfield lod4Width : I
/*     */     //   141: aload_0
/*     */     //   142: getfield lod5Width : I
/*     */     //   145: aload_0
/*     */     //   146: getfield hysteresisChunks : I
/*     */     //   149: invokestatic get : ()Lcom/angryalchemist/ecstatic/render/LodSettingsConfig;
/*     */     //   152: invokevirtual lodRenderDistanceScale : ()F
/*     */     //   155: invokestatic scaled : (IIIIIIIF)Lcom/angryalchemist/ecstatic/lod/RingConfig;
/*     */     //   158: astore #7
/*     */     //   160: aload #7
/*     */     //   162: iconst_5
/*     */     //   163: invokevirtual outerBoundary : (I)I
/*     */     //   166: aload_0
/*     */     //   167: getfield hysteresisChunks : I
/*     */     //   170: iadd
/*     */     //   171: istore #8
/*     */     //   173: dload_1
/*     */     //   174: iload #8
/*     */     //   176: i2d
/*     */     //   177: dsub
/*     */     //   178: invokestatic floor : (D)D
/*     */     //   181: d2i
/*     */     //   182: bipush #32
/*     */     //   184: invokestatic floorDiv : (II)I
/*     */     //   187: istore #9
/*     */     //   189: dload_1
/*     */     //   190: iload #8
/*     */     //   192: i2d
/*     */     //   193: dadd
/*     */     //   194: invokestatic ceil : (D)D
/*     */     //   197: d2i
/*     */     //   198: bipush #32
/*     */     //   200: invokestatic floorDiv : (II)I
/*     */     //   203: istore #10
/*     */     //   205: dload_3
/*     */     //   206: iload #8
/*     */     //   208: i2d
/*     */     //   209: dsub
/*     */     //   210: invokestatic floor : (D)D
/*     */     //   213: d2i
/*     */     //   214: bipush #32
/*     */     //   216: invokestatic floorDiv : (II)I
/*     */     //   219: istore #11
/*     */     //   221: dload_3
/*     */     //   222: iload #8
/*     */     //   224: i2d
/*     */     //   225: dadd
/*     */     //   226: invokestatic ceil : (D)D
/*     */     //   229: d2i
/*     */     //   230: bipush #32
/*     */     //   232: invokestatic floorDiv : (II)I
/*     */     //   235: istore #12
/*     */     //   237: iload #9
/*     */     //   239: istore #13
/*     */     //   241: iload #13
/*     */     //   243: iload #10
/*     */     //   245: if_icmpgt -> 683
/*     */     //   248: iload #11
/*     */     //   250: istore #14
/*     */     //   252: iload #14
/*     */     //   254: iload #12
/*     */     //   256: if_icmpgt -> 677
/*     */     //   259: new com/angryalchemist/ecstatic/lod/RegionCoord
/*     */     //   262: dup
/*     */     //   263: iload #13
/*     */     //   265: iload #14
/*     */     //   267: invokespecial <init> : (II)V
/*     */     //   270: astore #15
/*     */     //   272: aload #15
/*     */     //   274: dload_1
/*     */     //   275: dload_3
/*     */     //   276: invokevirtual distanceChunksTo : (DD)D
/*     */     //   279: dstore #16
/*     */     //   281: aload_0
/*     */     //   282: getfield currentLevelByRegion : Ljava/util/Map;
/*     */     //   285: aload #15
/*     */     //   287: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
/*     */     //   292: checkcast java/lang/Integer
/*     */     //   295: astore #18
/*     */     //   297: aload #18
/*     */     //   299: ifnonnull -> 306
/*     */     //   302: iconst_m1
/*     */     //   303: goto -> 311
/*     */     //   306: aload #18
/*     */     //   308: invokevirtual intValue : ()I
/*     */     //   311: istore #19
/*     */     //   313: dload #16
/*     */     //   315: iload #8
/*     */     //   317: i2d
/*     */     //   318: dcmpl
/*     */     //   319: ifle -> 362
/*     */     //   322: aload #18
/*     */     //   324: ifnull -> 671
/*     */     //   327: aload_0
/*     */     //   328: getfield inFlight : Ljava/util/Set;
/*     */     //   331: aload #15
/*     */     //   333: invokeinterface contains : (Ljava/lang/Object;)Z
/*     */     //   338: ifne -> 671
/*     */     //   341: aload_0
/*     */     //   342: getfield ready : Ljava/util/concurrent/ConcurrentLinkedQueue;
/*     */     //   345: new com/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult
/*     */     //   348: dup
/*     */     //   349: aload #15
/*     */     //   351: iconst_m1
/*     */     //   352: invokespecial <init> : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;I)V
/*     */     //   355: invokevirtual add : (Ljava/lang/Object;)Z
/*     */     //   358: pop
/*     */     //   359: goto -> 671
/*     */     //   362: iload #19
/*     */     //   364: iconst_m1
/*     */     //   365: if_icmpne -> 378
/*     */     //   368: aload #7
/*     */     //   370: dload #16
/*     */     //   372: invokevirtual classify : (D)I
/*     */     //   375: goto -> 387
/*     */     //   378: aload #7
/*     */     //   380: dload #16
/*     */     //   382: iload #19
/*     */     //   384: invokevirtual resolveLevel : (DI)I
/*     */     //   387: istore #20
/*     */     //   389: iload #20
/*     */     //   391: ifne -> 477
/*     */     //   394: aload #15
/*     */     //   396: dload_1
/*     */     //   397: dload_3
/*     */     //   398: invokevirtual farthestDistanceChunksTo : (DD)D
/*     */     //   401: dstore #21
/*     */     //   403: dload #21
/*     */     //   405: aload #7
/*     */     //   407: iconst_0
/*     */     //   408: invokevirtual outerBoundary : (I)I
/*     */     //   411: i2d
/*     */     //   412: dcmpg
/*     */     //   413: ifgt -> 464
/*     */     //   416: aload #18
/*     */     //   418: ifnull -> 671
/*     */     //   421: aload_0
/*     */     //   422: getfield inFlight : Ljava/util/Set;
/*     */     //   425: aload #15
/*     */     //   427: invokeinterface contains : (Ljava/lang/Object;)Z
/*     */     //   432: ifne -> 671
/*     */     //   435: aload #15
/*     */     //   437: invokestatic regionHasRealTerrain : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;)Z
/*     */     //   440: ifeq -> 671
/*     */     //   443: aload_0
/*     */     //   444: getfield ready : Ljava/util/concurrent/ConcurrentLinkedQueue;
/*     */     //   447: new com/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult
/*     */     //   450: dup
/*     */     //   451: aload #15
/*     */     //   453: iconst_m1
/*     */     //   454: invokespecial <init> : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;I)V
/*     */     //   457: invokevirtual add : (Ljava/lang/Object;)Z
/*     */     //   460: pop
/*     */     //   461: goto -> 671
/*     */     //   464: iconst_1
/*     */     //   465: aload #7
/*     */     //   467: dload #21
/*     */     //   469: invokevirtual classify : (D)I
/*     */     //   472: invokestatic max : (II)I
/*     */     //   475: istore #20
/*     */     //   477: iload #20
/*     */     //   479: iconst_m1
/*     */     //   480: if_icmpeq -> 671
/*     */     //   483: iload #20
/*     */     //   485: iload #19
/*     */     //   487: if_icmpeq -> 671
/*     */     //   490: aload_0
/*     */     //   491: getfield inFlight : Ljava/util/Set;
/*     */     //   494: aload #15
/*     */     //   496: invokeinterface contains : (Ljava/lang/Object;)Z
/*     */     //   501: ifeq -> 507
/*     */     //   504: goto -> 671
/*     */     //   507: aload_0
/*     */     //   508: getfield filesByLevel : Ljava/util/Map;
/*     */     //   511: iload #20
/*     */     //   513: invokestatic valueOf : (I)Ljava/lang/Integer;
/*     */     //   516: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
/*     */     //   521: checkcast com/angryalchemist/ecstatic/storage/LodRegionFile
/*     */     //   524: astore #21
/*     */     //   526: aload #21
/*     */     //   528: aload #15
/*     */     //   530: invokevirtual isFullySampled : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;)Z
/*     */     //   533: ifeq -> 558
/*     */     //   536: aload_0
/*     */     //   537: getfield ready : Ljava/util/concurrent/ConcurrentLinkedQueue;
/*     */     //   540: new com/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult
/*     */     //   543: dup
/*     */     //   544: aload #15
/*     */     //   546: iload #20
/*     */     //   548: invokespecial <init> : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;I)V
/*     */     //   551: invokevirtual add : (Ljava/lang/Object;)Z
/*     */     //   554: pop
/*     */     //   555: goto -> 671
/*     */     //   558: iload #20
/*     */     //   560: iconst_3
/*     */     //   561: if_icmpgt -> 627
/*     */     //   564: aload_0
/*     */     //   565: getfield bootstrapInFlight : Ljava/util/Set;
/*     */     //   568: aload #15
/*     */     //   570: invokeinterface contains : (Ljava/lang/Object;)Z
/*     */     //   575: ifne -> 627
/*     */     //   578: aload_0
/*     */     //   579: getfield bootstrapFile : Lcom/angryalchemist/ecstatic/storage/LodRegionFile;
/*     */     //   582: aload #15
/*     */     //   584: invokevirtual isFullySampled : (Lcom/angryalchemist/ecstatic/lod/RegionCoord;)Z
/*     */     //   587: ifne -> 627
/*     */     //   590: aload_0
/*     */     //   591: getfield bootstrapInFlight : Ljava/util/Set;
/*     */     //   594: aload #15
/*     */     //   596: invokeinterface add : (Ljava/lang/Object;)Z
/*     */     //   601: pop
/*     */     //   602: aload_0
/*     */     //   603: getfield pendingTasks : Ljava/util/concurrent/PriorityBlockingQueue;
/*     */     //   606: new com/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask
/*     */     //   609: dup
/*     */     //   610: iconst_m1
/*     */     //   611: dload #16
/*     */     //   613: aload_0
/*     */     //   614: aload #15
/*     */     //   616: <illegal opcode> run : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator;Lcom/angryalchemist/ecstatic/lod/RegionCoord;)Ljava/lang/Runnable;
/*     */     //   621: invokespecial <init> : (IDLjava/lang/Runnable;)V
/*     */     //   624: invokevirtual put : (Ljava/lang/Object;)V
/*     */     //   627: aload_0
/*     */     //   628: getfield inFlight : Ljava/util/Set;
/*     */     //   631: aload #15
/*     */     //   633: invokeinterface add : (Ljava/lang/Object;)Z
/*     */     //   638: pop
/*     */     //   639: iload #20
/*     */     //   641: istore #22
/*     */     //   643: aload_0
/*     */     //   644: getfield pendingTasks : Ljava/util/concurrent/PriorityBlockingQueue;
/*     */     //   647: new com/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask
/*     */     //   650: dup
/*     */     //   651: iload #22
/*     */     //   653: dload #16
/*     */     //   655: aload_0
/*     */     //   656: aload #15
/*     */     //   658: iload #22
/*     */     //   660: <illegal opcode> run : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator;Lcom/angryalchemist/ecstatic/lod/RegionCoord;I)Ljava/lang/Runnable;
/*     */     //   665: invokespecial <init> : (IDLjava/lang/Runnable;)V
/*     */     //   668: invokevirtual put : (Ljava/lang/Object;)V
/*     */     //   671: iinc #14, 1
/*     */     //   674: goto -> 252
/*     */     //   677: iinc #13, 1
/*     */     //   680: goto -> 241
/*     */     //   683: return
/*     */     // Line number table:
/*     */     //   Java source line number -> byte code offset
/*     */     //   #333	-> 0
/*     */     //   #335	-> 10
/*     */     //   #336	-> 17
/*     */     //   #337	-> 25
/*     */     //   #338	-> 33
/*     */     //   #339	-> 54
/*     */     //   #340	-> 63
/*     */     //   #342	-> 64
/*     */     //   #343	-> 69
/*     */     //   #344	-> 83
/*     */     //   #346	-> 84
/*     */     //   #349	-> 90
/*     */     //   #350	-> 95
/*     */     //   #351	-> 100
/*     */     //   #353	-> 105
/*     */     //   #354	-> 116
/*     */     //   #355	-> 123
/*     */     //   #356	-> 149
/*     */     //   #355	-> 155
/*     */     //   #357	-> 160
/*     */     //   #359	-> 173
/*     */     //   #360	-> 189
/*     */     //   #361	-> 205
/*     */     //   #362	-> 221
/*     */     //   #364	-> 237
/*     */     //   #365	-> 248
/*     */     //   #366	-> 259
/*     */     //   #367	-> 272
/*     */     //   #368	-> 281
/*     */     //   #369	-> 297
/*     */     //   #371	-> 313
/*     */     //   #372	-> 322
/*     */     //   #373	-> 341
/*     */     //   #378	-> 362
/*     */     //   #379	-> 368
/*     */     //   #380	-> 378
/*     */     //   #382	-> 389
/*     */     //   #398	-> 394
/*     */     //   #399	-> 403
/*     */     //   #429	-> 416
/*     */     //   #430	-> 443
/*     */     //   #449	-> 464
/*     */     //   #451	-> 477
/*     */     //   #452	-> 504
/*     */     //   #455	-> 507
/*     */     //   #456	-> 526
/*     */     //   #457	-> 536
/*     */     //   #458	-> 555
/*     */     //   #470	-> 558
/*     */     //   #471	-> 570
/*     */     //   #472	-> 590
/*     */     //   #473	-> 602
/*     */     //   #476	-> 627
/*     */     //   #477	-> 639
/*     */     //   #478	-> 643
/*     */     //   #365	-> 671
/*     */     //   #364	-> 677
/*     */     //   #481	-> 683
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	descriptor
/*     */     //   69	21	9	now	J
/*     */     //   25	65	5	dx	D
/*     */     //   33	57	7	dz	D
/*     */     //   403	74	21	farthestDistanceChunks	D
/*     */     //   272	399	15	region	Lcom/angryalchemist/ecstatic/lod/RegionCoord;
/*     */     //   281	390	16	distanceChunks	D
/*     */     //   297	374	18	previous	Ljava/lang/Integer;
/*     */     //   313	358	19	previousLevel	I
/*     */     //   389	282	20	targetLevel	I
/*     */     //   526	145	21	targetFile	Lcom/angryalchemist/ecstatic/storage/LodRegionFile;
/*     */     //   643	28	22	finalTargetLevel	I
/*     */     //   252	425	14	rz	I
/*     */     //   241	442	13	rx	I
/*     */     //   0	684	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator;
/*     */     //   0	684	1	playerChunkX	D
/*     */     //   0	684	3	playerChunkZ	D
/*     */     //   116	568	5	clientRenderDistanceChunks	I
/*     */     //   123	561	6	ring1StartChunks	I
/*     */     //   160	524	7	ringConfig	Lcom/angryalchemist/ecstatic/lod/RingConfig;
/*     */     //   173	511	8	maxOuterChunks	I
/*     */     //   189	495	9	minRegionX	I
/*     */     //   205	479	10	maxRegionX	I
/*     */     //   221	463	11	minRegionZ	I
/*     */     //   237	447	12	maxRegionZ	I
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static boolean regionHasRealTerrain(RegionCoord region) {
/* 506 */     ClientLevel level = (Minecraft.m_91087_()).f_91073_;
/* 507 */     if (level == null) {
/* 508 */       return false;
/*     */     }
/* 510 */     int minChunkX = region.originChunkX();
/* 511 */     int minChunkZ = region.originChunkZ();
/* 512 */     int maxChunkX = minChunkX + 32 - 1;
/* 513 */     int maxChunkZ = minChunkZ + 32 - 1;
/* 514 */     int midChunkX = (minChunkX + maxChunkX) / 2;
/* 515 */     int midChunkZ = (minChunkZ + maxChunkZ) / 2;
/* 516 */     return (level.m_7232_(midChunkX, midChunkZ) && level
/* 517 */       .m_7232_(minChunkX, minChunkZ) && level
/* 518 */       .m_7232_(minChunkX, maxChunkZ) && level
/* 519 */       .m_7232_(maxChunkX, minChunkZ) && level
/* 520 */       .m_7232_(maxChunkX, maxChunkZ));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void submitBackgroundTask(int level, double distanceChunks, Runnable action) {
/* 539 */     this.pendingTasks.put(new SampleTask(level, distanceChunks, action));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<RegionReadyResult> drainReady() {
/* 548 */     List<RegionReadyResult> result = new ArrayList<>();
/*     */     RegionReadyResult next;
/* 550 */     while ((next = this.ready.poll()) != null) {
/* 551 */       if (next.level() == -1) {
/* 552 */         this.currentLevelByRegion.remove(next.region());
/*     */       } else {
/* 554 */         this.currentLevelByRegion.put(next.region(), Integer.valueOf(next.level()));
/*     */       } 
/* 556 */       result.add(next);
/*     */     } 
/* 558 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sampleColumns(RegionCoord region, LodRegionFile file, int spacing) throws IOException {
/* 571 */     int last = file.samplesPerAxis - 1;
/*     */     
/* 573 */     for (int lz = 0; lz <= last; lz++) {
/* 574 */       int prevHeightInRow = Integer.MIN_VALUE;
/* 575 */       for (int j = 0; j <= last; j++) {
/* 576 */         int blockX = region.originBlockX() + j * spacing;
/* 577 */         int blockZ = region.originBlockZ() + lz * spacing;
/* 578 */         SurfaceSample sample = SurfaceSampler.sample(this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, blockX, blockZ, prevHeightInRow);
/*     */         
/* 580 */         prevHeightInRow = sample.height();
/* 581 */         writeSample(file, region, j, lz, sample);
/*     */       } 
/*     */     } 
/*     */     
/* 585 */     RegionCoord plusX = new RegionCoord(region.x() + 1, region.z());
/* 586 */     int plusXBlockX = plusX.originBlockX();
/* 587 */     int prevHeightPlusX = Integer.MIN_VALUE;
/* 588 */     for (int i = 0; i <= last; i++) {
/* 589 */       int blockZ = region.originBlockZ() + i * spacing;
/* 590 */       SurfaceSample sample = SurfaceSampler.sample(this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, plusXBlockX, blockZ, prevHeightPlusX);
/*     */       
/* 592 */       prevHeightPlusX = sample.height();
/* 593 */       writeSample(file, plusX, 0, i, sample);
/*     */     } 
/*     */     
/* 596 */     RegionCoord plusZ = new RegionCoord(region.x(), region.z() + 1);
/* 597 */     int plusZBlockZ = plusZ.originBlockZ();
/* 598 */     int prevHeightPlusZ = Integer.MIN_VALUE;
/* 599 */     for (int lx = 0; lx <= last; lx++) {
/* 600 */       int blockX = region.originBlockX() + lx * spacing;
/* 601 */       SurfaceSample sample = SurfaceSampler.sample(this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, blockX, plusZBlockZ, prevHeightPlusZ);
/*     */       
/* 603 */       prevHeightPlusZ = sample.height();
/* 604 */       writeSample(file, plusZ, lx, 0, sample);
/*     */     } 
/*     */     
/* 607 */     RegionCoord plusXZ = new RegionCoord(region.x() + 1, region.z() + 1);
/* 608 */     SurfaceSample cornerSample = SurfaceSampler.sample(this.generator, this.randomState, this.biomeRegistry, this.heightAccessor, plusXBlockX, plusZBlockZ, -2147483648);
/*     */     
/* 610 */     writeSample(file, plusXZ, 0, 0, cornerSample);
/*     */     
/* 612 */     file.flush();
/*     */   }
/*     */ 
/*     */   
/*     */   private void sampleRegion(RegionCoord region, int level) {
/*     */     try {
/* 618 */       sampleColumns(region, this.filesByLevel.get(Integer.valueOf(level)), LodLevel.sampleSpacingBlocks(level));
/* 619 */       this.ready.add(new RegionReadyResult(region, level));
/* 620 */     } catch (Exception e) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 627 */       Constants.LOG.error("Ecstatic region coordinator failed to sample region ({}, {}) at LOD{}", new Object[] {
/* 628 */             Integer.valueOf(region.x()), Integer.valueOf(region.z()), Integer.valueOf(level), e });
/*     */     } finally {
/* 630 */       this.inFlight.remove(region);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sampleBootstrapRegion(RegionCoord region) {
/*     */     try {
/* 644 */       sampleColumns(region, this.bootstrapFile, BOOTSTRAP_SAMPLE_SPACING_BLOCKS);
/* 645 */       this.bootstrapReady.add(region);
/* 646 */     } catch (Exception e) {
/* 647 */       Constants.LOG.error("Ecstatic region coordinator failed to sample bootstrap placeholder for region ({}, {})", new Object[] {
/* 648 */             Integer.valueOf(region.x()), Integer.valueOf(region.z()), e });
/*     */     } finally {
/* 650 */       this.bootstrapInFlight.remove(region);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static void writeSample(LodRegionFile file, RegionCoord region, int localX, int localZ, SurfaceSample sample) throws IOException {
/* 656 */     file.writeColumn(region, localX, localZ, sample.height(), sample.biomeRawId(), sample.colorRgb(), sample
/* 657 */         .hasTrees());
/*     */   }
/*     */ 
/*     */   
/*     */   public void shutdown() {
/* 662 */     this.shuttingDown = true;
/* 663 */     for (Worker worker : this.workers) {
/* 664 */       worker.thread.interrupt();
/*     */     }
/* 666 */     for (Worker worker : this.workers) {
/*     */       try {
/* 668 */         worker.thread.join(TimeUnit.SECONDS.toMillis(10L));
/* 669 */       } catch (InterruptedException e) {
/* 670 */         Thread.currentThread().interrupt();
/*     */       } 
/*     */     } 
/* 673 */     for (LodRegionFile file : this.filesByLevel.values()) {
/*     */       try {
/* 675 */         file.close();
/* 676 */       } catch (IOException e) {
/* 677 */         Constants.LOG.error("Ecstatic region coordinator failed to close a region file", e);
/*     */       } 
/*     */     } 
/*     */     try {
/* 681 */       this.bootstrapFile.close();
/* 682 */     } catch (IOException e) {
/* 683 */       Constants.LOG.error("Ecstatic region coordinator failed to close the bootstrap region file", e);
/*     */     } 
/*     */   }
/*     */   public static final class RegionReadyResult extends Record { private final RegionCoord region; private final int level;
/*     */     
/* 688 */     public RegionReadyResult(RegionCoord region, int level) { this.region = region; this.level = level; } public final String toString() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult;)Ljava/lang/String;
/*     */       //   6: areturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #688	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/* 688 */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult; } public RegionCoord region() { return this.region; } public final int hashCode() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult;)I
/*     */       //   6: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #688	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult; } public final boolean equals(Object o) { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: aload_1
/*     */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult;Ljava/lang/Object;)Z
/*     */       //   7: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #688	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$RegionReadyResult;
/* 688 */       //   0	8	1	o	Ljava/lang/Object; } public int level() { return this.level; }
/*     */      }
/*     */ 
/*     */   
/*     */   private static final class SampleTask extends Record implements Comparable<SampleTask> {
/*     */     private final int level;
/*     */     private final double distanceChunks;
/*     */     private final Runnable action;
/*     */     
/*     */     private SampleTask(int level, double distanceChunks, Runnable action) {
/* 698 */       this.level = level; this.distanceChunks = distanceChunks; this.action = action; } public final String toString() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> toString : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask;)Ljava/lang/String;
/*     */       //   6: areturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #698	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask; } public final int hashCode() { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: <illegal opcode> hashCode : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask;)I
/*     */       //   6: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #698	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	7	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask; } public final boolean equals(Object o) { // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: aload_1
/*     */       //   2: <illegal opcode> equals : (Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask;Ljava/lang/Object;)Z
/*     */       //   7: ireturn
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #698	-> 0
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   0	8	0	this	Lcom/angryalchemist/ecstatic/lod/RegionLodCoordinator$SampleTask;
/* 698 */       //   0	8	1	o	Ljava/lang/Object; } public int level() { return this.level; } public double distanceChunks() { return this.distanceChunks; } public Runnable action() { return this.action; }
/*     */     
/*     */     public int compareTo(SampleTask other) {
/* 701 */       int cmp = Integer.compare(this.level, other.level);
/* 702 */       if (cmp != 0) {
/* 703 */         return cmp;
/*     */       }
/* 705 */       return Double.compare(this.distanceChunks, other.distanceChunks);
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\Walker\Downloads\ecstatic-forge-1.20.1-1.3.0.jar!\com\angryalchemist\ecstatic\lod\RegionLodCoordinator.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */