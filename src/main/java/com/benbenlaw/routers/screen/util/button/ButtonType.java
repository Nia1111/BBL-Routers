package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public enum ButtonType {

    ITEM_FILTER(
            7, 31,
            "filter_buttons/item",
            "filter_buttons/item_hover",
            "tooltip.routers.button.item",
            "tooltip.routers.menu.item",
            RoutersTags.Items.ITEM_UPGRADES
    ),

    FLUID_FILTER(
            25, 31,
            "filter_buttons/fluid",
            "filter_buttons/fluid_hover",
            "tooltip.routers.button.fluid",
            "tooltip.routers.menu.fluid",
            RoutersTags.Items.FLUID_UPGRADES
    ),

    ENERGY_FILTER(
            43, 31,
            "filter_buttons/energy",
            "filter_buttons/energy_hover",
            "tooltip.routers.button.energy",
            "tooltip.routers.menu.energy",
            RoutersTags.Items.RF_UPGRADES
    );

    private final int xOffset;
    private final int yOffset;
    private final String texture;
    private final String textureHover;
    private final String buttonTooltip;
    private final String menuName;
    private final TagKey<Item> unlockedBy;

    ButtonType(int xOffset, int yOffset, String texture, String textureHover, String buttonTooltip, String menuName, TagKey<Item> unlockedBy) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.texture = texture;
        this.textureHover = textureHover;
        this.buttonTooltip = buttonTooltip;
        this.menuName = menuName;
        this.unlockedBy = unlockedBy;
    }

    public int getOffsetX() {
        return xOffset;
    }

    public int getOffsetY() {
        return yOffset;
    }

    public String getTexture() {
        return texture;
    }

    public String getTextureHover() {
        return textureHover;
    }

    public String getButtonTooltip() {
        return buttonTooltip;
    }

    public String getMenuName() {
        return menuName;
    }

    public TagKey<Item> getUnlockedBy() {
        return unlockedBy;
    }

}
