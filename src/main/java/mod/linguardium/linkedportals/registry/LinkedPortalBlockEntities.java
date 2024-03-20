package mod.linguardium.linkedportals.registry;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.blocks.blockentity.PortalStructureBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static mod.linguardium.linkedportals.LinkedPortals.id;
import static mod.linguardium.linkedportals.registry.LinkedPortalBlocks.*;

public class LinkedPortalBlockEntities {
    public static final BlockEntityType<PortalControlBlockEntity> PORTAL_CONTROL_BLOCKENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("portal_controller_blockentity"), BlockEntityType.Builder.create(PortalControlBlockEntity::new,PORTAL_CONTROL_BLOCK).build(null));
    public static final BlockEntityType<PortalStructureBlockEntity> PORTAL_STRUCTURE_BLOCK_ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("portal_structure_blockentity"), BlockEntityType.Builder.create(PortalStructureBlockEntity::new, PORTAL_FILL_BLOCK).build(null));

    public static void init() {
        PolymerBlockUtils.registerBlockEntity(PORTAL_CONTROL_BLOCKENTITY_TYPE, PORTAL_STRUCTURE_BLOCK_ENTITY_TYPE);
    }
}
