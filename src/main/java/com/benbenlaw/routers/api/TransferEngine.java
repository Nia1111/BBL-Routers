package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class TransferEngine {
    public static int run(ServerLevel level, ExporterBlockEntity exporter, List<GlobalPos> importers, boolean isRoundRobin, int lastIndex, IResourceTransfer transferLogic) {
        if (importers == null || importers.isEmpty()) return lastIndex;

        int size = importers.size();

        if (isRoundRobin) {
            for (int i = 0; i < size; i++) {
                int currentIndex = (lastIndex + i) % size;
                if (transferLogic.tryTransfer(level, exporter, importers.get(currentIndex))) {
                    return (currentIndex + 1) % size;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (transferLogic.tryTransfer(level, exporter, importers.get(i))) {
                    return lastIndex;
                }
            }
        }
        return lastIndex;
    }
}