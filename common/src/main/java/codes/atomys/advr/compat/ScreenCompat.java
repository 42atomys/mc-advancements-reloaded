package codes.atomys.advr.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Typed cross-version wrappers around screen-related Minecraft client APIs that
 * were renamed across the supported 26.x range.
 *
 * <p>
 * Minecraft 26.2 removed {@code Minecraft.setScreen(Screen)} in favour of
 * {@code Minecraft.setScreenAndShow(Screen)}, while 26.1.x exposes
 * {@code setScreen(Screen)} (26.1.2 ships both). The method is resolved once via
 * {@link Compat}, preferring the newer name.
 * </p>
 */
public final class ScreenCompat {

  private static final MethodHandle SET_SCREEN = Compat.method(
      Minecraft.class, MethodType.methodType(void.class, Screen.class), "setScreenAndShow", "setScreen");

  private ScreenCompat() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Opens the given screen on the Minecraft client, using whichever
   * screen-setting method is present on the running Minecraft version.
   *
   * @param client the Minecraft client instance
   * @param screen the screen to display, or {@code null} to close the current one
   */
  public static void setScreen(final Minecraft client, final Screen screen) {
    try {
      SET_SCREEN.invoke(client, screen);
    } catch (final Throwable throwable) {
      throw new IllegalStateException("Failed to open screen", throwable);
    }
  }
}
