package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.ForecastPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Right-click opens the three-day forecast on the CLIENT. Server owns forecast calculation. */
public final class ForecastCalendarItem extends Item {
    public ForecastCalendarItem(Settings settings) { super(settings); }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient && player instanceof ServerPlayerEntity sp
                && ServerPlayNetworking.canSend(sp, ForecastPayload.ID)) {
            ServerPlayNetworking.send(sp, RegionalForecast.forPlayer(sp));
        }
        return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
    }
}

