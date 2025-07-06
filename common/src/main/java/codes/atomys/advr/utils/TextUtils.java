package codes.atomys.advr.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Utility class for text-related operations, particularly for converting
 * formatted character sequences and components to plain strings.
 */
public final class TextUtils {
  private TextUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Converts a {@link FormattedCharSequence} to a plain {@link String}.
   *
   * @param charSequence the formatted character sequence to convert
   * @return the plain string representation of the character sequence
   */
  public static String toString(final FormattedCharSequence charSequence) {
    if (charSequence == null) {
      return "";
    }

    // You'd need to extract the text content by iterating through the sequence
    final StringBuilder builder = new StringBuilder();
    charSequence.accept((index, style, codepoint) -> {
      builder.appendCodePoint(codepoint);
      return true;
    });

    return builder.toString();
  }

  /**
   * Converts a {@link Component} to a plain {@link String}.
   *
   * @param component the component to convert
   * @return the plain string representation of the component
   */
  public static String toString(final Component component) {
    if (component == null) {
      return "";
    }
    return toString(component.getVisualOrderText());
  }

}
