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
        
        // Update noclip handler state
        NoclipHandler.setNoclipEnabled(e.player, enabled);

        if (enabled) {
            // Handle noclip movement and collision bypass
            NoclipHandler.handleNoclipMovement(e.player);
            
            // Authoritative server-side noclip so you truly pass through solids in single-player
            e.player.noPhysics = true;
            e.player.fallDistance = 0.0f;
            e.player.setOnGround(false);
            
            // Force position through blocks by disabling push from blocks
            e.player.pushthrough = 1.0f;
            
            // Override collision detection
            e.player.setDeltaMovement(e.player.getDeltaMovement());

            var ab = e.player.getAbilities();
            if (!tag.getBoolean("noclip_saved")) {
                tag.putBoolean("noclip_saved", true);
                tag.putBoolean("noclip_prev_mayfly", ab.mayfly);
                tag.putBoolean("noclip_prev_flying", ab.flying);
                tag.putBoolean("noclip_prev_invulnerable", ab.invulnerable);
            }
            ab.mayfly = true;
            ab.flying = true;
            ab.invulnerable = true;  // Prevent suffocation damage
            e.player.onUpdateAbilities();
        } else {
            // Restore abilities and physics
            if (tag.getBoolean("noclip_saved")) {
                var ab = e.player.getAbilities();
                ab.mayfly = tag.getBoolean("noclip_prev_mayfly");
                ab.flying = tag.getBoolean("noclip_prev_flying");
                ab.invulnerable = tag.getBoolean("noclip_prev_invulnerable");
                e.player.onUpdateAbilities();
                tag.putBoolean("noclip_saved", false);
            }
            e.player.noPhysics = false;
            e.player.pushthrough = 0.0f;
        }
    }
}