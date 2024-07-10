package codes.atomys.advancementinforeloaded.screen;

import com.google.common.collect.Maps;

import codes.atomys.advancementinforeloaded.AdvancementInfoReloaded;
import codes.atomys.advancementinforeloaded.AdvancementInfoReloadedClient;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementReloadedTab {
  private static final Identifier SELECTED_IDENTIFIER = Identifier.ofVanilla("advancements/task_frame_obtained");
  private static final Identifier UNSELECTED_IDENTIFIER = Identifier.ofVanilla("advancements/task_frame_unobtained");

  private final MinecraftClient client;
  private final AdvancementReloadedScreen screen;
  private final AdvancementReloadedTabType type;
  private final int index;
  private final PlacedAdvancement root;
  private final AdvancementReloadedDisplay display;
  private final ItemStack icon;
  private final Text title;
  private final AdvancementReloadedWidget rootWidget;
  private final Map<AdvancementEntry, AdvancementReloadedWidget> widgets = Maps.newLinkedHashMap();
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

  public AdvancementReloadedTab(MinecraftClient client, AdvancementReloadedScreen screen,
      AdvancementReloadedTabType type, int index, PlacedAdvancement root, AdvancementReloadedDisplay display) {
    this.client = client;
    this.screen = screen;
    this.type = type;
    this.index = index;
    this.root = root;
    this.display = display;
    this.icon = display.getIcon();
    this.title = display.getTitle();
    this.rootWidget = new AdvancementReloadedWidget(this, client, root, display);
    this.addWidget(this.rootWidget, root.getAdvancementEntry());
  }

  public AdvancementReloadedTabType getType() {
    return this.type;
  }

  public int getIndex() {
    return this.index;
  }

  public PlacedAdvancement getRoot() {
    return this.root;
  }

  public Text getTitle() {
    return this.title;
  }

  public AdvancementReloadedDisplay getDisplay() {
    return this.display;
  }

  public void setPos(int x, int y) {
    this.tab_x = x + this.type.getTabX(this.index);
    this.tab_y = y + this.type.getTabY(this.index);
  }

  public void drawBackground(DrawContext context, boolean selected) {
    Identifier texture = selected ? SELECTED_IDENTIFIER : UNSELECTED_IDENTIFIER;

    context.getMatrices().push();
    context.getMatrices().translate(0.0D, 0.0D, 220.0D);
    context.drawGuiTexture(texture, this.tab_x, this.tab_y, this.type.getWidth(), this.type.getHeight());
    context.getMatrices().pop();

  }

  public void drawIcon(DrawContext context) {
    context.getMatrices().push();
    context.getMatrices().translate(0.0D, 0.0D, 221.0D);
    context.drawItemWithoutEntity(this.icon, this.tab_x + this.type.getTopMargin(),
        this.tab_y + this.type.getLeftMargin());
    context.getMatrices().pop();
  }

  public int getWidth() {
    if (screen.getSelectedWidget() != null)
      return screen.width - AdvancementInfoReloaded.getConfig().criteriasWidth();

    return screen.width;
  }

  public int getHeight() {
    // 2 are the separator lines
    return screen.height - AdvancementInfoReloaded.getConfig().headerHeight()
        - AdvancementInfoReloaded.getConfig().footerHeight() - 2;
  }

  public void refresh() {
    this.initialized = false;
  }

  public void render(DrawContext context, int x, int y) {
    if (!this.initialized) {
      this.originX = (double) ((getWidth() / 2) - (this.maxPanX + this.minPanX) / 2);
      this.originY = (double) ((screen.height / 2 - AdvancementInfoReloaded.getConfig().headerHeight() - 1)
          - (this.maxPanY + this.minPanY) / 2);
      this.initialized = true;
    }

    context.enableScissor(x, y, x + getWidth(), y + getHeight());
    context.getMatrices().push();
    context.getMatrices().translate((float) x, (float) y, 0.0F);
    int i = MathHelper.floor(this.originX);
    int j = MathHelper.floor(this.originY);

    this.rootWidget.renderLines(context, i, j, true);
    this.rootWidget.renderLines(context, i, j, false);
    this.rootWidget.renderWidgets(context, i, j);
    context.getMatrices().pop();
    context.disableScissor();
  }

  public void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
    context.fill(0, 0, getWidth(), getHeight(), -200, MathHelper.floor(this.alpha * 255.0F) << 24);
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, 300.0F);
    boolean rendered = false;
    int i = MathHelper.floor(this.originX);
    int j = MathHelper.floor(this.originY);
    if (mouseX > 0 && mouseX < getWidth() && mouseY > 0 && mouseY < getHeight()) {
      Iterator<AdvancementReloadedWidget> widgets = this.widgets.values().iterator();

      while (widgets.hasNext()) {
        AdvancementReloadedWidget advancementWidget = (AdvancementReloadedWidget) widgets.next();
        if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
          rendered = true;
          advancementWidget.drawTooltip(context, i, j, this.alpha, x, y);
          break;
        }
      }
    }

    context.getMatrices().pop();
    if (rendered) {
      this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
    } else {
      this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
    }
  }

  public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
    return mouseX > (double) this.tab_x && mouseX < (double) (this.tab_x + this.type.getWidth())
        && mouseY > (double) this.tab_y && mouseY < (double) (this.tab_y + this.type.getHeight());
  }

  @Nullable
  public AdvancementReloadedWidget clickOnWidget(int screenX, int screenY, double mouseX, double mouseY) {
    int flooredOriginX = MathHelper.floor(this.originX);
    int flooredOriginY = MathHelper.floor(this.originY + AdvancementInfoReloaded.getConfig().headerHeight() - 1);

    // Prevent click outside of the advancement tree
    if (mouseX < screenX || mouseX > getWidth() || mouseY < screenY
        || mouseY > screen.height - AdvancementInfoReloaded.getConfig().footerHeight() - 1)
      return null;

    Iterator<AdvancementReloadedWidget> widgets = this.widgets.values().iterator();

    while (widgets.hasNext()) {
      AdvancementReloadedWidget advancementWidget = (AdvancementReloadedWidget) widgets.next();
      if (advancementWidget.isMouseOn(flooredOriginX, flooredOriginY, mouseX, mouseY))
        return advancementWidget;
    }

    return null;
  }

  @Nullable
  public static AdvancementReloadedTab create(MinecraftClient client, AdvancementReloadedScreen screen, int index,
      PlacedAdvancement root) {
    Optional<AdvancementDisplay> optional = root.getAdvancement().display();
    if (optional.isEmpty()) {
      return null;
    } else {
      AdvancementReloadedTabType[] types = AdvancementReloadedTabType.values();
      int numberOfTypes = types.length;

      for (int i = 0; i < numberOfTypes; ++i) {
        AdvancementReloadedTabType advancementTabType = types[i];
        if (index < advancementTabType.getTabLimit()) {
          return new AdvancementReloadedTab(client, screen, advancementTabType, index, root,
              AdvancementReloadedDisplay.cast(optional.get()));
        }

        index -= advancementTabType.getTabLimit();
      }

      return null;
    }
  }

  public void move(double offsetX, double offsetY) {
    int maxWidth = getWidth();
    if (this.maxPanX - this.minPanX > maxWidth - 8) {
      this.originX = MathHelper.clamp(this.originX + offsetX, (double) (-(this.maxPanX - maxWidth + 8)), 8D);
    }

    int maxHeight = getHeight();
    if (this.maxPanY - this.minPanY > maxHeight - 16) {
      this.originY = MathHelper.clamp(this.originY + offsetY, (double) (-(this.maxPanY - maxHeight + 16)), 16D);
    }

  }

  public void addAdvancement(PlacedAdvancement advancement) {
    Optional<AdvancementDisplay> optional = advancement.getAdvancement().display();
    if (!optional.isEmpty()) {
      AdvancementReloadedWidget advancementWidget = new AdvancementReloadedWidget(this, this.client, advancement,
          AdvancementReloadedDisplay.cast(optional.get()));
      this.addWidget(advancementWidget, advancement.getAdvancementEntry());
    }
  }

  private void addWidget(AdvancementReloadedWidget widget, AdvancementEntry advancement) {
    this.widgets.put(advancement, widget);
    int i = widget.getX();
    int j = i + 28;
    int k = widget.getY();
    int l = k + 27;
    this.minPanX = Math.min(this.minPanX, i);
    this.maxPanX = Math.max(this.maxPanX, j);
    this.minPanY = Math.min(this.minPanY, k);
    this.maxPanY = Math.max(this.maxPanY, l);
    Iterator<AdvancementReloadedWidget> var7 = this.widgets.values().iterator();

    while (var7.hasNext()) {
      AdvancementReloadedWidget advancementWidget = (AdvancementReloadedWidget) var7.next();
      advancementWidget.addToTree();
    }

  }

  @Nullable
  public AdvancementReloadedWidget getWidget(AdvancementEntry advancement) {
    return (AdvancementReloadedWidget) this.widgets.get(advancement);
  }

  public AdvancementReloadedScreen getScreen() {
    return this.screen;
  }
}
