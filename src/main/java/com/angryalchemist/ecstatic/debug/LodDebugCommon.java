package com.angryalchemist.ecstatic.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class LodDebugCommon {
    private LodDebugCommon() {
    }

    public static void sendMessage(Minecraft client, String message) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal("[Ecstatic] " + message), false);
        }
    }
}
