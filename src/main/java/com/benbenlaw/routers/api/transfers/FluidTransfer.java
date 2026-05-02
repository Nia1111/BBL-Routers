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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FluidTransfer {

    public static int transferFluids(ServerLevel level, ExporterBlockEntity exporter) {

        var state = level.getBlockState(exporter.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        ResourceHandler<FluidResource> source = exporter.getConnectedResources().getFluidHandler(
                level, exporter.getBlockPos().relative(facing), facing.getOpposite()
        ).orElse(null);

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<FluidResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    for (int slot = 0; slot < source.size(); slot++) {
                        FluidResource res = source.getResource(slot);
                        if (res.isEmpty()) continue;

                        boolean isWhitelist = !entity.isBlacklist();
                        boolean matches = true;

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterFluidHandler())) {
                            matches = entity.getFilterFluidHandler().matchesFluid(FluidResource.of(res.toStack(1000)), isWhitelist, entity.isIgnoreNbt());
                        }

                        if (matches && srvLevel.getBlockEntity(targetPos.pos()) instanceof ImporterBlockEntity importer) {
                            if (!ResourceHandlerUtil.isEmpty(importer.getFilterFluidHandler())) {
                                matches = importer.getFilterFluidHandler().matchesFluid(res.toStack(1000), isWhitelist);
                            }
                        }

                        if (!matches) continue;

                        try (Transaction tx = Transaction.open(null)) {
                            int amount = Math.min(source.getAmountAsInt(slot), entity.getUpgradeValue(RoutersTags.Items.FLUID_UPGRADES));
                            int accepted = target.insert(res, amount, tx);

                            if (accepted > 0 && source.extract(slot, res, accepted, tx) > 0) {
                                tx.commit();
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }

    private static ResourceHandler<FluidResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        var state = targetLevel.getBlockState(pos.pos());
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();
        return targetLevel.getCapability(Capabilities.Fluid.BLOCK, pos.pos().relative(state.getValue(RouterBlock.FACING)), facing);
    }
}