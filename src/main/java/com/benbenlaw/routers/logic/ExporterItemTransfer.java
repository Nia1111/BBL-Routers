package com.benbenlaw.routers.logic;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.ConnectedResources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class ExporterItemTransfer {

    public static int transferItems(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            BlockPos exporterPos,
            ConnectedResources connectedResources,
            List<GlobalPos> importerPositions,
            FilterItemHandler filter,
            boolean roundRobin,
            int lastImporterIndex,
            int amountToTransfer
    ) {
        if (importerPositions == null || importerPositions.isEmpty()) {
            return lastImporterIndex;
        }

        ResourceHandler<ItemResource> source =
                getSourceHandler(exporterLevel, exporterPos, connectedResources);

        if (source == null || source.size() == 0) {
            return lastImporterIndex;
        }

        return roundRobin
                ? insertRoundRobin(exporterLevel, exporter, source, importerPositions, filter, lastImporterIndex, amountToTransfer)
                : insertOrdered(exporterLevel, exporter, source, importerPositions, filter, amountToTransfer, lastImporterIndex);
    }

    private static ResourceHandler<ItemResource> getSourceHandler(
            ServerLevel level,
            BlockPos exporterPos,
            ConnectedResources connectedResources
    ) {
        BlockState exporterState = level.getBlockState(exporterPos);
        Direction facing = exporterState.getValue(RouterBlock.FACING);

        return connectedResources
                .getItemHandler(level, exporterPos.relative(facing), facing.getOpposite())
                .orElse(null);
    }


    private static int insertRoundRobin(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            ResourceHandler<ItemResource> source,
            List<GlobalPos> importers,
            FilterItemHandler filter,
            int startIndex,
            int amountToTransfer
    ) {
        int importerCount = importers.size();
        int index = startIndex;

        for (int slot = 0; slot < source.size(); slot++) {
            ItemResource resource = source.getResource(slot);
            if (resource.isEmpty()) continue;

            int safeAmount = Math.min(source.getAmountAsInt(slot), amountToTransfer);
            if (safeAmount <= 0) continue;

            for (int offset = 0; offset < importerCount; offset++) {
                int currentIndex = (index + offset) % importerCount;
                if (tryMoveToImporter(
                        exporterLevel,
                        exporter,
                        source,
                        slot,
                        importers.get(currentIndex),
                        filter,
                        safeAmount
                )) {
                    return (currentIndex + 1) % importerCount;
                }
            }
        }

        return startIndex;
    }


    private static int insertOrdered(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            ResourceHandler<ItemResource> source,
            List<GlobalPos> importers,
            FilterItemHandler filter,
            int amountToTransfer,
            int lastIndex
    ) {
        for (int slot = 0; slot < source.size(); slot++) {

            int safeAmount = Math.min(source.getAmountAsInt(slot), amountToTransfer);
            if (safeAmount <= 0) continue;

            for (GlobalPos importerPos : importers) {
                if (tryMoveToImporter(exporterLevel, exporter, source, slot, importerPos, filter, safeAmount)) {
                    return lastIndex;
                }
            }
        }

        return lastIndex;
    }

    private static boolean tryMoveToImporter(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            ResourceHandler<ItemResource> source,
            int slot,
            GlobalPos importerPos,
            FilterItemHandler exporterFilter,
            int amount
    ) {
        MinecraftServer server = exporterLevel.getServer();
        ServerLevel importerLevel = server.getLevel(importerPos.dimension());

        if (importerLevel == null) return false;

        // Check dimensional upgrade
        if (!exporterLevel.dimension().equals(importerPos.dimension()) && !exporter.canDoDimensionalTravel()) {
            return false;
        }

        if (!importerLevel.isLoaded(importerPos.pos())) return false;

        if (!(importerLevel.getBlockEntity(importerPos.pos()) instanceof ImporterBlockEntity importer)) {
            return false;
        }

        BlockState importerState = importerLevel.getBlockState(importerPos.pos());
        if (!(importerState.getBlock() instanceof RouterBlock)) return false;

        Direction facing = importerState.getValue(RouterBlock.FACING);

        ResourceHandler<ItemResource> target =
                importerLevel.getCapability(
                        Capabilities.Item.BLOCK,
                        importerPos.pos().relative(facing),
                        facing.getOpposite()
                );

        if (target == null) return false;

        FilterItemHandler importerFilter = importer.getFilterItemHandler();

        ResourceStack<ItemResource> moved =
                ResourceHandlerUtil.moveFirst(
                        source,
                        target,
                        resource -> {
                            boolean isWhitelist = !exporter.isBlacklist();

                            // If exporter filter is NOT empty, enforce it
                            if (!ResourceHandlerUtil.isEmpty(exporterFilter)) {
                                if (!exporterFilter.matchesItem(resource, isWhitelist, exporter.isIgnoreNbt())) {
                                    return false;
                                }
                            }

                            if (ResourceHandlerUtil.isEmpty(importerFilter)) {
                                return true;
                            }

                            return importerFilter.matchesItem(resource.toStack(), isWhitelist);
                        },
                        amount,
                        null
                );

        return moved != null && moved.amount() > 0;
    }


}