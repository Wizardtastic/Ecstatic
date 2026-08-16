package com.angryalchemist.ecstatic.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

final class LodTerrainColorScreen extends Screen {
    private static final int ROW_STRIDE = 26;
    private static final int FIELD_WIDTH = 80;
    private static final int WIDGET_HEIGHT = 20;
    private static final int SWATCH_SIZE = 20;
    private static final int TOP = 44;
    private final Screen parent;
    private final LodSettingsConfig config = LodSettingsConfig.get();
    private final Map<SurfaceMaterial.Kind, EditBox> fields = new EnumMap<>(SurfaceMaterial.Kind.class);

    LodTerrainColorScreen(Screen parent) {
        super(Component.literal("Ecstatic Settings - Terrain Colors"));
        this.parent = parent;
    }

    protected void init() {
        this.fields.clear();
        int centerX = this.width / 2;
        int y = 44;

        for (SurfaceMaterial.Kind kind : SurfaceMaterial.Kind.values()) {
            EditBox box = (EditBox)this.addRenderableWidget(new EditBox(this.font, centerX + 10, y, 80, 20, Component.literal(label(kind))));
            box.setMaxLength(6);
            box.setValue(toHex(this.config.nearTerrainTint(kind)));
            this.fields.put(kind, box);
            y += 26;
        }

        int footerY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> this.onReset()).bounds(centerX - 158, footerY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> this.onSave()).bounds(centerX - 52, footerY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(centerX + 54, footerY, 100, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 16777215);
        guiGraphics.drawCenteredString(this.font, "Hex tint, FFFFFF = unchanged. Applies on Save.", this.width / 2, 28, 8421504);

        for (Entry<SurfaceMaterial.Kind, EditBox> entry : this.fields.entrySet()) {
            EditBox box = entry.getValue();
            guiGraphics.drawString(this.font, label(entry.getKey()), box.getX() - 96, box.getY() + 6, 16777215);
            int rgb = parseHexOr(box.getValue(), this.config.nearTerrainTint(entry.getKey()));
            int x0 = box.getX() + box.getWidth() + 8;
            guiGraphics.fill(x0, box.getY(), x0 + 20, box.getY() + box.getHeight(), 0xFF000000 | rgb & 16777215);
        }
    }

    private static String label(SurfaceMaterial.Kind kind) {
        return switch (kind) {
            case GRASS -> "Grass";
            case DIRT -> "Dirt";
            case STONE -> "Stone";
            case SAND -> "Sand";
            case SNOW -> "Snow";
            case RED_SAND -> "Red sand";
            case TERRACOTTA -> "Terracotta";
        };
    }

    private void onReset() {
        for (SurfaceMaterial.Kind kind : SurfaceMaterial.Kind.values()) {
            this.config.setNearTerrainTint(kind, 16777215);
        }

        this.onSave();
        this.rebuildWidgets();
    }

    private void onSave() {
        for (Entry<SurfaceMaterial.Kind, EditBox> entry : this.fields.entrySet()) {
            int current = this.config.nearTerrainTint(entry.getKey());
            this.config.setNearTerrainTint(entry.getKey(), parseHexOr(entry.getValue().getValue(), current));
        }

        this.config.save();
        LodRenderer.rebuildAllMeshes();
    }

    private static int parseHexOr(String text, int fallback) {
        try {
            return Mth.clamp(Integer.parseInt(text.trim(), 16), 0, 16777215);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String toHex(int rgb) {
        return String.format("%06X", rgb & 16777215);
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}
