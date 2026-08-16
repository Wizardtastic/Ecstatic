package com.angryalchemist.ecstatic.client;

import com.angryalchemist.ecstatic.debug.LodDebugCommon;
import com.angryalchemist.ecstatic.debug.LodDebugCompare;
import com.angryalchemist.ecstatic.debug.LodDebugState;
import com.angryalchemist.ecstatic.render.BiomeStyleScreen;
import com.angryalchemist.ecstatic.render.LodRenderer;
import com.angryalchemist.ecstatic.render.LodSettingsScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.ViewportEvent.RenderFog;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "ecstatic", value = Dist.CLIENT)
public final class ForgeClientRenderEvents {
    private static final float FOG_DISABLE_MULTIPLIER = 100.0F;

    private ForgeClientRenderEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_PARTICLES) {
            LodRenderer.captureTrueRotation(new Matrix4f(event.getPoseStack().last().pose()));
            LodRenderer.render(event.getProjectionMatrix(), event.getCamera());
        }
    }

    @SubscribeEvent
    public static void onRenderFog(RenderFog event) {
        if (event.getMode() == FogMode.FOG_TERRAIN && event.getType() == FogType.NONE) {
            if (LodRenderer.isOverworldLoaded()) {
                Camera camera = event.getCamera();
                if (!(camera.getEntity() instanceof LivingEntity living && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS)))) {
                    Minecraft client = Minecraft.getInstance();
                    Vec3 pos = camera.getPosition();
                    boolean isFoggy = client.level.effects().isFoggyAt(Mth.floor(pos.x), Mth.floor(pos.y))
                        || client.gui.getBossOverlay().shouldCreateWorldFog();
                    if (!isFoggy) {
                        float farPlaneDistance = event.getFarPlaneDistance();
                        event.setNearPlaneDistance(farPlaneDistance * 100.0F);
                        event.setFarPlaneDistance(farPlaneDistance * 100.0F * 2.0F);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            Minecraft client = Minecraft.getInstance();
            boolean debugTools = LodDebugState.isEnabled();

            while (ForgeClientKeyBindings.CYCLE_VERTEX_FORMAT_KEY.consumeClick()) {
                if (debugTools) {
                    int override = LodDebugState.cycleVertexFormatOverride();
                    LodDebugCommon.sendMessage(client, "vertex format override: " + vertexFormatOverrideLabel(override));
                    LodRenderer.rebuildAllMeshes();
                }
            }

            while (ForgeClientKeyBindings.CYCLE_FORCED_LOD_KEY.consumeClick()) {
                if (debugTools) {
                    int level = LodDebugState.cycleForcedLevel();
                    LodDebugCommon.sendMessage(client, "forced level: " + (level == 0 ? "off (LOD1-4)" : "LOD" + level + " only"));
                }
            }

            while (ForgeClientKeyBindings.TOGGLE_REFERENCE_QUAD_KEY.consumeClick()) {
                if (debugTools) {
                    boolean enabled = LodDebugState.toggleReferenceQuad();
                    LodDebugCommon.sendMessage(client, "debug reference quad: " + (enabled ? "on (look down)" : "off"));
                }
            }

            while (ForgeClientKeyBindings.COMPARE_SAMPLE_KEY.consumeClick()) {
                if (debugTools) {
                    LodDebugCompare.compareAtPlayer(client);
                }
            }

            while (ForgeClientKeyBindings.OPEN_BIOME_STYLE_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new BiomeStyleScreen());
                }
            }

            while (ForgeClientKeyBindings.OPEN_SETTINGS_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new LodSettingsScreen());
                }
            }
        }
    }

    static String vertexFormatOverrideLabel(int override) {
        return switch (override) {
            case 1 -> "forced LIT";
            case 2 -> "forced CHEAP (position-color)";
            default -> "auto (config default / forced LIT if a shaderpack is active)";
        };
    }
}
