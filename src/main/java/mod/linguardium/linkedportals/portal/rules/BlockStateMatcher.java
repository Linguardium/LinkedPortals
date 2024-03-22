package mod.linguardium.linkedportals.portal.rules;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcherType;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockMatcherRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class BlockStateMatcher implements BlockMatcher {
    public static final Codec<BlockStateMatcher> CODEC = BlockState.CODEC.fieldOf("state")
            .xmap(  BlockStateMatcher::new,
                    matcher -> matcher.state
            ).codec();

    BlockState state;
    public BlockStateMatcher(BlockState state) {
        this.state = state;
    }
    @Override
    public BlockMatcherType<?> getType() {
        return LinkedPortalBlockMatcherRules.BLOCK_STATE_MATCHER_TYPE;
    }

    @Override
    public boolean test(BlockView blockView, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        return blockState == this.state;
    }
}
