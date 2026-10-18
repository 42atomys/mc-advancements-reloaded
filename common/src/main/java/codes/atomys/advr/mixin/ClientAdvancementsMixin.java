package codes.atomys.advr.mixin;

import codes.atomys.advr.AdvancementTreeRecalculator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
 * <p>
 * Only the trees touched by the packet are recalculated: datapacks such as
 * Blaze and Caves make the server add and remove hidden advancements every
 * second, and laying out thousands of advancements on each of these packets
 * freezes the game (issue #134).
 * </p>
 */
@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin {

  @Shadow
  @Final
  private AdvancementTree tree;

  @Unique
  private final Set<Identifier> advr$affectedRoots = new HashSet<>();

  /**
   * Remembers the roots of the advancements the packet is about to remove,
   * while they are still in the tree.
   *
   * @param packet the advancements update packet
   * @param ci     the callback info
   */
  @Inject(method = "update", at = @At("HEAD"))
  private void collectRemovedRoots(final ClientboundUpdateAdvancementsPacket packet, final CallbackInfo ci) {
    this.advr$affectedRoots.clear();
    if (!packet.shouldReset()) {
      AdvancementTreeRecalculator.collectAffectedRoots(this.tree, packet.removed(), this.advr$affectedRoots);
    }
  }

  /**
   * Recalculates the positions of the affected trees right after the positions
   * sent by the server have been applied to the tree, when the progress of the
   * packet starts to be processed.
   *
   * @param packet the advancements update packet
   * @param ci     the callback info
   */
  @Inject(method = "update", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/network/protocol/game/ClientboundUpdateAdvancementsPacket;progress()Ljava/util/Map;"))
  private void recalculateTreePositions(final ClientboundUpdateAdvancementsPacket packet, final CallbackInfo ci) {
    if (packet.shouldReset()) {
      AdvancementTreeRecalculator.recalculate(this.tree);
    } else {
      AdvancementTreeRecalculator.collectAffectedRoots(this.tree,
          packet.added().stream().map(added -> added.advancement().id()).toList(),
          this.advr$affectedRoots);
      AdvancementTreeRecalculator.recalculate(this.tree, this.advr$affectedRoots);
    }
    this.advr$affectedRoots.clear();
  }
}
