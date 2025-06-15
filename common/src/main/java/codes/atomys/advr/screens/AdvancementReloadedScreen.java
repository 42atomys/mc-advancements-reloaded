package codes.atomys.advr.screens;

import codes.atomys.advr.ClickableRegion;
import codes.atomys.advr.ReloadedCriterionProgress;
import codes.atomys.advr.TabPlacement;
import codes.atomys.advr.config.Configuration;
import codes.atomys.advr.config.gui.ConfigurationScreen;
import codes.atomys.advr.utils.Memory;
import codes.atomys.advr.utils.Utils;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

/**
 * The AdvancementReloadedScreen class represents a custom screen for displaying
 * advancements in a Minecraft client.
 * It extends the Screen class and implements the ClientAdvancements.Listener
 * interface to handle advancement-related events.
 *
 * <p>
 * This screen provides a user interface for viewing and interacting with
 * advancements, including displaying advancement trees,
 * handling mouse and keyboard input, and rendering various UI elements.
 * </p>
 *
 * <p>
 * Key Features:
 * - Displays advancement trees with customizable backgrounds and styles.
 * - Handles mouse and keyboard input for interacting with advancements.
 * - Supports scrolling and clickable regions for navigation.
 * - Renders tooltips and additional information for advancements.
 * - Manages the state of selected tabs and widgets.
 * </p>
 */
public class AdvancementReloadedScreen extends Screen implements ClientAdvancements.Listener {

  private static final ResourceLocation criteriasSeparator = ResourceLocation
      .parse("advancements_reloaded:textures/gui/inworld_right_separator.png");
  // As a GUI Textured rendered type, the full path arent needed, the path start
  // at `textures/gui/sprites/{{texture}}`
  private static final ResourceLocation GEAR_GUI_SPRITE_TEXURE = ResourceLocation.parse("advancements_reloaded:gear");
  private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.withDefaultNamespace("widget/scroller");
  private static final ResourceLocation SCROLLER_BACKGROUND_TEXTURE = ResourceLocation
      .withDefaultNamespace("widget/scroller_background");

  private static final Component SAD_LABEL_TEXT = Component.translatable("advancements.sad_label");
  private static final Component EMPTY_TEXT = Component.translatable("advancements.empty");
  private static final Component SEARCH_HINT_TEXT = Component.translatable("text.advancements_reloaded.search_hint")
      .withStyle(ChatFormatting.DARK_GRAY);
  @Nullable
  private final Screen parent;
  private final ClientAdvancements advancementHandler;
  private Map<AdvancementHolder, AdvancementReloadedTab> tabs = Maps
      .<AdvancementHolder, AdvancementReloadedTab>newLinkedHashMap();
  @Nullable
  private Optional<AdvancementReloadedTab> selectedTab;
  private AdvancementReloadedWidget selectedWidget;
  private List<ClickableRegion> clickableRegions;
  private int scrollOffset = 0;
  private int contentHeight = 0;
  private final RenderPipeline renderTypeGui = RenderPipelines.GUI_TEXTURED;
  private EditBox searchBox;
  private String searchText = "";
  private boolean isSearching = false;

  /**
   * Constructs a new AdvancementReloadedScreen with the specified
   * ClientAdvancements handler.
   *
   * @param advancementHandler the handler for client advancements
   */
  public AdvancementReloadedScreen(final ClientAdvancements advancementHandler) {
    this(advancementHandler, null);
  }

  /**
   * Constructs a new AdvancementReloadedScreen with the specified
   * ClientAdvancements handler and an optional parent screen.
   *
   * @param advancementHandler the handler for client advancements
   * @param parent             the parent screen, or null if there is no parent
   */
  public AdvancementReloadedScreen(final ClientAdvancements advancementHandler, @Nullable final Screen parent) {
    super(Component.empty());
    this.advancementHandler = advancementHandler;
    this.parent = parent;
  }

  /**
   * Initializes the screen. This method is called when the screen is first
   * created. It clears the tabs and resets the selected tab to null. It also
   * sets the listener for the ClientAdvancements handler and sets the selected
   * tab to the first tab in the list if the selected tab is null. Finally, it
   * sets the clickable regions for the screen.
   */
  @Override
  protected void init() {
    this.tabs.clear();
    this.selectedTab = null;
    this.selectedWidget = Memory.getWidget();
    this.advancementHandler.setListener(this);
    if (this.selectedTab.isEmpty() && !this.tabs.isEmpty()) {
      final AdvancementReloadedTab advancementTab = this.tabs.values().iterator().next();
      this.advancementHandler.setSelectedTab(advancementTab.getRoot().holder(), true);
    } else {
      this.selectedTab.ifPresent(tab -> this.advancementHandler.setSelectedTab(tab.getRoot().holder(), true));
    }

    this.initClickableRegions();
    this.initComponents();
  }

  private void initComponents() {
    this.initSettingsButton();
    this.initSearchBox();
  }

