package com.benbenlaw.routers.screen;

import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.routers.Routers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import java.util.ArrayList;
import java.util.List;

public class ImporterScreen extends AbstractContainerScreen<ImporterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Routers.MOD_ID, "textures/gui/importer_gui.png");

    public ImporterScreen(ImporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        for (int i = 0; i < 9; i++) {
            FluidRenderingUtils.renderFluidStack(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), x + 8 + (i * 18), y + 54, 16, 16, mouseX, mouseY);
        }

        for (int i = 0; i < menu.blockEntity.getFilterFluidHandler().size(); i++) {
            FluidRenderingUtils.renderFluidStackTooltip(guiGraphics, menu.blockEntity.getFilterFluidHandler().getFilter(i), menu.blockEntity.getFilterFluidHandler(), i, x + 8 + (i * 18), y + 54, 16, 16, mouseX, mouseY);
        }
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY) {
        super.extractSlot(guiGraphics, slot, mouseX, mouseY);

        if (slot.index > 35 && slot.index < 45) {
            if (isHovering(slot, mouseX, mouseY)) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) {
                    List<Component> lines = new ArrayList();
                    lines.add(Component.translatable("tooltip.routers.empty_item_filter_slot").withStyle(ChatFormatting.GRAY));
                    List<ClientTooltipComponent> tooltipComponents = lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
                    guiGraphics.tooltip(Minecraft.getInstance().font, tooltipComponents, mouseX - leftPos, mouseY - topPos, DefaultTooltipPositioner.INSTANCE, null);
                }
            }
        }
        if (slot.index > 44 && slot.index < 55) {
            if (isHovering(slot, mouseX, mouseY)) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) {
                    List<Component> lines = new ArrayList();
                    lines.add(Component.translatable("tooltip.routers.empty_fluid_filter_slot").withStyle(ChatFormatting.GRAY));
                    List<ClientTooltipComponent> tooltipComponents = lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
                    guiGraphics.tooltip(Minecraft.getInstance().font, tooltipComponents, mouseX - leftPos, mouseY - topPos, DefaultTooltipPositioner.INSTANCE, null);

                }
            }
        }
    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16;
    }

}