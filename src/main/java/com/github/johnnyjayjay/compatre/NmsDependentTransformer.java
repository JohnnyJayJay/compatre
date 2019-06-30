package com.github.johnnyjayjay.compatre;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter;

import org.bukkit.Bukkit;

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
      ClassWriter writer = new ClassWriter(reader, 0);
      ClassVisitor remappingAdapter = new RemappingClassAdapter(writer, new NmsVersionRemapper(getNmsVersion()));
      reader.accept(remappingAdapter, 0);
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
