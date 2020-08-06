package com.github.johnnyjayjay.compatre;

import org.bukkit.Bukkit;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

/**
 * The class used to check if a class is annotated with {@link NmsDependent}
 * and to apply compatre's transformations to it.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsDependentTransformer {

  private static String nmsVersion = null;

  private NmsDependentTransformer() {
  }

  /**
   * Checks whether the given class is annotated with {@link NmsDependent}.
   *
   * @param classfileBuffer The class, encoded as an array of bytes.
   * @return {@code true}, if the class is nms dependent.
   */
  public static boolean isNmsDependent(byte[] classfileBuffer) {
    ClassReader reader = new ClassReader(classfileBuffer);
    NmsDependentCheckVisitor checkVisitor = new NmsDependentCheckVisitor();
    reader.accept(checkVisitor, 0);
    return checkVisitor.isAnnotationPresent();
  }

  /**
   * Applies compatre's transformations to the given class.
   *
   * @param classfileBuffer The class, encoded as an array of bytes.
   * @return A new byte array representing the new class.
   */
  public static byte[] transform(byte[] classfileBuffer) {
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(0);
    ClassVisitor classRemapper = new ClassRemapper(writer, new NmsVersionRemapper(getNmsVersion()));
    reader.accept(classRemapper, 0);
    return writer.toByteArray();
  }

  private static String getNmsVersion() {
    if (nmsVersion != null)
      return nmsVersion;

    String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
    nmsVersion = craftBukkitPackage.substring(craftBukkitPackage.lastIndexOf('.') + 1);
    return nmsVersion;
  }
}
