package mod.linguardium.linkedportals.blocks.blockentity;

import mod.linguardium.linkedportals.interfaces.ReportsToController;
import mod.linguardium.linkedportals.interfaces.Tickable;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PortalStructureBlockEntity extends BlockEntity implements ReportsToController, Tickable {
    private static final String CONTROLLER_POS_KEY = "ControllerPos";
    BlockPos controlBlockPos;

    public PortalStructureBlockEntity(BlockPos pos, BlockState state) {
        super(LinkedPortalBlockEntities.PORTAL_STRUCTURE_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(CONTROLLER_POS_KEY, NbtElement.COMPOUND_TYPE)) {
            this.controlBlockPos = NbtHelper.toBlockPos(nbt.getCompound(CONTROLLER_POS_KEY));
        }
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (controlBlockPos != null) {
            nbt.put(CONTROLLER_POS_KEY, NbtHelper.fromBlockPos(controlBlockPos));
        }
    }

    @Override
    public @Nullable BlockPos getControllerPos() {
        return controlBlockPos;
    }

    @Override
    public void setControllerPos(BlockPos controllerPos) {
        this.controlBlockPos = controllerPos;
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        // placeholder
    }


}
