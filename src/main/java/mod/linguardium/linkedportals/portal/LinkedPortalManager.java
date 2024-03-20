package mod.linguardium.linkedportals.portal;

import com.google.common.collect.LinkedListMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.linguardium.linkedportals.LinkedPortals;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class LinkedPortalManager {
    private static final String PORTALS_KEY = "Portals";
    private static LinkedPortalManager instance;

    public static final Codec<Map.Entry<UUID, GlobalPos>> PORTAL_ENTRY_CODEC = RecordCodecBuilder.create(entryInstance->entryInstance.group(
            Uuids.CODEC.fieldOf("PortalId").forGetter(Map.Entry::getKey),
            GlobalPos.CODEC.fieldOf("ControllerPos").forGetter(Map.Entry::getValue)
    ).apply(entryInstance, Map::entry));
    public static final Codec<LinkedListMultimap<UUID, GlobalPos>> PORTAL_LIST_CODEC = Codec.list(PORTAL_ENTRY_CODEC).xmap(LinkedPortalManager::toMap, LinkedPortalManager::toList);

    private static List<Map.Entry<UUID, GlobalPos>> toList(LinkedListMultimap<UUID, GlobalPos> map) {
        return map.entries();
    }

    private static LinkedListMultimap<UUID, GlobalPos> toMap(List<Map.Entry<UUID, GlobalPos>> pairs) {
        LinkedListMultimap<UUID, GlobalPos> map = LinkedListMultimap.create();
        pairs.forEach(entry->map.put(entry.getKey(),entry.getValue()));
        return map;
    }


    LinkedListMultimap<UUID, GlobalPos> activatedControllers = LinkedListMultimap.create();

    public static void activateController(UUID portalId, ServerWorld world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        GlobalPos controllerPosition = GlobalPos.create(world.getRegistryKey(), pos);
        getInstance(server).activatedControllers.values().remove(controllerPosition);
        getInstance(server).activatedControllers.put(portalId, controllerPosition);
        getInstance(server).markDirty();
    }

    public static void deactivateController(ServerWorld world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        GlobalPos controllerPosition = GlobalPos.create(world.getRegistryKey(), pos);
        getInstance(server).activatedControllers.values().remove(controllerPosition);
        getInstance(server).markDirty();
    }

    public static Optional<GlobalPos> getNextPortal(UUID portalId, ServerWorld world, BlockPos pos) {
        MinecraftServer server = world.getServer();
        GlobalPos controllerPosition = GlobalPos.create(world.getRegistryKey(), pos);
        Iterator<GlobalPos> portalIter;
        List<GlobalPos> portals = getInstance(server).activatedControllers.get(portalId);
        if (portals.size() < 2) return Optional.empty();
        portalIter = portals.iterator();
        boolean found = false;
        while (portalIter.hasNext() && !found) {
            GlobalPos portal = portalIter.next();
            if (portal.equals(controllerPosition)) found = true;
        }
        if (!found) return Optional.empty();
        if (!portalIter.hasNext()) {
            return Optional.of(portals.get(0));
        }
        return Optional.of(portalIter.next());
    }


    private final MinecraftServer server;
    private final Path portalSavePath;
    private boolean dirty=false;

    public static LinkedPortalManager getInstance(MinecraftServer server) {
        if (instance == null) load(server);
        return instance;
    }

    private Path getSavePath() {
        return server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("linkedportals.nbt");
    }

    public static void load(MinecraftServer server) {
        instance = new LinkedPortalManager(server);
    }

    public LinkedPortalManager(MinecraftServer server) {
        this.server = server;
        this.portalSavePath = getSavePath();
        try {
            this.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void load() throws IOException {
        if (!Files.exists(portalSavePath.getParent())) {
            Files.createDirectories(portalSavePath.getParent());
        }
        if (Files.exists(portalSavePath)) {
            NbtCompound nbt = NbtIo.readCompressed(Files.newInputStream(this.portalSavePath, StandardOpenOption.READ), NbtSizeTracker.ofUnlimitedBytes());
            NbtElement element = nbt.get(PORTALS_KEY);
            try {
                LinkedListMultimap<UUID, GlobalPos> map = Util.getResult(PORTAL_LIST_CODEC.parse(NbtOps.INSTANCE,element), RuntimeException::new);
                activatedControllers.putAll(map);
            }catch(RuntimeException exception) {
                LinkedPortals.LOGGER.error(exception.getMessage(),exception);
            }
        }
    }

    public void save() throws IOException {
        if (!this.isDirty()) return;
        NbtCompound saveData = new NbtCompound();
        try {
            NbtElement nbtElement = Util.getResult(PORTAL_LIST_CODEC.encodeStart(NbtOps.INSTANCE, activatedControllers), RuntimeException::new);
            saveData.put(PORTALS_KEY, nbtElement);
        }catch(RuntimeException exception) {
            LinkedPortals.LOGGER.error(exception.getMessage(),exception);
        }

        Path savePath = getSavePath();
        if (!Files.exists(savePath.getParent())) {
            Files.createDirectories(savePath.getParent());
        }
        NbtIo.writeCompressed(saveData,savePath);
        this.markDirty(false);
    }
    public void markDirty() {
        this.markDirty(true);
    }
    public void markDirty(boolean dirty) {
        this.dirty=dirty;
    }
    public boolean isDirty() {
        return dirty;
    }
}
