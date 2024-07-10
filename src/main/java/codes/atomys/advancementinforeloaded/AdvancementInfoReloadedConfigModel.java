package codes.atomys.advancementinforeloaded;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = AdvancementInfoReloaded.MOD_ID)
@Config(name = "advancementinforeloaded", wrapperName = "AdvancementInfoReloadedConfig")
public class AdvancementInfoReloadedConfigModel {
  @RangeConstraint(min = 32, max = 128)
  public int headerHeight = 48;
  @RangeConstraint(min = 32, max = 128)
  public int footerHeight = 32;
  @RangeConstraint(min = 50, max = 512)
  public int criteriasWidth = 142;

  public boolean blackBackground = false;

  public boolean showDescription = true;

  @RangeConstraint(min = 0, max = 42)
  public int aboveWidgetLimit = 14;

  @RangeConstraint(min = 0, max = 42)
  public int belowWidgetLimit = 14;
}
