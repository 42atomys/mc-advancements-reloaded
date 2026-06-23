package codes.atomys.advr.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

/**
 * Generic reflection helpers for bridging Minecraft API differences across the
 * supported 26.x range with a single jar.
 *
 * <p>
 * Minecraft renames or removes members between 26.x drops. Rather than writing
 * bespoke reflection for each one, a compatibility point declares a cached
 * {@link MethodHandle} resolved from the first candidate name that exists on the
 * running version. See {@link ScreenCompat} for a concrete example.
 * </p>
 */
public final class Compat {

  private Compat() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Resolves a virtual (instance) method, returning a handle to the first
   * candidate name that exists on the owner with the given signature.
   *
   * @param owner the class declaring the method
   * @param type  the method signature, excluding the implicit receiver
   * @param names the candidate method names, in preference order
   * @return a method handle bound to the first matching method
   * @throws IllegalStateException if none of the names resolve
   */
  public static MethodHandle method(final Class<?> owner, final MethodType type, final String... names) {
    for (final String name : names) {
      try {
        return MethodHandles.lookup().findVirtual(owner, name, type);
      } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        // Method absent on this version; try the next candidate name.
      }
    }
    throw unresolved(owner, names);
  }

  /**
   * Resolves a static method, returning a handle to the first candidate name
   * that exists on the owner with the given signature.
   *
   * @param owner the class declaring the method
   * @param type  the method signature
   * @param names the candidate method names, in preference order
   * @return a method handle bound to the first matching method
   * @throws IllegalStateException if none of the names resolve
   */
  public static MethodHandle staticMethod(final Class<?> owner, final MethodType type, final String... names) {
    for (final String name : names) {
      try {
        return MethodHandles.lookup().findStatic(owner, name, type);
      } catch (final NoSuchMethodException | IllegalAccessException ignored) {
        // Method absent on this version; try the next candidate name.
      }
    }
    throw unresolved(owner, names);
  }

  /**
   * Resolves an instance field getter, returning a handle to the first candidate
   * name that exists on the owner with the given field type.
   *
   * @param owner     the class declaring the field
   * @param fieldType the declared type of the field
   * @param names     the candidate field names, in preference order
   * @return a getter handle bound to the first matching field
   * @throws IllegalStateException if none of the names resolve
   */
  public static MethodHandle getter(final Class<?> owner, final Class<?> fieldType, final String... names) {
    for (final String name : names) {
      try {
        return MethodHandles.lookup().findGetter(owner, name, fieldType);
      } catch (final NoSuchFieldException | IllegalAccessException ignored) {
        // Field absent on this version; try the next candidate name.
      }
    }
    throw unresolved(owner, names);
  }

  private static IllegalStateException unresolved(final Class<?> owner, final String... names) {
    return new IllegalStateException(
        "None of " + Arrays.toString(names) + " could be resolved on " + owner.getName());
  }
}
