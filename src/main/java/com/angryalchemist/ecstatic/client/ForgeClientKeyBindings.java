package com.angryalchemist.ecstatic.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "ecstatic", value = Dist.CLIENT, bus = Bus.MOD)
public final class ForgeClientKeyBindings {
    public static final KeyMapping CYCLE_VERTEX_FORMAT_KEY = new KeyMapping("key.ecstatic.cycle_vertex_format", Type.KEYSYM, 298, "key.categories.ecstatic");
    public static final KeyMapping CYCLE_FORCED_LOD_KEY = new KeyMapping("key.ecstatic.cycle_forced_lod", Type.KEYSYM, 299, "key.categories.ecstatic");
    public static final KeyMapping TOGGLE_REFERENCE_QUAD_KEY = new KeyMapping("key.ecstatic.toggle_reference_quad", Type.KEYSYM, 297, "key.categories.ecstatic");
    public static final KeyMapping COMPARE_SAMPLE_KEY = new KeyMapping("key.ecstatic.compare_sample", Type.KEYSYM, 296, "key.categories.ecstatic");
    public static final KeyMapping OPEN_BIOME_STYLE_KEY = new KeyMapping("key.ecstatic.open_biome_style", Type.KEYSYM, 295, "key.categories.ecstatic");
    public static final KeyMapping OPEN_SETTINGS_KEY = new KeyMapping("key.ecstatic.open_settings", Type.KEYSYM, 293, "key.categories.ecstatic");

    private ForgeClientKeyBindings() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CYCLE_VERTEX_FORMAT_KEY);
        event.register(CYCLE_FORCED_LOD_KEY);
        event.register(TOGGLE_REFERENCE_QUAD_KEY);
        event.register(COMPARE_SAMPLE_KEY);
        event.register(OPEN_BIOME_STYLE_KEY);
        event.register(OPEN_SETTINGS_KEY);
    }
}
