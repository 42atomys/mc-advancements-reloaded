package codes.atomys.advr.utils;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for common functions used in AdvancementsReloaded.
 */
public final class Utils {

  // Private constructor to prevent instantiation
  private Utils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static final Style SUCCESS_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(5635925));
  public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(16733525));
  public static final Logger LOGGER = LoggerFactory.getLogger("AdvancementsReloaded");

  private static String modVersion;

  /**
   * Gets the mod version.
   *
   * @return the mod version
   */
  public static String modVersion() {
    return modVersion;
  }

  /**
   * Sets the mod name.
   *
   * @param version the mod version
   */
  public static void modVersion(final String version) {
    modVersion = version;
  }

}
