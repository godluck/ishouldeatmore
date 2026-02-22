package com.gdlk.ishouldeatmore.event;

import com.gdlk.ishouldeatmore.data.LightningDamageScheduler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ServerTickHandler {

    private final LightningDamageScheduler lightningScheduler;

    public ServerTickHandler(LightningDamageScheduler lightningScheduler) {
        this.lightningScheduler = lightningScheduler;
    }

    @SubscribeEvent
    public void onServerTickPost(ServerTickEvent.Post event) {
        lightningScheduler.tick(event.getServer());
    }
}
