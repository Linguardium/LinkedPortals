package mod.linguardium.linkedportals.blocks.base;

import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.blocks.blockentity.PortalStructureBlockEntity;
import mod.linguardium.linkedportals.interfaces.ReportsToController;
import mod.linguardium.linkedportals.interfaces.Tickable;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractPortalStructureBlock extends BlockWithEntity implements PolymerPortalStructureBlock, FluidFillable {

    protected AbstractPortalStructureBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(Properties.FACING, Direction.NORTH));
    }

    public Optional<PortalControlBlockEntity> getControllerBlockEntity(WorldAccess world, BlockPos reporterPosition) {
        if (world.getBlockEntity(reporterPosition) instanceof ReportsToController reporter) {
            BlockPos controllerPos = reporter.getControllerPos();
            if (controllerPos != null) {
                if (world.getBlockEntity(controllerPos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
                    return Optional.of(portalControlBlockEntity);
                }
            }
        }
        return Optional.empty();
    }
    public static boolean setPortalControllerPosition(WorldAccess world, BlockPos reporterPosition, BlockPos controllerPosition) {
        if (world.getBlockEntity(reporterPosition) instanceof ReportsToController reporter) {
            reporter.setControllerPos(controllerPosition);
            return true;
        }
        return false;
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
    }

    @Override
    public boolean canMobSpawnInside(BlockState state) {
        return false;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PortalStructureBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        getControllerBlockEntity(world,pos).ifPresent(PortalControlBlockEntity::markStructureDirty);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    public Optional<PortalStructureBlockEntity> getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PortalStructureBlockEntity portalStructureBlockEntity) return Optional.of(portalStructureBlockEntity);
        return Optional.empty();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type.equals(getBlockEntityType())) return AbstractPortalStructureBlock::tick;
        return null;
    }

    private static void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof Tickable tickable) tickable.tick(world, pos, state);
    }

    protected abstract BlockEntityType<?> getBlockEntityType();
}
