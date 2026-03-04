package com.benbenlaw.routers.screen.upgrade;

import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.FilterFluidSlot;
import com.benbenlaw.core.screen.util.slot.FilterSlot;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.screen.RoutersMenuTypes;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.awt.*;

public class FilterMenu extends SimpleAbstractContainerMenu {


    protected ExporterBlockEntity blockEntity;
    protected Level level;
    protected ContainerData data;
    protected Player player;
    protected BlockPos blockPos;
    protected ButtonType buttonType;


    public FilterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), extraData.readEnum(ButtonType.class), new SimpleContainerData(2));
    }

    public FilterMenu(int containerID, Inventory inventory, BlockPos blockPos, ButtonType buttonType, ContainerData data) {
        super(RoutersMenuTypes.FILTER_MENU.get(), containerID, inventory, blockPos, 0);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.buttonType = buttonType;
        this.level = inventory.player.level();
        this.blockEntity = (ExporterBlockEntity) this.level.getBlockEntity(blockPos);

        assert blockEntity != null;

        //Add Item Slots
        if (buttonType == ButtonType.ITEM_FILTER) {
            for (int i = 0; i < 9; i++) {
                this.addSlot(new FilterSlot(blockEntity.getFilterItemHandler(), blockEntity.getFilterItemHandler()::set,
                        i, 8 + i * 18, 36));
            }
            for (int i = 9; i < 18; i++) {
                this.addSlot(new FilterSlot(blockEntity.getFilterItemHandler(), blockEntity.getFilterItemHandler()::set,
                        i, 8 + (i - 9) * 18, 54));
            }
        }

        //Add Fluid Slots
        if (buttonType == ButtonType.FLUID_FILTER) {
            SimpleContainer fluidFilterContainer = new SimpleContainer(18);
            for (int i = 0; i < 9; i++) {
                this.addSlot(new FilterFluidSlot(fluidFilterContainer, blockEntity.getFilterFluidHandler(), i, 8 + i * 18, 36));
            }
            for (int i = 9; i < 18; i++) {
                this.addSlot(new FilterFluidSlot(fluidFilterContainer, blockEntity.getFilterFluidHandler(), i, 8 + (i - 9) * 18, 54));
            }
        }


        this.addDataSlots(data);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
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