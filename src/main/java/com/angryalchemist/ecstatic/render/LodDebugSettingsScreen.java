package com.angryalchemist.ecstatic.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

final class LodDebugSettingsScreen extends Screen {
    private static final int ROW_STRIDE = 32;
    private static final int WIDGET_HEIGHT = 20;
    private static final int CHECKBOX_WIDTH = 260;
    private static final int VIEWPORT_TOP = 50;
    private static final int FOOTER_HEIGHT = 44;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final double SCROLL_SPEED = 16.0;
    private static final int ROW_COUNT = 4;
    private final Screen parent;
    private final LodSettingsConfig config = LodSettingsConfig.get();
    private double scrollAmount;
    private int maxScroll;

    LodDebugSettingsScreen(Screen parent) {
        super(Component.literal("Ecstatic Settings - Debug"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int viewportTop = 50;
        int viewportBottom = this.height - 44;
        int viewportHeight = viewportBottom - viewportTop;
        int contentHeight = 128;
        this.maxScroll = Math.max(0, contentHeight - viewportHeight);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0, this.maxScroll);
        int y = viewportTop - (int)this.scrollAmount;
        Checkbox debugTools = new Checkbox(
            centerX - 130, y, 260, 20, Component.literal("Enable debug tools (F7-F10 keybinds)"), this.config.debugToolsEnabled()
        ) {
            public void onPress() {
                super.onPress();
                LodDebugSettingsScreen.this.config.setDebugToolsEnabled(this.selected());
            }
        };
        debugTools.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(debugTools);
        y += 32;
        Checkbox litFormat = new Checkbox(
            centerX - 130, y, 260, 20, Component.literal("Lit vertex format (heavier, more GPU cost)"), this.config.useLitVertexFormat()
        ) {
            public void onPress() {
                super.onPress();
                LodDebugSettingsScreen.this.config.setUseLitVertexFormat(this.selected());
            }
        };
        litFormat.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(litFormat);
        y += 32;
        Checkbox shaderWater = new Checkbox(
            centerX - 130, y, 260, 20, Component.literal("Shaderpack water at distance (experimental)"), this.config.shaderWaterEnabled()
        ) {
            public void onPress() {
                super.onPress();
                LodDebugSettingsScreen.this.config.setShaderWaterEnabled(this.selected());
            }
        };
        shaderWater.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(shaderWater);
        y += 32;
        Checkbox backfaceCulling = new Checkbox(
            centerX - 130, y, 260, 20, Component.literal("Backface culling (faster, can show gaps)"), this.config.backfaceCullingEnabled()
        ) {
            public void onPress() {
                super.onPress();
                LodDebugSettingsScreen.this.config.setBackfaceCullingEnabled(this.selected());
            }
        };
        backfaceCulling.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(backfaceCulling);
        int footerY = this.height - 44 + 20;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> this.onSave()).bounds(centerX - 105, footerY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(centerX + 5, footerY, 100, 20).build());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.maxScroll > 0) {
            this.scrollAmount = Mth.clamp(this.scrollAmount - delta * 16.0, 0.0, this.maxScroll);
            this.rebuildWidgets();
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        guiGraphics.drawCenteredString(this.font, "Lit vertex format is currently broken - leave unchecked", this.width / 2, 32, 8421504);
        if (this.maxScroll > 0) {
            int centerX = this.width / 2;
            int trackX = centerX + 130 + 10;
            int trackTop = 50;
            int trackBottom = this.height - 44;
            int trackHeight = trackBottom - trackTop;
            guiGraphics.fill(trackX, trackTop, trackX + 6, trackBottom, 1090519039);
            int thumbHeight = Math.max(12, trackHeight * trackHeight / (trackHeight + this.maxScroll));
            int thumbY = trackTop + (int)((trackHeight - thumbHeight) * (this.scrollAmount / this.maxScroll));
            guiGraphics.fill(trackX, thumbY, trackX + 6, thumbY + thumbHeight, -4144960);
        }
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void onSave() {
        this.config.save();
        LodRenderer.rebuildAllMeshes();
    }
}
