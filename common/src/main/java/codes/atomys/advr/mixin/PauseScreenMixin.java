package codes.atomys.advr.mixin;

import codes.atomys.advr.screens.AdvancementReloadedScreen;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin class for modifying the PauseScreen to replaces the default
 * advancements screen when the player opens the menu by clicking the button
 * in the pause menu.
 */
@Mixin(value = PauseScreen.class, priority = 1001)
public abstract class PauseScreenMixin extends Screen {

  protected PauseScreenMixin(final Component title) {
    super(title);
  }

  /**
   * Replaces the default advancements screen with the custom
   * AdvancementsReloadedScreen when
   * the player opens the menu by clicking the button in the pause menu.
   *
   * <p>
   * This method is called by the createPauseMenu method of PauseScreen and is
   * used to modify the Supplier used to construct the button.
   * </p>
   *
   * @param original the original Supplier
   * @return the modified Supplier
   */
  @ModifyArg(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"), index = 1)
  private Supplier<Screen> modifyAdvancementsButton(final Supplier<Screen> original) {
    if (original.get() instanceof AdvancementsScreen) {
      return () -> new AdvancementReloadedScreen(minecraft.player.connection.getAdvancements(),
          this);
    } else {
      return original;
    }
  }
}
