package dev.easton.regionalseasons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SeasonSyncPayload(int seasonIndex, int day, int seasonLength, int coldSnapDeciC, long radarSeed, boolean raining) implements CustomPayload {
    public static final Id<SeasonSyncPayload> ID = new Id<>(Identifier.of("regionalseasons", "calendar"));

    public static final PacketCodec<RegistryByteBuf, SeasonSyncPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeVarInt(payload.seasonIndex());
                buf.writeVarInt(payload.day());
                buf.writeVarInt(payload.seasonLength());
                buf.writeInt(payload.coldSnapDeciC());
                buf.writeLong(payload.radarSeed()); buf.writeBoolean(payload.raining());
            },
            buf -> new SeasonSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readInt(), buf.readLong(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
