package codes.atomys.advr;

import codes.atomys.advr.config.Configuration;
import codes.atomys.advr.utils.Utils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.advancements.AdvancementNode;
import org.jetbrains.annotations.Nullable;

/**
 * Custom tree positioning algorithm for advancements that supports configurable
 * ordering modes (NONE, ALPHABETIC, CONFIGURED_ORDER).
 * <p>
 * This class replaces vanilla's TreeNodePosition to provide:
 * - Deterministic advancement row ordering (fixes issue #94)
 * - Extensibility for future positioning strategies
 * - Full control over tree layout algorithm
 * </p>
 * <p>
 * Based on vanilla TreeNodePosition but with sorting capabilities integrated
 * into the tree construction phase.
 * </p>
 */
public class AdvancementTreePositioning {

  private final AdvancementNode node;
  @Nullable
  private final AdvancementTreePositioning parent;
  @Nullable
  private final AdvancementTreePositioning previousSibling;
  private final int childIndex;
  private final List<AdvancementTreePositioning> children = Lists.newArrayList();
  private AdvancementTreePositioning ancestor;
  @Nullable
  private AdvancementTreePositioning thread;
  private int x;
  private float y;
  private float mod;
  private float change;
  private float shift;

  /**
   * Constructs a tree node position for the given advancement node.
   * Children are sorted according to the configured ordering mode before processing.
   *
   * @param node the advancement node to position
   * @param parent the parent tree position, or null if this is the root
   * @param previousSibling the previous sibling tree position, or null if this is the first child
   * @param childIndex the index of this child among its siblings (1-based)
   * @param x the x-coordinate (depth in tree)
   */
  public AdvancementTreePositioning(final AdvancementNode node,
      @Nullable final AdvancementTreePositioning parent,
      @Nullable final AdvancementTreePositioning previousSibling,
      final int childIndex, final int x) {
    if (node.advancement().display().isEmpty()) {
      throw new IllegalArgumentException("Can't position an invisible advancement!");
    }

    this.node = node;
    this.parent = parent;
    this.previousSibling = previousSibling;
    this.childIndex = childIndex;
    this.ancestor = this;
    this.x = x;
    this.y = -1.0F;

    // Sort children according to configuration before building tree
    final List<AdvancementNode> sortedChildren = this.getSortedChildren(node);

    // Debug logging for CONFIGURED_ORDER mode
    if (Configuration.advancementsOrder == Configuration.AdvancementOrder.CONFIGURED_ORDER
        && !sortedChildren.isEmpty()) {
      Utils.LOGGER.info("Sorting children of: " + node.holder().id());
      for (int i = 0; i < sortedChildren.size(); i++) {
        final AdvancementNode child = sortedChildren.get(i);
        final String childId = child.holder().id().toString();
        final int configPos = Configuration.customAdvancementsOrder.indexOf(childId);
        Utils.LOGGER.info("  [" + i + "] " + childId + " (config position: " + configPos + ")");
      }
    }

    AdvancementTreePositioning previousChild = null;
    for (final AdvancementNode child : sortedChildren) {
      previousChild = this.addChild(child, previousChild);
    }
  }

  /**
   * Returns the children of the given node, sorted according to the configured
   * advancement order mode.
   *
   * @param node the node whose children to sort
   * @return a list of sorted children
   */
  private List<AdvancementNode> getSortedChildren(final AdvancementNode node) {
    final List<AdvancementNode> children = new ArrayList<>();
    node.children().forEach(children::add);

    // Don't sort if mode is NONE or list is too small
    if (Configuration.advancementsOrder == Configuration.AdvancementOrder.NONE
        || children.size() <= 1) {
      return children;
    }

    // Sort based on configured mode
    switch (Configuration.advancementsOrder) {
      case ALPHABETIC:
        children.sort(Comparator.comparing(
            child -> child.advancement().display()
                .map(display -> display.getTitle().getString())
                .orElse(""),
            String.CASE_INSENSITIVE_ORDER));
        break;

      case CONFIGURED_ORDER:
        children.sort((node1, node2) -> {
          final String id1 = node1.holder().id().toString();
          final String id2 = node2.holder().id().toString();

          final int pos1 = Configuration.customAdvancementsOrder.indexOf(id1);
          final int pos2 = Configuration.customAdvancementsOrder.indexOf(id2);

          // Both in configured order - sort by position
          if (pos1 != -1 && pos2 != -1) {
            return Integer.compare(pos1, pos2);
          }

          // Only node1 in configured order - it comes first
          if (pos1 != -1) {
            return -1;
          }

          // Only node2 in configured order - it comes first
          if (pos2 != -1) {
            return 1;
          }

          // Neither in configured order - sort alphabetically
          final String title1 = node1.advancement().display()
              .map(display -> display.getTitle().getString())
              .orElse("");
          final String title2 = node2.advancement().display()
              .map(display -> display.getTitle().getString())
              .orElse("");
          return title1.compareToIgnoreCase(title2);
        });
        break;

      case NONE:
      default:
        // Already handled above
        break;
    }

    return children;
  }

