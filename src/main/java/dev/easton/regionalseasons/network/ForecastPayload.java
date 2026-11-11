package dev.easton.regionalseasons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ForecastPayload(String season, int day, long year, String current,
                              String[] dayNames, int[] highF, int[] lowF, int[] precipitationPct) implements CustomPayload {
    public static final Id<ForecastPayload> ID = new Id<>(Identifier.of("regionalseasons", "forecast"));
    public static final PacketCodec<RegistryByteBuf, ForecastPayload> CODEC = PacketCodec.of((payload, b) -> {
        b.writeString(payload.season()); b.writeVarInt(payload.day()); b.writeLong(payload.year()); b.writeString(payload.current());
        for (int i=0;i<3;i++) { b.writeString(payload.dayNames()[i]); b.writeVarInt(payload.highF()[i]); b.writeVarInt(payload.lowF()[i]); b.writeVarInt(payload.precipitationPct()[i]); }
    }, b -> {
        String season = b.readString(); int day = b.readVarInt(); long year = b.readLong(); String current = b.readString();
        String[] names = new String[3]; int[] hi = new int[3], lo = new int[3], odds = new int[3];
        for (int i=0;i<3;i++) { names[i] = b.readString(); hi[i] = b.readVarInt(); lo[i] = b.readVarInt(); odds[i] = b.readVarInt(); }
        return new ForecastPayload(season, day, year, current, names, hi, lo, odds);
    });
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}

