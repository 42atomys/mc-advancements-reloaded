package codes.atomys.advr.screens;

import codes.atomys.advr.ReloadedCriterionProgress;
import codes.atomys.advr.config.Configuration;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * The AdvancementReloadedWidget class represents a widget for rendering
 * advancements in the advancement tab GUI. It is responsible for rendering
 * the title, description, and icon of an advancement, as well as handling
 * user interactions such as clicking on the widget.
 *
 * <p>
 * The class contains several fields to hold the advancement node, display
 * information, title, description, and client instance. It also contains
 * methods to calculate the position and size of the widget, render the title
 * and description, and handle user clicks.
 * </p>
 *
 * @see AdvancementReloadedTab
 * @see AdvancementNode
 * @see DisplayInfo
 */
public class AdvancementReloadedWidget {
  private static final ResourceLocation TITLE_BOX_TEXTURE = ResourceLocation
      .withDefaultNamespace("advancements/title_box");

  // CHECKSTYLE:OFF
  private static final int[] SPLIT_OFFSET_CANDIDATES = new int[] { 0, 10, -10, 25, -25 };
  // CHECKSTYLE:ON

  private final AdvancementReloadedTab tab;
  private final AdvancementNode advancement;
  private final DisplayInfo display;
  private final FormattedCharSequence title;
  private final int width;
  private final List<FormattedCharSequence> description;
  private final Minecraft client;
  @Nullable
  private AdvancementReloadedWidget parent;
  private final List<AdvancementReloadedWidget> children = Lists.newArrayList();
  @Nullable
  private AdvancementProgress progress;
  private List<ReloadedCriterionProgress> steps;
  private final int x;
  private final int y;

  /**
   * The constructor for the AdvancementReloadedWidget class.
   * It takes an AdvancementReloadedTab, a Minecraft client, an AdvancementNode,
   * and a DisplayInfo as parameters, and initializes the widget with the given
   * data.
   * <p>
   * The constructor calculates the position and size of the widget, renders the
   * title and description, and handles user clicks.
   * </p>
   * <p>
   * The constructor also initializes the list of children and the list of steps
   * with the given parameters.
   * </p>
   *
   * @param tab         the tab that the widget belongs to
   * @param client      the Minecraft client instance
   * @param advancement the advancement node
   * @param display     the display information for the advancement
   */
  public AdvancementReloadedWidget(final AdvancementReloadedTab tab, final Minecraft client,
      final AdvancementNode advancement,
      final DisplayInfo display) {
    this.tab = tab;
    this.advancement = advancement;
    this.display = display;
    this.client = client;
    this.title = Language.getInstance().getVisualOrder(client.font.substrByWidth(display.getTitle(), 163));
    this.x = Mth.floor(display.getX() * 28.0F);
    this.y = Mth.floor(display.getY() * 27.0F);
    final int i = this.getProgressWidth();
    int j = 29 + client.font.width(this.title) + i;
    this.description = Language.getInstance()
        .getVisualOrder(this.wrapDescription(ComponentUtils.mergeStyles(display.getDescription().copy(),
            Style.EMPTY.withColor(display.getType().getChatColor())), j));

    for (final FormattedCharSequence orderedText : this.description) {
      j = Math.max(j, client.font.width(orderedText));
    }

    if (this.progress != null) {
      this.setSteps(this.progress);
    }

    this.width = j + 3 + 5;
  }

  /**
   * Gets the width of the progress bar that is displayed when the advancement has
   * multiple requirements.
   * The width is calculated from the translation key "advancements.progress" and
   * adds 8 pixels to the width of the text.
   * If the advancement has only one requirement, the width will be 0.
   *
   * @return the width of the progress bar
   */
  private int getProgressWidth() {
    final int i = this.advancement.advancement().requirements().size();
    if (i <= 1)
      return 0;
    final int j = 8;
    final MutableComponent mutableText = Component.translatable(
        "advancements.progress",
        // CHECKSTYLE:OFF
        new Object[] { Integer.valueOf(i), Integer.valueOf(i) });
    // CHECKSTYLE:ON
    return this.client.font.width((FormattedText) mutableText) + j;
  }

  /**
   * Gets the maximum width of the given lines when split by the given
   * StringSplitter.
   *
   * @param strSplitter the StringSplitter to split the lines with
   * @param lines       the lines to get the maximum width of
   * @return the maximum width of the lines
   */
  private static float getMaxWidth(final StringSplitter strSplitter, final List<FormattedText> lines) {
    Objects.requireNonNull(strSplitter);
    return (float) lines.stream().mapToDouble(strSplitter::stringWidth).max().orElse(0.0D);
  }