  private void initSearchBox() {
    this.searchBox = new EditBox(this.font, (this.width - 196) / 2, this.height - 16 - 5, 196, 16, SEARCH_HINT_TEXT);
    this.searchBox.setHint(SEARCH_HINT_TEXT);
    this.searchBox.setCanLoseFocus(true);
    this.searchBox.setVisible(true);
    this.searchBox.setTextColor(ARGB.opaque(ChatFormatting.WHITE.getColor()));
    this.searchBox.setBordered(true);
    this.searchBox.setMaxLength(32);
    this.searchBox.setValue(this.searchText);
    this.searchBox.setResponder(this::onSearchTextChanged);
    this.addRenderableWidget(this.searchBox);
  }

  private void initSettingsButton() {
    final SpriteIconButton settingsIconButton = this.addRenderableWidget(
        SpriteIconButton.builder(
            Component.translatable("options.settings"),
            button -> this.minecraft.setScreen(ConfigurationScreen.screen(this)),
            true)
            .width(20)
            .sprite(GEAR_GUI_SPRITE_TEXURE, 14, 14)
            .build());
    settingsIconButton.setPosition(this.width - 25, 5);
  }

  /**
   * Sets the clickable regions for the screen. The clickable regions are used
   * to determine which area of the screen was clicked. The regions are as
   * follows:
   * <ul>
   * <li>The advancement tree region is the main area of the screen where the
   * advancement tree is displayed.</li>
   * <li>The advancement criteria region is the area on the right side of the
   * screen where the criteria for the selected advancement are displayed.
   * This region is only visible if the configuration option
   * {@link Configuration} is true.</li>
   * <li>The advancement criteria scrollbar region is the small area on the
   * right side of the screen where the scrollbar for the advancement
   * criteria is displayed. This region is only visible if the
   * configuration option {@link Configuration} is true.</li>
   * </ul>
   */
  private void initClickableRegions() {
    this.clickableRegions = new ArrayList<ClickableRegion>();
    this.clickableRegions.add(
        ClickableRegion.create("advancement_tree", 0, Configuration.headerHeight + 1,
            width - (this.hasVisibleSidebar() ? Configuration.criteriasWidth : 0),
            height - Configuration.headerHeight
                - Configuration.footerHeight));

    if (this.hasVisibleSidebar()) {
      this.clickableRegions.add(
          ClickableRegion.create("advancement_criterias", width - Configuration.criteriasWidth,
              Configuration.headerHeight + 1,
              Configuration.criteriasWidth - 6,
              height - Configuration.headerHeight
                  - Configuration.footerHeight));
      this.clickableRegions.add(
          ClickableRegion.create("advancement_criterias_scrollbar", width - 6,
              Configuration.headerHeight + 1, 6,
              height - Configuration.footerHeight));
    }
  }

  /**
   * Closes the screen. This method is called when the user closes the screen or
   * the screen is removed. It sets the screen of the Minecraft client to the
   * parent screen.
   */
  @Override
  public void onClose() {
    this.minecraft.setScreen(this.parent);
  }

  /**
   * Removes the screen. This method is called when the screen is removed. It
   * sets the listener of the advancement handler to null and sends a packet to
   * the server to notify that the screen was closed.
   */
  @Override
  public void removed() {
    this.advancementHandler.setListener(null);
    final ClientPacketListener clientPlayNetworkHandler = this.minecraft.getConnection();
    if (clientPlayNetworkHandler != null) {
      clientPlayNetworkHandler.send(ServerboundSeenAdvancementsPacket.closedScreen());
    }
  }

