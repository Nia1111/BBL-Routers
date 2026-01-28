package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.core.block.entity.handler.item.InputItemHandler;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ImporterBlockEntity extends SyncableBlockEntity implements MenuProvider {

    public final ContainerData data;
    public final GlobalPos importerPos;
    private FilterItemHandler filterItemHandler = new FilterItemHandler(this, 9);
    private FilterFluidHandler filterFluidHandler = new FilterFluidHandler(this, 9);

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.IMPORTER_BLOCK_ENTITY.get(), pos, state);
         this.importerPos = null;

        this.data = new ContainerData() {;
            @Override
            public int get(int index) {
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    public void tick() {

    }

    public FilterItemHandler getFilterItemHandler() {
        return filterItemHandler;
    }

    public FilterFluidHandler getFilterFluidHandler() {
        return filterFluidHandler;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.importer");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ImporterMenu(container, inventory, this.getBlockPos(), data);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {

        filterItemHandler.serialize(output.child("itemFilter"));
        filterFluidHandler.serialize(output.child("fluidFilter"));

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {

        filterItemHandler.deserialize(input.childOrEmpty("itemFilter"));
        filterFluidHandler.deserialize(input.childOrEmpty("fluidFilter"));

        super.loadAdditional(input);
    }
}