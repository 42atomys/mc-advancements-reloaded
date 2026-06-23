package codes.atomys.advr.mixin;

import codes.atomys.advr.screens.AdvancementReloadedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla advancements screen with {@link AdvancementReloadedScreen}
 * on Minecraft 26.2+, where keybind handling and screen management moved from
 * {@code Minecraft} to {@link Gui}.
 *
 * <p>
 * On 26.1.x this opening happens in {@code Minecraft.handleKeybinds} and is
 * handled by {@code MinecraftClientMixin} instead; there {@code Gui} has no such
 * call, so this redirect simply matches nothing ({@code require = 0}).
 * </p>
 */
@Mixin(Gui.class)
public class GuiMixin {

  /**
   * Redirects the {@code Gui#setScreen(Screen)} call used to open the
   * advancements screen from the keybinding on Minecraft 26.2+.
   *
   * @param instance the Gui instance opening the screen
   * @param screen   the screen Minecraft was about to open
   */
  @Redirect(method = "handleKeybinds", require = 0, at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
  private void replaceAdvancementsScreen(final Gui instance, final Screen screen) {
    if (screen instanceof AdvancementsScreen) {
      final ClientAdvancements advancementManager = Minecraft.getInstance().player.connection.getAdvancements();
      instance.setScreen(new AdvancementReloadedScreen(advancementManager));
    } else {
      instance.setScreen(screen);
    }
  }
}
