package mod.linguardium.linkedportals.portal;

import mod.linguardium.linkedportals.config.LinkedPortalType;
import mod.linguardium.linkedportals.portal.rules.BlockInstanceMatcher;
import mod.linguardium.linkedportals.portal.rules.base.BlockMatcher;
import mod.linguardium.linkedportals.portal.rules.OrMatcher;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import mod.linguardium.linkedportals.registry.LinkedPortalRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class PortalBuilder {
    static final BlockMatcher CONTROLLER_BLOCK_MATCHER = new BlockInstanceMatcher(LinkedPortalBlocks.PORTAL_CONTROL_BLOCK);
    static final Function<BlockMatcher,BlockMatcher> controllerBlockOrMatcher = (matcher)->new OrMatcher(CONTROLLER_BLOCK_MATCHER, matcher);

    BlockMatcher edgeValidator;
    BlockMatcher edgeOrControllerValidator;
    BlockMatcher interiorValidator;
    BlockPos startPos;
    int limit;
    Direction projectionDirection;
    Direction.Axis spanAxis;
    Direction portalFacing;
    List<Pair<BlockPos, BlockPos>> layers;
    Identifier portalType;
    Collection<RegistryEntry<LinkedPortalType>> validPortalTypes;
    BlockPos foundController = null;

    public static Optional<PortalStructure> findPortal(World world, BlockPos startPos, Collection<RegistryEntry<LinkedPortalType>> validPortalTypes) {
        PortalBuilder builder = new PortalBuilder(validPortalTypes);
        if (!builder.iteratePortalTypesToFind(world, startPos)) return Optional.empty();
        return Optional.of(builder.getStructure());
    }

    private PortalBuilder(Collection<RegistryEntry<LinkedPortalType>> validPortalTypes) {
        this.validPortalTypes = validPortalTypes;
    }

    private boolean iteratePortalTypesToFind(World world, BlockPos startFramePos) {
        this.startPos = startFramePos;
        for (RegistryEntry<LinkedPortalType> registryEntry : this.validPortalTypes) {
            LinkedPortalType type = registryEntry.value();
            this.portalType = registryEntry.getKey().orElse(LinkedPortalRegistries.ENTRY_LINKED_PORTAL_TYPE_DEFAULT_KEY).getValue();
            this.interiorValidator = type.innerBlockStateValidator();
            this.edgeValidator = type.frameBlockStateValidator();
            this.edgeOrControllerValidator = controllerBlockOrMatcher.apply(this.edgeValidator);
            this.limit = type.limit();
            if (findPortalOfType(world)) return true;
        }
        this.portalType = null;
        this.edgeValidator = null;
        this.interiorValidator = null;
        this.startPos = null;
        this.foundController = null;
        return false;
    }
    private boolean findPortalOfType(World world) {
        if (!this.edgeValidator.test(world,this.startPos)) return false;
        for (Direction projectionDirectionTry : Direction.values()) {
            this.projectionDirection = projectionDirectionTry;
            foundController = null;
            for (Direction.Axis spanAxisTry : Direction.Axis.values()) {
                this.layers = new ArrayList<>();
                this.spanAxis = spanAxisTry;
                if (this.spanAxis.equals(this.projectionDirection.getAxis())) continue;
                this.portalFacing = this.projectionDirection.rotateClockwise(spanAxis);
                if (getPortalLayers(interiorValidator.getBlockPosMatcher(world), edgeOrControllerValidator.getBlockPosMatcher(world))) return true;
            }
        }
        layers = new ArrayList<>();
        return false;
    }
    private boolean getPortalLayers(Predicate<BlockPos> interiorPosValidator, Predicate<BlockPos> framePosValidator) {
        while (this.layers.size() < this.limit-2) {
            Optional<Pair<BlockPos, BlockPos>> maybeLayer = getNextLayer(interiorPosValidator, framePosValidator);
            if (maybeLayer.isEmpty()) break;
            Pair<BlockPos, BlockPos> layer = maybeLayer.get();
            if (this.layers.isEmpty()) {
                if (!addFrameLayer(layer, this.projectionDirection, framePosValidator)) {
                    return false;
                }
            }
            this.layers.add(layer);
        }
        if (this.layers.size() < 2) return false;
        Pair<BlockPos, BlockPos> lastLayer = this.layers.get(this.layers.size()-1);
        if (!addFrameLayer(lastLayer, this.projectionDirection.getOpposite(), framePosValidator)) return false;
        return checkLayerBounds();
    }
    private boolean checkLayerBounds() {
        if (layers.size() < 3) return false;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 1; i < layers.size(); i++) {
            Pair<BlockPos, BlockPos> thisLayer = layers.get(i);
            Pair<BlockPos, BlockPos> prevLayer = layers.get(i-1);
            if (!leftAndRightEdgesWithinRange(thisLayer,prevLayer)) return false;
            int leftAxisValue = thisLayer.getLeft().getComponentAlongAxis(this.spanAxis);
            int rightAxisValue = thisLayer.getRight().getComponentAlongAxis(this.spanAxis);
            min = Math.min(min, Math.min(leftAxisValue, rightAxisValue));
            max = Math.max(max, Math.max(leftAxisValue, rightAxisValue));
        }
        if (max - min >= this.limit || max - min < 0) return false;
        return true;
    }
    private boolean leftAndRightEdgesWithinRange(Pair<BlockPos, BlockPos>  pos1, Pair<BlockPos, BlockPos>  pos2) {
        return pos1.getLeft().getManhattanDistance(pos2.getLeft()) <= 2 && pos1.getRight().getManhattanDistance(pos2.getRight()) <= 2;
    }
    private boolean addFrameLayer(Pair<BlockPos, BlockPos> comparisonLayer, Direction offsetDirection, Predicate<BlockPos> frameValidator) {
        Pair<BlockPos, BlockPos> frameLayer = new Pair<>(null,null);
        int distance = comparisonLayer.getRight().getComponentAlongAxis(this.spanAxis) - comparisonLayer.getLeft().getComponentAlongAxis(this.spanAxis);
        distance--;
        frameLayer.setLeft(comparisonLayer.getLeft().offset(offsetDirection.getOpposite()).offset(this.spanAxis,1));
        if (!frameValidator.test(frameLayer.getLeft())) return false;
        Optional<BlockPos> foundPos = moveAlong(frameLayer.getLeft(),Direction.from(this.spanAxis, Direction.AxisDirection.POSITIVE),distance,frameValidator,pos->true);
        if (foundPos.isEmpty()) return false;
        frameLayer.setRight(foundPos.get());
        layers.add(frameLayer);
        return true;
    }

    private int getFirstEdgeDistanceFromCenter() {
        if (layers.isEmpty()) return this.limit;
        return 2;
    }
    private int getNextSecondEdgeDistanceFromFirstEdge() {
        if (this.layers.isEmpty()) return this.limit;
        Pair<BlockPos, BlockPos> lastLayer = this.layers.get(this.layers.size() - 1);
        return 2 + lastLayer.getRight().getComponentAlongAxis(this.spanAxis) - lastLayer.getLeft().getComponentAlongAxis(this.spanAxis);
    }
    private BlockPos getNextCenterPos(Direction projectionDirection, Direction.Axis spanAxis) {
        if (this.layers.isEmpty()) return this.startPos.offset(projectionDirection);
        return this.layers.get(layers.size() - 1).getLeft().offset(projectionDirection).offset(spanAxis,1);
    }
    private Optional<Pair<BlockPos, BlockPos>> getNextLayer(Predicate<BlockPos> interiorValidator, Predicate<BlockPos> frameValidator) {
        Pair<BlockPos,BlockPos> foundPair = new Pair<>(null,null);
        Optional<BlockPos> foundPos = moveAlong(getNextCenterPos(this.projectionDirection,spanAxis), Direction.from(this.spanAxis, Direction.AxisDirection.NEGATIVE), getFirstEdgeDistanceFromCenter(),interiorValidator,frameValidator);
        if (foundPos.isEmpty()) return Optional.empty();
        foundPair.setLeft(foundPos.get().offset(this.spanAxis,-1));
        foundPos = moveAlong(foundPos.get(),Direction.from(this.spanAxis, Direction.AxisDirection.POSITIVE),getNextSecondEdgeDistanceFromFirstEdge(), interiorValidator, frameValidator);
        if (foundPos.isPresent()) {
            foundPair.setRight(foundPos.get().offset(this.spanAxis,1));
            int width = Math.abs(foundPair.getLeft().subtract(foundPair.getRight()).getComponentAlongAxis(this.spanAxis));
            if (width < 2 || width > this.limit) return Optional.empty();
            return Optional.of(foundPair);
        }
        return Optional.empty();
    }

    private Optional<BlockPos> moveAlong(BlockPos startFromPos, Direction travelDirection, int limit, Predicate<BlockPos> continueMatcher, Predicate<BlockPos> edgeMatcher) {
        int distance = 0;
        BlockPos.Mutable pos = startFromPos.mutableCopy();
        while (++distance <= limit && continueMatcher.test(pos)) {
            pos.move(travelDirection);
        }
        if (edgeMatcher.test(pos)) return Optional.of(pos.move(travelDirection.getOpposite()).toImmutable());

        return Optional.empty();
    }
    private PortalStructure getStructure() {
        int layerCount = this.layers.size();
        if (layerCount < 3) throw new RuntimeException("Tried to get structure from an incomplete portal builder!");

        Set<BlockPos> frames = new HashSet<>();
        Set<BlockPos> teleporters = new HashSet<>();
        int lastLayerIndex = layerCount - 1;

        for (int i = 0; i < layerCount; i++) {
            Pair<BlockPos, BlockPos> layer = this.layers.get(i);
            if (i == 0 || i == lastLayerIndex) {
                BlockPos.iterate(layer.getLeft(),layer.getRight()).forEach(mutablePos->frames.add(mutablePos.toImmutable()));
                continue;
            }
            frames.add(layer.getLeft());
            frames.add(layer.getRight());
            BlockPos.iterate(layer.getLeft().offset(this.spanAxis,1),layer.getRight().offset(this.spanAxis,-1)).forEach(mutablePos->teleporters.add(mutablePos.toImmutable()));
        }
        frames.removeIf(pos->pos.equals(foundController));
        return new PortalStructure(List.copyOf(frames),List.copyOf(teleporters),this.portalFacing,this.portalType);
    }

}
