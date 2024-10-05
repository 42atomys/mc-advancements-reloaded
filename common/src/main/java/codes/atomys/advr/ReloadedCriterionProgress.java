package codes.atomys.advr;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

/**
 * Represents the progress of a specific criterion within an advancement.
 * This class provides methods to access the advancement, progress, and
 * criterion name, as well as to check if the criterion has been obtained
 * and to get the title and color of the criterion progress.
 */
public class ReloadedCriterionProgress {
  private final Advancement advancement;
  private final AdvancementProgress progress;
  private final String criterionName;

  private boolean obtained;

  /**
   * Represents the progress of a specific criterion within an advancement.
   *
   * @param advancement   The advancement to which the criterion belongs.
   * @param progress      The overall progress of the advancement.
   * @param criterionName The name of the criterion being tracked.
   */
  public ReloadedCriterionProgress(final Advancement advancement, final AdvancementProgress progress,
      final String criterionName) {
    this.advancement = advancement;
    this.progress = progress;
    this.criterionName = criterionName;

    this.obtained = progress.getCriterion(criterionName).isDone();
  }

  /**
   * Gets the advancement this criterion belongs to.
   *
   * @return the advancement this criterion belongs to
   */
  public Advancement getAdvancement() {
    return this.advancement;
  }

  /**
   * Gets the progress of this criterion.
   *
   * @return the progress of this criterion
   */
  public AdvancementProgress getProgress() {
    return this.progress;
  }

  /**
   * Gets the title of this criterion progress. If the criterion has no name,
   * this will return an empty component.
   *
   * @return the title of this criterion progress
   */
  public Component getTitle() {
    return Component.nullToEmpty(this.criterionName);
  }

  /**
   * Gets the color of this criterion progress based on whether it has been
   * obtained.
   *
   * @return the color of this criterion progress
   */
  public int getColor() {
    return this.isObtained() ? CommonColors.GREEN : CommonColors.RED;
  }

  /**
   * Whether the criterion has been obtained.
   *
   * @return true if the criterion has been obtained, false otherwise
   */
  public boolean isObtained() {
    return this.obtained;
  }
}
