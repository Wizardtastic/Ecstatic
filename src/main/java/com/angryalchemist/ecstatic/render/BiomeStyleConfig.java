package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

final class BiomeStyleConfig {
    private static final String WATER_KEY = "WATER";
    private static final int DEFAULT_WATER_COLOR = 2312061;
    private static final float DEFAULT_WATER_ALPHA = 0.7F;
    private static final String SNOW_KEY = "SNOW";
    private static final float DEFAULT_SNOW_HEIGHT_PERCENT = 48.0F;
    private static final String ICE_KEY = "ICE";
    private static final int DEFAULT_ICE_COLOR = 11062752;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BiomeStyleConfig instance;
    private final Map<TreeStyle.Group, BiomeStyleConfig.Entry> entries = new EnumMap<>(TreeStyle.Group.class);
    private final Map<TreeStyle.Group, DynamicTexture> trunkTextures = new EnumMap<>(TreeStyle.Group.class);
    private final Map<TreeStyle.Group, DynamicTexture> foliageTextures = new EnumMap<>(TreeStyle.Group.class);
    private final Map<TreeStyle.Group, ResourceLocation> trunkTextureIds = new EnumMap<>(TreeStyle.Group.class);
    private final Map<TreeStyle.Group, ResourceLocation> foliageTextureIds = new EnumMap<>(TreeStyle.Group.class);
    private BiomeStyleConfig.WaterEntry water = new BiomeStyleConfig.WaterEntry(2312061, 0.7F);
    private BiomeStyleConfig.SnowEntry snow = new BiomeStyleConfig.SnowEntry(48.0F);
    private BiomeStyleConfig.IceEntry ice = new BiomeStyleConfig.IceEntry(11062752);

    private BiomeStyleConfig() {
        for (TreeStyle.Group group : TreeStyle.Group.values()) {
            TreeStyle style = TreeStyle.forGroup(group);
            BiomeStyleConfig.Entry entry = new BiomeStyleConfig.Entry(style.trunkColor, style.foliageColor);
            if (group == TreeStyle.Group.BEACH) {
                entry.groundTintEnabled = false;
            }

            this.entries.put(group, entry);
        }

        this.load();

        for (TreeStyle.Group group : TreeStyle.Group.values()) {
            this.reloadTextures(group);
        }
    }

    static synchronized BiomeStyleConfig get() {
        if (instance == null) {
            instance = new BiomeStyleConfig();
        }

        return instance;
    }

    BiomeStyleConfig.Entry entry(TreeStyle.Group group) {
        return this.entries.get(group);
    }

    int trunkTint(TreeStyle.Group group) {
        return this.entries.get(group).trunkTint;
    }

    int foliageTint(TreeStyle.Group group) {
        return this.entries.get(group).foliageTint;
    }

    boolean groundTintEnabled(TreeStyle.Group group) {
        return this.entries.get(group).groundTintEnabled;
    }

    void setGroundTintEnabled(TreeStyle.Group group, boolean enabled) {
        this.entries.get(group).groundTintEnabled = enabled;
    }

    int applyGroundTint(TreeStyle.Group group, int baseColor) {
        BiomeStyleConfig.Entry entry = this.entries.get(group);
        int tint = entry.groundTint;
        if (entry.groundTintEnabled && tint != 16777215) {
            int br = baseColor >> 16 & 0xFF;
            int bg = baseColor >> 8 & 0xFF;
            int bb = baseColor & 0xFF;
            int tr = tint >> 16 & 0xFF;
            int tg = tint >> 8 & 0xFF;
            int tb = tint & 0xFF;
            int r = br * tr / 255;
            int g = bg * tg / 255;
            int b = bb * tb / 255;
            return r << 16 | g << 8 | b;
        } else {
            return baseColor;
        }
    }

    int waterColor() {
        return this.water.color;
    }

    float waterAlpha() {
        return this.water.alpha;
    }

    void setWaterColor(int rgb) {
        this.water.color = rgb;
    }

    void setWaterAlpha(float alpha) {
        this.water.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
    }

    float snowHeightPercent() {
        return this.snow.heightPercent;
    }

    void setSnowHeightPercent(float percent) {
        this.snow.heightPercent = Math.max(0.0F, Math.min(100.0F, percent));
    }

    int iceColor() {
        return this.ice.color;
    }

    void setIceColor(int rgb) {
        this.ice.color = rgb;
    }

    ResourceLocation trunkTextureId(TreeStyle.Group group) {
        return this.trunkTextureIds.get(group);
    }

    ResourceLocation foliageTextureId(TreeStyle.Group group) {
        return this.foliageTextureIds.get(group);
    }

