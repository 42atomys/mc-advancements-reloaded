package codes.atomys.advancementinforeloaded.screen;

import java.util.Optional;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AdvancementReloadedDisplay extends AdvancementDisplay {

  private float x;
  private float y;

  public AdvancementReloadedDisplay(ItemStack icon, Text title, Text description, Optional<Identifier> background,
      AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
    super(icon, title, description, background, frame, showToast, announceToChat, hidden);
  }

  public static AdvancementReloadedDisplay cast(AdvancementDisplay display) {
    AdvancementReloadedDisplay ard = new AdvancementReloadedDisplay(display.getIcon(), display.getTitle(),
        display.getDescription(), display.getBackground(), display.getFrame(), display.shouldShowToast(),
        display.shouldAnnounceToChat(), display.isHidden());
    ard.x = display.getX();
    ard.y = display.getY();

    return ard;
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }
}
