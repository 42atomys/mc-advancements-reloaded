package codes.atomys.advancementinforeloaded.screen;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
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

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import codes.atomys.advancementinforeloaded.AdvancementInfoReloadedClient;
import codes.atomys.advancementinforeloaded.AdvancementReloadedStep;
import codes.atomys.advancementinforeloaded.ClickableRegion;
import codes.atomys.advancementinforeloaded.PlaceholderConfig;

public class AdvancementReloadedScreen extends Screen implements ClientAdvancementManager.Listener {
  // public static final int WINDOW_WIDTH = 252;
  // public static final int WINDOW_HEIGHT = 140;
  // private static final int PAGE_OFFSET_X = 9;
  // private static final int PAGE_OFFSET_Y = 18;
  // public static final int PAGE_WIDTH = 234;
  // public static final int PAGE_HEIGHT = 113;
  // private static final int TITLE_OFFSET_X = 8;
  // private static final int TITLE_OFFSET_Y = 6;
  // public static final int field_32302 = 16;
  // public static final int field_32303 = 16;
  // public static final int field_32304 = 14;
  // public static final int field_32305 = 7;
  // private static final double field_45431 = 16.0;
  private static final Identifier criteriasSeparator = Identifier
      .of("advancementinforeloaded:textures/gui/inworld_right_separator.png");
  private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/widget/scroller.png");
  private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier
      .ofVanilla("textures/gui/sprites/widget/scroller_background.png");

