package codes.atomys.advr.utils;

import codes.atomys.advr.screens.AdvancementReloadedWidget;

/**
 * Memory module are used to save a state on the current playing session.
 */
public final class Memory {
  private static AdvancementReloadedWidget currentWidget;

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

}