  /**
   * Called when a mouse button is pressed. This method is called when a mouse
   * button is pressed and the screen is active. It sets the clicked flag of the
   * clickable regions that were clicked and handles the actions of the different
   * regions.
   *
   * <p>
   * If the left mouse button is pressed, the method sets the clicked flag of the
   * clickable regions that were clicked. It then finds the tab that was clicked,
   * and sets the selected tab to the clicked tab. It also finds the widget that
   * was clicked and sets the selected widget to the clicked widget.
   * </p>
   *
   * <p>
   * If the right mouse button is pressed, the method moves the scrollbar of the
   * advancement criteria to the position of the mouse cursor.
   * </p>
   *
   * @param mouseX the x-coordinate of the mouse cursor in the screen
   * @param mouseY the y-coordinate of the mouse cursor in the screen
   * @param button the mouse button that was pressed
   * @return true if the event was handled, false otherwise
   */
  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button == 0) {
      ClickableRegion.foundRegions(this.clickableRegions, mouseX, mouseY).forEach(region -> {
        region.setClicked(true);
      });

      final int i = 0;
      final int j = Configuration.headerHeight;

      for (final AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab == this.selectedTab.orElse(null)) {
          final AdvancementReloadedWidget clickedWidget = advancementTab.clickOnWidget(i, j, mouseX, mouseY);
          if (clickedWidget != null) {
            this.setSelectedWidget(clickedWidget);
          }
        }

        if (advancementTab.isClickOnTab(i, j, mouseX, mouseY)) {
          this.advancementHandler.setSelectedTab(advancementTab.getRoot().holder(), true);
          break;
        }
      }

      if (this.needScrollbarOnCriterias()) {
        ClickableRegion
            .findRegion(this.clickableRegions,
                region -> region.getName().equals("advancement_criterias_scrollbar") && region.isClicked())
            .ifPresent(region -> {
              this.moveScrollbarTo(mouseY);
            });
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  /**
   * Move the scrollbar to a new position based on the given mouse Y-coordinate.
   *
   * <p>
   * The new scroll offset is calculated as follows:
   * 1. Calculate the relative mouse position within the scrollbar container.
   * 2. Calculate the new scroll offset based on the relative mouse position.
   * 3. Ensure the scroll offset is within the valid range (0 to scrollRange).
   * 4. Set the new scroll offset.
   * </p>
   *
   * @param mouseY the mouse Y-coordinate
   */
  private void moveScrollbarTo(final double mouseY) {
    // Calculate the viewable height (excluding header and footer)
    final int viewableHeight = height - Configuration.headerHeight
        - Configuration.footerHeight;

    // Calculate the start and end positions of the scrollbar container
    final int scrollbarStart = Configuration.headerHeight + 1;
    final int scrollbarEnd = height - Configuration.footerHeight - 1;

    // Calculate the scroll range
    final int scrollRange = this.contentHeight - viewableHeight;

    // Calculate the relative mouse position within the scrollbar container
    final double relativeMouseY = mouseY - scrollbarStart;

    // Calculate the new scroll offset based on the relative mouse position
    // Ensure the scroll offset is within the valid range (0 to scrollRange)
    int newScrollOffset = (int) (relativeMouseY / (scrollbarEnd - scrollbarStart) * scrollRange);
    newScrollOffset = Math.max(0, Math.min(newScrollOffset, scrollRange));

    this.setScrollOffset(newScrollOffset);
  }

  /**
   * Resets the clicked state of all clickable regions when the left mouse button
   * is released.
   *
   * @param mouseX the mouse X-coordinate
   * @param mouseY the mouse Y-coordinate
   * @param button the mouse button that was released (0 for left, 1 for right,
   *               2 for middle)
   *
   * @return true to propagate the event, false to cancel it
   */
  @Override
  public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
    if (button == 0) {
      ClickableRegion.foundClickedRegions(this.clickableRegions).forEach(region -> {
        region.setClicked(false);
      });
    }

    return super.mouseReleased(mouseX, mouseY, button);
  }

  /**
   * Handles mouse drag events on the screen.
   * <p>
   * This method iterates over all clickable regions that are currently being
   * clicked.
   * </p>
   * <p>
   * The {@code deltaX} and {@code deltaY} parameters are used to move the
   * clicked region by the given amount.
   * </p>
   * <p>
   * The {@code button} parameter is used to determine which button is being
   * dragged. If the left mouse button is being dragged, the method moves the
   * selected tab. If the right mouse button is being dragged, the method moves
   * the scrollbar.
   * </p>
   * <p>
   * If the clicked region is the advancement criteria container, the method
   * moves the scrollbar by the given amount.
   * </p>
   * <p>
   * If the clicked region is the scrollbar, the method moves the scrollbar to
   * the given Y-coordinate.
   * </p>
   *
   * @param mouseX the mouse X-coordinate
   * @param mouseY the mouse Y-coordinate
   * @param button the mouse button that is being dragged
   * @param deltaX the amount to move the region in the X-direction
   * @param deltaY the amount to move the region in the Y-direction
   *
   * @return true to propagate the event, false to cancel it
   */
  @Override
  public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX,
      final double deltaY) {
    ClickableRegion.foundClickedRegions(this.clickableRegions).forEach(region -> {
      switch (region.getName()) {
        case "advancement_tree":
          if (this.selectedTab.isPresent()) {
            this.selectedTab.get().move(deltaX, deltaY);
          }
          break;
        case "advancement_criterias":
          this.setScrollOffset(this.scrollOffset - (int) deltaY);
          break;
        case "advancement_criterias_scrollbar":
          this.moveScrollbarTo(mouseY);
          break;
        default:
          break;
      }
    });

    return true;
  }

  /**
   * Handles mouse wheel events.
   * <p>
   * If the mouse is inside the advancement tree region, the method moves the
   * selected tab by the given amount in the X and Y directions.
   * </p>
   * <p>
   * If the mouse is inside the advancement criteria container or scrollbar
   * region, the method moves the scrollbar by the given amount in the Y
   * direction.
   * </p>
   *
   * @param mouseX           the mouse X-coordinate
   * @param mouseY           the mouse Y-coordinate
   * @param horizontalAmount the amount to move the region in the X-direction
   * @param verticalAmount   the amount to move the region in the Y-direction
   *
   * @return true to propagate the event, false to cancel it
   */
  @Override
  public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount,
      final double verticalAmount) {
    ClickableRegion.foundRegions(this.clickableRegions, mouseX, mouseY).forEach(region -> {
      switch (region.getName()) {
        case "advancement_tree":
          if (this.selectedTab.isPresent())
            this.selectedTab.get().move(horizontalAmount * 16.0, verticalAmount * 16.0);
          break;
        case "advancement_criterias", "advancement_criterias_scrollbar":
          this.setScrollOffset(this.scrollOffset - (int) verticalAmount * 16);
          break;
      }
    });

    return true;
  }

  /**
   * Handles key press events.
   * <p>
   * If the key is the advancements key binding, it closes the screen and
   * releases the mouse, unless the search box is focused.
   * </p>
   * <p>
   * If the key is the escape key and there is a visible sidebar, it resets the
   * screen's state by setting the selected widget to null and calling the
   * {@link #init()} method.
   * </p>
   *
   * @param keyCode   the key code of the key that was pressed
   * @param scanCode  the scan code of the key that was pressed
   * @param modifiers the modifiers of the key that was pressed
   *
   * @return true if the event was handled, false otherwise
   */
  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    // If search box is focused, don't exit the screen when the advancements key is
    // pressed
    if (this.searchBox.isFocused() && this.minecraft.options.keyAdvancements.matches(keyCode, scanCode)) {
      return true; // Consume the key press, don't exit the screen
    }

    if (this.minecraft.options.keyAdvancements.matches(keyCode, scanCode)) {
      this.minecraft.setScreen(this.parent);
      this.minecraft.mouseHandler.grabMouse();
      return true;
    } else if (InputConstants.KEY_ESCAPE == keyCode) {
      if (this.searchBox.isFocused()) {
        // Remove focus from search box before processing Escape
        this.searchBox.setFocused(false);
        return true;
      } else if (this.isSearching) {
        // Clear search text when Escape is pressed during search
        this.searchText = "";
        this.searchBox.setValue("");
        this.isSearching = false;
        return true;
      } else if (this.hasVisibleSidebar()) {
        this.setSelectedWidget(null);
        this.init();
        return true;
      }
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  /**
   * Renders the screen.
   * <p>
   * This method renders the background, calls the superclass's
   * {@link Screen#render(GuiGraphics, int, int, float)} method, and then renders
   * the advancement tree, window, widget tooltip, and advancement criteria.
   * </p>
   * <p>
   * The advancement tree is rendered at the top of the screen, and the window,
   * widget tooltip, and advancement criteria are rendered below it.
   * </p>
   * <p>
   * The window is rendered at the position (0, {@link Configuration#headerHeight
   * } + 1),
   * and the widget tooltip and advancement criteria are rendered below it.
   * </p>
   *
   * @param context the GUI graphics
   * @param mouseX  the mouse X-coordinate
   * @param mouseY  the mouse Y-coordinate
   * @param delta   the time elapsed since the last frame
   */
  @Override
  public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
    final int headerOffset = Configuration.headerHeight + 1; // 1 are the separator pixels

    context.nextStratum();
    this.renderAdvancementTree(context, mouseX, mouseY, 0, headerOffset);
    context.nextStratum();
    this.renderAdvancementCriterias(context, 0, headerOffset);
    context.nextStratum();
    this.renderWindow(context, 0, headerOffset);
    context.nextStratum();
    this.renderWidgetTooltip(context, mouseX, mouseY, 0, headerOffset);
    context.nextStratum();
    this.renderRenderable(context, mouseX, mouseY, delta);
  }

  /**
   * Renders all renderable components on the screen.
   * <p>
   * This method iterates over all the renderable components and invokes their
   * render method with the given graphics context and mouse coordinates.
   * </p>
   *
   * @param context the graphics context used for rendering
   * @param mouseX  the x-coordinate of the mouse
   * @param mouseY  the y-coordinate of the mouse
   * @param delta   the time elapsed since the last frame
   */
  public void renderRenderable(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
    for (final Renderable renderable : this.renderables) {
      renderable.render(context, mouseX, mouseY, delta);
    }
  }

  /**
   * Renders the background of the screen.
   * <p>
   * This method is called by the screen before rendering the rest of the UI.
   * </p>
   * <p>
   * The style of the background is determined by the
   * {@link Configuration#backgroundStyle} field. If it is set to
   * {@link Configuration.BackgroundStyle#TRANSPARENT}, the method renders a
   * blurred version of the background. If it is set to
   * {@link Configuration.BackgroundStyle#BLACK}, the method renders a solid
   * black background. If it is set to
   * {@link Configuration.BackgroundStyle#ACHIEVEMENT}, the method renders the
   * background of the currently selected advancement, if any.
   * </p>
   *
   * @param context the GUI graphics
   * @param mouseX  the mouse X-coordinate
   * @param mouseY  the mouse Y-coordinate
   * @param delta   the time elapsed since the last frame
   */
  @Override
  public void renderBackground(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
    switch (Configuration.backgroundStyle) {
      case Configuration.BackgroundStyle.TRANSPARENT:
        super.renderBackground(context, mouseX, mouseY, delta);
        break;
      case Configuration.BackgroundStyle.BLACK:
        context.fill(0, 0, width, height, CommonColors.BLACK);
        break;
      case Configuration.BackgroundStyle.ACHIEVEMENT:
        this.selectedTab.ifPresent(tab -> {
          final ResourceLocation textureResourceLocation = tab.getDisplay().getBackground()
              .orElse(Utils.INTENTIONAL_MISSING_TEXTURE)
              .texturePath();
          context.blit(this.renderTypeGui, textureResourceLocation, 0, 0, 0.0F, 0.0F, width, height, 16, 16);
        });
        context.fill(0, 0, width, height, Mth.floor(0.7 * 255.0F) << 24);
        break;
    }
  }

  /**
   * Draws the advancement tree, handling the background style of the tree.
   * <p>
   * If the background style is {@link Configuration.BackgroundStyle#TRANSPARENT},
   * nothing is rendered.
   * </p>
   * <p>
   * If the background style is {@link Configuration.BackgroundStyle#BLACK},
   * a black rectangle is rendered at the top of the screen.
   * </p>
   * <p>
   * If the background style is {@link Configuration.BackgroundStyle#ACHIEVEMENT},
   * the background texture of the currently selected tab is rendered at the top
   * of the screen, with a 70% opacity black rectangle rendered on top of it.
   * </p>
   * <p>
   * If no tab is selected, a message is rendered in the center of the screen
   * with a sad face and the text "No advancements found".
   * </p>
   * <p>
   * Otherwise, the currently selected tab is rendered at the position (x, y).
   * </p>
   *
   * @param context the GUI graphics
   * @param mouseX  the mouse X-coordinate
   * @param mouseY  the mouse Y-coordinate
   * @param x       the X-coordinate of the tab
   * @param y       the Y-coordinate of the tab
   */
  private void renderAdvancementTree(final GuiGraphics context, final int mouseX, final int mouseY, final int x,
      final int y) {
    if (this.selectedTab.isEmpty()) {
      context.drawCenteredString(this.font, EMPTY_TEXT, width / 2,
          (height / 2) - this.font.lineHeight * 2, CommonColors.WHITE);
      context.drawCenteredString(this.font, SAD_LABEL_TEXT, width / 2,
          (height / 2) + this.font.lineHeight * 2, CommonColors.WHITE);
    } else {
      this.selectedTab.get().render(context, x, y);
    }
  }

  /**
   * Draws the advancement criteria for the currently selected widget.
   *
   * <p>
   * This method is called by {@link #render(GuiGraphics, int, int, float)} and
   * is responsible for rendering the advancement criteria for the currently
   * selected widget. It first checks if the sidebar is visible and if the
   * criteria width is greater than 0. If not, it returns immediately.
   * </p>
   *
   * <p>
   * It then calculates the width of the text and the padding for the title and
   * description. It then draws the title and description using and also draws a
   * line below the description. Finally, it draws the advancement criteria for
   * each step in the selected widget's progress.
   * </p>
   *
   * @param context the graphics context to draw on
   * @param x       the x position of the screen
   * @param y       the y position of the screen
   */
  public void renderAdvancementCriterias(final GuiGraphics context, final int x, final int y) {
    if (!this.hasVisibleSidebar() || Configuration.criteriasWidth == 0)
      return;

    int paddingTop = Configuration.headerHeight + 6;
    final int sidebarXOffset = width - Configuration.criteriasWidth + 8;
    // 6 are the scrollbar width and 12 (6[left]-6[right]) are the right margin
    final int maxTextWidth = Configuration.criteriasWidth - (this.needScrollbarOnCriterias() ? 6 : 0) - 12;

    final Component title = this.getSelectedWidget().getAdvancement().name().get();
    final Component description = this.getSelectedWidget().getAdvancement().display().get().getDescription();

    context.fill(width - Configuration.criteriasWidth, Configuration.headerHeight, width,
        height - Configuration.footerHeight, Mth.floor(0.5F * 255.0F) << 24);

    context.blit(this.renderTypeGui, criteriasSeparator, width - Configuration.criteriasWidth,
        Configuration.headerHeight + 1, 0.0F, 0.0F, 2,
        height - Configuration.headerHeight - Configuration.footerHeight
            - 2,
        2, 32);

    final Matrix3x2fStack postStack = context.pose();
    postStack.pushMatrix();
    postStack.translate(0, -this.scrollOffset);

    this.contentHeight = 6; // 6 are the bottom margin

    // Drawing title
    context.drawWordWrap(this.font, title, sidebarXOffset, paddingTop, maxTextWidth, CommonColors.WHITE);
    // 4 are the padding bottom added
    paddingTop += (this.font.lineHeight) * this.font.split(title, maxTextWidth).size() + 4;
    this.contentHeight += (this.font.lineHeight) * this.font.split(title, maxTextWidth).size() + 4;

    // Drawing description
    if (Configuration.displayDescription && description != null) {
      context.drawWordWrap(this.font, description, sidebarXOffset, paddingTop, maxTextWidth,
          this.getSelectedWidget().getAdvancement().display().get().getType().getChatColor().getColor());
      // 4 are the padding bottom added
      paddingTop += (this.font.lineHeight) * this.font.split(description, maxTextWidth).size() + 4;
      this.contentHeight += (this.font.lineHeight) * this.font.split(description, maxTextWidth).size() + 4;
    }

    context.hLine(sidebarXOffset, width - 12, paddingTop, CommonColors.LIGHT_GRAY);
    paddingTop += 5;
    this.contentHeight += 5;

    // Drawing criterias
    for (final ReloadedCriterionProgress step : this.getSelectedWidget().getSteps()) {
      final Component stepTitle = step.getHumanCriterionName();
      final int lineNeeded = this.font.split(stepTitle, maxTextWidth).size();
      context.drawWordWrap(this.font, stepTitle, sidebarXOffset, paddingTop, maxTextWidth, step.getColor());
      // 4 are the padding bottom added
      paddingTop += (this.font.lineHeight) * lineNeeded + 4;
      this.contentHeight += (this.font.lineHeight) * lineNeeded + 4;
    }

    postStack.popMatrix();

    this.drawAdvancementCriteriaScrollbar(context, x, y);
  }

  /**
   * Draws the scrollbar for the advancement criteria area if needed.
   * A scrollbar is needed if the height of the content exceeds the available
   * height
   * of the window, minus the header and footer height.
   *
   * @param context the graphical context
   * @param x       the x position of the sidebar
   * @param y       the y position of the sidebar
   */
  private void drawAdvancementCriteriaScrollbar(final GuiGraphics context, final int x, final int y) {
    // Drawing scrollbar if needed
    if (!this.needScrollbarOnCriterias()) {
      return;
    }

    final int drawingHeight = height - Configuration.headerHeight
        - Configuration.footerHeight;

    // Drawing scrollbar background
    context.blitSprite(this.renderTypeGui, SCROLLER_BACKGROUND_TEXTURE, width - 6, Configuration.headerHeight,
        6, drawingHeight);

    // Drawing the scrollbar
    final int scrollBarHeight = (int) (drawingHeight * drawingHeight / (double) this.contentHeight);
    final int scrollBarY = Configuration.headerHeight
        + (int) ((drawingHeight - scrollBarHeight)
            * (this.scrollOffset / (double) (this.contentHeight - (drawingHeight))));

    // draw the scrollbar
    context.blitSprite(this.renderTypeGui, SCROLLER_TEXTURE, width - 6, scrollBarY, 6, scrollBarHeight);

  }

  /**
   * Returns whether a scrollbar is needed in the advancement criteria area.
   * This is true if the height of the content exceeds the available height
   * of the window, minus the header and footer height.
   *
   * @return true if a scrollbar is needed, false otherwise
   */
  private boolean needScrollbarOnCriterias() {
    return this.contentHeight > height - Configuration.headerHeight
        - Configuration.footerHeight;
  }

  /**
   * Whether the sidebar is currently visible.
   *
   * @return true if the sidebar is visible, false otherwise
   */
  public boolean hasVisibleSidebar() {
    return this.getSelectedWidget() != null && Configuration.displaySidebar;
  }

  /**
   * Draws the window for this screen.
   * 
   * <p>
   * This method draws the window background, header, footer, separators, and
   * title. It also
   * draws the tabs and tab icons.
   * </p>
   * 
   * <p>
   * The given x and y coordinates are used to position the window.
   * </p>
   *
   * @param context the GuiGraphics context to draw with
   * @param x       the x coordinate of the window
   * @param y       the y coordinate of the window
   */
  public void renderWindow(final GuiGraphics context, final int x, int y) {

    if (this.selectedTab.isPresent()) {
      final DisplayInfo display = this.selectedTab.get().getDisplay();
      final ResourceLocation textureResourceLocation = display.getBackground().orElse(Utils.INTENTIONAL_MISSING_TEXTURE)
          .texturePath();

      // Draw header
      final int headerDrawHeight = Configuration.headerHeight / 16 + 1;
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < headerDrawHeight; n++) {
          int textureHeight = 16;
          if (n == headerDrawHeight - 1) {
            textureHeight = Configuration.headerHeight % 16;
          }

          context.blit(this.renderTypeGui, textureResourceLocation, 16 * m, 16 * n, 0.0F, 0.0F, 16, textureHeight,
              16, 16);
        }
      }
      context.fill(0, 0, width, Configuration.headerHeight,
          Mth.floor(0.3F * 255.0F) << 24);

      // Draw footer
      final int footerDrawHeight = Configuration.footerHeight / 16 + 1;
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < footerDrawHeight; n++) {
          int textureHeight = 16;
          if (n == headerDrawHeight - 1) {
            textureHeight = Configuration.headerHeight % 16;
          }

          context.blit(this.renderTypeGui, textureResourceLocation, 16 * m,
              (height - Configuration.footerHeight) + 16 * n, 0.0F,
              0.0F, 16, textureHeight, 16, 16);
        }
      }
      context.fill(0, height - Configuration.footerHeight, width, height,
          Mth.floor(0.3F * 255.0F) << 24);

      // Draw separators
      this.drawSeparators(context, 0.7F);

      // Draw title on header
      context.drawCenteredString(this.font, display.getTitle(), width / 2,
          (Configuration.headerHeight - 20) / 2 - this.font.lineHeight / 2, 0xffffff);
    }

    if (this.tabs.size() > 0) {
      for (final AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab.getTabPlacement() == TabPlacement.ABOVE) {
          y = Configuration.headerHeight + 1;
        } else {
          y = height - Configuration.footerHeight - 1;
        }

        advancementTab.setPos(x + 4, y);
        advancementTab.drawBackground(context, advancementTab == this.selectedTab.orElse(null));
        advancementTab.drawIcon(context);
      }
    }

  }

  /**
   * Draws separators for this window.
   * 
   * <p>
   * Separators are horizontal lines that separate the header, footer, and main
   * content areas of the window.
   * </p>
   * 
   * <p>
   * The given alpha value controls the transparency of the separators. The
   * separators are drawn with the same texture as the header and footer
   * backgrounds.
   * </p>
   *
   * @param context the graphics context to draw on
   * @param alpha   the transparency of the separators (0.0-1.0)
   */
  private void drawSeparators(final GuiGraphics context, final float alpha) {
    // Enable blending
    // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

    // Bind and draw header texture
    context.blit(this.renderTypeGui, Screen.INWORLD_HEADER_SEPARATOR, 0,
        Configuration.headerHeight - 1, 0.0F, 0.0F,
        width, 2, 32, 2);

    // Bind and draw footer texture
    context.blit(this.renderTypeGui, Screen.INWORLD_FOOTER_SEPARATOR, 0,
        height - Configuration.footerHeight - 1, 0.0F,
        0.0F, width, 2, 32, 2);

    // Reset shader color to avoid affecting subsequent draws
    // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
  }

  /**
   * Draws a tooltip for a given widget and/or tab, if the given mouse position
   * is within the bounds of the widget or tab.
   *
   * @param context the graphics context to draw on
   * @param mouseX  the x-coordinate of the mouse
   * @param mouseY  the y-coordinate of the mouse
   * @param x       the x-coordinate of the widget or tab
   * @param y       the y-coordinate of the widget or tab
   */
  private void renderWidgetTooltip(final GuiGraphics context, final int mouseX, final int mouseY, final int x,
      final int y) {
    if (this.selectedTab.isPresent()) {
      context.pose().pushMatrix();
      context.pose().translate((float) (x), (float) (y));
      this.selectedTab.get().drawWidgetTooltip(context, mouseX - x, mouseY - y, x, y);
      context.pose().popMatrix();
    }

    if (this.tabs.size() > 1) {
      for (final AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab.isClickOnTab(x, y, (double) mouseX, (double) mouseY)) {
          context.setTooltipForNextFrame(this.font, advancementTab.getTitle(), mouseX, mouseY);
        }
      }
    }
  }

  /**
   * Sorts the tabs alphabetically by their title. If the configuration
   * value {@link Configuration#tabsAlphabeticOrder} is set to {@code true},
   * the tabs are sorted based on the string representation of their title.
   * Otherwise, the tabs are not sorted.
   */
  private void sortTabsAlphabetically() {
    if (Configuration.tabsAlphabeticOrder) {
      final List<AdvancementReloadedTab> sortedTabs = new ArrayList<>(this.tabs.values());
      sortedTabs.sort(Comparator.comparing(
          tab -> tab.getRoot().advancement().name().orElse(Component.literal(tab.getRoot().toString())).getString()));

      this.tabs.clear();
      for (int index = 0; index < sortedTabs.size(); index++) {
        final AdvancementReloadedTab tab = sortedTabs.get(index);

        // Set the index of the tab to its index in the sorted list
        // This is used to identify the tab in the tab list and to determine
        // the correct position on UI.
        tab.setIndex(index);
        tab.setTabPlacement(Configuration.aboveWidgetLimit > index ? TabPlacement.ABOVE : TabPlacement.BELOW);
        this.tabs.put(tab.getRoot().holder(), tab);
      }
    }
  }

  /**
   * Adds a new root advancement to the list of tabs, if the given root's
   * advancement has a display information.
   *
   * @param root the root advancement node to add
   */
  public void onRootAdded(final AdvancementNode root) {
    final AdvancementReloadedTab advancementTab = AdvancementReloadedTab.create(this.minecraft, this, this.tabs.size(),
        root);
    if (advancementTab != null) {
      this.tabs.put(root.holder(), advancementTab);
      this.sortTabsAlphabetically();
    }
  }

  /**
   * This implementation does nothing.
   *
   * @param root the removed root advancement node
   */
  public void onRootRemoved(final AdvancementNode root) {
  }

  /**
   * Called when a dependent of the given root advancement is added.
   * The given dependent is added to the tab the root is in.
   *
   * @param dependent the dependent to add
   */
  public void onDependentAdded(final AdvancementNode dependent) {
    final AdvancementReloadedTab advancementTab = this.getTab(dependent);
    if (advancementTab != null) {
      advancementTab.addAdvancement(dependent);
    }
  }

  /**
   * Called when a dependent of the given root advancement is removed.
   * The given dependent is removed from the tab the root is in.
   *
   * @param dependent the dependent to remove
   */
  public void onDependentRemoved(final AdvancementNode dependent) {
  }

  /**
   * Called when the progress of an advancement changes.
   * The given advancement is the advancement with changed progress, and the given
   * progress is the new progress.
   * The given progress is set on the widget for the given advancement, if such a
   * widget exists.
   *
   * @param advancement the advancement with changed progress
   * @param progress    the new progress
   */
  @Override
  public void onUpdateAdvancementProgress(final AdvancementNode advancement, final AdvancementProgress progress) {
    final AdvancementReloadedWidget advancementWidget = this.getAdvancementWidget(advancement);
    if (advancementWidget != null) {
      advancementWidget.setProgress(progress);
    }
  }

  /**
   * Called when the selected tab changes.
   * The given advancement is the selected tab, or null if no tab is selected.
   * The selected tab is set to the given tab.
   *
   * @param advancement the selected tab, or null if no tab is selected
   */
  @Override
  public void onSelectedTabChanged(@Nullable final AdvancementHolder advancement) {
    this.setSelectedTab(this.tabs.get(advancement));
  }

  /**
   * Sets the currently selected tab to the given tab.
   *
   * @param tab the tab to select, or null to select none
   */
  public void setSelectedTab(final AdvancementReloadedTab tab) {
    this.setSelectedTab(Optional.ofNullable(tab));
  }

  /**
   * Sets the currently selected tab to the given tab.
   * If the given tab is empty, no tab is selected.
   * The clickable regions for the widgets are recalculated.
   *
   * @param tab the tab to select, or an empty optional to select none
   */
  public void setSelectedTab(final Optional<AdvancementReloadedTab> tab) {
    this.selectedTab = tab;
    this.initClickableRegions();
  }

  /**
   * Sets the currently selected widget to the given widget.
   * The selected widget is used to determine the initial scroll position when the
   * window is closed and reopened.
   * The clickable regions for the widgets are recalculated.
   *
   * @param widget the widget to select, or null to select none
   */
  public void setSelectedWidget(final AdvancementReloadedWidget widget) {
    this.selectedWidget = widget;
    this.scrollOffset = 0;
    Memory.setWidget(widget);
    this.initClickableRegions();
  }

  /**
   * Sets the scroll offset to the given value, clamping it to the range from 0 to
   * the maximum scroll
   * offset.
   *
   * @param value the scroll offset to set
   */
  private void setScrollOffset(final int value) {
    if (!this.needScrollbarOnCriterias())
      return;
    final int max = this.contentHeight - (height - Configuration.headerHeight
        - Configuration.footerHeight);
    this.scrollOffset = Mth.clamp(value, 0, max);
  }

  /**
   * Returns the currently selected widget, or null if no widget is selected.
   * The selected widget is used to determine the initial scroll position when the
   * window is closed and reopened.
   *
   * @return the currently selected widget, or null if no widget is selected
   */
  @Nullable
  public AdvancementReloadedWidget getSelectedWidget() {
    return this.selectedWidget;
  }

  /**
   * Clears all tabs and resets the selected tab to none.
   */
  public void onClear() {
    this.tabs.clear();
    this.selectedTab = null;
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
  public AdvancementReloadedWidget getAdvancementWidget(final AdvancementNode advancement) {
    final AdvancementReloadedTab advancementTab = this.getTab(advancement);
    return advancementTab == null ? null : advancementTab.getWidget(advancement.holder());
  }

  /**
   * Gets the tab associated with the given advancement, or null if no such tab
   * exists. The tab is determined by the root of the given advancement.
   *
   * @param advancement the advancement to get the tab for
   * @return the tab associated with the given advancement, or null if no such
   *         tab exists
   */
  @Nullable
  private AdvancementReloadedTab getTab(final AdvancementNode advancement) {
    final AdvancementNode placedAdvancement = advancement.root();
    return this.tabs.get(placedAdvancement.holder());
  }

  /**
   * Adds a new root advancement to the list of tabs, if the given root's
   * advancement has a display information.
   *
   * @param advancement the root advancement node to add
   */
  @Override
  public void onAddAdvancementRoot(final AdvancementNode advancement) {
    final AdvancementReloadedTab advancementTab = AdvancementReloadedTab.create(this.minecraft, this, this.tabs.size(),
        advancement);
    if (advancementTab != null) {
      this.tabs.put(advancement.holder(), advancementTab);
      this.sortTabsAlphabetically();
    }
  }

  /**
   * Removes the tab associated with the given root advancement, if such a tab
   * exists.
   *
   * @param advancement the root advancement node to remove
   */
  @Override
  public void onRemoveAdvancementRoot(final AdvancementNode advancement) {
  }

  /**
   * Adds a new advancement to the tab associated with its root advancement, if
   * such a tab exists.
   *
   * @param advancement the advancement to add
   */
  @Override
  public void onAddAdvancementTask(final AdvancementNode advancement) {
    final AdvancementReloadedTab advancementTab = this.getTab(advancement);
    if (advancementTab != null) {
      advancementTab.addAdvancement(advancement);
    }
  }

  /**
   * Removes the widget associated with the given advancement from its tab, if
   * such a tab and widget exist.
   *
   * @param advancement the advancement to remove
   */
  @Override
  public void onRemoveAdvancementTask(final AdvancementNode advancement) {
  }

  /**
   * Called when all advancements have been cleared from the advancement manager.
   * Resets the screen state to its initial state.
   */
  @Override
  public void onAdvancementsCleared() {
    this.tabs.clear();
    this.selectedTab = null;
  }

  /**
   * Called when the search text changes.
   * Updates the search text field and triggers a search across all advancements.
   *
   * @param text the new search text
   */
  private void onSearchTextChanged(final String text) {
    this.searchText = text;
    this.isSearching = !text.isEmpty();
    // Refresh the screen to show search results
    this.initClickableRegions();
  }

  /**
   * Checks if the screen is currently in search mode.
   *
   * @return true if searching, false otherwise
   */
  public boolean isSearching() {
    return this.isSearching;
  }

  /**
   * Returns the current search text.
   *
   * @return the current search text
   */
  public String getSearchText() {
    return this.searchText;
  }
}
