package mod.linguardium.linkedportals.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.linguardium.linkedportals.config.LinkedPortalType;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import mod.linguardium.linkedportals.registry.LinkedPortalRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldAccess;

import java.util.List;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public record PortalStructure(List<BlockPos> frames, List<BlockPos> teleporters, Direction facing, Identifier portalType) {
    public static final PortalStructure EMPTY = new PortalStructure(List.of(), List.of(), Direction.NORTH, id("default"));
    public static Codec<PortalStructure> CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.list(BlockPos.CODEC).fieldOf("FramePositions").forGetter(PortalStructure::frames),
            Codec.list(BlockPos.CODEC).fieldOf("TeleporterPositions").forGetter(PortalStructure::teleporters),
            Direction.CODEC.fieldOf("Facing").forGetter(PortalStructure::facing),
            Identifier.CODEC.fieldOf("PortalType").forGetter(PortalStructure::portalType)
    ).apply(instance, PortalStructure::new));

    public boolean isEmpty() {
        return this.frames.isEmpty() && this.teleporters.isEmpty();
    }

    public boolean validate(WorldAccess world) {
        final LinkedPortalType type = LinkedPortalRegistries.portalTypes().getOrEmpty(portalType()).orElse(null);
        if (type == null) return false;
        return frames.stream().allMatch(pos->type.isValidFrameAtPosition(world,pos)) &&
                teleporters.stream().allMatch(pos->world.getBlockState(pos).isOf(LinkedPortalBlocks.PORTAL_FILL_BLOCK));
    }
    public Vec3d getLowestTeleportPos() {
        if (facing().getAxis().equals(Direction.Axis.Y)) {
            Vec3d center = BlockBox.encompassPositions(teleporters()).map(box->Box.from(box).getCenter()).orElse(null);
            if (center != null) return center;
        }
        BlockPos lowestBlockPos = null;
        for (BlockPos checkPos : teleporters()) {
            if (lowestBlockPos == null || checkPos.getY() < lowestBlockPos.getY()) lowestBlockPos = checkPos;
        }
        if (lowestBlockPos == null) return null;
        return lowestBlockPos.toCenterPos().withAxis(Direction.Axis.Y, lowestBlockPos.getY());
    }
}
