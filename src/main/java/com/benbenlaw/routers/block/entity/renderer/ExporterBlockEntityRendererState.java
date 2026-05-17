package com.benbenlaw.routers.block.entity.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.List;

public class ExporterBlockEntityRendererState extends BlockEntityRenderState {

    public GlobalPos exporterPosition;
    public Direction exporterFacing;
    public List<GlobalPos> importerPositions;
    public ItemStacksResourceHandler upgradeItems;

}
