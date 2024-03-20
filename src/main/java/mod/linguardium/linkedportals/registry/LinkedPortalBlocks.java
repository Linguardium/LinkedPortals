package mod.linguardium.linkedportals.registry;

import mod.linguardium.linkedportals.blocks.LinkedPortalFillerBlock;
import mod.linguardium.linkedportals.blocks.PortalControlBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public class LinkedPortalBlocks {
    public static final Block PORTAL_FILL_BLOCK = Registry.register(Registries.BLOCK, id("portal_fill_block"), new LinkedPortalFillerBlock());
    public static final Block PORTAL_CONTROL_BLOCK = Registry.register(Registries.BLOCK, id("portal_control_block"), new PortalControlBlock());
    public static void init() {
        // included to class load at init
    }
}
