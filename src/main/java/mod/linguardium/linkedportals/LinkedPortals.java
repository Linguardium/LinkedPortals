package mod.linguardium.linkedportals;

import mod.linguardium.linkedportals.events.EventHandler;
import mod.linguardium.linkedportals.registry.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedPortals implements ModInitializer {

	public static final String MODID = "linkedportals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		EventHandler.init();
		LinkedPortalBlocks.init();
		LinkedPortalBlockEntities.init();
		LinkedPortalItems.init();
		LinkedPortalTags.init();
		LinkedPortalRegistries.init();
		LinkedPortalRuleTestType.init();


		LOGGER.info("Hello Fabric world!");
	}
	public static Identifier id(String path) { return new Identifier(MODID, path);}
}