package codes.atomys.advancementinforeloaded.screen;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import codes.atomys.advancementinforeloaded.AdvancementInfoReloaded;
import codes.atomys.advancementinforeloaded.AdvancementInfoReloadedClient;
import codes.atomys.advancementinforeloaded.ConfigModel;
import codes.atomys.advancementinforeloaded.AdvancementReloadedStep;
import codes.atomys.advancementinforeloaded.ClickableRegion;

public class AdvancementReloadedScreen extends Screen implements ClientAdvancementManager.Listener {
  // private static final int TITLE_OFFSET_X = 8;
  // private static final int TITLE_OFFSET_Y = 6;
  // public static final int field_32302 = 16;
  // public static final int field_32303 = 16;
  // private static final double field_45431 = 16.0;
  private static final Identifier criteriasSeparator = Identifier
      .of("advancementinforeloaded:textures/gui/inworld_right_separator.png");
  private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("widget/scroller");
  private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.ofVanilla("widget/scroller_background");

  private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
  private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
  @Nullable
  private final Screen parent;
  private final ClientAdvancementManager advancementHandler;
  private final Map<AdvancementEntry, AdvancementReloadedTab> tabs = Maps
      .<AdvancementEntry, AdvancementReloadedTab>newLinkedHashMap();
  @Nullable
  private Optional<AdvancementReloadedTab> selectedTab;
  private AdvancementReloadedWidget selectedWidget;
  private List<ClickableRegion> clickableRegions;

  public AdvancementReloadedScreen(ClientAdvancementManager advancementHandler) {
    this(advancementHandler, null);
  }

  public AdvancementReloadedScreen(ClientAdvancementManager advancementHandler, @Nullable Screen parent) {
    super(Text.empty());
    this.advancementHandler = advancementHandler;
    this.parent = parent;
  }

  @Override
  protected void init() {
    this.tabs.clear();
    this.selectedTab = null;
    this.selectedWidget = AdvancementInfoReloadedClient.getCurrentWidget();
    this.advancementHandler.setListener(this);
    if (this.selectedTab.isEmpty() && !this.tabs.isEmpty()) {
      AdvancementReloadedTab advancementTab = (AdvancementReloadedTab) this.tabs.values().iterator().next();
      this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
    } else {
      this.selectedTab.ifPresent(tab -> this.advancementHandler.selectTab(tab.getRoot().getAdvancementEntry(), true));
    }

    setClickableRegions();
  }

  private void setClickableRegions() {
    this.clickableRegions = new ArrayList<ClickableRegion>();
    clickableRegions.add(
        ClickableRegion.create("advancement_tree", 0, AdvancementInfoReloaded.getConfig().headerHeight() + 1,
            width - (hasVisibleSidebar() ? AdvancementInfoReloaded.getConfig().criteriasWidth() : 0),
            height - AdvancementInfoReloaded.getConfig().headerHeight()
                - AdvancementInfoReloaded.getConfig().footerHeight()));

    if (hasVisibleSidebar()) {
      clickableRegions.add(
          ClickableRegion.create("advancement_criterias", width - AdvancementInfoReloaded.getConfig().criteriasWidth(),
              AdvancementInfoReloaded.getConfig().headerHeight() + 1,
              AdvancementInfoReloaded.getConfig().criteriasWidth() - 6,
              height - AdvancementInfoReloaded.getConfig().headerHeight()
                  - AdvancementInfoReloaded.getConfig().footerHeight()));
      clickableRegions.add(
          ClickableRegion.create("advancement_criterias_scrollbar", width - 6,
              AdvancementInfoReloaded.getConfig().headerHeight() + 1, 6,
              height - AdvancementInfoReloaded.getConfig().footerHeight()));
    }
  }

  @Override
  public void close() {
    this.client.setScreen(this.parent);
  }

