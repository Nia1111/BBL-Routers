package com.benbenlaw.routers.transfers;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.api.transfers.EnergyTransfer;
import com.benbenlaw.routers.api.transfers.FluidTransfer;
import com.benbenlaw.routers.api.transfers.ItemTransfer;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoutersTransfers {

    public static final ResourceKey<Registry<TransferModule>> TRANSFER_MODULE_KEY =
        ResourceKey.createRegistryKey(Routers.identifier("transfer_modules"));

    public static final DeferredRegister<TransferModule> TRANSFER_MODULES =
        DeferredRegister.create(TRANSFER_MODULE_KEY, "routers");

    public static final Registry<TransferModule> TRANSFER_MODULES_REGISTRY = TRANSFER_MODULES.makeRegistry(builder ->
            builder.sync(true)
    );

    public static final DeferredHolder<TransferModule, TransferModule> ITEMS =
        TRANSFER_MODULES.register("item_transfer", () -> 
            new TransferModule(RoutersTags.Items.ITEM_UPGRADES, ItemTransfer::transferItems));

    public static final DeferredHolder<TransferModule, TransferModule> FLUIDS = 
        TRANSFER_MODULES.register("fluid_transfer", () -> 
            new TransferModule(RoutersTags.Items.FLUID_UPGRADES, FluidTransfer::transferFluids));

    public static final DeferredHolder<TransferModule, TransferModule> ENERGY = 
        TRANSFER_MODULES.register("energy_transfer", () -> 
            new TransferModule(RoutersTags.Items.RF_UPGRADES, EnergyTransfer::transferEnergy));
}