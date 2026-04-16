package com.benbenlaw.routers.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.Optional;

public class ConnectedResources {

    private final GlobalPos targetPos;

    public ConnectedResources(GlobalPos targetPos) {
        this.targetPos = targetPos;
    }

    public List<Optional<ResourceHandler<?>>> getAllHandlers(Level level, Direction side) {
        BlockPos pos = targetPos.pos();
        return List.of(
                getItemHandler(level, pos, side).map(handler -> handler),
                getFluidHandler(level, pos, side).map(handler -> handler)
        );
    }

    public Optional<ResourceHandler<ItemResource>> getItemHandler(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getCapability(Capabilities.Item.BLOCK, pos, side));
    }

    public Optional<ResourceHandler<FluidResource>> getFluidHandler(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getCapability(Capabilities.Fluid.BLOCK, pos, side));
    }
    public Optional<EnergyHandler> getEnergyHandler(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getCapability(Capabilities.Energy.BLOCK, pos, side));
    }


}
