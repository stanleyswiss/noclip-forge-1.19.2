package com.example.noclip.client;

import com.example.noclip.NoclipMod;
import com.example.noclip.ToggleNoclipC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NoclipMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || ClientInit.TOGGLE == null) return;

        // Toggle key
        if (ClientInit.TOGGLE.consumeClick()) {
            enabled = !enabled;
            NoclipMod.CHANNEL.sendToServer(new ToggleNoclipC2S(enabled));
            mc.player.displayClientMessage(Component.literal("Noclip: " + (enabled ? "ON" : "OFF")), true);
        }

        // Client-side enforcement for smooth phasing (prevents visual pushback)
        if (enabled) {
            mc.player.noPhysics = true;
            mc.player.fallDistance = 0.0f;
            mc.player.setOnGround(false);
            
            // Make client-side bounding box tiny
            var pos = mc.player.position();
            mc.player.setBoundingBox(net.minecraft.world.phys.AABB.ofSize(pos, 0.001, 0.001, 0.001));
            
            // Force position updates to bypass collision
            var motion = mc.player.getDeltaMovement();
            
            if (motion.lengthSqr() > 0) {
                double newX = pos.x + motion.x;
                double newY = pos.y + motion.y;
                double newZ = pos.z + motion.z;
                
                // Multiple position setting methods for reliability
                mc.player.setPosRaw(newX, newY, newZ);
                mc.player.setPos(newX, newY, newZ);
                mc.player.setBoundingBox(net.minecraft.world.phys.AABB.ofSize(new net.minecraft.world.phys.Vec3(newX, newY, newZ), 0.001, 0.001, 0.001));
            }
        } else {
            mc.player.noPhysics = false;
            // Restore normal bounding box
            var pos = mc.player.position();
            mc.player.setBoundingBox(net.minecraft.world.phys.AABB.ofSize(pos, 0.6, 1.8, 0.6));
        }
    }
}