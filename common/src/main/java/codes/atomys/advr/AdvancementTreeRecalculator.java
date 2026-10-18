package codes.atomys.advr;

import codes.atomys.advr.utils.Utils;
import java.util.Collection;
import java.util.Set;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;

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

  /**
   * Recalculates the position of the given roots of the tree, and of their
   * children, using the current configuration. Roots that are no longer in the
   * tree or that are invisible are skipped.
   *
   * @param tree    the advancement tree to reposition
   * @param rootIds the ids of the roots to reposition
   */
  public static void recalculate(final AdvancementTree tree, final Collection<Identifier> rootIds) {
    for (final Identifier rootId : rootIds) {
      final AdvancementNode root = tree.get(rootId);
      if (root != null && root.advancement().display().isPresent()) {
        AdvancementTreePositioning.run(root);
      }
    }
  }

  /**
   * Adds to {@code rootIds} the root id of every given advancement whose
   * subtree contains a visible advancement, meaning that adding or removing it
   * changes the layout of its tree. Ids unknown to the tree are ignored.
   *
   * @param tree           the advancement tree holding the advancements
   * @param advancementIds the ids of the added or removed advancements
   * @param rootIds        the set receiving the ids of the affected roots
   */
  public static void collectAffectedRoots(final AdvancementTree tree,
      final Iterable<Identifier> advancementIds, final Set<Identifier> rootIds) {
    for (final Identifier advancementId : advancementIds) {
      final AdvancementNode node = tree.get(advancementId);
      if (node != null && hasVisibleNode(node)) {
        rootIds.add(node.root().holder().id());
      }
    }
  }

  /**
   * Checks whether the given node or one of its descendants is visible.
   *
   * @param node the node to check
   * @return true if the subtree of the node contains a visible advancement
   */
  private static boolean hasVisibleNode(final AdvancementNode node) {
    if (node.advancement().display().isPresent()) {
      return true;
    }

    for (final AdvancementNode child : node.children()) {
      if (hasVisibleNode(child)) {
        return true;
      }
    }

    return false;
  }
}
