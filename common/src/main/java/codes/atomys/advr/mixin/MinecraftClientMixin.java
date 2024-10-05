package codes.atomys.advr.mixin;

import codes.atomys.advr.screens.AdvancementReloadedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This mixin class replaces the default advancements screen when the player
 * opens the menu with the keybinding.
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

  /**
   * Redirects the call to {@link Minecraft#setScreen(Screen)} when the default
   * advancements screen is opened, and replaces it with the custom
   * {@link AdvancementReloadedScreen}.
   *
   * <p>
   * This method is called by the mixin when the player presses the keybinding
   * to open the default advancements screen.
   * </p>
   *
   * <p>
   * The method checks if the screen is an instance of
   * {@link AdvancementsScreen}. If it is, the method creates a new instance of
   * {@link AdvancementReloadedScreen} and passes the client's advancement manager
   * to it. Then it calls the overridden method with the new screen instance.
   * </p>
   *
   * <p>
   * If the screen is not an instance of {@link AdvancementsScreen}, the method
   * simply calls the overridden method with the original screen instance.
   * </p>
   *
   * @param client the Minecraft client instance
   * @param screen the original screen instance
   *
   */
  @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
  private void replaceAdvancementsScreen(final Minecraft client, final Screen screen) {
    if (screen instanceof AdvancementsScreen) {
      final ClientAdvancements advancementManager = client.player.connection.getAdvancements();
      client.setScreen(new AdvancementReloadedScreen(advancementManager));
    } else {
      client.setScreen(screen);
    }
  }
}
