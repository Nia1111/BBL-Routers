package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemTransfer {

    public static int transferItems(ServerLevel level, ExporterBlockEntity exporter) {

        var state = level.getBlockState(exporter.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        ResourceHandler<ItemResource> source = exporter.getConnectedResources().getItemHandler(
                level, exporter.getBlockPos().relative(facing), facing.getOpposite()
        ).orElse(null);

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<ItemResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    var moved = ResourceHandlerUtil.moveFirst(source, target, resource -> {
                        boolean isWhitelist = !entity.isBlacklist();

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterItemHandler())) {
                            if (!entity.getFilterItemHandler().matchesItem(resource, isWhitelist, entity.isIgnoreNbt())) {
                                return false;
                            }
                        }

                        if (srvLevel.getBlockEntity(targetPos.pos()) instanceof ImporterBlockEntity importer) {
                            if (!ResourceHandlerUtil.isEmpty(importer.getFilterItemHandler())) {
                                return importer.getFilterItemHandler().matchesItem(resource.toStack(), isWhitelist);
                            }
                        }

                        return true;
                    }, entity.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES), null);

                    return moved != null && moved.amount() > 0;
                });
    }

    private static ResourceHandler<ItemResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        Direction facing = exporter.getBlockState().getValue(RouterBlock.FACING);
        return targetLevel.getCapability(Capabilities.Item.BLOCK, pos.pos().relative(facing), facing.getOpposite());
    }
}