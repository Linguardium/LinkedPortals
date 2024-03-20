package mod.linguardium.linkedportals.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public class LinkedPortalTags {
    public static final TagKey<Block> VALID_PORTAL_FRAMES = TagKey.of(RegistryKeys.BLOCK, id("valid_frames"));
    public static final TagKey<Block> PORTAL_CONTROLLER_BASE = TagKey.of(RegistryKeys.BLOCK, id("portal_controller_base"));
    public static final TagKey<Item> VALID_ACTIVATORS = TagKey.of(RegistryKeys.ITEM, id("valid_activators"));
    public static void init() {
        // empty method called to classload at initialization time
    }
}
