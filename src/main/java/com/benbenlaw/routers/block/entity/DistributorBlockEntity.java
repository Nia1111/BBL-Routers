package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.entity.util.EnergyHandler;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.screen.DistributorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DistributorBlockEntity extends SyncableBlockEntity implements MenuProvider {

    private final ContainerData data;

    private final EnergyHandler energyHandler = new EnergyHandler(10000000, 100000, this);


    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.DISTRIBUTOR_BLOCK_ENTITY.get(), pos, state);

        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> energyHandler.getAmountAsInt();
                    case 1 -> energyHandler.getCapacityAsInt();
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> energyHandler.getAmountAsInt();
                    case 1 -> energyHandler.getCapacityAsInt();
                }
            }

            public int getCount() {
                return 2;
            }
        };
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Only run every X ticks to save performance, or every tick for high-speed transfer
        if (level.getGameTime() % 5 == 0 && energyHandler.getAmountAsLong() > 0) {
            distributeEnergy(10); // 10 block radius
        }
    }

    private void distributeEnergy(int radius) {
        BlockPos center = this.worldPosition;
        int maxTransferPerBlock = 1000;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    BlockPos targetPos = center.offset(x, y, z);

                    if (targetPos.equals(center)) continue;

                    for (Direction side : Direction.values()) {
                        net.neoforged.neoforge.transfer.energy.EnergyHandler targetHandler =
                                level.getCapability(Capabilities.Energy.BLOCK, targetPos, side);

                        if (targetHandler != null && energyHandler.getAmountAsLong() > 0) {
                            tryMoveEnergy(this.energyHandler, targetHandler, maxTransferPerBlock);
                        }
                    }
                }
            }
        }
    }

    private void tryMoveEnergy(EnergyHandler source, net.neoforged.neoforge.transfer.energy.EnergyHandler target, int maxAmount) {
        try (Transaction tx = Transaction.open(null)) {
            int extracted = source.extract(maxAmount, tx);
            if (extracted <= 0) return;

            try (Transaction nestedTx = Transaction.open(tx)) {
                int accepted = target.insert(extracted, nestedTx);

                if (accepted > 0) {
                    nestedTx.commit();
                    tx.commit();
                }
            }
        }
    }
    @Override
    protected void saveAdditional(ValueOutput output) {
        energyHandler.serialize(output.child("energy"));
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        energyHandler.deserialize(input.childOrEmpty("energy"));
        super.loadAdditional(input);
    }

    public EnergyHandler getEnergyHandler() { return energyHandler; }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int container, Inventory inventory, Player player) {
        return new DistributorMenu(container, inventory, this.worldPosition, data);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.routers.distributor");
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(RoutersDataComponents.ENERGY.get(), energyHandler.getAmountAsInt());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        int amount = components.getOrDefault(RoutersDataComponents.ENERGY.get(), 0);
        energyHandler.set(amount);
    }
}