  @Override
  public void removed() {
    this.advancementHandler.setListener(null);
    ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
    if (clientPlayNetworkHandler != null) {
      clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0) {
      ClickableRegion.foundRegions(clickableRegions, mouseX, mouseY).forEach(region -> {
        region.setClicked(true);
      });

      int i = 0;
      int j = AdvancementInfoReloaded.getConfig().headerHeight();

      for (AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab == this.selectedTab.orElse(null)) {
          AdvancementReloadedWidget clickedWidget = advancementTab.clickOnWidget(i, j, mouseX, mouseY);
          if (clickedWidget != null) {
            setSelectedWidget(clickedWidget);
          }
        }

        if (advancementTab.isClickOnTab(i, j, mouseX, mouseY)) {
          this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
          break;
        }
      }

      if (this.needScrollbarOnCriterias()) {
        ClickableRegion
            .findRegion(clickableRegions,
                region -> region.getName().equals("advancement_criterias_scrollbar") && region.isClicked())
            .ifPresent(region -> {
              moveScrollbarTo(mouseY);
            });
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void moveScrollbarTo(double mouseY) {
    // Calculate the viewable height (excluding header and footer)
    int viewableHeight = height - AdvancementInfoReloaded.getConfig().headerHeight()
        - AdvancementInfoReloaded.getConfig().footerHeight();

    // Calculate the start and end positions of the scrollbar container
    int scrollbarStart = AdvancementInfoReloaded.getConfig().headerHeight() + 1;
    int scrollbarEnd = height - AdvancementInfoReloaded.getConfig().footerHeight() - 1;

    // Calculate the scroll range
    int scrollRange = contentHeight - viewableHeight;

    // Calculate the relative mouse position within the scrollbar container
    double relativeMouseY = mouseY - scrollbarStart;

    // Calculate the new scroll offset based on the relative mouse position
    // Ensure the scroll offset is within the valid range (0 to scrollRange)
    int newScrollOffset = (int) (relativeMouseY / (scrollbarEnd - scrollbarStart) * scrollRange);
    newScrollOffset = Math.max(0, Math.min(newScrollOffset, scrollRange));

    setScrollOffset(newScrollOffset);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == 0) {
      ClickableRegion.foundClickedRegions(clickableRegions).forEach(region -> {
        region.setClicked(false);
      });
    }

    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    ClickableRegion.foundClickedRegions(clickableRegions).forEach(region -> {
      switch (region.getName()) {
        case "advancement_tree":
          if (this.selectedTab.isPresent()) {
            this.selectedTab.get().move(deltaX, deltaY);
          }
          break;
        case "advancement_criterias":
          setScrollOffset(scrollOffset - (int) deltaY);
          break;
        case "advancement_criterias_scrollbar":
          moveScrollbarTo(mouseY);
          break;
        default:
          break;
      }
    });

    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    ClickableRegion.foundRegions(clickableRegions, mouseX, mouseY).forEach(region -> {
      switch (region.getName()) {
        case "advancement_tree":
          if (this.selectedTab.isPresent())
            this.selectedTab.get().move(horizontalAmount * 16.0, verticalAmount * 16.0);
          break;
        case "advancement_criterias", "advancement_criterias_scrollbar":
          setScrollOffset(scrollOffset - (int) verticalAmount * 16);
          break;
      }
    });

    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
      this.client.setScreen(null);
      this.client.mouse.lockCursor();
      return true;
    } else if (InputUtil.GLFW_KEY_ESCAPE == keyCode && hasVisibleSidebar()) {
      setSelectedWidget(null);
      init();
      return true;
    } else {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    renderBackground(context, mouseX, mouseY, delta);
    super.render(context, mouseX, mouseY, delta);

    int i = 0;
    int j = AdvancementInfoReloaded.getConfig().headerHeight() + 1; // 1 are the separator pixels
    this.drawAdvancementTree(context, mouseX, mouseY, i, j);
    this.drawWindow(context, i, j);
    this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
    this.drawAdvancementCriterias(context, i, j);
  }

  private void drawAdvancementTree(DrawContext context, int mouseX, int mouseY, int x, int y) {

    switch (AdvancementInfoReloaded.getConfig().backgroundStyle()) {
      case ConfigModel.BackgroundStyle.TRANSPARENT:
        break;
      case ConfigModel.BackgroundStyle.BLACK:
        context.fill(0, 0, width, height, Colors.BLACK);
        break;
      case ConfigModel.BackgroundStyle.ACHIEVEMENT:
        this.selectedTab.ifPresent(tab -> {
            Identifier textureIdentifier = tab.getDisplay().getBackground().orElse(TextureManager.MISSING_IDENTIFIER);
            context.drawTexture(textureIdentifier, 0, 0, 0.0F, 0.0F, width, height, 16, 16);
        });
        context.fill(0, 0, width, height, -200, MathHelper.floor(0.7 * 255.0F) << 24);
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 300.0F);
        break;
    }

    if (this.selectedTab.isEmpty()) {
      context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, width / 2,
          (height / 2) - this.textRenderer.fontHeight * 2, Colors.WHITE);
      context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, width / 2,
          (height / 2) + this.textRenderer.fontHeight * 2, Colors.WHITE);
    } else {
      this.selectedTab.get().render(context, x, y);
    }
  }

  private int scrollOffset = 0;
  private int contentHeight = 0;

  public void drawAdvancementCriterias(DrawContext context, int x, int y) {
    if (!hasVisibleSidebar() || AdvancementInfoReloaded.getConfig().criteriasWidth() == 0)
      return;

    int paddingTop = AdvancementInfoReloaded.getConfig().headerHeight() + 6;
    final int textWidth = AdvancementInfoReloaded.getConfig().criteriasWidth() - 12 - 6;

    Text title = getSelectedWidget().getAdvancement().name().get();
    Text description = getSelectedWidget().getAdvancement().display().get().getDescription();

    context.fill(width - AdvancementInfoReloaded.getConfig().criteriasWidth(),
        AdvancementInfoReloaded.getConfig().headerHeight(), width,
        height - AdvancementInfoReloaded.getConfig().footerHeight(), MathHelper.floor(0.5F * 255.0F) << 24);

    context.drawTexture(criteriasSeparator, width - AdvancementInfoReloaded.getConfig().criteriasWidth(),
        AdvancementInfoReloaded.getConfig().headerHeight() + 1, 0.0F, 0.0F, 2,
        height - AdvancementInfoReloaded.getConfig().headerHeight() - AdvancementInfoReloaded.getConfig().footerHeight()
            - 2,
        2, 32);

    MatrixStack matrixStack = context.getMatrices();
    matrixStack.push();
    matrixStack.translate(0, -scrollOffset, 20D);

    contentHeight = 6; // 6 are the bottom margin

    // Drawing title
    context.drawTextWrapped(this.textRenderer, title, width - AdvancementInfoReloaded.getConfig().criteriasWidth() + 8,
        paddingTop,
        textWidth, Colors.WHITE);
    paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(title, textWidth).size() + 4;
    contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(title, textWidth).size() + 4;

    // Drawing description
    if (AdvancementInfoReloaded.getConfig().displayDescription() && description != null) {
      context.drawTextWrapped(this.textRenderer, description,
          width - AdvancementInfoReloaded.getConfig().criteriasWidth() + 8,
          paddingTop,
          textWidth, Colors.GRAY);
      paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(description, textWidth).size() + 4;
      contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(description, textWidth).size() + 4;
    }

    context.drawHorizontalLine(width - AdvancementInfoReloaded.getConfig().criteriasWidth() + 8, width - 12, paddingTop,
        Colors.LIGHT_GRAY);
    paddingTop += 5;
    contentHeight += 5;

    // Drawing criterias
    for (AdvancementReloadedStep step : getSelectedWidget().getSteps()) {
      context.drawTextWrapped(this.textRenderer, step.getTitle(),
          width - AdvancementInfoReloaded.getConfig().criteriasWidth() + 8,
          paddingTop, textWidth, step.getColor());
      paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(step.getTitle(), textWidth).size()
          + 4;
      contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(step.getTitle(), textWidth).size()
          + 4;
    }

    matrixStack.pop();

    drawAdvancementCriteriaScrollbar(context, x, y);
  }

  private void drawAdvancementCriteriaScrollbar(DrawContext context, int x, int y) {
    // Drawing scrollbar if needed
    if (!needScrollbarOnCriterias()) {
      return;
    }
  
    int drawingHeight = height - AdvancementInfoReloaded.getConfig().headerHeight()
        - AdvancementInfoReloaded.getConfig().footerHeight();

    // Drawing scrollbar background
    RenderSystem.enableBlend();
    context.drawGuiTexture(SCROLLER_BACKGROUND_TEXTURE, width - 6, AdvancementInfoReloaded.getConfig().headerHeight(),
        6, drawingHeight);

    // Drawing the scrollbar
    int scrollBarHeight = (int) (drawingHeight * drawingHeight / (double) contentHeight);
    int scrollBarY = AdvancementInfoReloaded.getConfig().headerHeight()
        + (int) ((drawingHeight - scrollBarHeight) * (scrollOffset / (double) (contentHeight - (drawingHeight))));

    // draw the scrollbar
    context.drawGuiTexture(SCROLLER_TEXTURE, width - 6, scrollBarY, 6, scrollBarHeight);
    RenderSystem.disableBlend();

  }

  private boolean needScrollbarOnCriterias() {
    return contentHeight > height - AdvancementInfoReloaded.getConfig().headerHeight()
        - AdvancementInfoReloaded.getConfig().footerHeight();
  }

  public boolean hasVisibleSidebar() {
    return getSelectedWidget() != null && AdvancementInfoReloaded.getConfig().displaySidebar();
  }

  public void drawWindow(DrawContext context, int x, int y) {
    RenderSystem.enableBlend();
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, 100.0F);

    if (this.selectedTab.isPresent()) {
      AdvancementDisplay display = this.selectedTab.get().getDisplay();
      Identifier textureIdentifier = display.getBackground().orElse(TextureManager.MISSING_IDENTIFIER);

      // Draw header
      int headerDrawHeight = AdvancementInfoReloaded.getConfig().headerHeight() / 16 + 1;
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < headerDrawHeight; n++) {
          int textureHeight = 16;
          if (n == headerDrawHeight - 1) {
            textureHeight = AdvancementInfoReloaded.getConfig().headerHeight() % 16;
          }

          context.drawTexture(textureIdentifier, 16 * m, 16 * n, 0.0F, 0.0F, 16, textureHeight, 16, 16);
        }
      }
      context.fill(0, 0, width, AdvancementInfoReloaded.getConfig().headerHeight(),
          MathHelper.floor(0.3F * 255.0F) << 24);

      // Draw footer
      int footerDrawHeight = AdvancementInfoReloaded.getConfig().footerHeight() / 16 + 1;
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < footerDrawHeight; n++) {
          int textureHeight = 16;
          if (n == headerDrawHeight - 1) {
            textureHeight = AdvancementInfoReloaded.getConfig().headerHeight() % 16;
          }

          context.drawTexture(textureIdentifier, 16 * m,
              (height - AdvancementInfoReloaded.getConfig().footerHeight()) + 16 * n, 0.0F,
              0.0F, 16, textureHeight, 16, 16);
        }
      }
      context.fill(0, height - AdvancementInfoReloaded.getConfig().footerHeight(), width, height,
          MathHelper.floor(0.3F * 255.0F) << 24);

      // Draw separators
      drawSeparators(context, 0.7F);

      // Draw title on header
      context.drawCenteredTextWithShadow(this.textRenderer, display.getTitle(), width / 2,
          (AdvancementInfoReloaded.getConfig().headerHeight() - 20) / 2 - this.textRenderer.fontHeight / 2, 0xffffff);
    }

    context.getMatrices().pop();

    if (this.tabs.size() > 1) {
      for (AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab.getType() == AdvancementReloadedTabType.BELOW) {
          y = height - AdvancementInfoReloaded.getConfig().footerHeight() - 1;
        }
        advancementTab.setPos(x + 4, y);
        advancementTab.drawBackground(context, advancementTab == this.selectedTab.orElse(null));
        advancementTab.drawIcon(context);
      }
    }

  }

  private void drawSeparators(DrawContext context, float alpha) {
    // Enable blending
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

    // Bind and draw header texture
    RenderSystem.setShaderTexture(0, Screen.INWORLD_HEADER_SEPARATOR_TEXTURE);
    context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 0,
        AdvancementInfoReloaded.getConfig().headerHeight() - 1, 0.0F, 0.0F,
        width, 2, 32, 2);

    // Bind and draw footer texture
    RenderSystem.setShaderTexture(0, Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE);
    context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 0,
        height - AdvancementInfoReloaded.getConfig().footerHeight() - 1, 0.0F,
        0.0F, width, 2, 32, 2);

    // Reset shader color to avoid affecting subsequent draws
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    // Disable blending if no longer needed
    RenderSystem.disableBlend();
  }

  private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
    if (this.selectedTab.isPresent()) {
      context.getMatrices().push();
      context.getMatrices().translate((float) (x), (float) (y), 400.0F);
      RenderSystem.enableDepthTest();
      this.selectedTab.get().drawWidgetTooltip(context, mouseX - x, mouseY - y, x, y);
      RenderSystem.disableDepthTest();
      context.getMatrices().pop();
    }

    if (this.tabs.size() > 1) {
      for (AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab.isClickOnTab(x, y, (double) mouseX, (double) mouseY)) {
          context.drawTooltip(this.textRenderer, advancementTab.getTitle(), mouseX, mouseY);
        }
      }
    }
  }

  public void onRootAdded(PlacedAdvancement root) {
    AdvancementReloadedTab advancementTab = AdvancementReloadedTab.create(this.client, this, this.tabs.size(), root);
    if (advancementTab != null) {
      this.tabs.put(root.getAdvancementEntry(), advancementTab);
    }
  }

  public void onRootRemoved(PlacedAdvancement root) {
  }

  public void onDependentAdded(PlacedAdvancement dependent) {
    AdvancementReloadedTab advancementTab = this.getTab(dependent);
    if (advancementTab != null) {
      advancementTab.addAdvancement(dependent);
    }
  }

  public void onDependentRemoved(PlacedAdvancement dependent) {
  }

  @Override
  public void setProgress(PlacedAdvancement advancement, AdvancementProgress progress) {
    AdvancementReloadedWidget advancementWidget = this.getAdvancementWidget(advancement);
    if (advancementWidget != null) {
      advancementWidget.setProgress(progress);
    }
  }

  @Override
  public void selectTab(@Nullable AdvancementEntry advancement) {
    setSelectedTab((AdvancementReloadedTab) this.tabs.get(advancement));
  }

  public void setSelectedTab(AdvancementReloadedTab tab) {
    this.setSelectedTab(Optional.ofNullable(tab));
  }

  public void setSelectedTab(Optional<AdvancementReloadedTab> tab) {
    this.selectedTab = tab;
    setClickableRegions();
  }

  public void setSelectedWidget(AdvancementReloadedWidget widget) {
    this.selectedWidget = widget;
    this.scrollOffset = 0;
    AdvancementInfoReloadedClient.setCurrentWidget(widget);
    setClickableRegions();
  }

  private void setScrollOffset(int value) {
    if (!needScrollbarOnCriterias())
      return;
    int max = contentHeight - (height - AdvancementInfoReloaded.getConfig().headerHeight()
        - AdvancementInfoReloaded.getConfig().footerHeight());
    this.scrollOffset = MathHelper.clamp(value, 0, max);
  }

  @Nullable
  public AdvancementReloadedWidget getSelectedWidget() {
    return this.selectedWidget;
  }

  public void onClear() {
    this.tabs.clear();
    this.selectedTab = null;
  }

  @Nullable
  public AdvancementReloadedWidget getAdvancementWidget(PlacedAdvancement advancement) {
    AdvancementReloadedTab advancementTab = this.getTab(advancement);
    return advancementTab == null ? null : advancementTab.getWidget(advancement.getAdvancementEntry());
  }

  @Nullable
  private AdvancementReloadedTab getTab(PlacedAdvancement advancement) {
    PlacedAdvancement placedAdvancement = advancement.getRoot();
    return (AdvancementReloadedTab) this.tabs.get(placedAdvancement.getAdvancementEntry());
  }
}
