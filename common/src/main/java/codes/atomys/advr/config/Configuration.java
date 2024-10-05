package codes.atomys.advr.config;

/**
 * General Options for the config.
 */
public final class Configuration {

  /**
   * Private constructor to prevent instantiation of the utility class.
   * Throws {@link UnsupportedOperationException} if called.
   */
  private Configuration() {
    throw new UnsupportedOperationException("Utility class");
  }

  // appearance section
  public static boolean displaySidebar = true; // added in v0.3
  public static boolean displayDescription = true; // added in v0.2
  public static boolean criteriasAlphabeticOrder = true; // added in v0.3, true by default in v0.5
  public static boolean tabsAlphabeticOrder = true; // added in v0.6
  public static BackgroundStyle backgroundStyle = BackgroundStyle.TRANSPARENT; // added in v0.4

  // Advanced customization
  public static int headerHeight = 48; // added in v0.2
  public static int footerHeight = 32; // added in v0.2
  public static int criteriasWidth = 142; // added in v0.2
  public static int aboveWidgetLimit = 14; // added in v0.2
  public static int belowWidgetLimit = 14; // added in v0.2

  /**
   * Enum representing different styles for background configuration.
   */
  public enum BackgroundStyle {
    TRANSPARENT,
    ACHIEVEMENT,
    BLACK,
  }
}
