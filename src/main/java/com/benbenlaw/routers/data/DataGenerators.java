package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Routers.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new RoutersBlockTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new RoutersItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new RoutersLangProvider(packOutput));
        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(RoutersLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(true, new RoutersModelProvider(packOutput));
        generator.addProvider(true, new RoutersRecipeProvider.Runner (packOutput, lookupProvider));

    }
}