package codes.atomys.advr;

import codes.atomys.advr.config.ModConfigurationFile;
import codes.atomys.advr.utils.Utils;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY;

/**
 * AdvancementsReloaded Forge Mod.
 */
// The value here should match an entry in the META-INF/mods.toml file
@Mod("advancements_reloaded")
public class AdvancementsReloadedForge {

  // Directly references a log4j logger.
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Instantiates a new AdvancementsReloaded mod for Forge.
   */
  public AdvancementsReloadedForge() {
    LOGGER.info("[AdvancementsReloaded] Starting...");

    if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
      LOGGER.warn("[AdvancementsReloaded] Not supported on dedicated server!");
    } else {
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::setup);
    }
  }

  private static final class ClientSetup {
    private static void setup() {
      setupModules();

      // Register server and game events that we are interested in.
      MinecraftForge.EVENT_BUS.register(AdvancementsReloadedForge.class);
      // Make sure the mod being absent on the other network side does not cause the
      // client to display the server
      // as incompatible.
      ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
          () -> new IExtensionPoint.DisplayTest(() -> IGNORESERVERONLY, (a, b) -> true));

      // Sets up Cloth Config if it is installed
      if (ModList.get().isLoaded("cloth_config"))
        ForgeModMenu.registerModsPage();
      else
        LOGGER.info(I18n.get("config.advancements_reloaded.need_cloth_config"));
    }

    private static void setupModules() {
      LOGGER.info("[AdvancementsReloaded] Loading...");

      Utils.modVersion(modVersion());

      // Setup config with TOML file type
      ModConfigurationFile.load(ModConfigurationFile.FileType.TOML);

      LOGGER.info("[AdvancementsReloaded] All done!");
    }

    private static String modVersion() {
      return ModList.get().getModContainerById("advancements_reloaded").orElseThrow(NullPointerException::new)
          .getModInfo().getVersion().toString();
    }
  }
}
