package codes.atomys.advancementinforeloaded.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
enum AdvancementReloadedTabType {
  
  ABOVE(26, 26, 5, 5, 14),
  BELOW(26, 26, 5, 5, 14);

   private final int width;
   private final int height;
   private final int topMargin;
   private final int leftMargin;
   private final int tabLimit;
  
  private AdvancementReloadedTabType(final int width, final int height, final int topMargin, final int leftMargin, final int tabLimit) {
    this.width = width;
    this.height = height;
    this.topMargin = topMargin;
    this.leftMargin = leftMargin;
    this.tabLimit = tabLimit;
  }

   public int getTabLimit() {
      return this.tabLimit;
   }

   public int getTabX(int index) {
      switch (this.ordinal()) {
         case 0: // ABOVE
            return (this.width + 2) * index;
         case 1: // BELOW
            return (this.width + 2) * index;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf(this));
      }
   }

   public int getTabY(int index) {
      switch (this.ordinal()) {
         case 0: // ABOVE
            return -this.height + 4;
         case 1: // BELOW
            return -4;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf(this));
      }
   }

   public int getWidth() {
      return this.width ;
   }

    public int getHeight() {
        return this.height;
    }

    public int getTopMargin() {
        return this.topMargin;
    }

    public int getLeftMargin() {
        return this.leftMargin;
    }
}
