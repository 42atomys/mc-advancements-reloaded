package codes.atomys.advr;

import codes.atomys.advr.config.gui.ConfigurationScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

/**
 * This class integrates with the Mod Menu API to provide a configuration screen
 * for the mod. It implements the ModMenuApi interface and overrides the
 * getModConfigScreenFactory method to return a ConfigScreenFactory.
 *
 * @see <a href="https://github.com/TerraformersMC/ModMenu">Mod Menu</a>
 * @see ConfigurationScreen#screen(Screen)
 */
public class ModMenuIntegration implements ModMenuApi {

  /**
   * Provides a config screen factory for the Mod Menu.
   *
   * <p>
   * The method returns a ConfigScreenFactory object that can be used to create
   * a configuration screen for the Mod Menu. The configuration screen is
   * provided by the
   * {@link ConfigurationScreen#screen(Screen)} method.
   * </p>
   *
   * @return a config screen factory for the Mod Menu.
   */
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> ConfigurationScreen.screen(parent);
  }
}
