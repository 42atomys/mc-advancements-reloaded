package codes.atomys.advr.config;

import java.util.List;

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
  public static AdvancementOrder advancementsOrder = AdvancementOrder.ALPHABETIC; // added in v0.11, ALPHABETIC by default
  public static TabOrder tabsOrder = TabOrder.ALPHABETIC; // added in v0.6, ALPHABETIC by default
  public static BackgroundStyle backgroundStyle = BackgroundStyle.TRANSPARENT; // added in v0.4
  public static TranslationMode criteriasTranslationMode = TranslationMode.ONLY_COMPATIBLE; // added in v0.6

  // Advanced customization
  public static int headerHeight = 48; // added in v0.2
  public static int footerHeight = 48; // added in v0.2 updated in v0.9
  public static int criteriasWidth = 142; // added in v0.2
  public static int aboveWidgetLimit = 14; // added in v0.2
  public static int belowWidgetLimit = 14; // added in v0.2
  public static List<String> customTabsOrder = List.of(); // added in v0.10
  public static List<String> customAdvancementsOrder = List.of(); // added in v0.11

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

  /**
   * Enum representing the order in which tabs can be displayed.
   *
   * <p>
   * NONE: Tabs are not ordered (vanilla placement)
   * </p>
   * <p>
   * ALPHABETIC: Tabs are ordered alphabetically, added in v0.6 (default)
   * </p>
   * <p>
   * CONFIGURED_ORDER: Tabs are ordered based on a custom order defined in the
   * configuration file,
   * added in v0.10
   * </p>
   */
  public enum TabOrder {
    // Tabs are not ordered (vanilla placement)
    NONE,
    // Tabs are ordered alphabetically, added in v0.6 (default)
    ALPHABETIC,
    // Tabs are ordered based on a custom order defined in the configuration file,
    // added in v0.10
    CONFIGURED_ORDER,
  }

  /**
   * Enum representing the order in which advancement rows/branches can be displayed.
   *
   * <p>
   * NONE: Advancements are not ordered (vanilla placement based on disk read order)
   * </p>
   * <p>
   * ALPHABETIC: Advancements are ordered alphabetically by title, added in v0.11 (default)
   * </p>
   * <p>
   * CONFIGURED_ORDER: Advancements are ordered based on a custom order defined in the
   * configuration file,
   * added in v0.11
   * </p>
   */
  public enum AdvancementOrder {
    // Advancements are not ordered (vanilla placement based on disk read order)
    NONE,
    // Advancements are ordered alphabetically by title, added in v0.11 (default)
    ALPHABETIC,
    // Advancements are ordered based on a custom order defined in the configuration file,
    // added in v0.11
    CONFIGURED_ORDER,
  }
}
