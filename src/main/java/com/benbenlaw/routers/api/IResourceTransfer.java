package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface IResourceTransfer {

    boolean tryTransfer(ServerLevel exporterLevel, ExporterBlockEntity exporter, GlobalPos importerPos);
}