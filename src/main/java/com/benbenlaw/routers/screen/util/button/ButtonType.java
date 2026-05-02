package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ButtonType {
    private final Identifier id;
    private final int xOffset;
    private final int yOffset;
    private final String texture;
    private final String textureHover;
    private final String buttonTooltip;
    private final String menuName;
    private final TagKey<Item> unlockedBy;

    public ButtonType(Identifier id, int xOffset, int yOffset, String texture, String textureHover,
                      String buttonTooltip, String menuName, TagKey<Item> unlockedBy) {
        this.id = id;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.texture = texture;
        this.textureHover = textureHover;
        this.buttonTooltip = buttonTooltip;
        this.menuName = menuName;
        this.unlockedBy = unlockedBy;
    }

    // Standard Getters...
    public Identifier getId() { return id; }
    public int getOffsetX() { return xOffset; }
    public int getOffsetY() { return yOffset; }
    public String getTexture() { return texture; }
    public String getTextureHover() { return textureHover; }
    public String getButtonTooltip() { return buttonTooltip; }
    public String getMenuName() { return menuName; }
    public TagKey<Item> getUnlockedBy() { return unlockedBy; }
}