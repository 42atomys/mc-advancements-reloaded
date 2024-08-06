package codes.atomys.advancementinforeloaded.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import codes.atomys.advancementinforeloaded.AdvancementInfoReloaded;
import codes.atomys.advancementinforeloaded.AdvancementReloadedStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementReloadedWidget {
  private static final Identifier TITLE_BOX_TEXTURE = Identifier.ofVanilla("advancements/title_box");
  // private static final int field_32286 = 26;
  // private static final int field_32287 = 0;
  // private static final int field_32288 = 200;
  // private static final int field_32289 = 26;
  // private static final int ICON_OFFSET_X = 8;
  // private static final int ICON_OFFSET_Y = 5;
  // private static final int ICON_SIZE = 26;
  // private static final int field_32293 = 3;
  // private static final int field_32294 = 5;
  // private static final int TITLE_OFFSET_X = 32;
  // private static final int TITLE_OFFSET_Y = 9;
  // private static final int TITLE_MAX_WIDTH = 163;
  private static final int[] SPLIT_OFFSET_CANDIDATES = new int[] { 0, 10, -10, 25, -25 };
  private final AdvancementReloadedTab tab;
  private final PlacedAdvancement advancement;
  private final AdvancementDisplay display;
  private final OrderedText title;
  private final int width;
  private final List<OrderedText> description;
  private final MinecraftClient client;
  @Nullable
  private AdvancementReloadedWidget parent;
  private final List<AdvancementReloadedWidget> children = Lists.newArrayList();
  @Nullable
  private AdvancementProgress progress;
  private List<AdvancementReloadedStep> steps;
  private final int x;
  private final int y;

  public AdvancementReloadedWidget(AdvancementReloadedTab tab, MinecraftClient client, PlacedAdvancement advancement,
      AdvancementDisplay display) {
    this.tab = tab;
    this.advancement = advancement;
    this.display = display;
    this.client = client;
    this.title = Language.getInstance().reorder(client.textRenderer.trimToWidth(display.getTitle(), 163));
    this.x = MathHelper.floor(display.getX() * 28.0F);
    this.y = MathHelper.floor(display.getY() * 27.0F);
    int i = this.getProgressWidth();
    int j = 29 + client.textRenderer.getWidth(this.title) + i;
    this.description = Language.getInstance()
        .reorder(this.wrapDescription(Texts.setStyleIfAbsent(display.getDescription().copy(),
            Style.EMPTY.withColor(display.getFrame().getTitleFormat())), j));

    for (OrderedText orderedText : this.description) {
      j = Math.max(j, client.textRenderer.getWidth(orderedText));
    }

    if (progress != null) {
      this.setSteps(progress);
    }

    this.width = j + 3 + 5;
  }

  private int getProgressWidth() {
    int i = this.advancement.getAdvancement().requirements().getLength();
    if (i <= 1)
      return 0;
    int j = 8;
    MutableText mutableText = Text.translatable("advancements.progress",
        new Object[] { Integer.valueOf(i), Integer.valueOf(i) });
    return this.client.textRenderer.getWidth((StringVisitable) mutableText) + j;
  }

  private static float getMaxWidth(TextHandler textHandler, List<StringVisitable> lines) {
    Objects.requireNonNull(textHandler);
    return (float) lines.stream().mapToDouble(textHandler::getWidth).max().orElse(0.0D);
  }

  private List<StringVisitable> wrapDescription(Text text, int width) {
    TextHandler textHandler = this.client.textRenderer.getTextHandler();
    List<StringVisitable> list = null;
    float f = Float.MAX_VALUE;
    int[] var6 = SPLIT_OFFSET_CANDIDATES;
    int var7 = var6.length;

    for (int var8 = 0; var8 < var7; ++var8) {
      int i = var6[var8];
      List<StringVisitable> list2 = textHandler.wrapLines(text, width - i, Style.EMPTY);
      float g = Math.abs(getMaxWidth(textHandler, list2) - (float) width);
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

  @Nullable
  private AdvancementReloadedWidget getParent(PlacedAdvancement advancement) {
    do {
      advancement = advancement.getParent();
    } while (advancement != null && advancement.getAdvancement().display().isEmpty());

    if (advancement != null && !advancement.getAdvancement().display().isEmpty()) {
      return this.tab.getWidget(advancement.getAdvancementEntry());
    } else {
      return null;
    }
  }
  
  public Advancement getAdvancement() {
    return this.advancement.getAdvancement();
  }

  public AdvancementProgress getProgress() {
    return this.progress;
  }

  public List<AdvancementReloadedStep> getSteps() {
    return this.steps;
  }

  public void renderLines(DrawContext context, int x, int y, boolean border) {
    if (this.parent != null) {
      int i = x + this.parent.x + 13;
      int j = x + this.parent.x + 26 + 4;
      int k = y + this.parent.y + 13;
      int l = x + this.x + 13;
      int m = y + this.y + 13;
      int n = border ? -16777216 : -1;
      if (border) {
        context.drawHorizontalLine(j, i, k - 1, n);
        context.drawHorizontalLine(j + 1, i, k, n);
        context.drawHorizontalLine(j, i, k + 1, n);
        context.drawHorizontalLine(l, j - 1, m - 1, n);
        context.drawHorizontalLine(l, j - 1, m, n);
        context.drawHorizontalLine(l, j - 1, m + 1, n);
        context.drawVerticalLine(j - 1, m, k, n);
        context.drawVerticalLine(j + 1, m, k, n);
      } else {
        context.drawHorizontalLine(j, i, k, n);
        context.drawHorizontalLine(l, j, m, n);
        context.drawVerticalLine(j, m, k, n);
      }
    }
    for (AdvancementReloadedWidget advancementWidget : this.children)
      advancementWidget.renderLines(context, x, y, border);
  }

  public void renderWidgets(DrawContext context, int x, int y) {
    if (!this.display.isHidden() || (this.progress != null && this.progress.isDone())) {
      AdvancementObtainedStatus advancementObtainedStatus;
      float f = (this.progress == null) ? 0.0F : this.progress.getProgressBarPercentage();
      if (f >= 1.0F) {
        advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
      } else {
        advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
      }
      context.drawGuiTexture(advancementObtainedStatus.getFrameTexture(this.display.getFrame()), x + this.x + 3,
          y + this.y, 26, 26);
      context.drawItemWithoutEntity(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
    }
    for (AdvancementReloadedWidget advancementWidget : this.children)
      advancementWidget.renderWidgets(context, x, y);
  }

  public int getWidth() {
    return this.width;
  }

  public void setProgress(AdvancementProgress progress) {
    this.progress = progress;
    this.setSteps(progress);
  }

  public void setSteps(AdvancementProgress progress) {
    List<AdvancementReloadedStep> steps = new ArrayList<>();
    Iterable<String> unobtainedIterable = progress.getUnobtainedCriteria();
    Iterable<String> obtainedIterable = progress.getObtainedCriteria();

    if (AdvancementInfoReloaded.getConfig().alphabeticOrder()) {
        List<String> unobtainedList = new ArrayList<>();
        List<String> obtainedList = new ArrayList<>();
        unobtainedIterable.forEach(unobtainedList::add);
        obtainedIterable.forEach(obtainedList::add);
        unobtainedList.sort(String::compareToIgnoreCase);
        obtainedList.sort(String::compareToIgnoreCase);
        unobtainedIterable = unobtainedList;
        obtainedIterable = obtainedList;
    }

    unobtainedIterable.forEach((criterion) -> {
        steps.add(new AdvancementReloadedStep(this.advancement.getAdvancement(), progress, criterion));
    });
    obtainedIterable.forEach((criterion) -> {
        steps.add(new AdvancementReloadedStep(this.advancement.getAdvancement(), progress, criterion));
    });
    

    this.steps = steps;
  }

  public void addChild(AdvancementReloadedWidget widget) {
    this.children.add(widget);
  }

  public void drawTooltip(DrawContext context, int originX, int originY, float alpha, int x, int y) {
    AdvancementObtainedStatus advancementObtainedStatus, advancementObtainedStatus2, advancementObtainedStatus3;
    int m;
    boolean bl = (x + originX + this.x + this.width + 26 >= (this.tab.getScreen()).width);
    Text text = (this.progress == null) ? null : this.progress.getProgressBarFraction();
    int i = (text == null) ? 0 : this.client.textRenderer.getWidth((StringVisitable) text);
    Objects.requireNonNull(this.client.textRenderer);
    boolean bl2 = (113 - originY - this.y - 26 <= 6 + this.description.size() * 9);
    float f = (this.progress == null) ? 0.0F : this.progress.getProgressBarPercentage();
    int j = MathHelper.floor(f * this.width);
    if (f >= 1.0F) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
      advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
      advancementObtainedStatus3 = AdvancementObtainedStatus.OBTAINED;
    } else if (j < 2) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
      advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
      advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
    } else if (j > this.width - 2) {
      j = this.width / 2;
      advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
      advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
      advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
    } else {
      advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
      advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
      advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
    }
    int k = this.width - j;
    RenderSystem.enableBlend();
    int l = originY + this.y;
    if (bl) {
      m = originX + this.x - this.width + 26 + 6;
    } else {
      m = originX + this.x;
    }
    Objects.requireNonNull(this.client.textRenderer);
    int n = 32 + this.description.size() * 9;
    if (!this.description.isEmpty())
      if (bl2) {
        context.drawGuiTexture(TITLE_BOX_TEXTURE, m, l + 26 - n, this.width, n);
      } else {
        context.drawGuiTexture(TITLE_BOX_TEXTURE, m, l, this.width, n);
      }
    context.drawGuiTexture(advancementObtainedStatus.getBoxTexture(), 200, 26, 0, 0, m, l, j, 26);
    context.drawGuiTexture(advancementObtainedStatus2.getBoxTexture(), 200, 26, 200 - k, 0, m + j, l, k, 26);
    context.drawGuiTexture(advancementObtainedStatus3.getFrameTexture(this.display.getFrame()), originX + this.x + 3,
        originY + this.y, 26, 26);
    if (bl) {
      context.drawTextWithShadow(this.client.textRenderer, this.title, m + 5, originY + this.y + 9, -1);
      if (text != null)
        context.drawTextWithShadow(this.client.textRenderer, text, originX + this.x - i, originY + this.y + 9, -1);
    } else {
      context.drawTextWithShadow(this.client.textRenderer, this.title, originX + this.x + 32, originY + this.y + 9, -1);
      if (text != null)
        context.drawTextWithShadow(this.client.textRenderer, text, originX + this.x + this.width - i - 5,
            originY + this.y + 9, -1);
    }
    if (bl2) {
      for (int o = 0; o < this.description.size(); o++) {
        Objects.requireNonNull(this.client.textRenderer);
        context.drawText(this.client.textRenderer, this.description.get(o), m + 5, l + 26 - n + 7 + o * 9, -5592406,
            false);
      }
    } else {
      for (int o = 0; o < this.description.size(); o++) {
        Objects.requireNonNull(this.client.textRenderer);
        context.drawText(this.client.textRenderer, this.description.get(o), m + 5, originY + this.y + 9 + 17 + o * 9,
            -5592406, false);
      }
    }
    context.drawItemWithoutEntity(this.display.getIcon(), originX + this.x + 8, originY + this.y + 5);
  }

  public boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
    if (this.display.isHidden() && (this.progress == null || !this.progress.isDone()))
      return false;
  
    return this.isMouseOn(originX, originY, mouseX, mouseY);
  }

  public boolean isMouseOn(int originX, int originY, double mouseX, double mouseY) {
    return (double) (originX + this.x) < mouseX && mouseX < (double) (originX + this.x + 26)
        && (double) (originY + this.y) < mouseY && mouseY < (double) (originY + this.y + 26);
  }

  public void addToTree() {
    if (this.parent == null && this.advancement.getParent() != null) {
      this.parent = this.getParent(this.advancement);
      if (this.parent != null) {
        this.parent.addChild(this);
      }
    }

  }

  public int getY() {
    return this.y;
  }

  public int getX() {
    return this.x;
  }
}
