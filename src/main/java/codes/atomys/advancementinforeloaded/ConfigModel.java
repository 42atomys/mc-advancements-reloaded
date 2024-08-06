package codes.atomys.advancementinforeloaded;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = AdvancementInfoReloaded.MOD_ID)
@Config(name = "advancementinforeloaded", wrapperName = "Config")
public class ConfigModel {
  @SectionHeader("appearance")
  public boolean displaySidebar = true; // added in v0.3
    public boolean displayDescription = true; // added in v0.2
    public boolean alphabeticOrder = false; // added in v0.3
    public BackgroundStyle backgroundStyle = BackgroundStyle.TRANSPARENT; // added in v0.4
    
    public enum BackgroundStyle {
      TRANSPARENT,
      ACHIEVEMENT,
      BLACK,
    }


  @SectionHeader("advanced_customization")
  @RangeConstraint(min = 32, max = 128)
  public int headerHeight = 48; // added in v0.2
  @RangeConstraint(min = 32, max = 128)
  public int footerHeight = 32; // added in v0.2
  @RangeConstraint(min = 50, max = 512)
  public int criteriasWidth = 142; // added in v0.2
  @RangeConstraint(min = 0, max = 42)
  public int aboveWidgetLimit = 14; // added in v0.2
  @RangeConstraint(min = 0, max = 42)
  public int belowWidgetLimit = 14; // added in v0.2
}
