package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.RadioPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Sneak + right-click with two AA batteries in the offhand to load the portable radio. */
public final class StormRadioItem extends Item {
    public StormRadioItem(Settings settings){super(settings);}
    public static int batteries(ItemStack stack){
        NbtComponent data=stack.get(DataComponentTypes.CUSTOM_DATA);
        return data==null?0:data.getNbt().getInt("rs_aa_batteries");
    }
    private static void load(ItemStack stack){
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA,stack,nbt->nbt.putInt("rs_aa_batteries",2));
    }
    @Override public TypedActionResult<ItemStack> use(World world,PlayerEntity player,Hand hand){
        ItemStack radio=player.getStackInHand(hand);
        if(world.isClient()) return TypedActionResult.success(radio,true);
        if(!(player instanceof ServerPlayerEntity sp))return TypedActionResult.pass(radio);
        if(player.isSneaking()) {
            ItemStack off=player.getStackInHand(hand==Hand.MAIN_HAND?Hand.OFF_HAND:Hand.MAIN_HAND);
            if(off.isOf(RegionalItems.AA_BATTERY)&&off.getCount()>=2&&batteries(radio)<2){
                off.decrement(2);load(radio);sp.sendMessage(Text.literal("§aStorm radio: two AA batteries loaded."),true);
            } else sp.sendMessage(Text.literal("§7Hold two AA batteries in the other hand, then sneak + use."),true);
            return TypedActionResult.success(radio,false);
        }
        if(batteries(radio)<2) {
            sp.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT,.5f,.5f);
            sp.sendMessage(Text.literal("§7[Radio] Insert 2 AA batteries to power on."),true);
            return TypedActionResult.success(radio,false);
        }
        RadioPayload payload=RadioAlerts.current(sp);
        if(!payload.active()) sp.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT,.7f,.6f);
        if(ServerPlayNetworking.canSend(sp,RadioPayload.ID))ServerPlayNetworking.send(sp,payload);
        return TypedActionResult.success(radio,false);
    }
}
