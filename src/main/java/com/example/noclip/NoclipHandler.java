package com.example.noclip;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoclipHandler {
    private static final Map<UUID, Boolean> noclipPlayers = new HashMap<>();
    
    public static void setNoclipEnabled(Player player, boolean enabled) {
        if (enabled) {
            noclipPlayers.put(player.getUUID(), true);
        } else {
            noclipPlayers.remove(player.getUUID());
        }
    }
    
    public static boolean isNoclipEnabled(Player player) {
        return noclipPlayers.getOrDefault(player.getUUID(), false);
    }
    
    public static void handleNoclipMovement(Player player) {
        if (!isNoclipEnabled(player)) return;
        
        // Bypass collision by setting collision shape to empty
        player.noPhysics = true;
        
        // Allow movement through blocks by overriding position
        Vec3 motion = player.getDeltaMovement();
        if (motion.lengthSqr() > 0) {
            Vec3 newPos = player.position().add(motion);
            
            // Force position update without collision check
            player.setPosRaw(newPos.x, newPos.y, newPos.z);
            player.setDeltaMovement(motion);
            
            // Prevent getting stuck in blocks
            player.fallDistance = 0;
            player.setOnGround(false);
        }
    }
    
    public static VoxelShape getCollisionShape(Player player, BlockState state, Level level, BlockPos pos) {
        if (isNoclipEnabled(player)) {
            // Return empty collision shape for noclip players
            return Shapes.empty();
        }
        return state.getCollisionShape(level, pos);
    }
}