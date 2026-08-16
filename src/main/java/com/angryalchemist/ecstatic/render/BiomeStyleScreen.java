package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.Constants;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class BiomeStyleScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int SWATCH_SIZE = 20;
    private static final int FIELD_WIDTH = 80;
    private static final int GROUP_ROW_Y = 40;
    private static final int WATER_COLUMN_GAP = 20;
    private final BiomeStyleConfig config = BiomeStyleConfig.get();
    private TreeStyle.Group group = TreeStyle.Group.DEFAULT;
    private EditBox trunkTintBox;
    private EditBox foliageTintBox;
    private EditBox groundTintBox;
    private Checkbox groundTintEnabledCheckbox;
    private Button trunkTextureButton;
    private Button foliageTextureButton;
    private EditBox waterColorBox;
    private EditBox waterOpacityBox;
    private EditBox snowHeightBox;
    private int waterLabelY;

    public BiomeStyleScreen() {
        super(Component.literal("Biome Style Editor"));
    }

    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.cycleGroup(-1)).bounds(centerX - 110, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.cycleGroup(1)).bounds(centerX + 90, y, 20, 20).build());
        int leftColumnX = centerX - 60 - 80 - 20 - 20;
        y += 36;
        int trunkRowY = y;
        this.trunkTintBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, centerX - 60, y, 80, 20, Component.literal("Trunk Tint")));
        this.trunkTintBox.setMaxLength(7);
        this.trunkTintBox.setValue(toHex(this.config.trunkTint(this.group)));
        this.trunkTintBox.setResponder(value -> applyHex(value, rgb -> this.config.entry(this.group).trunkTint = rgb));
        y += 24;
        int foliageRowY = y;
        this.foliageTintBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, centerX - 60, y, 80, 20, Component.literal("Foliage Tint")));
        this.foliageTintBox.setMaxLength(7);
        this.foliageTintBox.setValue(toHex(this.config.foliageTint(this.group)));
        this.foliageTintBox.setResponder(value -> applyHex(value, rgb -> this.config.entry(this.group).foliageTint = rgb));
        y += 24;
        int groundRowY = y;
        this.groundTintBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, centerX - 60, y, 80, 20, Component.literal("Ground Tint")));
        this.groundTintBox.setMaxLength(7);
        this.groundTintBox.setValue(toHex(this.config.entry(this.group).groundTint));
        this.groundTintBox.setResponder(value -> applyHex(value, rgb -> this.config.entry(this.group).groundTint = rgb));
        this.groundTintEnabledCheckbox = (Checkbox)this.addRenderableWidget(
            new Checkbox(
                this.groundTintBox.getX() + this.groundTintBox.getWidth() + 20 + 16,
                groundRowY,
                60,
                20,
                Component.literal("Tint"),
                this.config.groundTintEnabled(this.group)
            ) {
                public void onPress() {
                    super.onPress();
                    BiomeStyleScreen.this.config.setGroundTintEnabled(BiomeStyleScreen.this.group, this.selected());
                }
            }
        );
        this.waterLabelY = trunkRowY + 6;
        this.waterColorBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, leftColumnX, foliageRowY, 80, 20, Component.literal("Water Color")));
        this.waterColorBox.setMaxLength(7);
        this.waterColorBox.setValue(toHex(this.config.waterColor()));
        this.waterColorBox.setResponder(value -> applyHex(value, this.config::setWaterColor));
        this.waterOpacityBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, leftColumnX, groundRowY, 80, 20, Component.literal("Water Opacity")));
        this.waterOpacityBox.setMaxLength(3);
        this.waterOpacityBox.setValue(toPercent(this.config.waterAlpha()));
        this.waterOpacityBox.setResponder(this::applyWaterOpacityPercent);
        int rightColumnX = this.groundTintEnabledCheckbox.getX() + this.groundTintEnabledCheckbox.getWidth() + 20;
        this.snowHeightBox = (EditBox)this.addRenderableWidget(new EditBox(this.font, rightColumnX, foliageRowY, 80, 20, Component.literal("Snow Height")));
        this.snowHeightBox.setMaxLength(3);
        this.snowHeightBox.setValue(String.valueOf(Math.round(this.config.snowHeightPercent())));
        this.snowHeightBox.setResponder(this::applySnowHeightPercent);
        y += 36;
        this.trunkTextureButton = (Button)this.addRenderableWidget(
            Button.builder(this.trunkTextureLabel(), b -> this.onTrunkTextureButton()).bounds(centerX - 100, y, 150, 20).build()
        );
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), b -> this.onClearTrunkTexture()).bounds(centerX + 55, y, 50, 20).build());
        y += 24;
        this.foliageTextureButton = (Button)this.addRenderableWidget(
            Button.builder(this.foliageTextureLabel(), b -> this.onFoliageTextureButton()).bounds(centerX - 100, y, 150, 20).build()
        );
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), b -> this.onClearFoliageTexture()).bounds(centerX + 55, y, 50, 20).build());
        y += 36;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> this.onSave()).bounds(centerX - 105, y, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(centerX + 5, y, 100, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        guiGraphics.drawCenteredString(this.font, titleCase(this.group.name()), this.width / 2, 46, 16777215);
        this.drawSwatch(guiGraphics, this.trunkTintBox, this.config.trunkTint(this.group));
        this.drawSwatch(guiGraphics, this.foliageTintBox, this.config.foliageTint(this.group));
        this.drawSwatch(guiGraphics, this.groundTintBox, this.config.entry(this.group).groundTint);
        int waterColumnCenterX = this.waterColorBox.getX() + this.waterColorBox.getWidth() / 2;
        guiGraphics.drawCenteredString(this.font, "Water", waterColumnCenterX, this.waterLabelY, 16777215);
        this.drawSwatch(guiGraphics, this.waterColorBox, this.config.waterColor());
        int snowColumnCenterX = this.snowHeightBox.getX() + this.snowHeightBox.getWidth() / 2;
        guiGraphics.drawCenteredString(this.font, "Snow %", snowColumnCenterX, this.waterLabelY, 16777215);
    }

    private void drawSwatch(GuiGraphics guiGraphics, EditBox box, int rgb) {
        int x0 = box.getX() + box.getWidth() + 8;
        int y0 = box.getY();
        guiGraphics.fill(x0, y0, x0 + 20, y0 + box.getHeight(), 0xFF000000 | rgb & 16777215);
    }

    private void cycleGroup(int delta) {
        TreeStyle.Group[] groups = TreeStyle.Group.values();
        this.group = groups[Math.floorMod(this.group.ordinal() + delta, groups.length)];
        this.rebuildWidgets();
    }

    private void onTrunkTextureButton() {
        pickPngFile(path -> {
            try {
                this.config.setTrunkTexture(this.group, path);
            } catch (IOException e) {
                Constants.LOG.error("Failed to set trunk texture for {}", this.group, e);
            }

            this.trunkTextureButton.setMessage(this.trunkTextureLabel());
        });
    }

    private void onFoliageTextureButton() {
        pickPngFile(path -> {
            try {
                this.config.setFoliageTexture(this.group, path);
            } catch (IOException e) {
                Constants.LOG.error("Failed to set foliage texture for {}", this.group, e);
            }

            this.foliageTextureButton.setMessage(this.foliageTextureLabel());
        });
    }

    private void onClearTrunkTexture() {
        this.config.clearTrunkTexture(this.group);
        this.trunkTextureButton.setMessage(this.trunkTextureLabel());
    }

    private void onClearFoliageTexture() {
        this.config.clearFoliageTexture(this.group);
        this.foliageTextureButton.setMessage(this.foliageTextureLabel());
    }

    private void onSave() {
        this.config.save();
        LodRenderer.rebuildAllMeshes();
    }

    private Component trunkTextureLabel() {
        return Component.literal("Trunk: " + (this.config.entry(this.group).trunkTexture != null ? "Custom" : "Default"));
    }

    private Component foliageTextureLabel() {
        return Component.literal("Foliage: " + (this.config.entry(this.group).foliageTexture != null ? "Custom" : "Default"));
    }

    private static void pickPngFile(Consumer<Path> onPicked) {
        Thread thread = new Thread(() -> {
            FileDialog dialog = new FileDialog((Frame)null, "Select PNG Texture", 0);
            dialog.setFile("*.png");
            dialog.setVisible(true);
            String file = dialog.getFile();
            String dir = dialog.getDirectory();
            dialog.dispose();
            if (file != null) {
                Path path = Paths.get(dir, file);
                Minecraft.getInstance().execute(() -> onPicked.accept(path));
            }
        }, "ecstatic-texture-picker");
        thread.setDaemon(true);
        thread.start();
    }

    private void applyWaterOpacityPercent(String value) {
        try {
            float percent = Float.parseFloat(value.trim());
            this.config.setWaterAlpha(percent / 100.0F);
        } catch (NumberFormatException var3) {
        }
    }

    private static String toPercent(float alpha01) {
        return String.valueOf(Math.round(alpha01 * 100.0F));
    }

    private void applySnowHeightPercent(String value) {
        try {
            float percent = Float.parseFloat(value.trim());
            this.config.setSnowHeightPercent(percent);
        } catch (NumberFormatException var3) {
        }
    }

    private static void applyHex(String value, IntConsumer setter) {
        Integer parsed = parseHex(value);
        if (parsed != null) {
            setter.accept(parsed);
        }
    }

    private static Integer parseHex(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }

        if (trimmed.length() != 6) {
            return null;
        }

        try {
            return Integer.parseInt(trimmed, 16) & 16777215;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String toHex(int rgb) {
        return String.format("%06X", rgb & 16777215);
    }

    private static String titleCase(String enumName) {
        String[] words = enumName.toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return result.toString();
    }
}
