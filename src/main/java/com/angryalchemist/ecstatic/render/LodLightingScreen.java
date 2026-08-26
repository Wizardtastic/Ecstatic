package com.angryalchemist.ecstatic.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

final class LodLightingScreen extends Screen {
    private static final int ROW_STRIDE = 32;
    private static final int WIDGET_HEIGHT = 20;
    private static final int SLIDER_WIDTH = 260;
    private static final int VIEWPORT_TOP = 50;
    private static final int FOOTER_HEIGHT = 44;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final double SCROLL_SPEED = 16.0;
    private static final int ROW_COUNT = 8;
    private static final int STEP_PERCENT = 1;
    private final Screen parent;
    private final LodSettingsConfig config = LodSettingsConfig.get();
    private double scrollAmount;
    private int maxScroll;

    LodLightingScreen(Screen parent) {
        super(Component.literal("Ecstatic Settings - Lighting"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int viewportTop = 50;
        int viewportBottom = this.height - 44;
        int viewportHeight = viewportBottom - viewportTop;
        int contentHeight = 256;
        this.maxScroll = Math.max(0, contentHeight - viewportHeight);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0, this.maxScroll);
        int y = viewportTop - (int)this.scrollAmount;
        y = this.addSlider(centerX, y, viewportTop, viewportBottom, "Night brightness", this.config.nightBrightness(), this.config::setNightBrightness);
        y = this.addSlider(centerX, y, viewportTop, viewportBottom, "Day brightness", this.config.dayBrightness(), this.config::setDayBrightness);
        y = this.addSlider(
            centerX, y, viewportTop, viewportBottom, "Shading softness (far)", this.config.slopeShadingFloor(), this.config::setSlopeShadingFloor
        );
        y = this.addSlider(
            centerX, y, viewportTop, viewportBottom, "Shading softness (near)", this.config.nearSlopeShadingFloor(), this.config::setNearSlopeShadingFloor
        );
        y = this.addSlider(
            centerX,
            y,
            viewportTop,
            viewportBottom,
            "Shading softness (structures)",
            this.config.structureSlopeShadingFloor(),
            this.config::setStructureSlopeShadingFloor
        );
        y = this.addSlider(centerX, y, viewportTop, viewportBottom, "Desaturation", this.config.saturationReduction(), this.config::setSaturationReduction);
        y = this.addSlider(centerX, y, viewportTop, viewportBottom, "Sun relief", this.config.sunReliefStrength(), this.config::setSunReliefStrength);
        this.addSlider(
            centerX, y, viewportTop, viewportBottom, "Light temperature)", this.config.lightTemperature(), this.config::setLightTemperature
        );
        int footerY = this.height - 44 + 20;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> this.onSave()).bounds(centerX - 105, footerY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(centerX + 5, footerY, 100, 20).build());
    }

    private int addSlider(
        int centerX, int y, int viewportTop, int viewportBottom, final String label, float initialValue, final LodLightingScreen.FloatSetter setter
    ) {
        int initialPercent = Math.round(initialValue * 100.0F);
        AbstractSliderButton slider = new AbstractSliderButton(centerX - 130, y, 260, 20, Component.literal(label + ": " + initialPercent + "%"), initialValue) {
            protected void updateMessage() {
                this.setMessage(Component.literal(label + ": " + this.percent() + "%"));
            }

            protected void applyValue() {
                setter.set(this.percent() / 100.0F);
            }

            private int percent() {
                int raw = (int)Math.round(this.value * 100.0);
                return Math.round(raw);
            }
        };
        slider.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(slider);
        return y + 32;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollAmount = Mth.clamp(scrollAmount - scrollY * SCROLL_SPEED, 0, maxScroll);
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        guiGraphics.drawCenteredString(this.font, "Brightness and desaturation preview live; the rest apply on Save", this.width / 2, 32, 8421504);
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

    @FunctionalInterface
    private interface FloatSetter {
        void set(float var1);
    }
}
