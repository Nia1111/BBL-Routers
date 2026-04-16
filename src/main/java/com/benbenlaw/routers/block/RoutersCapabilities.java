package com.benbenlaw.routers.block;

import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class RoutersCapabilities {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(Capabilities.Energy.BLOCK, RoutersBlockEntities.DISTRIBUTOR_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getEnergyHandler());
    }


}
