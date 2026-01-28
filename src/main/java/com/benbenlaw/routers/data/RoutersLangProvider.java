package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class RoutersLangProvider extends LanguageProvider {

    public RoutersLangProvider(PackOutput output) {
        super(output, Routers.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.routers", "Routers");

        //Items
        add("item.routers.connector", "Connector");

        add("item.routers.rf_upgrade_1", "RF Upgrade I");
        add("item.routers.rf_upgrade_2", "RF Upgrade II");
        add("item.routers.rf_upgrade_3", "RF Upgrade III");
        add("item.routers.rf_upgrade_4", "RF Upgrade IV");

        add("item.routers.item_upgrade_1", "Item Upgrade I");
        add("item.routers.item_upgrade_2", "Item Upgrade II");
        add("item.routers.item_upgrade_3", "Item Upgrade III");
        add("item.routers.item_upgrade_4", "Item Upgrade IV");

        add("item.routers.fluid_upgrade_1", "Fluid Upgrade I");
        add("item.routers.fluid_upgrade_2", "Fluid Upgrade II");
        add("item.routers.fluid_upgrade_3", "Fluid Upgrade III");
        add("item.routers.fluid_upgrade_4", "Fluid Upgrade IV");

        add("item.routers.round_robin_upgrade", "Round Robin Upgrade");
        add("item.routers.dimensional_upgrade", "Dimensional Upgrade");
        add("item.routers.blacklist_upgrade", "Blacklist Upgrade");
        add("item.routers.ignore_nbt_upgrade", "Ignore NBT/Components Upgrade");
        add("item.routers.mod_filter_upgrade", "Mod Filter Upgrade (Not Yet Implemented)");
        add("item.routers.tag_filter_upgrade", "Tag Filter Upgrade (Not Yet Implemented)");


        //Blocks
        add("block.routers.exporter", "Exporter");
        add("block.routers.importer", "Importer");

        //Tooltips
        add("tooltip.routers.connector", "Shift Right Click to set Exporter / Importer target. Right Click to connect to target");
        add("tooltip.routers.exporter", "Exports resources from a connected block to Importer, Exporters extract every %s ticks");
        add("tooltip.routers.importer", "Receives resources from connected Exporters");

        add("tooltip.routers.item_upgrade", "Allows the Extraction of Items from an Exporter at %s Per Operation");
        add("tooltip.routers.fluid_upgrade", "Allows the Extraction of Fluids from an Exporter at %smb Per Operation");
        add("tooltip.routers.energy_upgrade", "Allows the Extraction of Energy from an Exporter at %sRF Per Tick");
        add("tooltip.routers.speed_upgrade", "Allows the Extractor to extract every %s ticks");

        add("tooltip.routers.round_robin_upgrade", "After each operation, the Exporter will try to insert into the next connected Importer");
        add("tooltip.routers.dimensional_upgrade", "Allows the Exporter to send resources to an Importer in a different dimension");
        add("tooltip.routers.blacklist_upgrade", "Changes the filtering to be a Blacklist. Effects all resource types that can be filtered");
        add("tooltip.routers.ignore_nbt_upgrade", "Ignores NBT/Data when filtering resources. Effects all resource types that can be filtered");



        //Client Messages
        add("message.routers.exporter_selected", "Exporter target set to %s");
        add("message.routers.importer_selected", "Importer target set to %s");
        add("message.routers.connected_exporter_to_importer", "Connected Exporter to Importer at %s");
        add("message.routers.connected_importer_to_exporter", "Connected Importer to Exporter at %s");
        add("message.routers.not_loaded", "Area not loaded to connect routers!");
        add("message.routers.no_exporter_importer_selected", "No Exporter / Importer selected. Shift right click to set main connection !");


    }


}

