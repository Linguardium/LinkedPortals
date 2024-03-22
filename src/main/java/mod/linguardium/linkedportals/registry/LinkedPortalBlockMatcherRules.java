package mod.linguardium.linkedportals.registry;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.portal.rules.*;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcherType;
import net.minecraft.registry.Registry;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public class LinkedPortalBlockMatcherRules {
    public static final BlockMatcherType<BlockInstanceMatcher> BLOCK_INSTANCE_MATCHER_TYPE = register("block", BlockInstanceMatcher.CODEC);
    public static final BlockMatcherType<BlockTagMatcher> BLOCK_TAG_MATCHER_TYPE = register("tag", BlockTagMatcher.CODEC);
    public static final BlockMatcherType<BlockStateMatcher> BLOCK_STATE_MATCHER_TYPE = register("state", BlockStateMatcher.CODEC);
    public static final BlockMatcherType<OrMatcher> OR_MATCHER_TYPE = register("or", OrMatcher.CODEC);
    public static void init() { }
    private static <T extends BlockMatcher> BlockMatcherType<T> register(String typeId, Codec<T> codecSupplier) {
        return Registry.register(LinkedPortalRegistries.BLOCK_MATCHER_TYPE, id(typeId), ()->codecSupplier);
    }
}
