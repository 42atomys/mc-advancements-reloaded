package codes.atomys.advancementinforeloaded;

import codes.atomys.advancementinforeloaded.screen.AdvancementReloadedWidget;
import net.fabricmc.api.ClientModInitializer;

public class AdvancementInfoReloadedClient implements ClientModInitializer {

  public static final String MOD_ID = "advancementinforeloaded";
  public static AdvancementReloadedWidget currentWidget = null;

  public static AdvancementReloadedWidget getCurrentWidget() {
    return currentWidget;
  }

  public static boolean hasCurrentWidget() {
    return currentWidget != null;
  }

  public static void setCurrentWidget(AdvancementReloadedWidget widget) {
    currentWidget = widget;
  }

  @Override
  public void onInitializeClient() {
    System.out.println(AdvancementInfoReloaded.MOD_ID + " loaded!");
    // This entrypoint is suitable for setting up client-specific logic, such as rendering.
  }

}
