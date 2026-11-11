package dev.easton.regionalseasons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** 32 x 32 coarse radar cells: tiny packet; no 2,000-block chunk loading. */
public record RadarPayload(int centerX,int centerZ,int centerF,boolean raining,byte[] intensity,byte[] deltaF) implements CustomPayload {
    public static final Id<RadarPayload> ID=new Id<>(Identifier.of("regionalseasons","weather_radar"));
    public static final PacketCodec<RegistryByteBuf,RadarPayload> CODEC=PacketCodec.of((v,b)->{
        b.writeInt(v.centerX());b.writeInt(v.centerZ());b.writeInt(v.centerF());b.writeBoolean(v.raining());
        b.writeByteArray(v.intensity()); b.writeByteArray(v.deltaF());
    },b->new RadarPayload(b.readInt(),b.readInt(),b.readInt(),b.readBoolean(),b.readByteArray(1024),b.readByteArray(1024)));
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
