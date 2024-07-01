package codes.atomys.advancementinforeloaded;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = AdvancementInfoReloaded.MOD_ID)
@Config(name = "advancementinforeloaded", wrapperName = "AdvancementInfoReloadedConfig")
public class AdvancementInfoReloadedConfigModel {
  @RangeConstraint(min = 0, max = 9999)
  public int marginX = 30;
  @RangeConstraint(min = 0, max = 9999)
  public int marginY = 30;
}