  /**
   * Wraps the given description to fit the given width.
   * This method works by splitting the description into lines and adjusting the
   * width of each line until the description fits the given width.
   * The method attempts to find the best width for the description by trying out
   * different offset values and selecting the one that results in the smallest
   * difference between the actual width and the given width.
   * If no suitable offset is found, the method returns the original list of
   * lines.
   * <p>
   * The method is used to wrap the description of an advancement in the
   * advancement tree.
   * </p>
   *
   * @param text  the description text
   * @param width the width to wrap the text to
   * @return the wrapped description
   */
  private List<FormattedText> wrapDescription(final Component text, final int width) {
    final StringSplitter strSplitter = this.client.font.getSplitter();
    List<FormattedText> list = null;
    float f = Float.MAX_VALUE;
    final int[] var6 = SPLIT_OFFSET_CANDIDATES;
    final int var7 = var6.length;

    for (int var8 = 0; var8 < var7; ++var8) {
      final int i = var6[var8];
      final List<FormattedText> list2 = strSplitter.splitLines(text, width - i, Style.EMPTY);
      final float g = Math.abs(getMaxWidth(strSplitter, list2) - (float) width);
      if (g <= 10.0F) {
        return list2;
      }

      if (g < f) {
        f = g;
        list = list2;
      }
    }

    return list;
  }

  /**
   * Finds the parent widget of the given advancement node.
   * <p>
   * The parent widget is the widget representing the parent of the given
   * advancement node. The parent advancement node is found by traversing the
   * tree of advancements up until an advancement with a display is found, or
   * until the root of the tree is reached. If no parent advancement with a
   * display is found, the method returns null.
   * </p>
   *
   * @param advancement the advancement node to find the parent of
   * @return the parent widget of the given advancement node, or null if no such
   *         parent exists
   */
  @Nullable
  private AdvancementReloadedWidget getParent(AdvancementNode advancement) {
    do {
      advancement = advancement.parent();
    } while (advancement != null && advancement.advancement().display().isEmpty());

    if (advancement != null && !advancement.advancement().display().isEmpty()) {
      return this.tab.getWidget(advancement.holder());
    } else {
      return null;
    }
  }

  /**
   * Gets the advancement this widget belongs to.
   *
   * @return the advancement this widget belongs to
   */
  public Advancement getAdvancement() {
    return this.advancement.advancement();
  }

  /**
   * Returns the progress of this widget's advancement (vanilla minecraft
   * progress).
   *
   * @return the progress of this widget's advancement
   */
  public AdvancementProgress getProgress() {
    return this.progress;
  }

  /**
   * Returns the list of {@link ReloadedCriterionProgress} for this widget's
   * criteria. The list is in the order of the criteria in the advancement JSON.
   *
   * @return the list of {@link ReloadedCriterionProgress} for this widget's
   *         criteria
   */
  public List<ReloadedCriterionProgress> getSteps() {
    return this.steps;
  }

  /**
   * Renders lines connecting this widget to its parent, if any.
   *
   * @param context the GUI graphics context to render to
   * @param x       the x-coordinate of the widget
   * @param y       the y-coordinate of the widget
   * @param border  whether to render a border around the connecting lines
   */
  public void renderLines(final GuiGraphics context, final int x, final int y, final boolean border) {
    if (this.parent != null) {
      final int i = x + this.parent.x + 13;
      final int j = x + this.parent.x + 26 + 4;
      final int k = y + this.parent.y + 13;
      final int l = x + this.x + 13;
      final int m = y + this.y + 13;
      final int n = border ? -16777216 : -1;
      if (border) {
        context.hLine(j, i, k - 1, n);
        context.hLine(j + 1, i, k, n);
        context.hLine(j, i, k + 1, n);
        context.hLine(l, j - 1, m - 1, n);
        context.hLine(l, j - 1, m, n);
        context.hLine(l, j - 1, m + 1, n);
        context.vLine(j - 1, m, k, n);
        context.vLine(j + 1, m, k, n);
      } else {
        context.hLine(j, i, k, n);
        context.hLine(l, j, m, n);
        context.vLine(j, m, k, n);
      }
    }
    for (final AdvancementReloadedWidget advancementWidget : this.children)
      advancementWidget.renderLines(context, x, y, border);
  }

