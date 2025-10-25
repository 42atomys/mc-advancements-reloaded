package codes.atomys.advr.utils;

import codes.atomys.advr.screens.AdvancementReloadedWidget;
import org.jetbrains.annotations.Nullable;

/**
 * Memory module are used to save a state on the current playing session.
 */
public final class Memory {
  private static AdvancementReloadedWidget currentWidget;
  private static String currentTabId;

  // Private constructor to prevent instantiation
  private Memory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Set the current widget being hovered/rendered. This is used to save the state
   * of the widget when the window is closed.
   *
   * @param widget the widget to be saved
   */
  public static void setWidget(final AdvancementReloadedWidget widget) {
    currentWidget = widget;
  }

  /**
   * Get the current widget being hovered/rendered. This is used to save the state
   * of the widget when the window is closed.
   *
   * @return the current widget being hovered/rendered. This is used to save the
   *         state of the widget when the window is closed.
   */
  public static AdvancementReloadedWidget getWidget() {
    return currentWidget;
  }

  /**
   * Set the current selected tab ID. This is used to preserve the selected tab
   * during advancement reloads (e.g., when /reload command is run).
   *
   * @param tabId the tab ID (ResourceLocation as string) to be saved, or null to clear
   */
  public static void setTabId(@Nullable final String tabId) {
    currentTabId = tabId;
  }

  /**
   * Get the current selected tab ID. This is used to restore the selected tab
   * after advancement reloads.
   *
   * @return the current selected tab ID (ResourceLocation as string), or null if none is saved
   */
  @Nullable
  public static String getTabId() {
    return currentTabId;
  }

  /**
   * Clear the stored tab ID from memory.
   */
  public static void clearTabId() {
    currentTabId = null;
  }

}
