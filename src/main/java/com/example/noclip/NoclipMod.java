package com.example.noclip;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

@Mod(NoclipMod.MODID)
public final class NoclipMod {
    public static final String MODID = "noclip";
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
    }
}
