package com.benbenlaw.routers.item;

import com.benbenlaw.routers.Routers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoutersCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Routers.MOD_ID);

    public static final Supplier<CreativeModeTab> ROUTERS_TAB = CREATIVE_MODE_TABS.register("routers", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RoutersItems.CONNECTOR.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.routers"))
            .displayItems(RoutersItems.ITEMS.getEntries()).build());

}
