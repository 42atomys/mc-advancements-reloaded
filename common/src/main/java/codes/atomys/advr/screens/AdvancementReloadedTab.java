package codes.atomys.advr.screens;

import codes.atomys.advr.ReloadedDisplayInfo;
import codes.atomys.advr.TabPlacement;
import codes.atomys.advr.config.Configuration;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code AdvancementReloadedTab} class represents a tab in the advancements
 * GUI.
 * It handles the rendering and interaction of a specific tab, including its
 * background, icon, and the advancements it contains.
 * <p>
 * This class is responsible for managing the state and rendering of a single
 * tab,including its position, size, and the advancements it displays. It also
 * handles user interactions such as clicking on the tab or its widgets.
 * </p>
 * <p>
 * The tab can be created using the
 * {@link #create(Minecraft, AdvancementReloadedScreen, int, AdvancementNode)}
 * method, which initializes the tab with the given parameters. The tab's
 * position can be set using the {@link #setPos(int, int)} method, and it can be
 * rendered using the {@link #render(GuiGraphics, int, int)} method.
 * </p>
 * <p>
 * The tab contains a map of widgets, each representing an advancement. These
 * widgets can be added using the {@link #addAdvancement(AdvancementNode)}
 * method, and the widget associated with a specific advancement can be
 * retrieved using the {@link #getWidget(AdvancementHolder)} method.
 * </p>
 * <p>
 * The tab also provides methods for drawing the background and icon, rendering
 * tooltips, and handling user interactions such as clicking on the tab or its
 * widgets.
 * </p>
 *
 * @see AdvancementReloadedScreen
 * @see AdvancementReloadedWidget
 * @see TabPlacement
 * @see AdvancementNode
 * @see ReloadedDisplayInfo
 */
public class AdvancementReloadedTab {
  private static final ResourceLocation SELECTED_IDENTIFIER = ResourceLocation
      .withDefaultNamespace("advancements/task_frame_obtained");
  private static final ResourceLocation UNSELECTED_IDENTIFIER = ResourceLocation
      .withDefaultNamespace("advancements/task_frame_unobtained");

  private final Minecraft client;
  private final AdvancementReloadedScreen screen;
  private final TabPlacement type;
  private final AdvancementNode root;
  private final ReloadedDisplayInfo display;
  private final ItemStack icon;
  private final Component title;
  private final AdvancementReloadedWidget rootWidget;
  private final Map<AdvancementHolder, AdvancementReloadedWidget> widgets = Maps.newLinkedHashMap();
  private int index;
  private double originX;
  private double originY;
  private int minPanX = Integer.MAX_VALUE;
  private int minPanY = Integer.MAX_VALUE;
  private int maxPanX = Integer.MIN_VALUE;
  private int maxPanY = Integer.MIN_VALUE;
  private float alpha;
  private boolean initialized;
  private int tab_x;
  private int tab_y;

  /**
   * Creates a new instance of the {@link AdvancementReloadedTab} class.
   * <p>
   * This constructor is used to create a new instance of the tab, which is
   * used to display the contents of a specific tab in the GUI.
   * </p>
   *
   * @param client  the Minecraft client instance
   * @param screen  the screen that the tab is associated with
   * @param type    the type of the tab
   * @param index   the index of the tab in the list of tabs
   * @param root    the root node of the tab
   * @param display the display information for the tab
   */
  public AdvancementReloadedTab(final Minecraft client, final AdvancementReloadedScreen screen,
      final TabPlacement type, final int index, final AdvancementNode root, final ReloadedDisplayInfo display) {
    this.client = client;
    this.screen = screen;
    this.type = type;
    this.index = index;
    this.root = root;
    this.display = display;
    this.icon = display.getIcon();
    this.title = display.getTitle();
    this.rootWidget = new AdvancementReloadedWidget(this, client, root, display);
    this.addWidget(this.rootWidget, root.holder());
  }

  /**
   * Gets the type of the tab.
   * <p>
   * The type of the tab is either {@link TabPlacement#ABOVE} or
   * {@link TabPlacement#BELOW}, and is used to determine the position of the
   * tab in the GUI.
   * </p>
   *
   * @return The type of the tab.
   */
  public TabPlacement getType() {
    return this.type;
  }

  /**
   * Gets the index of the tab.
   * <p>
   * The index of the tab is its position in the list of tabs, starting from 0.
   * The index is used to identify the tab in the tab list, and is used by the
   * client to determine which tab to display when the player switches between
   * tabs.
   * </p>
   *
   * @return the index of the tab
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Sets the index of the tab.
   * <p>
   * The index is the position of the tab in the list of tabs, starting from 0.
   * The index is used to identify the tab in the tab list, and is used by the
   * client to determine which tab to display when the player switches between
   * tabs.
   * </p>
   *
   * @param index the new index of the tab
   */
  public void setIndex(final int index) {
    this.index = index;
  }

  /**
   * Gets the root advancement of the tab.
   * <p>
   * The root advancement is the topmost advancement in the tree of advancements
   * displayed in the tab. It is the root of the tree, and all other advancements
   * in the tab are children of this root.
   * </p>
   *
   * @return the root advancement of the tab
   */
  public AdvancementNode getRoot() {
    return this.root;
  }

  /**
   * Gets the title of the tab.
   * <p>
   * The title is a Component object that contains the display name of the tab.
   * </p>
   *
   * @return the title of the tab
   */
  public Component getTitle() {
    return this.title;
  }

  /**
   * Gets the display information of the tab.
   * <p>
   * The returned object contains the title, description, icon, and other
   * information of the tab.
   * </p>
   *
   * @return the display information of the tab
   */
  public ReloadedDisplayInfo getDisplay() {
    return this.display;
  }

  /**
   * Sets the position of this tab.
   * <p>
   * The actual position of the tab is calculated based on the position of the
   * tab bar, the index of the tab, and the type of the tab.
   * </p>
   *
   * @param x the x position of the tab bar
   * @param y the y position of the tab bar
   */
  public void setPos(final int x, final int y) {
    this.tab_x = x + this.type.getTabX(this.index);
    this.tab_y = y + this.type.getTabY(this.index);
  }

  /**
   * Draws the background of the tab.
   * <p>
   * The background is rendered as a sprite at the position specified by
   * {@link #setPos(int, int)} and has the dimensions specified by
   * {@link TabPlacement#getWidth()} and {@link TabPlacement#getHeight()}.
   * If the tab is selected, the selected texture is used, otherwise the
   * unselected texture is used.
   * </p>
   *
   * @param context  the graphics context to draw on
   * @param selected whether the tab is selected
   */
  public void drawBackground(final GuiGraphics context, final boolean selected) {
    final ResourceLocation texture = selected ? SELECTED_IDENTIFIER : UNSELECTED_IDENTIFIER;

    context.pose().pushPose();
    context.pose().translate(0.0D, 0.0D, 220.0D);
    context.blitSprite(texture, this.tab_x, this.tab_y, this.type.getWidth(), this.type.getHeight());
    context.pose().popPose();

  }

  /**
   * Draws the icon of this tab.
   * <p>
   * The icon is rendered at the top left corner of the tab, with the position
   * specified by {@link #setPos(int, int)} and the top margin specified by
   * {@link TabPlacement#getTopMargin()}.
   * </p>
   *
   * @param context the graphics context to draw on
   */
  public void drawIcon(final GuiGraphics context) {
    context.pose().pushPose();
    context.pose().translate(0.0D, 0.0D, 221.0D);
    context.renderFakeItem(this.icon, this.tab_x + this.type.getTopMargin(),
        this.tab_y + this.type.getLeftMargin());
    context.pose().popPose();
  }

  /**
   * Gets the width of the tab.
   *
   * <p>
   * If the sidebar is visible, the width of the tab is the width of the screen
   * minus the width of the sidebar. Otherwise, the width of the tab is the
   * width of the screen.
   * </p>
   *
   * @return The width of the tab.
   */
  public int getWidth() {
    if (this.screen.hasVisibleSidebar())
      return this.screen.width - Configuration.criteriasWidth;

    return this.screen.width;
  }

  /**
   * Gets the height of this tab.
   *
   * <p>
   * The height of this tab is the height of the screen, minus the height of the
   * header, the footer, and two separator lines.
   * </p>
   *
   * @return The height of the tab.
   */
  public int getHeight() {
    // 2 are the separator lines
    return this.screen.height - Configuration.headerHeight
        - Configuration.footerHeight - 2;
  }

  /**
   * Renders the tab.
   * <p>
   * This method is called by the screen to render the tab. It first checks if
   * the tab has been initialized, and if not, sets the origin of the tab based
   * on the center of the screen and the size of the tab. It then renders the
   * lines and widgets of the tab, and finally disables the scissor.
   * </p>
   *
   * @param context the graphics context to draw on
   * @param x       the x position of the screen
   * @param y       the y position of the screen
   */
  public void render(final GuiGraphics context, final int x, final int y) {
    if (!this.initialized) {
      this.originX = (double) ((this.getWidth() / 2) - (this.maxPanX + this.minPanX) / 2);
      this.originY = (double) ((this.screen.height / 2 - Configuration.headerHeight - 1)
          - (this.maxPanY + this.minPanY) / 2);
      this.initialized = true;
    }

    context.enableScissor(x, y, x + this.getWidth(), y + this.getHeight());
    context.pose().pushPose();
    context.pose().translate((float) x, (float) y, 0.0F);
    final int i = Mth.floor(this.originX);
    final int j = Mth.floor(this.originY);

    this.rootWidget.renderLines(context, i, j, true);
    this.rootWidget.renderLines(context, i, j, false);
    this.rootWidget.renderWidgets(context, i, j);
    context.pose().popPose();
    context.disableScissor();
  }

  /**
   * Renders the tooltip for the widget that is currently under the mouse, if
   * any.
   * <p>
   * This method is called by the screen to render the tooltip for the currently
   * selected widget. It first checks if the mouse is inside the tab, and if
   * not, immediately returns. It then iterates over all of the widgets in the
   * tab, and for the first widget that is currently under the mouse, it renders
   * the tooltip for that widget. It then clamps the alpha of the tooltip to
   * between 0 and 0.3, and if the alpha is 0, it sets it to 0 and immediately
   * returns.
   * </p>
   *
   * @param context the graphics context to draw on
   * @param mouseX  the x position of the mouse
   * @param mouseY  the y position of the mouse
   * @param x       the x position of the screen
   * @param y       the y position of the screen
   */
  public void drawWidgetTooltip(final GuiGraphics context, final int mouseX, final int mouseY, final int x,
      final int y) {
    context.fill(0, 0, this.getWidth(), this.getHeight(), -200, Mth.floor(this.alpha * 255.0F) << 24);
    context.pose().pushPose();
    context.pose().translate(0.0F, 0.0F, 300.0F);
    boolean rendered = false;
    final int i = Mth.floor(this.originX);
    final int j = Mth.floor(this.originY);
    if (mouseX > 0 && mouseX < this.getWidth() && mouseY > 0 && mouseY < this.getHeight()) {
      final Iterator<AdvancementReloadedWidget> widgets = this.widgets.values().iterator();

      while (widgets.hasNext()) {
        final AdvancementReloadedWidget advancementWidget = widgets.next();
        if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
          rendered = true;
          advancementWidget.drawTooltip(context, i, j, this.alpha, x, y);
          break;
        }
      }
    }

    context.pose().popPose();
    if (rendered) {
      this.alpha = Mth.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
    } else {
      this.alpha = Mth.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
    }
  }

  /**
   * Returns whether the given mouse coordinates are within the bounds of the
   * tab of this {@link AdvancementReloadedTab}.
   *
   * @param screenX the x position of the screen
   * @param screenY the y position of the screen
   * @param mouseX  the x position of the mouse
   * @param mouseY  the y position of the mouse
   * @return true if the mouse is on the tab, false otherwise
   */
  public boolean isClickOnTab(final int screenX, final int screenY, final double mouseX, final double mouseY) {
    return mouseX > (double) this.tab_x && mouseX < (double) (this.tab_x + this.type.getWidth())
        && mouseY > (double) this.tab_y && mouseY < (double) (this.tab_y + this.type.getHeight());
  }

  /**
   * Returns the widget associated with the given mouse coordinates, or null if
   * no widget is at the given coordinates.
   *
   * @param screenX the x position of the screen
   * @param screenY the y position of the screen
   * @param mouseX  the x position of the mouse
   * @param mouseY  the y position of the mouse
   * @return the widget associated with the given mouse coordinates, or null if
   *         no widget is at the given coordinates
   */
  @Nullable
  public AdvancementReloadedWidget clickOnWidget(final int screenX, final int screenY, final double mouseX,
      final double mouseY) {
    final int flooredOriginX = Mth.floor(this.originX);
    final int flooredOriginY = Mth.floor(this.originY + Configuration.headerHeight - 1);

    // Prevent click outside of the advancement tree
    if (mouseX < screenX || mouseX > this.getWidth() || mouseY < screenY
        || mouseY > this.screen.height - Configuration.footerHeight - 1)
      return null;

    final Iterator<AdvancementReloadedWidget> widgets = this.widgets.values().iterator();

    while (widgets.hasNext()) {
      final AdvancementReloadedWidget advancementWidget = widgets.next();
      if (advancementWidget.isMouseOn(flooredOriginX, flooredOriginY, mouseX, mouseY))
        return advancementWidget;
    }

    return null;
  }

  /**
   * Creates a new {@link AdvancementReloadedTab} for the given client, screen,
   * index, and root node.
   * <p>
   * This method first checks if the given root node has a display, and if not,
   * immediately returns null. It then iterates over all of the values of the
   * {@link TabPlacement} enum, and for the first value that is at or below the
   * given index, it creates a new tab with the given parameters. It then
   * returns this tab, or null if no tab could be created.
   * </p>
   *
   * @param client the Minecraft client
   * @param screen the screen to draw the tab on
   * @param index  the index of the tab to create
   * @param root   the root node of the tab
   * @return a new tab, or null if no tab could be created
   */
  @Nullable
  public static AdvancementReloadedTab create(final Minecraft client, final AdvancementReloadedScreen screen, int index,
      final AdvancementNode root) {
    final Optional<DisplayInfo> optional = root.advancement().display();
    if (optional.isEmpty()) {
      return null;
    } else {
      final TabPlacement[] types = TabPlacement.values();
      final int numberOfTypes = types.length;

      for (int i = 0; i < numberOfTypes; ++i) {
        final TabPlacement advancementTabType = types[i];
        if (index < advancementTabType.getTabLimit()) {
          return new AdvancementReloadedTab(client, screen, advancementTabType, index, root,
              ReloadedDisplayInfo.cast(optional.get()));
        }

        index -= advancementTabType.getTabLimit();
      }

      return null;
    }
  }

  /**
   * Moves the origin of the tab by the given offset.
   * <p>
   * This method first calculates the maximum width and height of the tab,
   * taking into account the size of the screen and the size of the tab. It
   * then uses these values to determine if the origin should be moved by the
   * given offset. If the origin should be moved, it clamps the origin to the
   * valid range, and then sets the origin to the clamped value.
   * </p>
   *
   * @param offsetX the x offset to move the origin by
   * @param offsetY the y offset to move the origin by
   */
  public void move(final double offsetX, final double offsetY) {
    final int maxWidth = this.getWidth();
    if (this.maxPanX - this.minPanX > maxWidth - 8) {
      this.originX = Mth.clamp(this.originX + offsetX, (double) (-(this.maxPanX - maxWidth + 8)), 8D);
    }

    final int maxHeight = this.getHeight();
    if (this.maxPanY - this.minPanY > maxHeight - 16) {
      this.originY = Mth.clamp(this.originY + offsetY, (double) (-(this.maxPanY - maxHeight + 16)), 16D);
    }

  }

  /**
   * Adds a new advancement to the tab.
   * <p>
   * This method first checks if the given advancement node has a display, and
   * if not, immediately returns. It then creates a new widget with the given
   * parameters, and adds it to the tab.
   * </p>
   *
   * @param advancement the advancement node to add
   */
  public void addAdvancement(final AdvancementNode advancement) {
    final Optional<DisplayInfo> optional = advancement.advancement().display();
    if (!optional.isEmpty()) {
      final AdvancementReloadedWidget advancementWidget = new AdvancementReloadedWidget(this, this.client, advancement,
          ReloadedDisplayInfo.cast(optional.get()));
      this.addWidget(advancementWidget, advancement.holder());
    }
  }

  /**
   * Adds a new widget to the tab, and updates the tab's minimum and maximum
   * pan positions.
   * <p>
   * This method first adds the given widget to the tab's map of widgets, and
   * then updates the tab's minimum and maximum pan positions to ensure that
   * the widget is visible in the tab.
   * </p>
   * <p>
   * It then iterates over all of the values of the tab's map of widgets, and
   * calls the {@link AdvancementReloadedWidget#addToTree()} method on each
   * widget.
   * </p>
   *
   * @param widget      the widget to add
   * @param advancement the holder of the widget
   */
  private void addWidget(final AdvancementReloadedWidget widget, final AdvancementHolder advancement) {
    this.widgets.put(advancement, widget);
    final int i = widget.getX();
    final int j = i + 28;
    final int k = widget.getY();
    final int l = k + 27;
    this.minPanX = Math.min(this.minPanX, i);
    this.maxPanX = Math.max(this.maxPanX, j);
    this.minPanY = Math.min(this.minPanY, k);
    this.maxPanY = Math.max(this.maxPanY, l);
    final Iterator<AdvancementReloadedWidget> var7 = this.widgets.values().iterator();

    while (var7.hasNext()) {
      final AdvancementReloadedWidget advancementWidget = var7.next();
      advancementWidget.addToTree();
    }

  }

  /**
   * Returns the widget associated with the given advancement, or null if no such
   * widget exists.
   *
   * @param advancement the advancement to get the widget for
   * @return the widget associated with the given advancement, or null if no such
   *         widget exists
   */
  @Nullable
  public AdvancementReloadedWidget getWidget(final AdvancementHolder advancement) {
    return this.widgets.get(advancement);
  }

  /**
   * Returns the screen associated with this tab.
   *
   * @return the screen associated with this tab
   */
  public AdvancementReloadedScreen getScreen() {
    return this.screen;
  }
}
