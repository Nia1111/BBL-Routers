package com.benbenlaw.routers.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ClientScreens {

    public static void openConfigScreen(ItemStack itemStack) {
        Minecraft.getInstance().setScreen(new ConfigScreen(Component.translatable("screen.routers.config"), itemStack));
    }
}