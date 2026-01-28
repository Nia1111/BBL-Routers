package com.benbenlaw.routers.screen;

import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.routers.Routers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class ImporterScreen extends AbstractContainerScreen<ImporterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Routers.MOD_ID, "textures/gui/importer_gui.png");

    public ImporterScreen(ImporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);


        for (int i = 0; i < 9; i++) {
            FluidRenderingUtils.renderFluidStack(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), x + 8 + (i * 18), y + 54, 16, 16, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);

        for (int i = 0; i < menu.blockEntity.getFilterFluidHandler().size(); i++) {
            FluidRenderingUtils.renderFluidStackTooltip(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), x + 8 + (i * 18), y + 54, 16, 16, mouseX, mouseY);
        }
    }


    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);


    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16;
    }

}