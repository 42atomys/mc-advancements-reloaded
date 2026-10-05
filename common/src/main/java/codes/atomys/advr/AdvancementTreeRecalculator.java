package codes.atomys.advr;

import codes.atomys.advr.utils.Utils;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;

/**
 * Utility class for recalculating advancement tree positions.
 * <p>
 * This is useful when configuration changes (e.g., advancement order mode)
 * and all trees need to be repositioned without reloading advancements.
 * </p>
 */
public final class AdvancementTreeRecalculator {

  /**
   * Private constructor to prevent instantiation of the utility class.
   * Throws {@link UnsupportedOperationException} if called.
   */
  private AdvancementTreeRecalculator() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Recalculates all advancement tree positions using the current configuration.
   * This should be called when advancement ordering settings change.
   */
  public static void recalculateAll() {
    final Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null || minecraft.player.connection == null) {
      return;
    }

    final ClientAdvancements advancements = minecraft.player.connection.getAdvancements();
    if (advancements == null) {
      return;
    }

    Utils.LOGGER.info("Recalculating all advancement tree positions due to configuration change");

    recalculate(advancements.tree());

    Utils.LOGGER.info("Advancement tree positions recalculated");
  }

  /**
   * Recalculates the position of every visible root of the given tree, and of
   * their children, using the current configuration.
   *
   * @param tree the advancement tree to reposition
   */
  public static void recalculate(final AdvancementTree tree) {
    for (final AdvancementNode root : tree.roots()) {
      if (root.advancement().display().isPresent()) {
        AdvancementTreePositioning.run(root);
      }
    }
  }
}
