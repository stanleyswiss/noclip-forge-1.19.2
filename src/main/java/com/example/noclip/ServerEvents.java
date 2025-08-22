package com.example.noclip;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityDimensions;
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
            // Set player to have no collision box
            e.player.noPhysics = true;
            e.player.fallDistance = 0.0f;
            e.player.setOnGround(false);
            
            // Make player dimension tiny to avoid collision
            if (!tag.getBoolean("noclip_dims_saved")) {
                tag.putFloat("noclip_prev_width", e.player.dimensions.width);
                tag.putFloat("noclip_prev_height", e.player.dimensions.height);
                tag.putBoolean("noclip_dims_saved", true);
                
                // Set to very small dimensions
                e.player.dimensions = EntityDimensions.fixed(0.01f, 0.01f);
                e.player.refreshDimensions();
            }
            
            // Force movement without collision checks
            Vec3 motion = e.player.getDeltaMovement();
            if (motion.lengthSqr() > 0) {
                Vec3 pos = e.player.position();
                e.player.teleportTo(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);
                e.player.setDeltaMovement(motion);
            }

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
            
            // Restore dimensions
            if (tag.getBoolean("noclip_dims_saved")) {
                float width = tag.getFloat("noclip_prev_width");
                float height = tag.getFloat("noclip_prev_height");
                e.player.dimensions = EntityDimensions.fixed(width, height);
                e.player.refreshDimensions();
                tag.putBoolean("noclip_dims_saved", false);
            }
            
            e.player.noPhysics = false;
        }
    }
}