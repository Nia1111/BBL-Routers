package com.benbenlaw.routers.event;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.item.RoutersDataComponents;
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

@EventBusSubscriber(modid = Routers.MOD_ID)
public class ConnectionsEvent {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        GlobalPos clickedPos = GlobalPos.of(level.dimension(), event.getPos());
        BlockState blockState = level.getBlockState(clickedPos.pos());

        if (!(blockState.getBlock() instanceof RouterBlock)) return;
        if (level.isClientSide()) return;

        ItemStack heldItem = event.getItemStack();
        Player player = event.getEntity();
        String clickedPosStr = clickedPos.pos().toShortString();

        if (heldItem.is(Tags.Items.TOOLS_WRENCH)) {

           GlobalPos mainExporterPos = heldItem.get(RoutersDataComponents.EXPORTER_POSITION.value());
           GlobalPos mainImporterPos = heldItem.get(RoutersDataComponents.IMPORTER_POSITION.value());

            if (player.isShiftKeyDown()) {
                player.swing(event.getHand(), true);

                if (blockState.is(RoutersBlocks.EXPORTER)) {
                    player.sendSystemMessage(
                            Component.translatable("message.routers.exporter_selected", clickedPosStr)
                    );
                    heldItem.set(RoutersDataComponents.EXPORTER_POSITION, clickedPos);

                } else if (blockState.is(RoutersBlocks.IMPORTER)) {
                    player.sendSystemMessage(
                            Component.translatable("message.routers.importer_selected", clickedPosStr)
                    );
                    heldItem.set(RoutersDataComponents.IMPORTER_POSITION, clickedPos);
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

            player.sendSystemMessage(
                    Component.translatable("message.routers.no_exporter_importer_selected")
            );
        }
    }

    private static void connectExporterToImporter(Level level, Player player, GlobalPos exporterPos, GlobalPos importerPos) {
        ServerLevel exporterLevel = level.getServer().getLevel(exporterPos.dimension());

        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) {
            player.sendSystemMessage(
                    Component.translatable("message.routers.not_loaded")
            );
            return;
        }

        if (!(exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter)) {
            player.sendSystemMessage(
                    Component.translatable("message.routers.not_loaded")
            );
            return;
        }

        boolean connected = exporter.toggleImporterPosition(importerPos);

        if (connected) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.routers.connected_exporter_to_importer",
                            importerPos.pos().toShortString()
                    )
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.routers.disconnected_exporter_from_importer",
                            importerPos.pos().toShortString()
                    )
            );
        }
    }
}