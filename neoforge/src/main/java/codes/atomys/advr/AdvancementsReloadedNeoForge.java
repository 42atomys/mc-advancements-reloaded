package codes.atomys.advr;

import codes.atomys.advr.config.ModConfigurationFile;
import codes.atomys.advr.utils.Utils;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * AdvancementsReloaded NeoForge Mod.
 */
// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod("advancements_reloaded")
public class AdvancementsReloadedNeoForge {

  // Directly references a log4j logger.
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Instantiates a new AdvancementsReloaded mod for NeoForge.
   *
   * @param eventBus the event bus
   */
  public AdvancementsReloadedNeoForge(final IEventBus eventBus) {
    LOGGER.info("[AdvancementsReloaded] Starting...");

    if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
      LOGGER.warn("[AdvancementsReloaded] Not supported on dedicated server!");
    } else {
      ClientSetup.setup(eventBus);
    }
  }

  private static final class ClientSetup {
    private static void setup(final IEventBus eventBus) {
      setupModules();

      // Sets up Cloth Config if it is installed
      if (ModList.get().isLoaded("cloth_config"))
        NeoForgeModMenu.registerModsPage();
      else
        LOGGER.info(I18n.get("config.advancements_reloaded.need_cloth_config"));
    }

    private static void setupModules() {
      LOGGER.info("[AdvancementsReloaded] Loading...");

      Utils.modVersion(modVersion());

      // Setup config with JSON file type
      ModConfigurationFile.load(ModConfigurationFile.FileType.JSON);

      LOGGER.info("[AdvancementsReloaded] All done!");
    }

    private static String modVersion() {
      return ModList.get().getModContainerById("advancements_reloaded").orElseThrow(NullPointerException::new)
          .getModInfo().getVersion().toString();
    }
  }
}
