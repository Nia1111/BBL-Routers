package com.benbenlaw.routers.item;

import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectorItem extends Item {
    public ConnectorItem(Properties properties) {
        super(properties);
    }


}
