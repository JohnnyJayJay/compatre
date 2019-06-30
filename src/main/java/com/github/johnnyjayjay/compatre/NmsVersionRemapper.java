package com.github.johnnyjayjay.compatre;

import org.objectweb.asm.commons.Remapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
final class NmsVersionRemapper extends Remapper {

  private static final Pattern VERSION_PACKAGE_PATTERN =
      Pattern.compile("(?<=(net/minecraft/server|org/bukkit/craftbukkit)/)(v\\d_\\d{1,2}_R\\d)");

  private final String nmsVersion;

  NmsVersionRemapper(String nmsVersion) {
    this.nmsVersion = nmsVersion;
  }

  @Override
  public String map(String internalName) {
    return adjust(internalName);
  }

  private String adjust(String descriptor) {
    Matcher matcher = VERSION_PACKAGE_PATTERN.matcher(descriptor);
    return matcher.replaceFirst(nmsVersion);
  }

}
