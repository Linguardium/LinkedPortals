package mod.linguardium.linkedportals.portal.rules.base;

import com.mojang.serialization.Codec;

public interface BlockMatcherType<T extends BlockMatcher> {
    Codec<T> codec();
}
