package codes.atomys.advr.config.gui;

import codes.atomys.advr.config.Configuration;
import codes.atomys.advr.config.ModConfigurationFile;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The ConfigurationScreen class provides a GUI screen for configuring various
 * settings
 * related to the advancements reloaded mod. This class is final and cannot be
 * instantiated.
 * 
 * <p>
 * It contains methods to create a configuration screen and build the
 * configuration
 * settings using a ConfigBuilder.
 * </p>
 * 
 * <p>
 * Methods:
 * </p>
 * <ul>
 * <li>{@link #screen(Screen)}: Creates and returns a configuration screen.</li>
 * <li>{@link #configBuilder(Screen)}: Builds and returns a ConfigBuilder with
 * various
 * configuration settings.</li>
 * </ul>
 * 
 * <p>
 * Each setting is configurable through a GUI element such as a boolean toggle,
 * dropdown menu,
 * or integer slider, and includes tooltips for additional information.
 * </p>
 * 
 * <p>
 * The settings are saved using save consumers that update the corresponding
 * fields in the
 * Configuration class.
 * </p>
 */
public final class ConfigurationScreen {

  /**
   * Private constructor to prevent instantiation of the utility class.
   * Throws {@link UnsupportedOperationException} if called.
   */
  private ConfigurationScreen() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Creates and returns a configuration screen.
   *
   * <p>
   * The method returns a Screen object that contains a configuration screen
   * with various
   * settings related to the advancements reloaded mod. The settings are
   * configurable
   * through a GUI element such as a boolean toggle, dropdown menu, or
   * integer slider,
   * and include tooltips for additional information.
   * </p>
   *
   * <p>
   * The settings are saved using save consumers that update the corresponding
   * fields in the
   * Configuration class.
   * </p>
   *
   * @param parent the parent screen.
   * @return a configuration screen.
   */
  public static Screen screen(final Screen parent) {
    return configBuilder(parent).build();
  }

  /**
   * Builds and returns a ConfigBuilder with various configuration settings.
   *
   * <p>
   * The method returns a ConfigBuilder object that contains various settings
   * related to the advancements reloaded mod. The settings are configurable
   * through a GUI element such as a boolean toggle, dropdown menu, or
   * integer slider,
   * and include tooltips for additional information.
   * </p>
   *
   * <p>
   * The settings are saved using save consumers that update the corresponding
   * fields in the
   * Configuration class.
   * </p>
   *
   * @param parent the parent screen.
   * @return a ConfigBuilder with various configuration settings.
   */
  public static ConfigBuilder configBuilder(final Screen parent) {

    final ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTransparentBackground(true)
        .setTitle(Component.translatable("text.config.advancements_reloaded.title"))
        .setSavingRunnable(ModConfigurationFile.saveRunnable);

    createApparanceEntries(builder);
    createAdvancedCustomizationEntries(builder);

    return builder;
  }

  private static void createApparanceEntries(final ConfigBuilder builder) {
    final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    final ConfigCategory appearance = builder
        .getOrCreateCategory(Component.translatable("text.config.advancements_reloaded.section.appearance"));

    appearance.addEntry(
        entryBuilder
            .startBooleanToggle(Component.translatable("text.config.advancements_reloaded.option.display_sidebar"),
                Configuration.displaySidebar)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.display_sidebar.tooltip"))
            .setSaveConsumer(newValue -> Configuration.displaySidebar = newValue)
            .build());

    appearance.addEntry(
        entryBuilder
            .startBooleanToggle(Component.translatable("text.config.advancements_reloaded.option.display_description"),
                Configuration.displayDescription)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.display_description.tooltip"))
            .setSaveConsumer(newValue -> Configuration.displayDescription = newValue)
            .build());

    appearance.addEntry(
        entryBuilder
            .startBooleanToggle(Component.translatable("text.config.advancements_reloaded.option.alphabetic_order"),
                Configuration.alphabeticOrder)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.alphabetic_order.tooltip"))
            .setSaveConsumer(newValue -> Configuration.alphabeticOrder = newValue)
            .build());

    appearance.addEntry(
        entryBuilder
            .startEnumSelector(Component.translatable("text.config.advancements_reloaded.option.background_style"),
                Configuration.BackgroundStyle.class, Configuration.backgroundStyle)
            .setDefaultValue(Configuration.BackgroundStyle.TRANSPARENT)
            .setTooltip(
                Component.translatable("text.config.advancements_reloaded.option.background_style.tooltip"))
            .setSaveConsumer(newValue -> Configuration.backgroundStyle = newValue)
            .build());
  }

  private static void createAdvancedCustomizationEntries(final ConfigBuilder builder) {
    final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    final ConfigCategory advancedCustomization = builder.getOrCreateCategory(
        Component.translatable("text.config.advancements_reloaded.section.advanced_customization"));

    advancedCustomization.addEntry(
        entryBuilder
            .startIntSlider(Component.translatable("text.config.advancements_reloaded.option.header_height"),
                Configuration.headerHeight, 32, 128)
            .setDefaultValue(48)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.header_height.tooltip"))
            .setSaveConsumer(newValue -> Configuration.headerHeight = newValue)
            .build());

    advancedCustomization.addEntry(
        entryBuilder
            .startIntSlider(Component.translatable("text.config.advancements_reloaded.option.footer_height"),
                Configuration.footerHeight, 32, 128)
            .setDefaultValue(32)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.footer_height.tooltip"))
            .setSaveConsumer(newValue -> Configuration.footerHeight = newValue)
            .build());

    advancedCustomization.addEntry(
        entryBuilder
            .startIntSlider(Component.translatable("text.config.advancements_reloaded.option.criterias_width"),
                Configuration.criteriasWidth, 50, 512)
            .setDefaultValue(142)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.criterias_width.tooltip"))
            .setSaveConsumer(newValue -> Configuration.criteriasWidth = newValue)
            .build());

    advancedCustomization.addEntry(
        entryBuilder
            .startIntSlider(Component.translatable("text.config.advancements_reloaded.option.above_widget_limit"),
                Configuration.aboveWidgetLimit, 0, 42)
            .setDefaultValue(14)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.above_widget_limit.tooltip"))
            .setSaveConsumer(newValue -> Configuration.aboveWidgetLimit = newValue)
            .build());

    advancedCustomization.addEntry(
        entryBuilder
            .startIntSlider(Component.translatable("text.config.advancements_reloaded.option.below_widget_limit"),
                Configuration.belowWidgetLimit, 0, 42)
            .setDefaultValue(14)
            .setTooltip(Component.translatable("text.config.advancements_reloaded.option.below_widget_limit.tooltip"))
            .setSaveConsumer(newValue -> Configuration.belowWidgetLimit = newValue)
            .build());
  }
}
