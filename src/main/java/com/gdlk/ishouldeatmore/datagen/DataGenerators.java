package com.gdlk.ishouldeatmore.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Registers data providers (e.g. item tags for enchantable) when the data generator runs.
 *
 * @see <a href="https://docs.neoforged.net/docs/1.21.1/resources/server/tags/#datagen">NeoForge Tags Datagen</a>
 */
public final class DataGenerators {

    private DataGenerators() {}

    public static void gatherData(GatherDataEvent event) {
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        AtomicReference<ModBlockTagsProvider> blockTagsRef = new AtomicReference<>();

        DataProvider.Factory<DataProvider> blockTagsFactory = packOutput -> {
            ModBlockTagsProvider blockTags = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
            blockTagsRef.set(blockTags);
            return blockTags;
        };
        event.getGenerator().addProvider(event.includeServer(), blockTagsFactory);

        DataProvider.Factory<DataProvider> itemTagsFactory = packOutput ->
                new ModItemTagsProvider(packOutput, lookupProvider, blockTagsRef.get().contentsGetter(), existingFileHelper);
        event.getGenerator().addProvider(event.includeServer(), itemTagsFactory);
    }
}
