package mod.linguardium.linkedportals.exceptions;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

public class Exceptions {

    public static class MissingPortalBlockEntityException extends Exception {
        public MissingPortalBlockEntityException(String message) {
            super(message);
        }
    }
    public static class UnexpectedAtBlockPosException extends Exception {
        protected UnexpectedAtBlockPosException(String type, World world, BlockPos pos, Object expected, Object found, Function<Object, String> lookup) {
                    super(String.format(
                    "Unexpected %s found in world %s(%s) at %s. Expected: %s(%s) found %s(%s)",
                    type,
                    world.getRegistryKey().getValue(),
                    world.getClass().getName(),
                    pos.toString(),
                    lookup.apply(expected),
                    expected.getClass().getName(),
                    lookup.apply(found),
                    found.getClass().getName()
            ));
        }
        protected UnexpectedAtBlockPosException(String type, World world, BlockPos pos, String expected, String found) {
            super(String.format(
                    "Unexpected %s found in world %s(%s) at %s. Expected: %s but found %s",
                    type,
                    world.getRegistryKey().getValue(),
                    world.getClass().getName(),
                    pos.toString(),
                    expected,
                    found
            ));
        }

    }
    public static class UnexpectedBlockException extends UnexpectedAtBlockPosException {
        protected UnexpectedBlockException(String type, World world, BlockPos pos, Object expected, Object found, Function<Object, String> lookup) {
            super(type, world, pos, expected, found, lookup);
        }
        public UnexpectedBlockException at(World world, BlockPos pos, Block expected, Block found) {
            return new UnexpectedBlockException("Block", world, pos, expected, found, obj->Registries.BLOCK.getId((Block)obj).toString());
        }
    }
    public static class UnexpectedBlockEntityException extends UnexpectedAtBlockPosException {
        protected UnexpectedBlockEntityException(String type, World world, BlockPos pos, Object expected, Object found, Function<Object, String> lookup) {
            super(type, world, pos, expected, found, lookup);
        }
        public static UnexpectedBlockEntityException at(World world, BlockPos pos, BlockEntityType<?> expected, BlockEntityType<?> found) {
            return new UnexpectedBlockEntityException("BlockEntity", world, pos, expected, found, obj-> Optional.ofNullable(Registries.BLOCK_ENTITY_TYPE.getId(((BlockEntityType<?>)obj))).map(Identifier::toString).orElse("[Unknown Block Entity Type]"));
        }
    }
    public static class BlockEntityNotFoundException extends UnexpectedAtBlockPosException {
        protected BlockEntityNotFoundException(String type, World world, BlockPos pos, String expected, String found) {
            super(type, world, pos, expected, found);
        }
        public static BlockEntityNotFoundException at(World world, BlockPos pos, BlockEntityType<?> expected) {
            return new BlockEntityNotFoundException("BlockEntity", world, pos, Optional.ofNullable(Registries.BLOCK_ENTITY_TYPE.getId(expected)).map(Identifier::toString).orElse("UnknownType").concat("(".concat(expected.getClass().getName()).concat(")")),"nothing");
        }
    }
}
