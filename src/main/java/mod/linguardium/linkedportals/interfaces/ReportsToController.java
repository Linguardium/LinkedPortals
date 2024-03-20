package mod.linguardium.linkedportals.interfaces;

import net.minecraft.util.math.BlockPos;

public interface ReportsToController extends PortalStructurePart {
    void setControllerPos(BlockPos controllerPos);
}
