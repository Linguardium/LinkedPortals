package mod.linguardium.linkedportals.portal.rules;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcherType;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockMatcherRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class BlockInstanceMatcher implements BlockMatcher {
    public static final Codec<BlockInstanceMatcher> CODEC = Block.CODEC.fieldOf("block")
            .xmap(
                    BlockInstanceMatcher::new,
                    matcher -> matcher.block
            )
            .codec();

    Block block;
    @Override
    public BlockMatcherType<?> getType() {
        return LinkedPortalBlockMatcherRules.BLOCK_INSTANCE_MATCHER_TYPE;
    }
    public BlockInstanceMatcher(Block block) {
        this.block = block;
    }
    @Override
    public boolean test(BlockView blockView, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        return blockState.isOf(block);
    }


}
