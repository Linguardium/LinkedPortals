package mod.linguardium.linkedportals.portal.rules;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcherType;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockMatcherRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class BlockTagMatcher implements BlockMatcher {
    public static final Codec<BlockTagMatcher> CODEC =
            Codecs.alternatively(
                TagKey.unprefixedCodec(RegistryKeys.BLOCK),
                TagKey.codec(RegistryKeys.BLOCK)
            )
            .fieldOf("tag")
            .xmap(BlockTagMatcher::new, matcher -> matcher.blockTagKey)
            .codec();

    TagKey<Block> blockTagKey;

    public BlockTagMatcher(TagKey<Block> tagKey) {
        this.blockTagKey = tagKey;
    }

    @Override
    public BlockMatcherType<?> getType() {
        return LinkedPortalBlockMatcherRules.BLOCK_TAG_MATCHER_TYPE;
    }

    @Override
    public boolean test(BlockView blockView, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        return blockState.isIn(blockTagKey);
    }
}
