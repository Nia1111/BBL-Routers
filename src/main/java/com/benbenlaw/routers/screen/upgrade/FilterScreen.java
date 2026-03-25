package com.benbenlaw.routers.screen.upgrade;

import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.core.screen.util.slot.FilterFluidSlot;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.screen.util.MousePositionManagerUtil;
import com.benbenlaw.routers.screen.util.button.BackButton;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.screen.util.button.FilterButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class FilterScreen extends AbstractContainerScreen<FilterMenu> {

    private static final Identifier TEXTURE = Routers.identifier("textures/gui/filter_gui.png");
    private static final Identifier INVENTORY_SLOTS_9 = Routers.identifier("inventory_slots_9");

    public FilterScreen(FilterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        MousePositionManagerUtil.setLastKnownPosition();

        addRenderableWidget(BackButton.create(x + 151, y + 4, 20, 20, menu.blockEntity));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        if (menu.buttonType == ButtonType.FLUID_FILTER || menu.buttonType == ButtonType.ITEM_FILTER) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, INVENTORY_SLOTS_9, x + 7, y + 35, 162, 18);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, INVENTORY_SLOTS_9, x + 7, y + 53, 162, 18);
        }
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;


        if (menu.buttonType == ButtonType.FLUID_FILTER) {
            for (int i = 0; i < 9; i++) {

                FluidRenderingUtils.renderFluidStack(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), x + 8 + (i * 18), y + 36, 16, 16, mouseX, mouseY);

            }
            for (int i = 9; i < 18; i++) {

                FluidRenderingUtils.renderFluidStack(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), x + 8 + ((i - 9) * 18), y + 54, 16, 16, mouseX, mouseY);

            }
        }

        if (menu.buttonType == ButtonType.FLUID_FILTER) {
            for (int i = 0; i < menu.blockEntity.getFilterFluidHandler().size(); i++) {

                FluidRenderingUtils.renderFluidStackTooltip(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i) ,menu.blockEntity.getFilterFluidHandler(), i,  x + 8 + (i * 18), y + 36, 16, 16, mouseX, mouseY);
            }
            for (int i = 9; i < 18; i++) {

                FluidRenderingUtils.renderFluidStackTooltip(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), menu.blockEntity.getFilterFluidHandler(), i, x + 8 + ((i - 9) * 18), y + 54, 16, 16, mouseX, mouseY);

            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        MousePositionManagerUtil.getLastKnownPosition();
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onClose() {
        MousePositionManagerUtil.clear();
        super.onClose();
    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16;
    }


}
