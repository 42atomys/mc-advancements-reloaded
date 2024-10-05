package codes.atomys.advr;

import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * The ReloadedDisplayInfo class extends DisplayInfo and provides additional
 * functionality for managing display information in the advancement tab GUI.
 * It includes coordinates for the icon's position and methods to create
 * instances from existing DisplayInfo objects.
 *
 * @see DisplayInfo
 */
public class ReloadedDisplayInfo extends DisplayInfo {

  private float x;
  private float y;

  /**
   * Constructs a new ReloadedDisplayInfo object.
   *
   * @param icon         The ItemStack representing the icon.
   * @param title        The Component representing the title.
   * @param description  The Component representing the description.
   * @param background   An Optional containing the ResourceLocation of the
   *                     background.
   * @param type         The type of the advancement.
   * @param showToast    A boolean indicating whether to show a toast
   *                     notification.
   * @param announceChat A boolean indicating whether to announce in chat.
   * @param hidden       A boolean indicating whether the advancement is hidden.
   */
  public ReloadedDisplayInfo(final ItemStack icon, final Component title, final Component description,
      final Optional<ResourceLocation> background, final AdvancementType type, final boolean showToast,
      final boolean announceChat,
      final boolean hidden) {
    super(icon, title, description, background, type, showToast, announceChat, hidden);
  }

  /**
   * Creates a new ReloadedDisplayInfo with the same properties as the given
   * DisplayInfo. This is used to copy the properties of an existing DisplayInfo
   * and turn it into a ReloadedDisplayInfo.
   *
   * @param display The DisplayInfo to copy.
   * @return The new ReloadedDisplayInfo.
   */
  public static ReloadedDisplayInfo cast(final DisplayInfo display) {
    final ReloadedDisplayInfo ard = new ReloadedDisplayInfo(display.getIcon(), display.getTitle(),
        display.getDescription(), display.getBackground(), display.getType(), display.shouldShowToast(),
        display.shouldAnnounceChat(), display.isHidden());
    ard.x = display.getX();
    ard.y = display.getY();

    return ard;
  }

  /**
   * Returns the x-coordinate of the icon in the GUI.
   *
   * @return The x-coordinate of the icon.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Returns the y-coordinate of the icon in the GUI.
   *
   * @return The y-coordinate of the icon.
   */
  public float getY() {
    return this.y;
  }
}
