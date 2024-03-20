package mod.linguardium.linkedportals.data;

import mod.linguardium.linkedportals.config.LinkedPortalType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public class LinkedPortalsProvider extends FabricCodecDataProvider<LinkedPortalType> {

    public LinkedPortalsProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture, DataOutput.OutputType.DATA_PACK, "linkedportals/types", LinkedPortalType.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, LinkedPortalType> provider, RegistryWrapper.WrapperLookup lookup) {
        provider.accept(id("default"),LinkedPortalType.DEFAULT);
    }

    @Override
    public String getName() {
        return "Linked Portals Provider";
    }
}
