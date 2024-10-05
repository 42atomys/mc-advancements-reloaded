package codes.atomys.advr;

import codes.atomys.advr.config.Configuration;
import java.lang.reflect.Field;

/**
 * Enum representing the placement of tabs in the GUI.
 *
 * <p>
 * This enum defines the possible placements of tabs in the GUI, along with
 * their dimensions and margins. It provides methods to calculate the position
 * of tabs based on their index, and to retrieve the dimensions and margins of
 * the tabs.
 * </p>
 *
 * <p>
 * The placement of tabs can be either ABOVE or BELOW, each with specific width,
 * height, top margin, and left margin values.
 * </p>
 *
 * <p>
 * The tab limit for each placement is dynamically determined by reading the
 * configuration values from the Configuration class.
 * </p>
 *
 * <p>
 * The x and y positions of tabs are calculated based on their index and the
 * dimensions of the tabs. The width and height of the tabs include an
 * additional
 * 2 pixels for the border around the tabs.
 * </p>
 *
 * <p>
 * The top and left margins of the tabs are also provided, representing the
 * distance from the top and left sides of the screen to the top and left sides
 * of the tabs, respectively.
 * </p>
 *
 * @see codes.atomys.advr.config.Configuration
 */
public enum TabPlacement {

  ABOVE(26, 26, 5, 5),
  BELOW(26, 26, 5, 5);

  private final int width;
  private final int height;
  private final int topMargin;
  private final int leftMargin;

  /**
   * Constructs a TabPlacement object with specified dimensions and margins.
   *
   * @param width      the width of the tab
   * @param height     the height of the tab
   * @param topMargin  the top margin of the tab
   * @param leftMargin the left margin of the tab
   */
  TabPlacement(final int width, final int height, final int topMargin, final int leftMargin) {
    this.width = width;
    this.height = height;
    this.topMargin = topMargin;
    this.leftMargin = leftMargin;
  }

  /**
   * Get the limit of tabs on this placement based on the configuration.
   *
   * @return The limit of tabs on this side.
   */
  public int getTabLimit() {
    return this.getDynamicLimit();
  }

  /**
   * Dynamically get the widget limit for this placement from the Configuration
   * class.
   *
   * <p>
   * The widget limit is the number of tabs that can be placed on this side of the
   * screen. The limit is determined by reading the value of a method in the
   * Configuration class.
   * </p>
   *
   * <p>
   * The method name is determined by appending "WidgetLimit" to the lowercase
   * name of the placement.
   * </p>
   *
   * @return The widget limit for this placement.
   */
  private int getDynamicLimit() {
    final String fieldName = name().toLowerCase() + "WidgetLimit";
    try {
      final Field field = Configuration.class.getField(fieldName);
      return (int) field.get(Configuration.class);
    } catch (final Exception e) {
      e.printStackTrace();
      return 14;
    }
  }

  /**
   * Calculate the x position of the tab with the given index.
   *
   * <p>
   * The x position is calculated by multiplying the width of the tab by the
   * index of the tab. An offset of 2 is added to the calculated value to
   * account for the 1 pixel border around each tab.
   * </p>
   *
   * @param index The index of the tab to get the x position of.
   * @return The x position of the tab.
   */
  public int getTabX(final int index) {
    switch (this.ordinal()) {
      case 0: // ABOVE
        return (this.width + 2) * index;
      case 1: // BELOW
        return (this.width + 2) * index;
      default:
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf(this));
    }
  }

  /**
   * Calculate the y position of the tab with the given index.
   *
   * <p>
   * The y position is calculated by taking the negative of the height of the
   * tab, and adding 4 to the result. This is because the GUI is drawn from the
   * top of the screen down, and the y origin is at the bottom of the screen.
   * </p>
   *
   * @param index The index of the tab to get the y position of.
   * @return The y position of the tab.
   */
  public int getTabY(final int index) {
    switch (this.ordinal()) {
      case 0: // ABOVE
        return -this.height + 4;
      case 1: // BELOW
        return -4;
      default:
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf(this));
    }
  }

  /**
   * Gets the width of this tab.
   *
   * <p>
   * The width of the tab is the width of the tab widget, plus 2 pixels for the
   * border around the tab.
   * </p>
   *
   * @return The width of the tab.
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the height of this tab.
   *
   * <p>
   * The height of the tab is the height of the tab widget, plus 2 pixels for
   * the border around the tab.
   * </p>
   *
   * @return The height of the tab.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the top margin of this tab.
   *
   * <p>
   * The top margin is the distance from the top of the screen to the top of the
   * tab. This is the same as the y position of the tab, but is a separate
   * variable to make it easier to read and understand the code.
   * </p>
   *
   * @return The top margin of the tab.
   */
  public int getTopMargin() {
    return this.topMargin;
  }

  /**
   * Gets the left margin of this tab.
   *
   * <p>
   * The left margin is the distance from the left side of the screen to the
   * left side of the tab. This is the same as the x position of the tab, but
   * is a separate variable to make it easier to read and understand the code.
   * </p>
   *
   * @return The left margin of the tab.
   */
  public int getLeftMargin() {
    return this.leftMargin;
  }
}
