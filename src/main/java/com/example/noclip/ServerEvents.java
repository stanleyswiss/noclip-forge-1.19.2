package com.example.noclip;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
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
            // Complete physics override
            e.player.noPhysics = true;
            e.player.fallDistance = 0.0f;
            e.player.setOnGround(false);
            
            // Get input-based movement direction from abilities
            Vec3 motion = e.player.getDeltaMovement();
            Vec3 pos = e.player.position();
            
            // Normalize flying speed to prevent speed increase
            double speed = 0.1; // Normal creative flying speed
            if (motion.lengthSqr() > 0) {
                motion = motion.normalize().scale(speed);
            }
            
            // Completely bypass collision by teleporting in small steps
            if (motion.lengthSqr() > 0) {
                // Break movement into tiny steps to ensure we pass through any block
                int steps = 10;
                double stepX = motion.x / steps;
                double stepY = motion.y / steps;
                double stepZ = motion.z / steps;
                
                for (int i = 0; i < steps; i++) {
                    double newX = pos.x + stepX * (i + 1);
                    double newY = pos.y + stepY * (i + 1);
                    double newZ = pos.z + stepZ * (i + 1);
                    
                    // Force teleport to each step position
                    e.player.teleportTo(newX, newY, newZ);
                }
                
                // Set final position and ensure tiny bounding box
                e.player.setBoundingBox(AABB.ofSize(e.player.position(), 0.0001, 0.0001, 0.0001));
                
                // Clear motion to prevent additional movement
                e.player.setDeltaMovement(Vec3.ZERO);
            } else {
                // Even when not moving, keep tiny bounding box
                e.player.setBoundingBox(AABB.ofSize(pos, 0.0001, 0.0001, 0.0001));
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
            
            // Restore normal bounding box
            Vec3 pos = e.player.position();
            e.player.setBoundingBox(AABB.ofSize(pos, 0.6, 1.8, 0.6));
            
            e.player.noPhysics = false;
        }
    }
}