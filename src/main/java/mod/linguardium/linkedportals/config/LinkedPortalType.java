package mod.linguardium.linkedportals.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.linguardium.linkedportals.portal.rules.BlockInstanceMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.BlockTagMatcher;
import mod.linguardium.linkedportals.registry.LinkedPortalTags;
import net.minecraft.block.Blocks;

public record  LinkedPortalType(int limit, boolean lockable, BlockMatcher innerBlockStateValidator, BlockMatcher frameBlockStateValidator) {
    public static final Codec<LinkedPortalType> CODEC = RecordCodecBuilder.create(instance-> instance.group(
            Codec.INT.optionalFieldOf("size_limit",21).forGetter(LinkedPortalType::limit),
            Codec.BOOL.optionalFieldOf("lockable",true).forGetter(LinkedPortalType::lockable),
            BlockMatcher.TYPE_CODEC.optionalFieldOf("valid_interior_rule", new BlockInstanceMatcher(Blocks.AIR)).forGetter(LinkedPortalType::innerBlockStateValidator) ,
            BlockMatcher.TYPE_CODEC.fieldOf("valid_frame_rule").forGetter(LinkedPortalType::frameBlockStateValidator)
    ).apply(instance,LinkedPortalType::new));

    public static LinkedPortalType DEFAULT = new LinkedPortalType(21, true, new BlockInstanceMatcher(Blocks.AIR), new BlockTagMatcher(LinkedPortalTags.VALID_PORTAL_FRAMES));

}
