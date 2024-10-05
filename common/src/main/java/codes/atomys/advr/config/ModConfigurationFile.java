package codes.atomys.advr.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The ModConfigurationFile class is a utility class responsible for saving and
 * loading
 * configuration settings for the "advancements_reloaded" mod. It supports two
 * file types: JSON and TOML.
 *
 * @see Configuration
 */
public final class ModConfigurationFile {

  /**
   * Private constructor to prevent instantiation of the utility class.
   * Throws {@link UnsupportedOperationException} if called.
   */
  private ModConfigurationFile() {
    throw new UnsupportedOperationException("Utility class");
  }

  private static FileType storedFileType;

  /**
   * A Runnable that saves the current configuration settings to a file.
   * The file format is determined by the storedFileType (either JSON or TOML).
   * If the file does not exist, the necessary directories are created.
   * The configuration is saved concurrently and with autosave enabled.
   */
  public final static Runnable saveRunnable = () -> {
    final Path path = Paths.get(
        storedFileType == FileType.JSON ? "config/advancements_reloaded.json" : "config/advancements_reloaded.toml");

    final File file = path.toFile();
    if (!file.exists())
      file.getParentFile().mkdirs();

    final FileConfig config = FileConfig.builder(path).concurrent().autosave().build();

    final Config appearance = Config.inMemory();
    appearance.set("display_sideabar", Configuration.displaySidebar);
    appearance.set("display_description", Configuration.displayDescription);
    appearance.set("alphabetic_order", Configuration.alphabeticOrder);
    appearance.set("background_style", Configuration.backgroundStyle.name());

    final Config advancedCustomization = Config.inMemory();
    advancedCustomization.set("header_height", Configuration.headerHeight);
    advancedCustomization.set("footer_height", Configuration.footerHeight);
    advancedCustomization.set("criterias_width", Configuration.criteriasWidth);
    advancedCustomization.set("above_widget_limit", Configuration.aboveWidgetLimit);
    advancedCustomization.set("below_widget_limit", Configuration.belowWidgetLimit);

    config.set("appearance", appearance);
    config.set("advanced_customization", advancedCustomization);

    config.close();
  };

  /**
   * Load the configuration from the file of the given filetype.
   * If the file does not exist, do nothing.
   * If the file contains invalid data, the invalid data is ignored.
   *
   * @param filetype the type of the file to load from.
   */
  public static void load(final FileType filetype) {
    storedFileType = filetype;
    final File file = new File(
        storedFileType == FileType.JSON ? "config/advancements_reloaded.json" : "config/advancements_reloaded.toml");

    if (!file.exists()) {
      return;
    }

    final FileConfig config = FileConfig.builder(file).concurrent().autosave().build();

    config.load();
    final Config appearance = config.getOrElse("appearance", () -> null);
    final Config advancedCustomization = config.getOrElse("advanced_customization", () -> null);

    if (appearance == null) {
      config.close();
      return;
    }

    Configuration.displaySidebar = appearance.getOrElse("display_sideabar", true);
    Configuration.displayDescription = appearance.getOrElse("display_description", true);
    Configuration.alphabeticOrder = appearance.getOrElse("alphabetic_order", true);
    Configuration.backgroundStyle = Configuration.BackgroundStyle
        .valueOf(appearance.getOrElse("background_style", "TRANSPARENT").toUpperCase());

    Configuration.headerHeight = advancedCustomization.getOrElse("header_height", 48);
    Configuration.footerHeight = advancedCustomization.getOrElse("footer_height", 32);
    Configuration.criteriasWidth = advancedCustomization.getOrElse("criterias_width", 142);
    Configuration.aboveWidgetLimit = advancedCustomization.getOrElse("above_widget_limit", 14);
    Configuration.belowWidgetLimit = advancedCustomization.getOrElse("below_widget_limit", 14);

    config.close();

  }

  /**
   * Enum representing the types of configuration files supported by the
   * application.
   * <p>
   * This enum defines the following file types:
   * </p>
   * <ul>
   * <li>{@link #JSON} - Represents a JSON configuration file.</li>
   * <li>{@link #TOML} - Represents a TOML configuration file.</li>
   * </ul>
   */
  public enum FileType {
    JSON,
    TOML
  }

}
