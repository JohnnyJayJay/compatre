package com.github.johnnyjayjay.compatre;

import org.objectweb.asm.commons.Remapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.johnnyjayjay.compatre.Compatre.LOGGER;

/**
 * An ASM {@code Remapper} that replaces the version String in nms and craftbukkit types.
 *
 * @see NmsDependentTransformer#transform(byte[])
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsVersionRemapper extends Remapper {

  private static final Pattern VERSION_PACKAGE_PATTERN =
      Pattern.compile("(?<=(net/minecraft/server|org/bukkit/craftbukkit)/)(v\\d_\\d{1,2}_R\\d)");

  private final String nmsVersion;

  /**
   * Creates a new {@code NmsVersionRemapper} that will use the given version as a replacement.
   *
   * @param nmsVersion The version to replace the version segments of nms/craftbukkit types with.
   */
  public NmsVersionRemapper(String nmsVersion) {
    this.nmsVersion = nmsVersion;
  }

  /**
   * Takes the name of a type and returns it with a replaced version, if applicable.
   *
   * @param internalName the internal name of a type.
   * @return the new internal name.
   */
  @Override
  public String map(String internalName) {
    LOGGER.finest(() -> "Remapping " + internalName + "...");
    Matcher matcher = VERSION_PACKAGE_PATTERN.matcher(internalName);
    String result = matcher.replaceFirst(nmsVersion);
    LOGGER.finest(() -> internalName.equals(result) ? "Nothing to remap." : "Remapped to " + result + ".");
    return result;
  }

}
