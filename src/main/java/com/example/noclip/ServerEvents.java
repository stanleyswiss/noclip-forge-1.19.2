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
            // Set player to have no collision box
            e.player.noPhysics = true;
            e.player.fallDistance = 0.0f;
            e.player.setOnGround(false);
            
            // Spectator-like movement
            Vec3 motion = e.player.getDeltaMovement();
            Vec3 pos = e.player.position();
            
            // Direct position update bypassing collision
            double newX = pos.x + motion.x;
            double newY = pos.y + motion.y;
            double newZ = pos.z + motion.z;
            
            // Set position directly without collision checks
            e.player.setPosRaw(newX, newY, newZ);
            e.player.setBoundingBox(AABB.ofSize(new Vec3(newX, newY, newZ), 0.1, 0.1, 0.1));
            
            // Keep motion for smooth movement
            e.player.setDeltaMovement(motion);
            
            // Clear block position to prevent collision checks
            e.player.blockPosition = new BlockPos(newX, newY, newZ);

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