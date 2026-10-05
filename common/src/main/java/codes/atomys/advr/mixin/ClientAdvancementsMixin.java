package codes.atomys.advr.mixin;

import codes.atomys.advr.AdvancementTreeRecalculator;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to recalculate advancement tree positions when advancements are
 * received from the server.
 * <p>
 * The server calculates the positions of the advancements and sends them with
 * the advancements. In multiplayer, the server uses the vanilla
 * TreeNodePosition, so this mixin forces a recalculation using our custom
 * ordering algorithm once the received positions have been applied, and before
 * the listener (the advancements screen) is notified.
 * </p>
 */
@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin {

  @Shadow
  @Final
  private AdvancementTree tree;

  /**
   * Recalculates the tree positions right after the positions sent by the
   * server have been applied to the tree, when the progress of the packet
   * starts to be processed.
   *
   * @param packet the advancements update packet
   * @param ci     the callback info
   */
  @Inject(method = "update", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/network/protocol/game/ClientboundUpdateAdvancementsPacket;progress()Ljava/util/Map;"))
  private void recalculateTreePositions(final ClientboundUpdateAdvancementsPacket packet, final CallbackInfo ci) {
    AdvancementTreeRecalculator.recalculate(this.tree);
  }
}
