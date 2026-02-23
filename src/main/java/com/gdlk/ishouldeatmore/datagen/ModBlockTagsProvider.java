package com.gdlk.ishouldeatmore.datagen;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Block tag provider for datagen. Used to supply block tag lookup to {@link ModItemTagsProvider}.
 * This mod has no blocks; add block tags here if needed later.
 */
public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Ishouldeatmore.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // No blocks in this mod; block tags can be added here if needed.
    }
}
