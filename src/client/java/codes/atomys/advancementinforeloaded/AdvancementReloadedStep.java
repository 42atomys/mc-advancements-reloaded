package codes.atomys.advancementinforeloaded;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class AdvancementReloadedStep {
  private final Advancement advancement;
  private final AdvancementProgress progress;
  private final String criterionName;

  private boolean obtained;

  public AdvancementReloadedStep(Advancement advancement, AdvancementProgress progress, String criterionName) {
    this.advancement = advancement;
    this.progress = progress;
    this.criterionName = criterionName;

    this.obtained = progress.getCriterionProgress(criterionName).isObtained();
  }

  public Advancement getAdvancement() {
    return this.advancement;
  }

  public AdvancementProgress getProgress() {
    return this.progress;
  }

  public Text getTitle() {
    return Text.of(this.criterionName);
  }

  public int getColor() {
    return isObtained() ? Colors.GREEN : Colors.RED;
  }

  public boolean isObtained() {
    return this.obtained;
  }
}
