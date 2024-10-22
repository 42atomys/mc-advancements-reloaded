package codes.atomys.advr;

import codes.atomys.advr.config.Configuration;
import codes.atomys.advr.utils.Utils;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;

/**
 * Represents the progress of a specific criterion within an advancement.
 * This class provides methods to access the advancement, progress, and
 * criterion name, as well as to check if the criterion has been obtained
 * and to get the title and color of the criterion progress.
 */
public class ReloadedCriterionProgress {
  private final AdvancementNode advancementNode;
  private final AdvancementProgress progress;
  private final ResourceLocation criterion;

  private boolean obtained;

  /**
   * Represents the progress of a specific criterion within an advancement.
   *
   * @param advancementNode The advancement node to which the criterion
   *                        belongs.
   * @param progress        The overall progress of the advancement.
   * @param criterionName   The name of the criterion being tracked.
   */
  public ReloadedCriterionProgress(final AdvancementNode advancementNode, final AdvancementProgress progress,
      final String criterionName) {
    this.advancementNode = advancementNode;
    this.progress = progress;
    this.criterion = ResourceLocation.parse(criterionName);

    this.obtained = progress.getCriterion(criterionName).isDone();
  }

  /**
   * Gets the advancement node this criterion belongs to.
   *
   * @return the advancement node this criterion belongs to
   */
  public AdvancementNode getAdvancementNode() {
    return this.advancementNode;
  }

  /**
   * Gets the advancement this criterion belongs to.
   *
   * @return the advancement this criterion belongs to
   */
  public Advancement getAdvancement() {
    return this.advancementNode.holder().value();
  }

  /**
   * Gets the resource location of the advancement this criterion belongs to.
   *
   * @return the resource location of the advancement this criterion belongs to
   */
  public ResourceLocation getResourceLocation() {
    return this.advancementNode.holder().id();
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
    return Component.nullToEmpty(this.criterion.toString());
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

  /**
   * Gets the human-readable name of the criterion.
   *
   * @return the human-readable name of the criterion
   */
  public Component getHumanCriterionName() {
    final String translationKey = this.getTranslationKey();

    switch (Configuration.criteriasTranslationMode) {
      case NONE:
        // No translation
        break;
      case ONLY_COMPATIBLE:
        // Only translate when compatible with mod
        return Component.translatableWithFallback(translationKey, this.criterion.getPath());
      case TRY_TO_TRANSLATE:
        // Try to translate all the time (try to translate as possible) but
        // can be a performance hit
        return Component.translatableWithFallback(translationKey, this.retrieveTranslationOnGame().getString());
      default:
        break;
    }

    return Component.literal(this.criterion.getPath());
  }

  /**
   * Gets the translation key for the criterion name. This is used for the
   * advancement sidebar. The key is formatted as
   * "advancements.{root-advancement-id}.{advancement-id}.criteria.{criterion-name>}"
   * <p>
   * This method will return an empty string if the translation key cannot be
   * retrieved.
   * </p>
   *
   * @return the translation key for the criterion name
   */
  public String getTranslationKey() {
    final String criterionName = this.criterion.getPath();

    final List<String> parts = List.of(
        "advancements",
        this.getAdvancementCategory(),
        this.getAdvancementIdentifier(),
        "criteria",
        criterionName);

    return parts.stream()
        .filter(part -> part != null && !part.isEmpty()) // Remove null or empty elements
        .collect(Collectors.joining("."));
  }

  /**
   * Retrieves the identifier of the advancement for the current criterion.
   * This is used to get the translation key for the criterion name in the
   * sidebar.
   *
   * @return the identifier of the advancement
   */
  private String getAdvancementIdentifier() {

    final ResourceLocation locationId = this.getResourceLocation();
    final String path = locationId.getPath();

    final String[] pathSegments = path.split("/");
    if (pathSegments.length == 0) {
      Utils.LOGGER.warn("advancement cannot be retrieved from the path: " + path);
      return "";
    }

    return pathSegments[pathSegments.length - 1];
  }

  /**
   * Retrieves the category of the advancement for the current criterion.
   * The category are the root advancement of the advancement if there is one.
   * This is used to get the translation key for the criterion name in the
   * sidebar.
   *
   * @return the category of the advancement
   */
  private String getAdvancementCategory() {
    final ResourceLocation locationId = this.getAdvancementNode().root().holder().id();
    final String path = locationId.getPath();

    if (!path.contains("/")) {
      return "";
    }

    final String[] pathSegments = path.split("/");
    if (pathSegments.length >= 2) {
      return pathSegments[pathSegments.length - 2];
    }

    return "";
  }

  private Component retrieveTranslationOnGame() {
    final String criterionNamespace = this.criterion.getNamespace();
    final String criteria = this.criterion.getPath();

    // Try to translate the name by finding the item in the namespace and the
    // default namespace (if not the same as the namespace).
    final List<String> namespaces = Lists.newArrayList(criterionNamespace);
    if (!criterionNamespace.equals("minecraft")) {
      namespaces.add("minecraft");
    }

    // CHECKSTYLE:OFF
    final String[] keyTypes = { "biome", "block", "color", "container", "effect", "enchantment", "entity", "instrument",
        "item", "jukebox_song", "painting", "stat" };
    // CHECKSTYLE:ON

    for (final String namespace : namespaces) {
      for (final String keyType : keyTypes) {
        // Special case for paintings since they have a different translation key
        final String translationKey = keyType + "." + namespace + "." + criteria
            + (keyType == "painting" ? ".title" : "");
        final Component translation = Component.translatable(translationKey);
        if (!translation.getString().equals(translationKey)) {
          translation.getStyle().withItalic(true).applyTo(translation.getStyle());
          return translation;
        }
      }
    }

    Utils.LOGGER.warn(
        "Unable to translate {} to a more meaningful name, adding as is, performance may be degraded. You can add your own translation for this criterion by adding the translation key: `{}`.",
        criteria, this.getTranslationKey());
    return Component.literal(criteria);
  }
}
