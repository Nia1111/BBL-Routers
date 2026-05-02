package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;

public record TransferModule(
    TagKey<Item> upgradeTag,
    BiFunction<ServerLevel, ExporterBlockEntity, Integer> logic
) {}