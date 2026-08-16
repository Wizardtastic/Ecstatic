package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.angryalchemist.ecstatic.debug.LodDebugState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class LodSettingsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LodSettingsConfig instance;
    private LodSettingsConfig.Data data = new LodSettingsConfig.Data();

    private LodSettingsConfig() {
        this.load();
    }

    public static synchronized LodSettingsConfig get() {
        if (instance == null) {
            instance = new LodSettingsConfig();
        }

        return instance;
    }

    boolean useLitVertexFormat() {
        return this.data.useLitVertexFormat;
    }

    void setUseLitVertexFormat(boolean useLitVertexFormat) {
        this.data.useLitVertexFormat = useLitVertexFormat;
    }

    boolean shaderWaterEnabled() {
        return this.data.shaderWaterEnabled;
    }

    void setShaderWaterEnabled(boolean shaderWaterEnabled) {
        this.data.shaderWaterEnabled = shaderWaterEnabled;
    }

    boolean frustumCullingEnabled() {
        return this.data.frustumCullingEnabled;
    }

    void setFrustumCullingEnabled(boolean frustumCullingEnabled) {
        this.data.frustumCullingEnabled = frustumCullingEnabled;
    }

    boolean oceanPlaneEnabled() {
        return this.data.oceanPlaneEnabled;
    }

    void setOceanPlaneEnabled(boolean oceanPlaneEnabled) {
        this.data.oceanPlaneEnabled = oceanPlaneEnabled;
    }

    boolean opaqueWaterEnabled() {
        return this.data.opaqueWaterEnabled;
    }

    void setOpaqueWaterEnabled(boolean opaqueWaterEnabled) {
        this.data.opaqueWaterEnabled = opaqueWaterEnabled;
    }

    boolean backfaceCullingEnabled() {
        return this.data.backfaceCullingEnabled;
    }

    float nightBrightness() {
        return this.data.nightBrightness;
    }

    void setNightBrightness(float value) {
        this.data.nightBrightness = Mth.clamp(value, 0.0F, 1.0F);
    }

    float dayBrightness() {
        return this.data.dayBrightness;
    }

    void setDayBrightness(float value) {
        this.data.dayBrightness = Mth.clamp(value, 0.0F, 1.0F);
    }

    float slopeShadingFloor() {
        return this.data.slopeShadingFloor;
    }

    void setSlopeShadingFloor(float value) {
        this.data.slopeShadingFloor = Mth.clamp(value, 0.0F, 1.0F);
    }

    float nearSlopeShadingFloor() {
        return this.data.nearSlopeShadingFloor;
    }

    void setNearSlopeShadingFloor(float value) {
        this.data.nearSlopeShadingFloor = Mth.clamp(value, 0.0F, 1.0F);
    }

    float structureSlopeShadingFloor() {
        return this.data.structureSlopeShadingFloor;
    }

    void setStructureSlopeShadingFloor(float value) {
        this.data.structureSlopeShadingFloor = Mth.clamp(value, 0.0F, 1.0F);
    }

    float saturationReduction() {
        return this.data.saturationReduction;
    }

    void setSaturationReduction(float value) {
        this.data.saturationReduction = Mth.clamp(value, 0.0F, 1.0F);
    }

    float sunReliefStrength() {
        return this.data.sunReliefStrength;
    }

    void setSunReliefStrength(float value) {
        this.data.sunReliefStrength = Mth.clamp(value, 0.0F, 1.0F);
    }

    float lightTemperature() {
        return this.data.lightTemperature;
    }

    void setLightTemperature(float value) {
        this.data.lightTemperature = Mth.clamp(value, 0.0F, 1.0F);
    }

    int nearTerrainTint(SurfaceMaterial.Kind kind) {
        return switch (kind) {
            case GRASS -> this.data.tintGrass;
            case DIRT -> this.data.tintDirt;
            case STONE -> this.data.tintStone;
            case SAND -> this.data.tintSand;
            case SNOW -> this.data.tintSnow;
            case RED_SAND -> this.data.tintRedSand;
            case TERRACOTTA -> this.data.tintTerracotta;
        };
    }

    void setNearTerrainTint(SurfaceMaterial.Kind kind, int rgb) {
        int value = rgb & 16777215;
        switch (kind) {
            case GRASS:
                this.data.tintGrass = value;
                break;
            case DIRT:
                this.data.tintDirt = value;
                break;
            case STONE:
                this.data.tintStone = value;
                break;
            case SAND:
                this.data.tintSand = value;
                break;
            case SNOW:
                this.data.tintSnow = value;
                break;
            case RED_SAND:
                this.data.tintRedSand = value;
                break;
            case TERRACOTTA:
                this.data.tintTerracotta = value;
        }
    }

    boolean debugToolsEnabled() {
        return this.data.debugToolsEnabled;
    }

    void setDebugToolsEnabled(boolean debugToolsEnabled) {
        this.data.debugToolsEnabled = debugToolsEnabled;
        LodDebugState.setEnabled(debugToolsEnabled);
    }

    void setBackfaceCullingEnabled(boolean backfaceCullingEnabled) {
        this.data.backfaceCullingEnabled = backfaceCullingEnabled;
    }

    int lod1SubStepBlocks() {
        return this.data.lod1SubStepBlocks == 2 ? 2 : 1;
    }

    void setLod1SubStepBlocks(int lod1SubStepBlocks) {
        this.data.lod1SubStepBlocks = lod1SubStepBlocks == 2 ? 2 : 1;
    }

    public float lodRenderDistanceScale() {
        return Mth.clamp(this.data.lodRenderDistanceScale, 0.25F, 2.0F);
    }

    void setLodRenderDistanceScale(float lodRenderDistanceScale) {
        this.data.lodRenderDistanceScale = Mth.clamp(lodRenderDistanceScale, 0.25F, 2.0F);
    }

    public int workerThreadCount() {
        int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        return Mth.clamp(this.data.workerThreadCount, 1, maxThreads);
    }

    void setWorkerThreadCount(int workerThreadCount) {
        int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        this.data.workerThreadCount = Mth.clamp(workerThreadCount, 1, maxThreads);
    }

    float fogFalloffScale() {
        return Mth.clamp(this.data.fogFalloffScale, 0.25F, 3.0F);
    }

    void setFogFalloffScale(float fogFalloffScale) {
        this.data.fogFalloffScale = Mth.clamp(fogFalloffScale, 0.25F, 3.0F);
    }

    float fogIntensity() {
        return Mth.clamp(this.data.fogIntensity, 0.0F, 1.0F);
    }

    void setFogIntensity(float fogIntensity) {
        this.data.fogIntensity = Mth.clamp(fogIntensity, 0.0F, 1.0F);
    }

    void save() {
        try {
            Files.createDirectories(this.configDir());

            try (Writer writer = Files.newBufferedWriter(this.configFile())) {
                GSON.toJson(this.data, writer);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to save LOD settings config", e);
        }
    }

    private void load() {
        Path file = this.configFile();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                LodSettingsConfig.Data loaded = (LodSettingsConfig.Data)GSON.fromJson(reader, LodSettingsConfig.Data.class);
                if (loaded != null) {
                    this.data = loaded;
                }

                LodDebugState.setEnabled(this.data.debugToolsEnabled);
            } catch (IOException e) {
                Constants.LOG.error("Failed to load LOD settings config", e);
            }
        }
    }

    private Path configDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("ecstatic");
    }

    private Path configFile() {
        return this.configDir().resolve("settings.json");
    }

    static final class Data {
        boolean useLitVertexFormat = false;
        boolean shaderWaterEnabled = false;
        boolean frustumCullingEnabled = true;
        boolean oceanPlaneEnabled = true;
        boolean opaqueWaterEnabled = true;
        boolean backfaceCullingEnabled = false;
        boolean debugToolsEnabled = false;
        float nightBrightness = 0.26F;
        float dayBrightness = 0.77F;
        float slopeShadingFloor = 0.64F;
        float nearSlopeShadingFloor = 0.25F;
        float structureSlopeShadingFloor = 0.72F;
        float saturationReduction = 0.3F;
        float sunReliefStrength = 0.21F;
        float lightTemperature = 0.84F;
        int tintGrass = 16777215;
        int tintDirt = 13092807;
        int tintStone = 16777215;
        int tintSand = 16777215;
        int tintSnow = 16777215;
        int tintRedSand = 16777215;
        int tintTerracotta = 16777215;
        int lod1SubStepBlocks = 2;
        float lodRenderDistanceScale = 0.9F;
        int workerThreadCount = Math.max(1, Math.round(Runtime.getRuntime().availableProcessors() * 0.8F));
        float fogFalloffScale = 1.25F;
        float fogIntensity = 1.0F;
    }
}
