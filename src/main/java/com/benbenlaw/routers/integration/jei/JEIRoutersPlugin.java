package com.benbenlaw.routers.integration.jei;

import com.benbenlaw.core.integration.jei.GhostFilter;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import com.benbenlaw.routers.integration.thaumaturge.ThaumaturgeJei;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

@JeiPlugin
public class JEIRoutersPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Routers.MOD_ID, "jei_plugin");
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        boolean thaumaturge = ModList.get().isLoaded("thaumaturge");

        registration.addGhostIngredientHandler(FilterScreen.class, new GhostFilter<FilterScreen>() {
            @Override
            public <I> List<Target<I>> getTargetsTyped(FilterScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
                List<Target<I>> targets = super.getTargetsTyped(gui, ingredient, doStart);
                if (thaumaturge) targets.addAll(ThaumaturgeJei.targets(gui, ingredient));
                return targets;
            }
        });
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersItems.CONNECTOR.get())), "wrench");

        Collection<String> exporterAliases = List.of("export", "exporter", "cable", "wireless");
        Collection<String> importerAliases = List.of("import", "importer", "cable", "wireless");

        Collection<String> energyUpgrades = List.of("rf", "energy", "power");
        Collection<String> fluidUpgrades = List.of("fluid", "liquid");
        Collection<String> itemUpgrades = List.of("item");

        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersBlocks.EXPORTER.get())),  exporterAliases);
        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersBlocks.IMPORTER.get())),  importerAliases);

        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(
                new ItemStack(RoutersItems.RF_UPGRADE_1.get()),
                new ItemStack(RoutersItems.RF_UPGRADE_2.get()),
                new ItemStack(RoutersItems.RF_UPGRADE_3.get()),
                new ItemStack(RoutersItems.RF_UPGRADE_4.get()),
                new ItemStack(RoutersItems.RF_UPGRADE_5.get())
        ),  energyUpgrades);

        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(
                new ItemStack(RoutersItems.FLUID_UPGRADE_1.get()),
                new ItemStack(RoutersItems.FLUID_UPGRADE_2.get()),
                new ItemStack(RoutersItems.FLUID_UPGRADE_3.get()),
                new ItemStack(RoutersItems.FLUID_UPGRADE_4.get()),
                new ItemStack(RoutersItems.FLUID_UPGRADE_5.get())
        ),  fluidUpgrades);

        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(
                new ItemStack(RoutersItems.ITEM_UPGRADE_1.get()),
                new ItemStack(RoutersItems.ITEM_UPGRADE_1.get()),
                new ItemStack(RoutersItems.ITEM_UPGRADE_1.get()),
                new ItemStack(RoutersItems.ITEM_UPGRADE_1.get()),
                new ItemStack(RoutersItems.ITEM_UPGRADE_1.get())
        ),  itemUpgrades);


    }
}
