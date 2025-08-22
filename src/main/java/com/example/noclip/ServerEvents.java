package com.example.noclip;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NoclipMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level.isClientSide) return;

        var tag = e.player.getPersistentData();
        boolean enabled = tag.getBoolean("noclip_enabled");

        if (enabled) {
            e.player.noPhysics = true;
            e.player.fallDistance = 0.0f;
            e.player.setOnGround(false);

            var ab = e.player.getAbilities();
            if (!tag.getBoolean("noclip_saved")) {
                tag.putBoolean("noclip_saved", true);
                tag.putBoolean("noclip_prev_mayfly", ab.mayfly);
                tag.putBoolean("noclip_prev_flying", ab.flying);
            }
            ab.mayfly = true;
            ab.flying = true;
            e.player.onUpdateAbilities();
        } else {
            if (tag.getBoolean("noclip_saved")) {
                var ab = e.player.getAbilities();
                ab.mayfly = tag.getBoolean("noclip_prev_mayfly");
                ab.flying = tag.getBoolean("noclip_prev_flying");
                e.player.onUpdateAbilities();
                tag.putBoolean("noclip_saved", false);
            }
            e.player.noPhysics = false;
        }
    }
}
