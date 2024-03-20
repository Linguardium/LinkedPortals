package mod.linguardium.linkedportals.misc;

import com.mojang.serialization.Codec;
import mod.linguardium.linkedportals.registry.LinkedPortalTags;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.math.random.Random;

import static mod.linguardium.linkedportals.registry.LinkedPortalRuleTestType.DEFAULT_FRAME_RULE_TEST_TYPE;

public class DefaultFrameRuleTest extends RuleTest {
    public static final DefaultFrameRuleTest INSTANCE = new DefaultFrameRuleTest();
    public static final Codec<DefaultFrameRuleTest> CODEC = Codec.unit(() -> INSTANCE);

    private DefaultFrameRuleTest() { }

    @Override
    public boolean test(BlockState state, Random random) {
        return state.isIn(LinkedPortalTags.VALID_PORTAL_FRAMES);
    }

    @Override
    protected RuleTestType<?> getType() {
        return DEFAULT_FRAME_RULE_TEST_TYPE;
    }
}
