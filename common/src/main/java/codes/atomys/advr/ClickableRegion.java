package codes.atomys.advr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a clickable region in a 2D space.
 * <p>
 * A clickable region is defined by its name and its bounding coordinates
 * (minX, minY, maxX, maxZ). It also has a state indicating whether it is
 * currently being clicked.
 * </p>
 * <p>
 * This class provides methods to check if a point is inside the region,
 * set various properties of the region, and find regions from a list based
 * on certain criteria.
 * </p>
 * <p>
 * Instances of this class are immutable once created, except for the
 * clicked state and name, which can be modified.
 * </p>
 */
public final class ClickableRegion {
  private String name;
  private int minX;
  private int minY;
  private int maxX;
  private int maxZ;
  private boolean clicked = false;

  private ClickableRegion(final String name, final int minX, final int maxX, final int minY, final int maxZ) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxZ = maxZ;
    this.name = name;
  }

  /**
   * Check if the given (x, y) coordinates are inside this clickable region.
   * <p>
   * The coordinates are considered to be inside if they are within the region's
   * bounds,
   * inclusive of the minimum and maximum values.
   * </p>
   *
   * @param x the x-coordinate to check
   * @param y the y-coordinate to check
   * @return true if the coordinates are inside, false otherwise
   */
  public boolean isInside(final double x, final double y) {
    return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxZ;
  }

  /**
   * Sets the name of this clickable region.
   * <p>
   * This is mainly used for debugging purposes, as it allows to identify which
   * region is being clicked.
   * </p>
   *
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Sets the width of this clickable region.
   * <p>
   * The width is the difference between the maximum x-coordinate and the minimum
   * x-coordinate of the region.
   * </p>
   *
   * @param width the width to set
   */
  public void setWidth(final int width) {
    this.maxX = this.minX + width;
  }

  /**
   * Sets the height of this clickable region.
   * <p>
   * The height is the difference between the maximum y-coordinate and the
   * minimum y-coordinate of the region.
   * </p>
   *
   * @param height the height to set
   */
  public void setHeight(final int height) {
    this.maxZ = this.minY + height;
  }

  /**
   * Sets the x-coordinate of the origin of this clickable region.
   * <p>
   * The origin is the point on the region that is used to position it in the
   * world.
   * </p>
   *
   * @param x the x-coordinate of the origin
   */
  public void setOriginX(final int x) {
    this.minX = x;
  }

  /**
   * Sets the y-coordinate of the origin of this clickable region.
   * <p>
   * The origin is the point on the region that is used to position it in the
   * world.
   * </p>
   *
   * @param y the y-coordinate of the origin
   */
  public void setOriginY(final int y) {
    this.minY = y;
  }

  /**
   * Sets whether this clickable region is currently being clicked.
   * <p>
   * This is used to determine whether the region should be highlighted as
   * being clicked.
   * </p>
   *
   * @param clicked whether the region is being clicked
   */
  public void setClicked(final boolean clicked) {
    this.clicked = clicked;
  }

  /**
   * Checks whether this clickable region is currently being clicked.
   * <p>
   * This is used to determine whether the region should be highlighted as
   * being clicked.
   * </p>
   *
   * @return true if the region is being clicked, false otherwise
   */
  public boolean isClicked() {
    return this.clicked;
  }

  /**
   * Returns the name of this clickable region.
   * <p>
   * The name is used to identify the region, and is mainly used for debugging
   * purposes.
   * </p>
   *
   * @return the name of the region
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns a string representation of this clickable region.
   * <p>
   * The returned string is in the format
   * <code>ClickableRegion{name='&lt;name&gt;', minX=&lt;minX&gt;, minY=&lt;minY&gt;, maxX=&lt;maxX&gt;, maxZ=&lt;maxZ&gt;, clicked=&lt;clicked&gt;}</code>.
   * </p>
   *
   * @return a string representation of the region
   */
  public String toString() {
    return "ClickableRegion{name='" + this.name + "', minX=" + this.minX + ", minY=" + this.minY + ", maxX=" + this.maxX
        + ", maxZ=" + this.maxZ + ", clicked=" + this.clicked + "}";
  }

  /**
   * Creates a new clickable region.
   * <p>
   * This method creates a region with the given name, at the specified
   * coordinates, with the given width and height.
   * </p>
   *
   * @param name   the name of the region
   * @param x      the x-coordinate of the region
   * @param y      the y-coordinate of the region
   * @param width  the width of the region
   * @param height the height of the region
   *
   * @return a new clickable region
   */
  public static ClickableRegion create(final String name, final int x, final int y, final int width, final int height) {
    return new ClickableRegion(name, x, x + width, y, y + height);
  }

  /**
   * Finds all clickable regions from the given list that the given (x, y)
   * coordinates are inside.
   * <p>
   * This method iterates over the given list of regions and checks whether the
   * given (x, y)
   * coordinates are inside each region. If they are, the region is added to the
   * returned list.
   * </p>
   *
   * @param regions the list of regions to search
   * @param x       the x-coordinate to check
   * @param y       the y-coordinate to check
   *
   * @return a list of regions that the given coordinates are inside
   */
  public static List<ClickableRegion> foundRegions(final List<ClickableRegion> regions, final double x,
      final double y) {
    final List<ClickableRegion> foundRegions = new ArrayList<ClickableRegion>();
    for (final ClickableRegion region : regions) {
      if (region.isInside(x, y)) {
        foundRegions.add(region);
      }
    }
    return foundRegions;
  }

  /**
   * Finds all clickable regions from the given list that are currently being
   * clicked.
   * <p>
   * This method iterates over the given list of regions and checks whether each
   * region is being clicked. If it is, the region is added to the returned list.
   * </p>
   *
   * @param regions the list of regions to search
   *
   * @return a list of regions that are being clicked
   */
  public static List<ClickableRegion> foundClickedRegions(final List<ClickableRegion> regions) {
    final List<ClickableRegion> foundRegions = new ArrayList<ClickableRegion>();
    if (regions == null) {
      return foundRegions;
    }

    for (final ClickableRegion region : regions) {
      if (region.isClicked()) {
        foundRegions.add(region);
      }
    }
    return foundRegions;
  }

  /**
   * Finds the first clickable region from the given list that matches the given
   * predicate.
   * <p>
   * This method iterates over the given list of regions and checks whether each
   * region matches the given predicate. If a region matches, it is immediately
   * returned. If no region matches, an empty optional is returned.
   * </p>
   *
   * @param regions   the list of regions to search
   * @param predicate the predicate to test each region against
   *
   * @return an optional containing the first region that matches the predicate,
   *         or an empty optional if no region matches
   */
  public static Optional<ClickableRegion> findRegion(final List<ClickableRegion> regions,
      final Predicate<ClickableRegion> predicate) {
    for (final ClickableRegion region : regions) {
      if (predicate.test(region)) {
        return Optional.of(region);
      }
    }
    return Optional.empty();
  }
}
