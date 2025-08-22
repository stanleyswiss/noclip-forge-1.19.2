package com.example.noclip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(NoclipMod.MODID)
public final class NoclipMod {
    public static final String MODID = "noclip";
    private static boolean noclipEnabled = false;
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> "1", "1"::equals, "1"::equals);

    public NoclipMod() {
        int id = 0;
        CHANNEL.messageBuilder(ToggleNoclipC2S.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleNoclipC2S::encode)
                .decoder(ToggleNoclipC2S::decode)
                .consumerMainThread(ToggleNoclipC2S::handle)
                .add();
                
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        // Toggle with N key (GLFW_KEY_N = 78)
        if (event.getKey() == 78 && event.getAction() == 1) {
            toggleNoclip(mc.player);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof LocalPlayer)) return;
        
        LocalPlayer player = (LocalPlayer) event.player;
        
        if (noclipEnabled) {
            // Force noclip state every tick
            player.noPhysics = true;
            player.setNoGravity(true);
            
            // Ensure flight capabilities
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void toggleNoclip(LocalPlayer player) {
        noclipEnabled = !noclipEnabled;
        
        if (noclipEnabled) {
            // Enable noclip
            player.noPhysics = true;
            player.setNoGravity(true);
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.displayClientMessage(Component.literal("§aNoclip: ON"), true);
        } else {
            // Disable noclip
            player.noPhysics = false;
            player.setNoGravity(false);
            
            // Only disable flight if not in creative
            if (!player.getAbilities().instabuild) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            
            player.displayClientMessage(Component.literal("§cNoclip: OFF"), true);
        }
        
        player.onUpdateAbilities();
    }
}