  /**
   * Renders this widget, including its icon and connecting lines to any
   * children, if any. The widget is only rendered if the advancement is not
   * hidden or if the player has completed the advancement.
   *
   * @param context the GUI graphics context to render to
   * @param x       the x-coordinate of the widget
   * @param y       the y-coordinate of the widget
   */
  public void renderWidgets(final GuiGraphics context, final int x, final int y) {
    if (!this.display.isHidden() || (this.progress != null && this.progress.isDone())) {
      final AdvancementWidgetType advancementObtainedStatus;
      final float f = (this.progress == null) ? 0.0F : this.progress.getPercent();
      if (f >= 1.0F) {
        advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
      } else {
        advancementObtainedStatus = AdvancementWidgetType.UNOBTAINED;
      }
      context.blitSprite(advancementObtainedStatus.frameSprite(this.display.getType()), x + this.x + 3,
          y + this.y, 26, 26);
      context.renderFakeItem(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
    }
    for (final AdvancementReloadedWidget advancementWidget : this.children)
      advancementWidget.renderWidgets(context, x, y);
  }

  /**
   * Gets the width of the widget.
   *
   * @return the width of the widget
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Sets the progress of this widget.
   * <p>
   * This method also calls
   * {@link #setSteps(AdvancementProgress)} to update the steps of this widget
   * based on the given progress.
   * </p>
   *
   * @param progress the progress to set
   */
  public void setProgress(final AdvancementProgress progress) {
    this.progress = progress;
    this.setSteps(progress);
  }

  /**
   * Sets the steps of this widget.
   * <p>
   * This method gets the remaining and completed criteria from the given
   * progress,
   * and creates a list of {@link ReloadedCriterionProgress} objects from the
   * criteria. If the configuration option for alphabetical ordering is enabled,
   * the criteria are sorted alphabetically, and the unobtained and obtained
   * criteria are sorted separately.
   * </p>
   * <p>
   * The resulting list of steps is stored in the {@link #steps} field.
   * </p>
   *
   * @param progress the progress to get the steps from
   */
  public void setSteps(final AdvancementProgress progress) {
    final List<ReloadedCriterionProgress> steps = new ArrayList<>();
    Iterable<String> remainingCriteriaIterable = progress.getRemainingCriteria();
    Iterable<String> completedCriteriaIterable = progress.getCompletedCriteria();

    if (Configuration.criteriasAlphabeticOrder) {
      final List<String> unobtainedList = new ArrayList<>();
      final List<String> obtainedList = new ArrayList<>();
      remainingCriteriaIterable.forEach(unobtainedList::add);
      completedCriteriaIterable.forEach(obtainedList::add);
      unobtainedList.sort(String::compareToIgnoreCase);
      obtainedList.sort(String::compareToIgnoreCase);
      remainingCriteriaIterable = unobtainedList;
      completedCriteriaIterable = obtainedList;
    }

    remainingCriteriaIterable.forEach((criterion) -> {
      steps.add(new ReloadedCriterionProgress(this.advancement, progress, criterion));
    });
    completedCriteriaIterable.forEach((criterion) -> {
      steps.add(new ReloadedCriterionProgress(this.advancement, progress, criterion));
    });

    this.steps = steps;
  }

  /**
   * Adds a child widget to this widget.
   * <p>
   * This method is used to build the tree of widgets in the screen.
   * </p>
   *
   * @param widget the widget to add as a child of this widget
   */
  public void addChild(final AdvancementReloadedWidget widget) {
    this.children.add(widget);
  }

  /**
   * Renders the tooltip for this widget.
   * <p>
   * This method renders the background, icon, and description of the tooltip,
   * as well as the progress text if the advancement is not completed.
   * </p>
   *
   * @param context the GUI graphics context to render to
   * @param originX the x-coordinate of the tooltip
   * @param originY the y-coordinate of the tooltip
   * @param alpha   the alpha value to use for rendering
   * @param x       the x-coordinate of the mouse
   * @param y       the y-coordinate of the mouse
   */
  public void drawTooltip(final GuiGraphics context, final int originX, final int originY, final float alpha,
      final int x, final int y) {
    final AdvancementWidgetType advancementObtainedStatus;
    final AdvancementWidgetType advancementObtainedStatus2;
    final AdvancementWidgetType advancementObtainedStatus3;
    final int m;
    final boolean bl = (x + originX + this.x + this.width + 26 >= (this.tab.getScreen()).width);
    final Component text = (this.progress == null) ? null : this.progress.getProgressText();
    final int i = (text == null) ? 0 : this.client.font.width(text);
    Objects.requireNonNull(this.client.font);
    final boolean bl2 = (113 - originY - this.y - 26 <= 6 + this.description.size() * 9);
    final float f = (this.progress == null) ? 0.0F : this.progress.getPercent();
    int j = Mth.floor(f * this.width);
    if (f >= 1.0F) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
      advancementObtainedStatus2 = AdvancementWidgetType.OBTAINED;
      advancementObtainedStatus3 = AdvancementWidgetType.OBTAINED;
    } else if (j < 2) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementWidgetType.UNOBTAINED;
      advancementObtainedStatus2 = AdvancementWidgetType.UNOBTAINED;
      advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
    } else if (j > this.width - 2) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
      advancementObtainedStatus2 = AdvancementWidgetType.OBTAINED;
      advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
    } else {
      advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
      advancementObtainedStatus2 = AdvancementWidgetType.UNOBTAINED;
      advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
    }
    final int k = this.width - j;
    RenderSystem.enableBlend();
    final int l = originY + this.y;
    if (bl) {
      m = originX + this.x - this.width + 26 + 6;
    } else {
      m = originX + this.x;
    }
    Objects.requireNonNull(this.client.font);
    final int n = 32 + this.description.size() * 9;
    if (!this.description.isEmpty())
      if (bl2) {
        context.blitSprite(TITLE_BOX_TEXTURE, m, l + 26 - n, this.width, n);
      } else {
        context.blitSprite(TITLE_BOX_TEXTURE, m, l, this.width, n);
      }
    context.blitSprite(advancementObtainedStatus.boxSprite(), 200, 26, 0, 0, m, l, j, 26);
    context.blitSprite(advancementObtainedStatus2.boxSprite(), 200, 26, 200 - k, 0, m + j, l, k, 26);
    context.blitSprite(advancementObtainedStatus3.frameSprite(this.display.getType()), originX + this.x + 3,
        originY + this.y, 26, 26);
    if (bl) {
      context.drawString(this.client.font, this.title, m + 5, originY + this.y + 9, -1);
      if (text != null)
        context.drawString(this.client.font, text, originX + this.x - i, originY + this.y + 9, -1);
    } else {
      context.drawString(this.client.font, this.title, originX + this.x + 32, originY + this.y + 9, -1);
      if (text != null)
        context.drawString(this.client.font, text, originX + this.x + this.width - i - 5,
            originY + this.y + 9, -1);
    }
    if (bl2) {
      for (int o = 0; o < this.description.size(); o++) {
        Objects.requireNonNull(this.client.font);
        context.drawString(this.client.font, this.description.get(o), m + 5, l + 26 - n + 7 + o * 9, -5592406,
            false);
      }
    } else {
      for (int o = 0; o < this.description.size(); o++) {
        Objects.requireNonNull(this.client.font);
        context.drawString(this.client.font, this.description.get(o), m + 5, originY + this.y + 9 + 17 + o * 9,
            -5592406, false);
      }
    }
    context.renderFakeItem(this.display.getIcon(), originX + this.x + 8, originY + this.y + 5);
  }

  /**
   * Determines if the widget should be rendered in the given position.
   * <p>
   * A widget should be rendered if it is not hidden and either the progress is
   * done or the mouse is on the widget.
   * </p>
   *
   * @param originX the x position of the origin in the screen
   * @param originY the y position of the origin in the screen
   * @param mouseX  the x position of the mouse in the screen
   * @param mouseY  the y position of the mouse in the screen
   * @return {@code true} if the widget should be rendered, {@code false}
   *         otherwise
   */
  public boolean shouldRender(final int originX, final int originY, final int mouseX, final int mouseY) {
    if (this.display.isHidden() && (this.progress == null || !this.progress.isDone()))
      return false;

    return this.isMouseOn(originX, originY, mouseX, mouseY);
  }

  /**
   * Determines if the mouse is on the widget.
   * <p>
   * The mouse is on the widget if it is within the bounds of the widget, which
   * are determined by the widget's position and size.
   * </p>
   *
   * @param originX the x position of the origin in the screen
   * @param originY the y position of the origin in the screen
   * @param mouseX  the x position of the mouse in the screen
   * @param mouseY  the y position of the mouse in the screen
   * @return {@code true} if the mouse is on the widget, {@code false} otherwise
   */
  public boolean isMouseOn(final int originX, final int originY, final double mouseX, final double mouseY) {
    return (double) (originX + this.x) < mouseX && mouseX < (double) (originX + this.x + 26)
        && (double) (originY + this.y) < mouseY && mouseY < (double) (originY + this.y + 26);
  }

  /**
   * Adds the widget to the tree, if it has not been added already.
   * <p>
   * This method first checks if the widget has been added to the tree already
   * by checking if the parent is null. If the parent is null, the method then
   * gets the parent widget of the advancement widget and adds itself to the
   * parent.
   * </p>
   */
  public void addToTree() {
    if (this.parent == null && this.advancement.parent() != null) {
      this.parent = this.getParent(this.advancement);
      if (this.parent != null) {
        this.parent.addChild(this);
      }
    }

  }

  /**
   * Returns the y-coordinate of the widget in the tree.
   *
   * @return the y-coordinate of the widget in the tree
   */
  public int getY() {
    return this.y;
  }

  /**
   * Returns the x-coordinate of the widget in the tree.
   *
   * @return the x-coordinate of the widget in the tree
   */
  public int getX() {
    return this.x;
  }
}
