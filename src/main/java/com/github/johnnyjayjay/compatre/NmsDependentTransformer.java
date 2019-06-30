package com.github.johnnyjayjay.compatre;

import org.bukkit.Bukkit;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
final class NmsDependentTransformer {

  private static String nmsVersion = null;

  private NmsDependentTransformer() {}

  public static byte[] transformIfNmsDependent(byte[] classfileBuffer) {
    ClassReader reader = new ClassReader(classfileBuffer);
    NmsDependentCheckVisitor checkVisitor = new NmsDependentCheckVisitor();
    reader.accept(checkVisitor, 0);
    if (checkVisitor.isAnnotationPresent()) {
      ClassWriter writer = new ClassWriter(0);
      ClassVisitor classRemapper = new ClassRemapper(writer, new NmsVersionRemapper(getNmsVersion()));
      reader.accept(classRemapper, 0);
      return writer.toByteArray();
    } else {
      return classfileBuffer;
    }
  }

  private static String getNmsVersion() {
    if (nmsVersion != null)
      return nmsVersion;

    String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
    nmsVersion = craftBukkitPackage.substring(craftBukkitPackage.lastIndexOf('.') + 1);
    return nmsVersion;
  }
}
