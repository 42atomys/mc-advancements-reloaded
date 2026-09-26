package codes.atomys.advr;

import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * The ReloadedDisplayInfo class wraps DisplayInfo and provides additional
 * functionality for managing display information in the advancement tab GUI.
 *
 * @see DisplayInfo
 */
public class ReloadedDisplayInfo {

  private final DisplayInfo displayInfo;
  private float x;
  private float y;

  /**
   * Constructs a new ReloadedDisplayInfo object.
   *
   * @param icon         The ItemStack representing the icon.
   * @param title        The Component representing the title.
   * @param description  The Component representing the description.
   * @param background   An Optional containing the ClientAsset.ResourceTexture of
   *                     the background.
   * @param type         The type of the advancement.
   * @param showToast    A boolean indicating whether to show a toast
   *                     notification.
   * @param announceChat A boolean indicating whether to announce in chat.
   * @param hidden       A boolean indicating whether the advancement is hidden.
   */
  public ReloadedDisplayInfo(final ItemStackTemplate icon, final Component title, final Component description,
      final Optional<ClientAsset.ResourceTexture> background, final AdvancementType type, final boolean showToast,
      final boolean announceChat,
      final boolean hidden) {
    this(new DisplayInfo(icon, title, description, background, type, showToast, announceChat, hidden));
  }

  /**
   * Wraps an existing DisplayInfo.
   *
   * @param displayInfo The display information to wrap.
   */
  public ReloadedDisplayInfo(final DisplayInfo displayInfo) {
    this.displayInfo = displayInfo;
  }

  /**
   * Wraps the given DisplayInfo in a ReloadedDisplayInfo.
   *
   * @param display The DisplayInfo to copy.
   * @return The new ReloadedDisplayInfo.
   */
  public static ReloadedDisplayInfo cast(final DisplayInfo display) {
    return new ReloadedDisplayInfo(display);
  }

  /**
   * Returns the wrapped display information.
   *
   * @return the wrapped display information
   */
  public DisplayInfo getDisplayInfo() {
    return this.displayInfo;
  }

  /**
   * Returns the advancement icon.
   *
   * @return the icon
   */
  public ItemStackTemplate icon() {
    return this.displayInfo.icon();
  }

  /**
   * Returns the advancement title.
   *
   * @return the title
   */
  public Component title() {
    return this.displayInfo.title();
  }

  /**
   * Returns the advancement description.
   *
   * @return the description
   */
  public Component description() {
    return this.displayInfo.description();
  }

  /**
   * Returns the optional advancement background texture.
   *
   * @return the optional background texture
   */
  public Optional<ClientAsset.ResourceTexture> background() {
    return this.displayInfo.background();
  }

  /**
   * Returns the advancement frame type.
   *
   * @return the frame type
   */
  public AdvancementType type() {
    return this.displayInfo.type();
  }

  /**
   * Returns whether completing the advancement should show a toast.
   *
   * @return {@code true} if a toast should be shown
   */
  public boolean showToast() {
    return this.displayInfo.showToast();
  }

  /**
   * Returns whether completing the advancement should be announced in chat.
   *
   * @return {@code true} if the completion should be announced in chat
   */
  public boolean announceToChat() {
    return this.displayInfo.announceToChat();
  }

  /**
   * Returns whether the advancement is hidden.
   *
   * @return {@code true} if the advancement is hidden
   */
  public boolean hidden() {
    return this.displayInfo.hidden();
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
