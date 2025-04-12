package codes.atomys.advr.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for rendering item stacks on the GUI with a specified purpose.
 */
public final class ItemRenderHelper {

  // Private constructor to prevent instantiation
  private ItemRenderHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Renders an item stack on the GUI with a specified brightness.
   *
   * @param guiGraphics The {@link GuiGraphics} instance used for rendering.
   * @param itemStack   The {@link ItemStack} to render. If the stack is empty,
   *                    the method returns immediately.
   * @param x           The x-coordinate on the screen where the item should be
   *                    rendered.
   * @param y           The y-coordinate on the screen where the item should be
   *                    rendered.
   * @param brightness  The brightness level of the item, clamped between 0.0f
   *                    (minimum
   *                    brightness) and 1.0f (full brightness).
   */
  public static void renderItemWithBrightness(final GuiGraphics guiGraphics, final ItemStack itemStack, final int x,
      final int y, final float brightness) {
    if (itemStack.isEmpty())
      return;

    final Minecraft minecraft = Minecraft.getInstance();

    // Clamp brightness between 0.0f and 1.0f
    final float clampedBrightness = Mth.clamp(brightness, 0.0f, 1.0f);

    // Extract the default light components
    final int defaultLight = 15728880; // 0x00F00060
    int skyLight = (defaultLight >> 20) & 0xF; // Extract sky light (15)
    int blockLight = (defaultLight >> 4) & 0xF; // Extract block light (6)

    // Reduce light levels based on brightness
    if (clampedBrightness < 1.0f) {
      final float factor = 0.2f + (0.8f * clampedBrightness);
      blockLight = Math.max(1, (int) (blockLight * factor));
      skyLight = Math.max(1, (int) (skyLight * factor));
    }

    // Reconstruct the light value with our adjusted components
    final int light = (skyLight << 20) | (blockLight << 4);

    final ItemStackRenderState renderState = new ItemStackRenderState();
    minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GUI, minecraft.level,
        minecraft.player, 0);

    final PoseStack pose = guiGraphics.pose();
    pose.pushPose();
    pose.translate((x + 8), (y + 8), 150);
    pose.scale(16.0F, -16.0F, 16.0F);

    final boolean flat = !renderState.usesBlockLight();
    if (flat) {
      guiGraphics.flush();
      Lighting.setupForFlatItems();
    }

    guiGraphics.drawSpecial(bufferSource -> {
      renderState.render(pose, bufferSource, light, OverlayTexture.NO_OVERLAY);
    });
    guiGraphics.flush();

    if (flat) {
      Lighting.setupFor3DItems();
    }

    pose.popPose();
  }
}
