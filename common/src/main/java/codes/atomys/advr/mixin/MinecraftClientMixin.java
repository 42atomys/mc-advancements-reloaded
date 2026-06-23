package codes.atomys.advr.mixin;

import codes.atomys.advr.compat.ScreenCompat;
import codes.atomys.advr.screens.AdvancementReloadedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla advancements screen with {@link AdvancementReloadedScreen}
 * on Minecraft 26.1.x, where the advancements keybind is handled in
 * {@code Minecraft.handleKeybinds}.
 *
 * <p>
 * Minecraft 26.2 moved this handling to {@code net.minecraft.client.gui.Gui}
 * (see {@code GuiMixin}); there this redirect matches nothing
 * ({@code require = 0}).
 * </p>
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

  /**
   * Redirects the {@code Minecraft#setScreen(Screen)} call used to open the
   * advancements screen from the keybinding on Minecraft 26.1.x.
   *
   * @param client the Minecraft client instance
   * @param screen the screen Minecraft was about to open
   */
  @Redirect(method = "handleKeybinds", require = 0, at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
  private void replaceAdvancementsScreen(final Minecraft client, final Screen screen) {
    if (screen instanceof AdvancementsScreen) {
      final ClientAdvancements advancementManager = client.player.connection.getAdvancements();
      ScreenCompat.setScreen(client, new AdvancementReloadedScreen(advancementManager));
    } else {
      ScreenCompat.setScreen(client, screen);
    }
  }
}