    void setTrunkTexture(TreeStyle.Group group, Path sourceFile) throws IOException {
        String fileName = group.name().toLowerCase(Locale.ROOT) + "_trunk.png";
        Files.createDirectories(this.texturesDir());
        Files.copy(sourceFile, this.texturesDir().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        this.entries.get(group).trunkTexture = fileName;
        this.reloadTextures(group);
    }

    void setFoliageTexture(TreeStyle.Group group, Path sourceFile) throws IOException {
        String fileName = group.name().toLowerCase(Locale.ROOT) + "_foliage.png";
        Files.createDirectories(this.texturesDir());
        Files.copy(sourceFile, this.texturesDir().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        this.entries.get(group).foliageTexture = fileName;
        this.reloadTextures(group);
    }

    void clearTrunkTexture(TreeStyle.Group group) {
        this.entries.get(group).trunkTexture = null;
        this.reloadTextures(group);
    }

    void clearFoliageTexture(TreeStyle.Group group) {
        this.entries.get(group).foliageTexture = null;
        this.reloadTextures(group);
    }

    void save() {
        try {
            Files.createDirectories(this.configDir());

            try (Writer writer = Files.newBufferedWriter(this.configFile())) {
                GSON.toJson(this.toNameKeyedMap(), writer);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to save biome style config", e);
        }
    }

    private void load() {
        Path file = this.configFile();
        if (Files.exists(file)) {
            try {
                label87: {
                    try (Reader reader = Files.newBufferedReader(file)) {
                        JsonObject root = (JsonObject)GSON.fromJson(reader, JsonObject.class);
                        if (root != null) {
                            for (Map.Entry<String, JsonElement> jsonEntry : root.entrySet()) {
                                if (jsonEntry.getKey().equals("WATER")) {
                                    BiomeStyleConfig.WaterEntry loadedWater = (BiomeStyleConfig.WaterEntry)GSON.fromJson(
                                        jsonEntry.getValue(), BiomeStyleConfig.WaterEntry.class
                                    );
                                    if (loadedWater != null) {
                                        this.water = loadedWater;
                                    }
                                } else if (jsonEntry.getKey().equals("SNOW")) {
                                    BiomeStyleConfig.SnowEntry loadedSnow = (BiomeStyleConfig.SnowEntry)GSON.fromJson(
                                        jsonEntry.getValue(), BiomeStyleConfig.SnowEntry.class
                                    );
                                    if (loadedSnow != null) {
                                        this.snow = loadedSnow;
                                    }
                                } else if (jsonEntry.getKey().equals("ICE")) {
                                    BiomeStyleConfig.IceEntry loadedIce = (BiomeStyleConfig.IceEntry)GSON.fromJson(
                                        jsonEntry.getValue(), BiomeStyleConfig.IceEntry.class
                                    );
                                    if (loadedIce != null) {
                                        this.ice = loadedIce;
                                    }
                                } else {
                                    try {
                                        TreeStyle.Group group = TreeStyle.Group.valueOf(jsonEntry.getKey());
                                        BiomeStyleConfig.Entry loadedEntry = (BiomeStyleConfig.Entry)GSON.fromJson(
                                            jsonEntry.getValue(), BiomeStyleConfig.Entry.class
                                        );
                                        if (loadedEntry != null) {
                                            this.entries.put(group, loadedEntry);
                                        }
                                    } catch (IllegalArgumentException var9) {
                                    }
                                }
                            }
                            break label87;
                        }
                    }

                    return;
                }
            } catch (IOException e) {
                Constants.LOG.error("Failed to load biome style config", e);
            }
        }
    }

    private Map<String, Object> toNameKeyedMap() {
        Map<String, Object> out = new LinkedHashMap<>();

        for (Map.Entry<TreeStyle.Group, BiomeStyleConfig.Entry> e : this.entries.entrySet()) {
            out.put(e.getKey().name(), e.getValue());
        }

        out.put("WATER", this.water);
        out.put("SNOW", this.snow);
        out.put("ICE", this.ice);
        return out;
    }

    private void reloadTextures(TreeStyle.Group group) {
        this.trunkTextureIds.put(group, this.loadTexture(group, "trunk", this.entries.get(group).trunkTexture, this.trunkTextures));
        this.foliageTextureIds.put(group, this.loadTexture(group, "foliage", this.entries.get(group).foliageTexture, this.foliageTextures));
    }

    private ResourceLocation loadTexture(TreeStyle.Group group, String part, String fileName, Map<TreeStyle.Group, DynamicTexture> cache) {
        DynamicTexture previous = cache.remove(group);
        if (previous != null) {
            previous.close();
        }

        if (fileName == null) {
            return null;
        }

        Path path = this.texturesDir().resolve(fileName);
        if (!Files.exists(path)) {
            return null;
        }

        try (InputStream in = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(in);
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = new ResourceLocation("ecstatic", "biome_style/" + group.name().toLowerCase(Locale.ROOT) + "_" + part);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            cache.put(group, texture);
            return id;
        } catch (IOException e) {
            Constants.LOG.error("Failed to load {} texture for {}", new Object[]{part, group, e});
            return null;
        }
    }

    private Path configDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("ecstatic");
    }

    private Path texturesDir() {
        return this.configDir().resolve("textures");
    }

    private Path configFile() {
        return this.configDir().resolve("biome_styles.json");
    }

    static final class Entry {
        int trunkTint;
        int foliageTint;
        int groundTint = 16777215;
        boolean groundTintEnabled = true;
        String trunkTexture;
        String foliageTexture;

        Entry() {
        }

        Entry(int trunkTint, int foliageTint) {
            this.trunkTint = trunkTint;
            this.foliageTint = foliageTint;
        }
    }

    static final class IceEntry {
        int color;

        IceEntry(int color) {
            this.color = color;
        }
    }

    static final class SnowEntry {
        float heightPercent;

        SnowEntry(float heightPercent) {
            this.heightPercent = heightPercent;
        }
    }

    static final class WaterEntry {
        int color;
        float alpha;

        WaterEntry(int color, float alpha) {
            this.color = color;
            this.alpha = alpha;
        }
    }
}
