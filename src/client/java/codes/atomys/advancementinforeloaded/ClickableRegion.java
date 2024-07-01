package codes.atomys.advancementinforeloaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ClickableRegion {
  private String name;
  private int minX;
  private int minY;
  private int maxX;
  private int maxZ;
  private boolean clicked = false;

  private ClickableRegion(String name, int minX, int maxX, int minY, int maxZ) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxZ = maxZ;
    this.name = name;
  }

  public boolean isInside(double x, double y) {
    return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxZ;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWidth(int width) {
    this.maxX = this.minX + width;
  }

  public void setHeight(int height) {
    this.maxZ = this.minY + height;
  }

  public void setOriginX(int x) {
    this.minX = x;
  }

  public void setOriginY(int y) {
    this.minY = y;
  }

  public void setClicked(boolean clicked) {
    this.clicked = clicked;
  }

  public boolean isClicked() {
    return this.clicked;
  }

  public String getName() {
    return this.name;
  }

  public String toString() {
    return "ClickableRegion{name='" + this.name + "', minX=" + this.minX + ", minY=" + this.minY + ", maxX=" + this.maxX
        + ", maxZ=" + this.maxZ + ", clicked=" + this.clicked + "}";
  }

  public static ClickableRegion create(String name, int x, int y, int width, int height) {
    return new ClickableRegion(name, x, x + width, y, y + height);
  }

  public static List<ClickableRegion> foundRegions(List<ClickableRegion> regions, double x, double y) {
    List<ClickableRegion> foundRegions = new ArrayList<ClickableRegion>();
    for (ClickableRegion region : regions) {
      if (region.isInside(x, y)) {
        foundRegions.add(region);
      }
    }
    return foundRegions;
  }

  public static List<ClickableRegion> foundClickedRegions(List<ClickableRegion> regions) {
    List<ClickableRegion> foundRegions = new ArrayList<ClickableRegion>();
    for (ClickableRegion region : regions) {
      if (region.isClicked()) {
        foundRegions.add(region);
      }
    }
    return foundRegions;
  }

  public static Optional<ClickableRegion> findRegion(List<ClickableRegion> regions, Predicate<ClickableRegion> predicate) {
    for (ClickableRegion region : regions) {
      if (predicate.test(region)) {
        return Optional.of(region);
      }
    }
    return Optional.empty();
  }
}
