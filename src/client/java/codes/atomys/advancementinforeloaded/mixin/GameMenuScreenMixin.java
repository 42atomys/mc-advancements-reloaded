package codes.atomys.advancementinforeloaded.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import codes.atomys.advancementinforeloaded.screen.AdvancementReloadedScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.text.Text;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

  protected GameMenuScreenMixin(Text title) {
    super(title);
  }

  @ModifyArg(
    method = "initWidgets",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;createButton(Lnet/minecraft/text/Text;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/widget/ButtonWidget;"
    ),
    index = 1
  )  private Supplier<Screen> modifyAdvancementsButton(Supplier<Screen> original) {
    if (original.get() instanceof AdvancementsScreen) {
      return () -> new AdvancementReloadedScreen(client.player.networkHandler.getAdvancementHandler(), this);
    } else {
      return original;
    }
  }
}
