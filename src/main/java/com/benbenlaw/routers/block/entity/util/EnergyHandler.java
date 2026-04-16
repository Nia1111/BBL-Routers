package com.benbenlaw.routers.block.entity.util;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class EnergyHandler extends SimpleEnergyHandler {
    private final SyncableBlockEntity blockEntity;

    public EnergyHandler(int capacity, int maxTransfer, SyncableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        super(capacity, maxTransfer);
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        blockEntity.sync();
        super.onEnergyChanged(previousAmount);
    }
}