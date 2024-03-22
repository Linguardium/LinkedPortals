package mod.linguardium.linkedportals.portal.rules.base;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.registry.LinkedPortalRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface BlockMatcher {
    Codec<BlockMatcher> TYPE_CODEC = LinkedPortalRegistries.BLOCK_MATCHER_TYPE.getCodec().dispatch("matcher_type", BlockMatcher::getType, BlockMatcherType::codec);
     BlockMatcherType<?> getType();
    boolean test(BlockView blockView, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity);
    default boolean test(BlockView blockView, BlockPos blockPos) {
        return this.test(blockView, blockPos, blockView.getBlockState(blockPos), blockView.getBlockEntity(blockPos));
    }
    default Predicate<BlockPos> getBlockPosMatcher(BlockView blockView) {
        return pos->this.test(blockView, pos);
    }
}
