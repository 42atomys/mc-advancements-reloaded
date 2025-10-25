package codes.atomys.advr.mixin;

import codes.atomys.advr.AdvancementTreePositioning;
import java.util.Set;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to recalculate advancement tree positions when advancements are
 * received
 * from the server in multiplayer.
 * <p>
 * In multiplayer, the server calculates positions using vanilla
 * TreeNodePosition
 * and sends pre-calculated positions to clients. This mixin intercepts when
 * a root advancement is added on the client and forces a recalculation using
 * our custom ordering algorithm.
 * </p>
 */
@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {

  @Shadow
  @Final
  private Set<AdvancementNode> roots;

  /**
   * Recalculates tree positions after a root advancement is inserted.
   * This ensures custom ordering is applied even in multiplayer where
   * positions are received from the server.
   *
   * @param advancement the advancement holder that was inserted
   * @param cir         the callback info returnable
   */
  @Inject(method = "tryInsert", at = @At("RETURN"))
  private void recalculateTreePositions(
      final net.minecraft.advancements.AdvancementHolder advancement,
      final CallbackInfoReturnable<Boolean> cir) {
    // Only recalculate if insertion was successful and it's a root advancement
    if (cir.getReturnValue() && advancement.value().parent().isEmpty()) {
      // Find the newly added root node
      for (final AdvancementNode root : this.roots) {
        if (root.holder().equals(advancement) && root.advancement().display().isPresent()) {
          // Force recalculation of tree positions using our custom algorithm
          AdvancementTreePositioning.run(root);
          break;
        }
      }
    }
  }
}
