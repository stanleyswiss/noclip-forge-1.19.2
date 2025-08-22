package com.example.noclip;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToggleNoclipC2S(boolean enable) {
    public static void encode(ToggleNoclipC2S msg, FriendlyByteBuf buf) { buf.writeBoolean(msg.enable()); }
    public static ToggleNoclipC2S decode(FriendlyByteBuf buf) { return new ToggleNoclipC2S(buf.readBoolean()); }

    public static void handle(ToggleNoclipC2S msg, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer p = c.getSender();
            if (p != null) {
                p.getPersistentData().putBoolean("noclip_enabled", msg.enable());
                if (!msg.enable()) {
                    p.noPhysics = false;
                }
            }
        });
        c.setPacketHandled(true);
    }
}
