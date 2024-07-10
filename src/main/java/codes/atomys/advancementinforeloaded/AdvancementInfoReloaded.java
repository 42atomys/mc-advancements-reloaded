package codes.atomys.advancementinforeloaded;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementInfoReloaded implements ModInitializer {

  public static final String MOD_ID = "advancementinforeloaded";
  public static final Style SUCCESS_STYLE = Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.GREEN));
  public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.RED));

  public static final AdvancementInfoReloadedConfig CONFIG = AdvancementInfoReloadedConfig.createAndLoad();

  // This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
  public static final Logger LOGGER = LoggerFactory.getLogger("AdvancementInfoReloaded");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}

  public static AdvancementInfoReloadedConfig getConfig() {
    return CONFIG;
  }
}