  /**
   * Adds a child advancement to this node's tree, recursively handling
   * invisible advancements.
   *
   * @param child the child advancement node to add
   * @param previousSibling the previous sibling in the tree
   * @return the last added tree position node
   */
  @Nullable
  private AdvancementTreePositioning addChild(final AdvancementNode child,
      @Nullable AdvancementTreePositioning previousSibling) {
    if (child.advancement().display().isPresent()) {
      previousSibling = new AdvancementTreePositioning(child, this, previousSibling,
          this.children.size() + 1, this.x + 1);
      this.children.add(previousSibling);
    } else {
      // Handle invisible advancements by processing their children
      for (final AdvancementNode grandchild : child.children()) {
        previousSibling = this.addChild(grandchild, previousSibling);
      }
    }

    return previousSibling;
  }

  /**
   * First pass of the tree layout algorithm.
   * Performs a post-order traversal to calculate preliminary Y positions.
   */
  private void firstWalk() {
    if (this.children.isEmpty()) {
      if (this.previousSibling != null) {
        this.y = this.previousSibling.y + 1.0F;
      } else {
        this.y = 0.0F;
      }
    } else {
      AdvancementTreePositioning defaultAncestor = null;

      for (final AdvancementTreePositioning child : this.children) {
        child.firstWalk();
        defaultAncestor = child.apportion(
            defaultAncestor == null ? child : defaultAncestor);
      }

      this.executeShifts();
      final float midpoint = (this.children.get(0).y
          + this.children.get(this.children.size() - 1).y) / 2.0F;

      if (this.previousSibling != null) {
        this.y = this.previousSibling.y + 1.0F;
        this.mod = this.y - midpoint;
      } else {
        this.y = midpoint;
      }
    }
  }

  /**
   * Second pass of the tree layout algorithm.
   * Performs a pre-order traversal to finalize Y positions and determine the
   * minimum Y coordinate.
   *
   * @param offsetY the Y offset to add to this node
   * @param columnX the X column index
   * @param subtreeTopY the current minimum Y coordinate
   * @return the new minimum Y coordinate
   */
  private float secondWalk(final float offsetY, final int columnX, float subtreeTopY) {
    this.y += offsetY;
    this.x = columnX;

    if (this.y < subtreeTopY) {
      subtreeTopY = this.y;
    }

    for (final AdvancementTreePositioning child : this.children) {
      subtreeTopY = child.secondWalk(offsetY + this.mod, columnX + 1, subtreeTopY);
    }

    return subtreeTopY;
  }

  /**
   * Third pass of the tree layout algorithm.
   * Adjusts all Y positions by a given offset to ensure all nodes are positive.
   *
   * @param y the Y offset to add
   */
  private void thirdWalk(final float y) {
    this.y += y;

    for (final AdvancementTreePositioning child : this.children) {
      child.thirdWalk(y);
    }
  }

  /**
   * Executes accumulated shifts on children nodes.
   * Part of the tree layout algorithm for balanced positioning.
   */
  private void executeShifts() {
    float shiftAccumulator = 0.0F;
    float changeAccumulator = 0.0F;

    for (int i = this.children.size() - 1; i >= 0; i--) {
      final AdvancementTreePositioning child = this.children.get(i);
      child.y += shiftAccumulator;
      child.mod += shiftAccumulator;
      changeAccumulator += child.change;
      shiftAccumulator += child.shift + changeAccumulator;
    }
  }

  /**
   * Gets the previous node in traversal order, or the thread if set.
   *
   * @return the previous node or thread
   */
  @Nullable
  private AdvancementTreePositioning previousOrThread() {
    if (this.thread != null) {
      return this.thread;
    } else {
      return !this.children.isEmpty() ? this.children.get(0) : null;
    }
  }

  /**
   * Gets the next node in traversal order, or the thread if set.
   *
   * @return the next node or thread
   */
  @Nullable
  private AdvancementTreePositioning nextOrThread() {
    if (this.thread != null) {
      return this.thread;
    } else {
      return !this.children.isEmpty()
          ? this.children.get(this.children.size() - 1) : null;
    }
  }

