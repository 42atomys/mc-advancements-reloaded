package codes.atomys.advancementinforeloaded.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import codes.atomys.advancementinforeloaded.screen.AdvancementReloadedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientAdvancementManager;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

  @Redirect(
        method = "handleInputEvents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
        )
    )
    private void replaceAdvancementsScreen(MinecraftClient client, Screen screen) {
        if (screen instanceof AdvancementsScreen) {
            ClientAdvancementManager advancementManager = client.player.networkHandler.getAdvancementHandler();
            client.setScreen(new AdvancementReloadedScreen(advancementManager));
        } else {
            client.setScreen(screen);
        }
    }
}