  private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
  private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
  @Nullable
  private final Screen parent;
  private final ClientAdvancementManager advancementHandler;
  private final Map<AdvancementEntry, AdvancementReloadedTab> tabs = Maps
      .<AdvancementEntry, AdvancementReloadedTab>newLinkedHashMap();
  @Nullable
  private AdvancementReloadedTab selectedTab;
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
    if (this.selectedTab == null && !this.tabs.isEmpty()) {
      AdvancementReloadedTab advancementTab = (AdvancementReloadedTab) this.tabs.values().iterator().next();
      this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
    } else {
      this.advancementHandler
          .selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot().getAdvancementEntry(), true);
    }

    setClickableRegions();
  }

  private void setClickableRegions() {
    this.clickableRegions = new ArrayList<ClickableRegion>();
    clickableRegions.add(
        ClickableRegion.create("advancement_tree", 0, PlaceholderConfig.HEADER_HEIGHT + 1,
            width - (hasSelectedWidget() ? PlaceholderConfig.CRITERIAS_WIDTH : 0),
            height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT));

    if (hasSelectedWidget()) {
      clickableRegions.add(
          ClickableRegion.create("advancement_criterias", width - PlaceholderConfig.CRITERIAS_WIDTH,
              PlaceholderConfig.HEADER_HEIGHT + 1, PlaceholderConfig.CRITERIAS_WIDTH - 6,
              height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT));
      clickableRegions.add(
          ClickableRegion.create("advancement_criterias_scrollbar", width - 6, PlaceholderConfig.HEADER_HEIGHT + 1, 6,
              height - PlaceholderConfig.FOOTER_HEIGHT));
    }

    System.out.println("Clickable regions: " + this.clickableRegions);
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
      int j = PlaceholderConfig.HEADER_HEIGHT;

      for (AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab == this.selectedTab) {
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

    System.out.println("Mouse clicked at " + mouseX + ", " + mouseY + " with button " + button + " on regions "
        + ClickableRegion.foundRegions(clickableRegions, mouseX, mouseY));
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void moveScrollbarTo(double mouseY) {
    // Calculate the viewable height (excluding header and footer)
    int viewableHeight = height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT;

    // Calculate the start and end positions of the scrollbar container
    int scrollbarStart = PlaceholderConfig.HEADER_HEIGHT + 1;
    int scrollbarEnd = height - PlaceholderConfig.FOOTER_HEIGHT - 1;

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
        System.out.println("Mouse released on region " + region.getName());
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
          if (this.selectedTab != null) {
            this.selectedTab.move(deltaX, deltaY);
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
          if (this.selectedTab != null)
            this.selectedTab.move(horizontalAmount * 16.0, verticalAmount * 16.0);
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
    } else if (InputUtil.GLFW_KEY_ESCAPE == keyCode && hasSelectedWidget()) {
      this.selectedWidget = null;
      AdvancementInfoReloadedClient.setCurrentWidget(null);
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
    int j = PlaceholderConfig.HEADER_HEIGHT + 1; // 1 are the separator pixels
    this.drawAdvancementTree(context, mouseX, mouseY, i, j);
    this.drawWindow(context, i, j);
    this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
    this.drawAdvancementCriterias(context, i, j);
  }

  private void drawAdvancementTree(DrawContext context, int mouseX, int mouseY, int x, int y) {
    AdvancementReloadedTab advancementTab = this.selectedTab;
    if (advancementTab == null) {
      context.fill(x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
      int i = x + 9 + 117;
      context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, i, y + 18 + 56 - 9 / 2, Colors.WHITE);
      context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, i, y + 18 + 113 - 9, Colors.WHITE);
    } else {
      advancementTab.render(context, x, y);
    }
  }

  private int scrollOffset = 0;
  private int contentHeight = 0;

  public void drawAdvancementCriterias(DrawContext context, int x, int y) {
    if (!hasSelectedWidget() || PlaceholderConfig.CRITERIAS_WIDTH == 0)
      return;

    int paddingTop = PlaceholderConfig.HEADER_HEIGHT + 6;
    final int textWidth = PlaceholderConfig.CRITERIAS_WIDTH - 12 - 6;

    Text title = getSelectedWidget().getAdvancement().name().get();
    Text description = getSelectedWidget().getAdvancement().display().get().getDescription();

    context.fill(width - PlaceholderConfig.CRITERIAS_WIDTH, PlaceholderConfig.HEADER_HEIGHT, width,
        height - PlaceholderConfig.FOOTER_HEIGHT, MathHelper.floor(0.5F * 255.0F) << 24);

    context.drawTexture(criteriasSeparator, width - PlaceholderConfig.CRITERIAS_WIDTH,
        PlaceholderConfig.HEADER_HEIGHT + 1, 0.0F, 0.0F, 2,
        height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT - 2, 2, 32);

    MatrixStack matrixStack = context.getMatrices();
    matrixStack.push();
    matrixStack.translate(0, -scrollOffset, 20D);

    contentHeight = 6; // 6 are the bottom margin

    // Drawing title
    context.drawTextWrapped(this.textRenderer, title, width - PlaceholderConfig.CRITERIAS_WIDTH + 8, paddingTop,
        textWidth, Colors.WHITE);
    paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(title, textWidth).size() + 4;
    contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(title, textWidth).size() + 4;

    // Drawing description
    if (description != null) {
      context.drawTextWrapped(this.textRenderer, description, width - PlaceholderConfig.CRITERIAS_WIDTH + 8,
          paddingTop,
          textWidth, Colors.GRAY);
      paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(description, textWidth).size() + 4;
      contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(description, textWidth).size() + 4;

      context.drawHorizontalLine(width - PlaceholderConfig.CRITERIAS_WIDTH + 8, width - 12, paddingTop, Colors.LIGHT_GRAY);
      paddingTop += 5;
      contentHeight += 5;
    }

    // Drawing criterias
    for (AdvancementReloadedStep step : getSelectedWidget().getSteps()) {
      context.drawTextWrapped(this.textRenderer, step.getTitle(), width - PlaceholderConfig.CRITERIAS_WIDTH + 8,
          paddingTop, textWidth, step.getColor());
      paddingTop += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(step.getTitle(), textWidth).size()
          + 4;
      contentHeight += (this.textRenderer.fontHeight) * this.textRenderer.wrapLines(step.getTitle(), textWidth).size()
          + 4;
    }

    matrixStack.pop();

    // Drawing scrollbar if needed
    if (needScrollbarOnCriterias()) {
      // Drawing scrollbar background
      context.drawTexture(SCROLLER_BACKGROUND_TEXTURE, width - 6,
          PlaceholderConfig.HEADER_HEIGHT + 1, 0.0F, 0.0F, 6,
          height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT - 2, 6, 32);

      // Drawing the scrollbar
      int scrollBarHeight = (int) ((height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT)
          * (height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT) / (double) contentHeight);
      int scrollBarY = PlaceholderConfig.HEADER_HEIGHT
          + (int) ((height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT - scrollBarHeight)
              * (scrollOffset / (double) (contentHeight
                  - (height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT))));

      int bottomHeight = 2; // bottom pixel height of the scrollbar
      int middleHeight = 32 - bottomHeight;
      int remainingHeight = scrollBarHeight - bottomHeight;

      // Extend the scrollbar based on the content height
      int currentY = scrollBarY;
      while (remainingHeight > 0) {
        int drawHeight = Math.min(middleHeight, remainingHeight);
        context.drawTexture(SCROLLER_TEXTURE, width - 6, currentY, 6, drawHeight, 0.0F, 0.0F, 6, drawHeight, 6, 32);
        currentY += drawHeight;
        remainingHeight -= drawHeight;
      }
      // draw the bottom pixel of the scrollbar
      context.drawTexture(SCROLLER_TEXTURE, width - 6, currentY, 6, bottomHeight, 0.0F,
          32 - bottomHeight, 6, bottomHeight, 6, 32);
    }
  }

  private boolean needScrollbarOnCriterias() {
    return contentHeight > height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT;
  }

  public boolean hasSelectedWidget() {
    return getSelectedWidget() != null;
  }

  public void drawWindow(DrawContext context, int x, int y) {
    RenderSystem.enableBlend();
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, 100.0F);

    if (this.selectedTab != null) {
      AdvancementDisplay display = this.selectedTab.getDisplay();
      Identifier textureIdentifier = display.getBackground().orElse(TextureManager.MISSING_IDENTIFIER);

      // Draw header
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < PlaceholderConfig.HEADER_HEIGHT / 16; n++) {
          context.drawTexture(textureIdentifier, 16 * m, 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
        }
      }
      context.fill(0, 0, width, PlaceholderConfig.HEADER_HEIGHT, MathHelper.floor(0.3F * 255.0F) << 24);

      // Draw footer
      for (int m = 0; m <= width / 16; m++) {
        for (int n = 0; n < (PlaceholderConfig.FOOTER_HEIGHT) / 16; n++) {
          context.drawTexture(textureIdentifier, 16 * m, (height - PlaceholderConfig.FOOTER_HEIGHT) + 16 * n, 0.0F,
              0.0F, 16, 16, 16, 16);
        }
      }
      context.fill(0, height - PlaceholderConfig.FOOTER_HEIGHT, width, height, MathHelper.floor(0.3F * 255.0F) << 24);

      // Draw separators
      drawSeparators(context, 0.7F);

      // Draw title on header
      context.drawCenteredTextWithShadow(this.textRenderer, display.getTitle(), width / 2,
          (PlaceholderConfig.HEADER_HEIGHT - 20) / 2 - this.textRenderer.fontHeight / 2, 0xffffff);
    }

    context.getMatrices().pop();

    if (this.tabs.size() > 1) {
      for (AdvancementReloadedTab advancementTab : this.tabs.values()) {
        if (advancementTab.getType() == AdvancementReloadedTabType.BELOW) {
          y = height - PlaceholderConfig.FOOTER_HEIGHT - 1;
        }
        advancementTab.setPos(x + 4, y);
        advancementTab.drawBackground(context, advancementTab == this.selectedTab);
        advancementTab.drawIcon(context);
      }
    }

    // addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, buttonWidget ->
    // this.close()).width(200).build());
  }

  private void drawSeparators(DrawContext context, float alpha) {
    // Enable blending
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

    // Bind and draw header texture
    RenderSystem.setShaderTexture(0, Screen.INWORLD_HEADER_SEPARATOR_TEXTURE);
    context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 0, PlaceholderConfig.HEADER_HEIGHT - 1, 0.0F, 0.0F,
        width, 2, 32, 2);

    // Bind and draw footer texture
    RenderSystem.setShaderTexture(0, Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE);
    context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 0, height - PlaceholderConfig.FOOTER_HEIGHT - 1, 0.0F,
        0.0F, width, 2, 32, 2);

    // Reset shader color to avoid affecting subsequent draws
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    // Disable blending if no longer needed
    RenderSystem.disableBlend();
  }

  private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
    if (this.selectedTab != null) {
      context.getMatrices().push();
      context.getMatrices().translate((float) (x), (float) (y), 400.0F);
      RenderSystem.enableDepthTest();
      this.selectedTab.drawWidgetTooltip(context, mouseX - x, mouseY - y, x, y);
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
    this.selectedTab = (AdvancementReloadedTab) this.tabs.get(advancement);
    setClickableRegions();
  }

  public void setSelectedTab(AdvancementReloadedTab tab) {
    this.selectedTab = tab;
    setClickableRegions();
  }

  public void setSelectedWidget(AdvancementReloadedWidget widget) {
    System.out.println("Selected widget: " + widget.getAdvancement().name().get().getString());
    this.selectedWidget = widget;
    this.scrollOffset = 0;
    AdvancementInfoReloadedClient.setCurrentWidget(widget);
    setClickableRegions();
  }

  private void setScrollOffset(int value) {
    if (!needScrollbarOnCriterias())
      return;
    int max = contentHeight - (height - PlaceholderConfig.HEADER_HEIGHT - PlaceholderConfig.FOOTER_HEIGHT);
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
