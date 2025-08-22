package com.example.noclip.client;

import com.example.noclip.NoclipMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = NoclipMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientInit {
    public static KeyMapping TOGGLE;

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        TOGGLE = new KeyMapping(
                "key.noclip.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "key.categories.movement");
        e.register(TOGGLE);
    }
}
