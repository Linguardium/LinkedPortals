package mod.linguardium.linkedportals.interfaces;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface PortalStructurePart {
    @Nullable BlockPos getControllerPos();
}
