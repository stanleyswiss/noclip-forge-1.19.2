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
            
            // Force position updates to bypass collision
            var motion = mc.player.getDeltaMovement();
            var pos = mc.player.position();
            
            // Use direct position setting to bypass collision
            mc.player.setPos(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);
        } else {
            mc.player.noPhysics = false;
        }
    }
}