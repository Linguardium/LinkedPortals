package mod.linguardium.linkedportals.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.linguardium.linkedportals.misc.DefaultFrameRuleTest;
import net.minecraft.block.Blocks;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public record  LinkedPortalType(int limit, boolean lockable, RuleTest innerBlockStateValidator, RuleTest frameBlockStateValidator) {
    public static final Codec<LinkedPortalType> CODEC = RecordCodecBuilder.create(instance-> instance.group(
            Codec.INT.optionalFieldOf("size_limit",21).forGetter(LinkedPortalType::limit),
            Codec.BOOL.optionalFieldOf("lockable",true).forGetter(LinkedPortalType::lockable),
            RuleTest.TYPE_CODEC.optionalFieldOf("valid_interior_rule", new BlockMatchRuleTest(Blocks.AIR)).forGetter(LinkedPortalType::innerBlockStateValidator) ,
            RuleTest.TYPE_CODEC.fieldOf("valid_frame_rule").forGetter(LinkedPortalType::frameBlockStateValidator)
    ).apply(instance,LinkedPortalType::new));

    public static LinkedPortalType DEFAULT = new LinkedPortalType(21, true, new BlockMatchRuleTest(Blocks.AIR), DefaultFrameRuleTest.INSTANCE);
    public boolean isValidFrameAtPosition(WorldAccess world, BlockPos pos) {
        return isValidAtPosition(world, pos, frameBlockStateValidator);
    }
    public boolean isValidAtPosition(WorldAccess world, BlockPos pos, RuleTest validator) {
        return validator.test(world.getBlockState(pos),world.getRandom());
    }
}
