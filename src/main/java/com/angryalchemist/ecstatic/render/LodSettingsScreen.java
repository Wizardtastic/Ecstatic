package com.angryalchemist.ecstatic.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class LodSettingsScreen extends Screen {
    private static final int ROW_STRIDE = 32;
    private static final int WIDGET_HEIGHT = 20;
    private static final int CHECKBOX_WIDTH = 260;
    private static final int VIEWPORT_TOP = 50;
    private static final int FOOTER_HEIGHT = 44;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final double SCROLL_SPEED = 16.0;
    private static final int ROW_COUNT = 11;
    private static final int RENDER_DISTANCE_MIN_PERCENT = 25;
    private static final int RENDER_DISTANCE_MAX_PERCENT = 200;
    private static final int RENDER_DISTANCE_STEP_PERCENT = 5;
    private static final int FOG_FALLOFF_MIN_PERCENT = 25;
    private static final int FOG_FALLOFF_MAX_PERCENT = 300;
    private static final int FOG_FALLOFF_STEP_PERCENT = 5;
    private static final int FOG_INTENSITY_MIN_PERCENT = 0;
    private static final int FOG_INTENSITY_MAX_PERCENT = 100;
    private static final int FOG_INTENSITY_STEP_PERCENT = 5;
    private final LodSettingsConfig config = LodSettingsConfig.get();
    private double scrollAmount;
    private int maxScroll;

    public LodSettingsScreen() {
        super(Component.literal("Ecstatic Settings"));
    }

    protected void init() {
        int centerX = this.width / 2;
        int viewportTop = 50;
        int viewportBottom = this.height - 44;
        int viewportHeight = viewportBottom - viewportTop;
        int contentHeight = 352;
        this.maxScroll = Math.max(0, contentHeight - viewportHeight);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0, this.maxScroll);
        int y = viewportTop - (int)this.scrollAmount;
        y += 32;
        double renderDistanceInitialValue = valueFromPercent(Math.round(this.config.lodRenderDistanceScale() * 100.0F), 25, 200);
        int renderDistanceInitialPercent = percentFromValue(renderDistanceInitialValue, 25, 200, 5);
        AbstractSliderButton renderDistance = new AbstractSliderButton(
            centerX - 130, y, 260, 20, Component.literal("Render distance: " + renderDistanceInitialPercent + "%, going beyond 115% may cause lag"), renderDistanceInitialValue
        ) {
            protected void updateMessage() {
                this.setMessage(Component.literal("Render distance: " + LodSettingsScreen.percentFromValue(this.value, 40, 200, 5) + "%"));
            }

            protected void applyValue() {
                LodSettingsScreen.this.config.setLodRenderDistanceScale(LodSettingsScreen.percentFromValue(this.value, 40, 200, 5) / 100.0F);
            }
        };
        renderDistance.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(renderDistance);
        y += 32;
        final int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        double workerThreadsInitialValue = this.threadsValueFromCount(this.config.workerThreadCount(), maxThreads);
        int workerThreadsInitialCount = this.threadsFromValue(workerThreadsInitialValue, maxThreads);
        AbstractSliderButton workerThreads = new AbstractSliderButton(
            centerX - 130,
            y,
            260,
            20,
            Component.literal("Worker threads: " + workerThreadsInitialCount + " / " + maxThreads + " detected"),
            workerThreadsInitialValue
        ) {
            protected void updateMessage() {
                this.setMessage(
                    Component.literal("Worker threads: " + LodSettingsScreen.this.threadsFromValue(this.value, maxThreads) + " / " + maxThreads + " detected")
                );
            }

            protected void applyValue() {
                LodSettingsScreen.this.config.setWorkerThreadCount(LodSettingsScreen.this.threadsFromValue(this.value, maxThreads));
            }
        };
        workerThreads.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(workerThreads);
        y += 32;
        double fogFalloffInitialValue = valueFromPercent(Math.round(this.config.fogFalloffScale() * 100.0F), 25, 300);
        int fogFalloffInitialPercent = percentFromValue(fogFalloffInitialValue, 25, 300, 5);
        AbstractSliderButton fogFalloff = new AbstractSliderButton(
            centerX - 130, y, 260, 20, Component.literal("Fog falloff: " + fogFalloffInitialPercent + "%"), fogFalloffInitialValue
        ) {
            protected void updateMessage() {
                this.setMessage(Component.literal("Fog falloff: " + LodSettingsScreen.percentFromValue(this.value, 25, 300, 5) + "%"));
            }

            protected void applyValue() {
                LodSettingsScreen.this.config.setFogFalloffScale(LodSettingsScreen.percentFromValue(this.value, 25, 300, 5) / 100.0F);
            }
        };
        fogFalloff.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(fogFalloff);
        y += 32;
        double fogIntensityInitialValue = valueFromPercent(Math.round(this.config.fogIntensity() * 100.0F), 0, 100);
        int fogIntensityInitialPercent = percentFromValue(fogIntensityInitialValue, 0, 100, 5);
        AbstractSliderButton fogIntensity = new AbstractSliderButton(
            centerX - 130, y, 260, 20, Component.literal("Fog intensity: " + fogIntensityInitialPercent + "%"), fogIntensityInitialValue
        ) {
            protected void updateMessage() {
                this.setMessage(Component.literal("Fog intensity: " + LodSettingsScreen.percentFromValue(this.value, 0, 100, 5) + "%"));
            }

            protected void applyValue() {
                LodSettingsScreen.this.config.setFogIntensity(LodSettingsScreen.percentFromValue(this.value, 0, 100, 5) / 100.0F);
            }
        };
        fogIntensity.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(fogIntensity);
        y += 32;
        Button lightingButton = Button.builder(Component.literal("Lighting..."), b -> Minecraft.getInstance().setScreen(new LodLightingScreen(this)))
            .bounds(centerX - 130, y, 260, 20)
            .build();
        lightingButton.visible = y >= viewportTop && y + 20 <= viewportBottom;
        //this.addRenderableWidget(lightingButton); // technically a debug menu, average user should never need to access this
        //y += 32;
        Button debugButton = Button.builder(Component.literal("Debug Settings..."), b -> Minecraft.getInstance().setScreen(new LodDebugSettingsScreen(this)))
            .bounds(centerX - 130, y, 260, 20)
            .build();
        debugButton.visible = y >= viewportTop && y + 20 <= viewportBottom;
        this.addRenderableWidget(debugButton);
        int footerY = this.height - 44 + 20;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> this.onSave()).bounds(centerX - 105, footerY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(centerX + 5, footerY, 100, 20).build());
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

    private static int percentFromValue(double value, int minPercent, int maxPercent, int stepPercent) {
        int steps = (maxPercent - minPercent) / stepPercent;
        int stepIndex = Mth.clamp((int)Math.round(value * steps), 0, steps);
        return minPercent + stepIndex * stepPercent;
    }

    private static double valueFromPercent(int percent, int minPercent, int maxPercent) {
        int clamped = Mth.clamp(percent, minPercent, maxPercent);
        return (double)(clamped - minPercent) / (maxPercent - minPercent);
    }

    private int threadsFromValue(double value, int maxThreads) {
        int steps = Math.max(1, maxThreads - 1);
        int threads = 1 + (int)Math.round(value * steps);
        return Mth.clamp(threads, 1, maxThreads);
    }

    private double threadsValueFromCount(int threads, int maxThreads) {
        if (maxThreads <= 1) {
            return 0.0;
        }

        int clamped = Mth.clamp(threads, 1, maxThreads);
        return (double)(clamped - 1) / (maxThreads - 1);
    }

    private void onSave() {
        this.config.save();
        LodRenderer.rebuildAllMeshes();
    }
}
