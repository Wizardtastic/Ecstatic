package com.angryalchemist.ecstatic;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("ecstatic")
public class EcstaticForge {
    public EcstaticForge() {
        Ecstatic.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        Ecstatic.onServerStarted(event.getServer().overworld());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Ecstatic.onServerStopping(event.getServer());
    }
}
