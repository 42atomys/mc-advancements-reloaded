package codes.atomys.advr.mixin;

import codes.atomys.advr.AdvancementTreePositioning;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.TreeNodePosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace vanilla TreeNodePosition with our custom implementation
 * that supports configurable advancement ordering.
 * <p>
 * This allows us to control the tree layout algorithm entirely, enabling:
 * - Deterministic advancement row ordering
 * - Future extensibility for different positioning strategies
 * - Full control over tree visualization
 * </p>
 */
@Mixin(TreeNodePosition.class)
public class TreeNodePositionMixin {

  /**
   * Intercepts TreeNodePosition.run() and replaces it with our custom
   * implementation that respects the configured advancement order.
   *
   * @param rootNode the root advancement node to position
   * @param ci the callback info to cancel vanilla execution
   */
  @Inject(method = "run", at = @At("HEAD"), cancellable = true)
  private static void replaceTreePositioning(final AdvancementNode rootNode, final CallbackInfo ci) {
    // Use our custom positioning algorithm instead of vanilla
    AdvancementTreePositioning.run(rootNode);

    // Cancel vanilla execution
    ci.cancel();
  }
}
