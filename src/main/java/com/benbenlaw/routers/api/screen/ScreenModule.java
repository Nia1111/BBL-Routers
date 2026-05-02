package com.benbenlaw.routers.api.screen;

import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.screen.upgrade.FilterMenu;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import org.apache.commons.lang3.function.TriConsumer;

public record ScreenModule(
        ButtonType buttonType,
        TriConsumer<FilterMenu, ExporterBlockEntity, Integer> slotAdder
) {}