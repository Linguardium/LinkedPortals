package mod.linguardium.linkedportals.registry;

import mod.linguardium.linkedportals.misc.DefaultFrameRuleTest;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.rule.RuleTestType;

import static mod.linguardium.linkedportals.LinkedPortals.id;


public class LinkedPortalRuleTestType {
    public static final RuleTestType<DefaultFrameRuleTest> DEFAULT_FRAME_RULE_TEST_TYPE = Registry.register(Registries.RULE_TEST, id("default_frame"), ()->DefaultFrameRuleTest.CODEC);

    public static void init() {
        // included to allow class load from init
    }
}
