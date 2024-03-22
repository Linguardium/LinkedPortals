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

import java.util.List;

public class OrMatcher implements BlockMatcher {
    public static final Codec<OrMatcher> CODEC = Codec.list(BlockMatcher.TYPE_CODEC)
            .xmap(OrMatcher::new, om->om.matchers)
            .fieldOf("or")
            .codec();



    List<BlockMatcher> matchers;
    public OrMatcher(List<BlockMatcher> matchers) {
        this.matchers = matchers;
    }
    public OrMatcher(BlockMatcher...matcherList) {
        matchers = List.of(matcherList);
    }

    @Override
    public BlockMatcherType<?> getType() {
        return LinkedPortalBlockMatcherRules.OR_MATCHER_TYPE;
    }

    @Override
    public boolean test(BlockView blockView, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        return false;
    }
}
