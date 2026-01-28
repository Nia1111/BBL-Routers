package com.benbenlaw.routers.logic;

import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.ConnectedResources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class ExporterFluidTransfer {

    public static int transferFluids(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            BlockPos exporterPos,
            ConnectedResources connectedResources,
            List<GlobalPos> importerPositions,
            FilterFluidHandler filter,
            boolean roundRobin,
            int lastImporterIndex,
            int amountToTransfer
    ) {
        if (importerPositions == null || importerPositions.isEmpty()) {
            return lastImporterIndex;
        }

        ResourceHandler<FluidResource> source =
                getSourceHandler(exporterLevel, exporterPos, connectedResources);

        if (source == null || source.size() == 0) {
            return lastImporterIndex;
        }

        return roundRobin
                ? insertRoundRobin(exporterLevel, exporter, source, importerPositions, filter, lastImporterIndex, amountToTransfer)
                : insertOrdered(exporterLevel, exporter, source, importerPositions, filter, amountToTransfer, lastImporterIndex);
    }

    private static ResourceHandler<FluidResource> getSourceHandler(
            ServerLevel level,
            BlockPos exporterPos,
            ConnectedResources connectedResources
    ) {
        BlockState state = level.getBlockState(exporterPos);
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();

        return connectedResources
                .getFluidHandler(level, exporterPos.relative(state.getValue(RouterBlock.FACING)), facing)
                .orElse(null);
    }

    private static int insertRoundRobin(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            ResourceHandler<FluidResource> source,
            List<GlobalPos> importers,
            FilterFluidHandler filter,
            int startIndex,
            int amountToTransfer
    ) {
        int importerCount = importers.size();
        int index = startIndex;

        for (int slot = 0; slot < source.size(); slot++) {
            FluidResource resource = source.getResource(slot);
            if (resource.isEmpty()) continue;

            int safeAmount = Math.min(source.getAmountAsInt(slot), amountToTransfer);
            if (safeAmount <= 0) continue;

            for (int offset = 0; offset < importerCount; offset++) {
                int currentIndex = (index + offset) % importerCount;
                if (tryMoveToImporter(exporterLevel, exporter, source, slot, importers.get(currentIndex), filter, safeAmount)) {
                    return (currentIndex + 1) % importerCount;
                }
            }
        }

        return startIndex;
    }

    private static int insertOrdered(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            ResourceHandler<FluidResource> source,
            List<GlobalPos> importers,
            FilterFluidHandler filter,
            int amountToTransfer,
            int lastIndex
    ) {
        for (int slot = 0; slot < source.size(); slot++) {
            FluidResource resource = source.getResource(slot);
            if (resource.isEmpty()) continue;

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
            ResourceHandler<FluidResource> source,
            int sourceSlot,
            GlobalPos importerPos,
            FilterFluidHandler exporterFilter,
            int maxAmount
    ) {
        MinecraftServer server = exporterLevel.getServer();
        ServerLevel importerLevel = server.getLevel(importerPos.dimension());
        if (importerLevel == null) return false;

        // Dimensional travel check
        if (!importerLevel.dimension().equals(exporterLevel.dimension()) && !exporter.canDoDimensionalTravel()) {
            return false;
        }

        if (!importerLevel.isLoaded(importerPos.pos())) return false;

        if (!(importerLevel.getBlockEntity(importerPos.pos()) instanceof ImporterBlockEntity importer)) {
            return false;
        }

        BlockState state = importerLevel.getBlockState(importerPos.pos());
        if (!(state.getBlock() instanceof RouterBlock)) return false;

        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();

        ResourceHandler<FluidResource> target =
                importerLevel.getCapability(
                        Capabilities.Fluid.BLOCK,
                        importerPos.pos().relative(state.getValue(RouterBlock.FACING)),
                        facing
                );
        if (target == null) return false;

        FluidResource resource = source.getResource(sourceSlot);
        if (resource.isEmpty()) return false;

        boolean exporterWhitelist = !exporter.isBlacklist();
        boolean exporterIgnoreNbt = exporter.isIgnoreNbt();
        if (!exporterFilter.matchesFluid(FluidResource.of(resource.toStack(1000)), exporterWhitelist, exporterIgnoreNbt)) {
            return false;
        }

        FilterFluidHandler importerFilter = importer.getFilterFluidHandler();
        if (!ResourceHandlerUtil.isEmpty(importerFilter)) {
            if (!importerFilter.matchesFluid(resource.toStack(1000), exporterWhitelist)) {
                return false;
            }
        }

        int available = source.getAmountAsInt(sourceSlot);
        int toMove = Math.min(available, maxAmount);
        if (toMove <= 0) return false;

        int accepted;
        try (Transaction tx = Transaction.open(null)) {
            accepted = target.insert(resource, toMove, tx);
        }

        if (accepted <= 0) return false;

        try (Transaction tx = Transaction.open(null)) {
            int extracted = source.extract(sourceSlot, resource, accepted, tx);
            if (extracted <= 0) return false;

            target.insert(resource, extracted, tx);
            tx.commit();
            return true;
        }
    }
}
