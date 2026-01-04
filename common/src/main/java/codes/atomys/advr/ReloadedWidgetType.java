package codes.atomys.advr;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;

/**
 * Represents the types of widgets used in the advancements system, categorized
 * as either
 * "OBTAINED" or "UNOBTAINED". Each type contains a set of sprites for different
 * advancement
 * frames (box, task, challenge, goal) and their dimmed variants.
 *
 * @see Identifier
 * @see AdvancementType
 */
public enum ReloadedWidgetType {
  OBTAINED(
      Identifier.withDefaultNamespace("advancements/box_obtained"),
      Identifier.withDefaultNamespace("advancements/task_frame_obtained"),
      Identifier.withDefaultNamespace("advancements/challenge_frame_obtained"),
      Identifier.withDefaultNamespace("advancements/goal_frame_obtained"),
      Identifier.parse("advancements_reloaded:box_obtained_dimmed"),
      Identifier.parse("advancements_reloaded:task_frame_obtained_dimmed"),
      Identifier.parse("advancements_reloaded:challenge_frame_obtained_dimmed"),
      Identifier.parse("advancements_reloaded:goal_frame_obtained_dimmed")),
  UNOBTAINED(
      Identifier.withDefaultNamespace("advancements/box_unobtained"),
      Identifier.withDefaultNamespace("advancements/task_frame_unobtained"),
      Identifier.withDefaultNamespace("advancements/challenge_frame_unobtained"),
      Identifier.withDefaultNamespace("advancements/goal_frame_unobtained"),
      Identifier.parse("advancements_reloaded:box_unobtained_dimmed"),
      Identifier.parse("advancements_reloaded:task_frame_unobtained_dimmed"),
      Identifier.parse("advancements_reloaded:challenge_frame_unobtained_dimmed"),
      Identifier.parse("advancements_reloaded:goal_frame_unobtained_dimmed"));

  private final Identifier boxSprite;
  private final Identifier taskFrameSprite;
  private final Identifier challengeFrameSprite;
  private final Identifier goalFrameSprite;

  private final Identifier boxSpriteDimmed;
  private final Identifier taskFrameSpriteDimmed;
  private final Identifier challengeFrameSpriteDimmed;
  private final Identifier goalFrameSpriteDimmed;

  /**
   * Constructs a new {@link ReloadedWidgetType} with the specified sprite
   * locations.
   *
   * @param box             The box sprite location.
   * @param task            The task frame sprite location.
   * @param challenge       The challenge frame sprite location.
   * @param goal            The goal frame sprite location.
   * @param boxDimmed       The dimmed box sprite location.
   * @param taskDimmed      The dimmed task frame sprite location.
   * @param challengeDimmed The dimmed challenge frame sprite location.
   * @param goalDimmed      The dimmed goal frame sprite location.
   */
  ReloadedWidgetType(
      final Identifier box,
      final Identifier task,
      final Identifier challenge,
      final Identifier goal,
      final Identifier boxDimmed,
      final Identifier taskDimmed,
      final Identifier challengeDimmed,
      final Identifier goalDimmed) {
    this.boxSprite = box;
    this.taskFrameSprite = task;
    this.challengeFrameSprite = challenge;
    this.goalFrameSprite = goal;
    this.boxSpriteDimmed = boxDimmed;
    this.taskFrameSpriteDimmed = taskDimmed;
    this.challengeFrameSpriteDimmed = challengeDimmed;
    this.goalFrameSpriteDimmed = goalDimmed;
  }

  /**
   * Retrieves the box sprite for the given dimmed state.
   *
   * @param dimmed Whether the box sprite should be dimmed.
   * @return The {@link Identifier} of the box sprite corresponding to the
   *         specified
   *         dimmed state.
   */
  public Identifier boxSprite(final boolean dimmed) {
    return dimmed ? this.boxSpriteDimmed : this.boxSprite;
  }

  /**
   * Retrieves the appropriate frame sprite for the given advancement type and
   * dimmed state.
   *
   * @param type   The type of the advancement (e.g., TASK, CHALLENGE, GOAL).
   * @param dimmed Whether the frame sprite should be dimmed.
   * @return The {@link Identifier} of the frame sprite corresponding to the
   *         specified
   *         advancement type and dimmed state.
   */
  public Identifier frameSprite(final AdvancementType type, final boolean dimmed) {
    return switch (type) {
      case TASK -> dimmed ? this.taskFrameSpriteDimmed : this.taskFrameSprite;
      case CHALLENGE -> dimmed ? this.challengeFrameSpriteDimmed : this.challengeFrameSprite;
      case GOAL -> dimmed ? this.goalFrameSpriteDimmed : this.goalFrameSprite;
    };
  }
}
