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
        
        // Ensure noPhysics is always true for noclip players
        player.noPhysics = true;
        
        // Prevent any damage or physics effects
        player.fallDistance = 0;
        player.setOnGround(false);
        player.setSwimming(false);
        
        // Clear any block-related states that might interfere
        player.resetFallDistance();
    }
    
    public static VoxelShape getCollisionShape(Player player, BlockState state, Level level, BlockPos pos) {
        if (isNoclipEnabled(player)) {
            // Return empty collision shape for noclip players
            return Shapes.empty();
        }
        return state.getCollisionShape(level, pos);
    }
}