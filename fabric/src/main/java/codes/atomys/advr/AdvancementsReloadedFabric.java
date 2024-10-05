package codes.atomys.advr;

import codes.atomys.advr.config.ModConfigurationFile;
import codes.atomys.advr.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main class for the "Advancements Reloaded" mod for Fabric.
 *
 * <p>
 * This class implements the {@link ClientModInitializer} interface, which is
 * a special interface for Fabric mods that want to run code on the client side
 * when the mod is initialized.
 * </p>
 *
 * <p>
 * In this class, the {@link #onInitializeClient()} method is overridden to
 * print a message to the console, print the version of the mod, and load the
 * configuration file with the {@link ModConfigurationFile.FileType#JSON} type.
 * </p>
 *
 * @see ClientModInitializer
 */
public class AdvancementsReloadedFabric implements ClientModInitializer {

  public final Logger logger = LogManager.getLogger("advancements_reloaded");

  /**
   * This method is called once the mod is initialized on the client side.
   * <p>
   * It logs messages to the console, prints the version of the mod, and loads
   * the configuration file with the {@link ModConfigurationFile.FileType#JSON}
   * file type.
   * </p>
   */
  @Override
  public void onInitializeClient() {
    this.logger.info("[Advancements Reloaded] Loading...");

    Utils.modVersion(this.modVersion());

    // Setup config with JSON file type
    ModConfigurationFile.load(ModConfigurationFile.FileType.JSON);

    this.logger.info("[Advancements Reloaded] All done!");
  }

  /**
   * Returns the version of the mod as a string.
   *
   * <p>
   * The method uses the {@link FabricLoader} to get the mod container for the
   * "advancements_reloaded" mod, and then retrieves the mod's metadata and
   * version.
   * </p>
   *
   * <p>
   * The method returns the friendly string representation of the version.
   * </p>
   *
   * @return the version of the mod as a string
   */
  private String modVersion() {
    return FabricLoader.getInstance().getModContainer("advancements_reloaded").orElseThrow(NullPointerException::new)
        .getMetadata().getVersion().getFriendlyString();
  }
}
