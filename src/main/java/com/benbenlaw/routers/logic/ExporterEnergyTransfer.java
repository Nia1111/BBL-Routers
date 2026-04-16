package com.benbenlaw.routers.logic;

import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.ConnectedResources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class ExporterEnergyTransfer {

    public static int transferEnergy(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            BlockPos exporterPos,
            ConnectedResources connectedResources,
            List<GlobalPos> importerPositions,
            boolean roundRobin,
            int lastImporterIndex,
            int amountToTransfer
    ) {
        if (importerPositions == null || importerPositions.isEmpty()) {
            return lastImporterIndex;
        }

        EnergyHandler source = getSourceHandler(exporterLevel, exporterPos, connectedResources);

        // Check if source exists and has energy to move
        if (source == null || source.getAmountAsLong() <= 0) {
            return lastImporterIndex;
        }

        return roundRobin
                ? insertRoundRobin(exporterLevel, exporter, source, importerPositions, lastImporterIndex, amountToTransfer)
                : insertOrdered(exporterLevel, exporter, source, importerPositions, amountToTransfer, lastImporterIndex);
    }

    private static EnergyHandler getSourceHandler(
            ServerLevel level,
            BlockPos exporterPos,
            ConnectedResources connectedResources
    ) {
        BlockState state = level.getBlockState(exporterPos);
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();

        return connectedResources
                .getEnergyHandler(level, exporterPos.relative(state.getValue(RouterBlock.FACING)), facing)
                .orElse(null);
    }

    private static int insertRoundRobin(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            EnergyHandler source,
            List<GlobalPos> importers,
            int startIndex,
            int amountToTransfer
    ) {
        int importerCount = importers.size();
        int index = startIndex;

        for (int offset = 0; offset < importerCount; offset++) {
            int currentIndex = (index + offset) % importerCount;
            if (tryMoveToImporter(exporterLevel, exporter, source, importers.get(currentIndex), amountToTransfer)) {
                return (currentIndex + 1) % importerCount;
            }
        }

        return startIndex;
    }

    private static int insertOrdered(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            EnergyHandler source,
            List<GlobalPos> importers,
            int amountToTransfer,
            int lastIndex
    ) {
        for (GlobalPos importerPos : importers) {
            if (tryMoveToImporter(exporterLevel, exporter, source, importerPos, amountToTransfer)) {
                return lastIndex;
            }
        }
        return lastIndex;
    }

    private static boolean tryMoveToImporter(
            ServerLevel exporterLevel,
            ExporterBlockEntity exporter,
            EnergyHandler source,
            GlobalPos importerPos,
            int maxAmount
    ) {
        MinecraftServer server = exporterLevel.getServer();
        ServerLevel importerLevel = server.getLevel(importerPos.dimension());
        if (importerLevel == null) return false;

        if (!importerLevel.dimension().equals(exporterLevel.dimension()) && !exporter.canDoDimensionalTravel()) {
            return false;
        }

        if (!importerLevel.isLoaded(importerPos.pos())) return false;

        if (!(importerLevel.getBlockEntity(importerPos.pos()) instanceof ImporterBlockEntity)) {
            return false;
        }

        BlockState state = importerLevel.getBlockState(importerPos.pos());
        if (!(state.getBlock() instanceof RouterBlock)) return false;

        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();

        EnergyHandler target = importerLevel.getCapability(
                Capabilities.Energy.BLOCK,
                importerPos.pos().relative(state.getValue(RouterBlock.FACING)),
                facing
        );

        if (target == null) return false;

        try (Transaction tx = Transaction.open(null)) {

            int extracted = source.extract(maxAmount, tx);
            if (extracted <= 0) return false;

            try (Transaction nestedTx = Transaction.open(tx)) {
                int accepted = target.insert(extracted, nestedTx);

                if (accepted > 0) {
                    if (accepted < extracted) {
                        nestedTx.commit();
                        tx.commit();
                        return true;
                    }

                    nestedTx.commit();
                    tx.commit();
                    return true;
                }
            }
        }
        return false;
    }
}