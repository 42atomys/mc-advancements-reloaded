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
  public static TranslationMode criteriasTranslationMode = TranslationMode.ONLY_COMPATIBLE; // added in v0.6

  // Advanced customization
  public static int headerHeight = 48; // added in v0.2
  public static int footerHeight = 32; // added in v0.2
  public static int criteriasWidth = 142; // added in v0.2
  public static int aboveWidgetLimit = 14; // added in v0.2
  public static int belowWidgetLimit = 14; // added in v0.2

  /**
   * Enum representing different styles for background configuration.
   * 
   * <p>
   * TRANSPARENT: Background is transparent
   * </p>
   * <p>
   * ACHIEVEMENT: Background is an achievement
   * </p>
   * <p>
   * BLACK: Background is black
   * </p>
   */
  public enum BackgroundStyle {
    TRANSPARENT,
    ACHIEVEMENT,
    BLACK,
  }

  /**
   * Enum representing different translation modes.
   *
   * <p>
   * NONE: No translation
   * </p>
   * <p>
   * ONLY_COMPATIBLE: Only translate advancements that are compatible
   * </p>
   * <p>
   * ALL: Translate all advancements
   * </p>
   */
  public enum TranslationMode {
    NONE,
    ONLY_COMPATIBLE,
    TRY_TO_TRANSLATE,
  }
}
