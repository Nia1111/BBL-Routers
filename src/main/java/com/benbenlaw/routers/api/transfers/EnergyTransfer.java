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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class EnergyTransfer {

    public static int transferEnergy(ServerLevel level, ExporterBlockEntity exporter) {

        int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);
        EnergyHandler source = exporter.getConnectedResources().getEnergyHandler(
                level, exporter.getBlockPos().relative(level.getBlockState(exporter.getBlockPos()).getValue(RouterBlock.FACING)),
                level.getBlockState(exporter.getBlockPos()).getValue(RouterBlock.FACING).getOpposite()
        ).orElse(null);

        if (source == null || source.getAmountAsLong() <= 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetGlobalPos) -> {

                    EnergyHandler target = getTargetHandler(srvLevel, entity, targetGlobalPos);
                    if (target == null) return false;

                    try (Transaction tx = Transaction.open(null)) {
                        int extracted = source.extract(amount, tx);
                        if (extracted <= 0) return false;

                        int accepted = target.insert(extracted, tx);
                        if (accepted > 0) {
                            tx.commit();
                            return true;
                        }
                    }
                    return false;
                });
    }

    private static EnergyHandler getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos()) || !(targetLevel.getBlockEntity(pos.pos()) instanceof ImporterBlockEntity)) return null;

        var state = targetLevel.getBlockState(pos.pos());
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();
        return targetLevel.getCapability(Capabilities.Energy.BLOCK, pos.pos().relative(state.getValue(RouterBlock.FACING)), facing);
    }
}