package com.benbenlaw.routers.screen;

import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.FilterFluidSlot;
import com.benbenlaw.core.screen.util.slot.FilterSlot;
import com.benbenlaw.core.screen.util.slot.InputSlot;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ImporterMenu extends SimpleAbstractContainerMenu {

    protected ImporterBlockEntity blockEntity;
    protected Level level;
    protected ContainerData data;
    protected Player player;
    protected BlockPos blockPos;

    public ImporterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), new SimpleContainerData(2));
    }

    public ImporterMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(RoutersMenuTypes.IMPORTER_MENU.get(), containerID, inventory, blockPos, 9);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.level = inventory.player.level();
        this.blockEntity = (ImporterBlockEntity) this.level.getBlockEntity(blockPos);


        for (int i = 0; i < 9; i++) {
            this.addSlot(new FilterSlot(blockEntity.getFilterItemHandler(), blockEntity.getFilterItemHandler()::set,
                    i, 8 + i * 18, 23));
        }

        SimpleContainer fluidFilterContainer = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            this.addSlot(new FilterFluidSlot(fluidFilterContainer, blockEntity.getFilterFluidHandler(), i, 8 + i * 18, 54));
        }


        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            if (this.slots.get(slotId) instanceof FilterSlot filterSlot) {
                ItemStack carried = this.getCarried();
                if (!carried.isEmpty()) {
                    filterSlot.set(carried.copyWithCount(1));
                } else {
                    filterSlot.set(ItemStack.EMPTY);
                }
                return;
            }

            if (this.slots.get(slotId) instanceof FilterFluidSlot filterSlot) {

                if (this.getCarried().isEmpty()) {
                    filterSlot.setEmpty();
                } else {
                    ItemStack carried = this.getCarried();
                    FluidStack fluidInStack = FluidUtil.getFirstStackContained(carried);
                    if (!fluidInStack.isEmpty()) {
                        filterSlot.set(fluidInStack);
                    }
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);

    }

}