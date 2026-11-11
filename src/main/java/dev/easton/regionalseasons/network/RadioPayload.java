package dev.easton.regionalseasons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RadioPayload(String message,String level,int estimatedSnowLayers,boolean active) implements CustomPayload {
    public static final Id<RadioPayload> ID=new Id<>(Identifier.of("regionalseasons","storm_radio"));
    public static final PacketCodec<RegistryByteBuf,RadioPayload> CODEC=PacketCodec.of((p,b)->{
        b.writeString(p.message()); b.writeString(p.level());b.writeVarInt(p.estimatedSnowLayers());b.writeBoolean(p.active());
    },b->new RadioPayload(b.readString(),b.readString(),b.readVarInt(),b.readBoolean()));
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
