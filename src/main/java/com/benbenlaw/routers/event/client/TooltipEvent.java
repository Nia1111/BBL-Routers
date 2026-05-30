package com.benbenlaw.routers.event.client;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.*;
import com.benbenlaw.routers.util.RoutersTags;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Routers.MOD_ID)
public class TooltipEvent {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String moreInfo = "";

        if (stack.getItem() instanceof UpgradeItem upgradeItem) {
            moreInfo = String.valueOf(upgradeItem.getExtractAmount());
        }

        addShiftTooltip(stack, event, RoutersBlocks.EXPORTER.get().asItem(), "tooltip.routers.exporter",
                String.valueOf(StartupConfig.defaultSpeedPerOperation.get()));

        addShiftTooltip(stack, event, RoutersBlocks.IMPORTER.get().asItem(), "tooltip.routers.importer");
        addShiftTooltip(stack, event, RoutersBlocks.DISTRIBUTOR.get().asItem(), "tooltip.routers.distributor");

        addShiftTooltip(stack, event, RoutersItems.CONNECTOR.get(), "tooltip.routers.connector");

        addShiftTooltip(stack, event, RoutersTags.Items.ITEM_UPGRADES, "tooltip.routers.item_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.FLUID_UPGRADES, "tooltip.routers.fluid_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.RF_UPGRADES, "tooltip.routers.energy_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.SPEED_UPGRADES, "tooltip.routers.speed_upgrade", moreInfo);

        addShiftTooltip(stack, event, RoutersItems.ROUND_ROBIN_UPGRADE.get(), "tooltip.routers.round_robin_upgrade");
        addShiftTooltip(stack, event, RoutersItems.DIMENSIONAL_UPGRADE.get(), "tooltip.routers.dimensional_upgrade");
        addShiftTooltip(stack, event, RoutersItems.BLACKLIST_UPGRADE.get(), "tooltip.routers.blacklist_upgrade");
        addShiftTooltip(stack, event, RoutersItems.IGNORE_NBT_UPGRADE.get(), "tooltip.routers.ignore_nbt_upgrade");

        //Connectors
        GlobalPos exporterPos = stack.get(RoutersDataComponents.EXPORTER_POSITION.value());
        GlobalPos importerPos = stack.get(RoutersDataComponents.IMPORTER_POSITION.value());

        if (exporterPos != null) {
            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.routers.wrench_exporter", exporterPos.pos().toShortString()).withStyle(ChatFormatting.BLUE));
            }
        }
        if (importerPos != null) {
            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.routers.wrench_importer", importerPos.pos().toShortString()).withStyle(ChatFormatting.BLUE));
            }
        }

        if (stack.getItem() instanceof FilterItem filterItem) {
            FilterType mode = filterItem.filterType;

            if (mode == FilterType.TAG) {
                Identifier tagInfo = stack.get(RoutersDataComponents.TAG_FILTER.value());
                if (tagInfo != null) {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.tag_filter", tagInfo.toString());
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.tag_filter_info");
                }
            }
            if (mode == FilterType.MOD) {
                String modInfo = stack.get(RoutersDataComponents.MOD_FILTER.value());
                if (modInfo != null) {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.mod_filter", modInfo);
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.mod_filter_info");
                }
            }
            if (mode == FilterType.STOCK) {
                StockFilter stockInfo = stack.get(RoutersDataComponents.STOCK_FILTER.value());
                if (stockInfo != null) {
                    ItemStack stockStack = stockInfo.stack();
                    int stockCount = stockInfo.amount();
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.stock_filter", stockStack.getHoverName().getString(), String.valueOf(stockCount));
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.stock_filter_info");
                }
            }
        }
    }


    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, Item item, String tooltipText, String additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, TagKey<Item> item, String tooltipText, String... additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, (Object[]) additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, Item item, String tooltipText, String... additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, (Object[]) additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }
}
