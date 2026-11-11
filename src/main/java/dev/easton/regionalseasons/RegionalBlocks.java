package dev.easton.regionalseasons;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Passive electric fan: cooling within a few blocks. Requires no tile ticking. */
public final class RegionalBlocks {
    public static final Block AC_FAN=Registry.register(Registries.BLOCK,Identifier.of("regionalseasons","ac_fan"),
            new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(2.5f)));
    public static final Item AC_FAN_ITEM=Registry.register(Registries.ITEM,Identifier.of("regionalseasons","ac_fan"),
            new BlockItem(AC_FAN,new Item.Settings()));
    private RegionalBlocks() {}
    public static void initialize(){ ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(e->e.add(AC_FAN_ITEM)); }
}
