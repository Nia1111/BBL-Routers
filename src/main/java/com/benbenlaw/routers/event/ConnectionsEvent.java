package com.benbenlaw.routers.event;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Routers.MOD_ID)
public class ConnectionsEvent {

    public static GlobalPos mainExporterPos;
    public static GlobalPos mainImporterPos;

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {

        ItemStack heldItem = event.getItemStack();
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) return;

        GlobalPos clickedPos = GlobalPos.of(level.dimension(), event.getPos());
        BlockState blockState = level.getBlockState(clickedPos.pos());

        String clickedPosStr = clickedPos.pos().toShortString();

        if (heldItem.is(Tags.Items.TOOLS_WRENCH)) {

            if (player.isShiftKeyDown()) {
                player.swing(event.getHand(), true);

                if (blockState.is(RoutersBlocks.EXPORTER)) {
                    mainExporterPos = clickedPos;
                    mainImporterPos = null;
                    player.displayClientMessage(
                            Component.translatable("message.routers.exporter_selected", clickedPosStr),
                            true
                    );
                } else if (blockState.is(RoutersBlocks.IMPORTER)) {
                    mainImporterPos = clickedPos;
                    mainExporterPos = null;
                    player.displayClientMessage(
                            Component.translatable("message.routers.importer_selected", clickedPosStr),
                            true
                    );
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            if (mainExporterPos != null && blockState.is(RoutersBlocks.IMPORTER)) {
                connectExporterToImporter(level, player, mainExporterPos, clickedPos);
                return;
            }

            if (mainImporterPos != null && blockState.is(RoutersBlocks.EXPORTER)) {
                connectExporterToImporter(level, player, clickedPos, mainImporterPos);
                return;
            }

            player.displayClientMessage(
                    Component.translatable("message.routers.no_exporter_importer_selected"),
                    true
            );
        }
    }

    private static void connectExporterToImporter(
            Level level,
            Player player,
            GlobalPos exporterPos,
            GlobalPos importerPos
    ) {
        ServerLevel exporterLevel = level.getServer().getLevel(exporterPos.dimension());

        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) {
            player.displayClientMessage(
                    Component.translatable("message.routers.not_loaded"),
                    true
            );
            return;
        }

        if (!(exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter)) {
            player.displayClientMessage(
                    Component.translatable("message.routers.not_loaded"),
                    true
            );
            return;
        }

        exporter.addImporterPosition(importerPos);

        player.displayClientMessage(
                Component.translatable("message.routers.connected_exporter_to_importer",
                        importerPos.pos().toShortString()),
                true
        );
    }
}