  /**
   * Apportions space between subtrees to avoid conflicts.
   * Core part of the Reingold-Tilford tree layout algorithm.
   *
   * @param defaultAncestor the default ancestor for conflict resolution
   * @return the new default ancestor
   */
  private AdvancementTreePositioning apportion(AdvancementTreePositioning defaultAncestor) {
    if (this.previousSibling == null) {
      return defaultAncestor;
    }

    AdvancementTreePositioning leftContour = this;
    AdvancementTreePositioning rightContour = this;
    AdvancementTreePositioning leftSibling = this.previousSibling;
    AdvancementTreePositioning leftmostSibling = this.parent.children.get(0);

    float leftModSum = this.mod;
    float rightModSum = this.mod;
    float leftSiblingModSum = leftSibling.mod;
    float leftmostSiblingModSum = leftmostSibling.mod;

    while (leftSibling.nextOrThread() != null && leftContour.previousOrThread() != null) {
      leftSibling = leftSibling.nextOrThread();
      leftContour = leftContour.previousOrThread();
      leftmostSibling = leftmostSibling.previousOrThread();
      rightContour = rightContour.nextOrThread();
      rightContour.ancestor = this;

      final float shift = leftSibling.y + leftSiblingModSum
          - (leftContour.y + leftModSum) + 1.0F;

      if (shift > 0.0F) {
        leftSibling.getAncestor(this, defaultAncestor).moveSubtree(this, shift);
        leftModSum += shift;
        rightModSum += shift;
      }

      leftSiblingModSum += leftSibling.mod;
      leftModSum += leftContour.mod;
      leftmostSiblingModSum += leftmostSibling.mod;
      rightModSum += rightContour.mod;
    }

    if (leftSibling.nextOrThread() != null && rightContour.nextOrThread() == null) {
      rightContour.thread = leftSibling.nextOrThread();
      rightContour.mod += leftSiblingModSum - rightModSum;
    } else {
      if (leftContour.previousOrThread() != null
          && leftmostSibling.previousOrThread() == null) {
        leftmostSibling.thread = leftContour.previousOrThread();
        leftmostSibling.mod += leftModSum - leftmostSiblingModSum;
      }

      defaultAncestor = this;
    }

    return defaultAncestor;
  }

  /**
   * Moves this subtree relative to another, distributing the shift among siblings.
   *
   * @param target the target node to move relative to
   * @param shift the amount to shift
   */
  private void moveSubtree(final AdvancementTreePositioning target, final float shift) {
    final float siblingCount = target.childIndex - this.childIndex;
    if (siblingCount != 0.0F) {
      target.change -= shift / siblingCount;
      this.change += shift / siblingCount;
    }

    target.shift += shift;
    target.y += shift;
    target.mod += shift;
  }

  /**
   * Gets the appropriate ancestor node for conflict resolution.
   *
   * @param self the current node
   * @param defaultAncestor the default ancestor to use if current is invalid
   * @return the ancestor to use
   */
  private AdvancementTreePositioning getAncestor(final AdvancementTreePositioning self,
      final AdvancementTreePositioning defaultAncestor) {
    return this.ancestor != null && self.parent.children.contains(this.ancestor)
        ? this.ancestor : defaultAncestor;
  }

  /**
   * Finalizes the position by writing the calculated X and Y coordinates
   * to the advancement's display info.
   */
  private void finalizePosition() {
    this.node.advancement().display().ifPresent(displayInfo ->
        displayInfo.setLocation(this.x, this.y));

    if (!this.children.isEmpty()) {
      for (final AdvancementTreePositioning child : this.children) {
        child.finalizePosition();
      }
    }
  }

  /**
   * Runs the complete tree positioning algorithm on the given root node.
   * This is the main entry point that replaces vanilla TreeNodePosition.run().
   *
   * @param rootNode the root advancement node to position
   * @throws IllegalArgumentException if the root node has no display info
   */
  public static void run(final AdvancementNode rootNode) {
    if (rootNode.advancement().display().isEmpty()) {
      throw new IllegalArgumentException("Can't position children of an invisible root!");
    }

    final AdvancementTreePositioning root = new AdvancementTreePositioning(
        rootNode, null, null, 1, 0);

    // Run the three-pass layout algorithm
    root.firstWalk();
    final float minY = root.secondWalk(0.0F, 0, root.y);

    // Adjust if any nodes would be negative
    if (minY < 0.0F) {
      root.thirdWalk(-minY);
    }

    root.finalizePosition();
  }
}
