package com.github.johnnyjayjay.compatre;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An ASM {@code ClassVisitor} used to check whether the {@link NmsDependent} annotation is present on a class.
 *
 * @see NmsDependentTransformer#isNmsDependent(byte[])
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsDependentCheckVisitor extends ClassVisitor {

  private static final String ANNOTATION_DESCRIPTOR = "L" + NmsDependent.class.getName().replace('.', '/') + ";";

  private boolean annotationPresent;

  public NmsDependentCheckVisitor() {
    super(Opcodes.ASM8);
    this.annotationPresent = false;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (ANNOTATION_DESCRIPTOR.equals(descriptor)) {
      annotationPresent = true;
    }
    return null;
  }

  /**
   * Returns whether the annotation is present. Must be called after visiting.
   *
   * @return {@code true}, if it is present.
   */
  public boolean isAnnotationPresent() {
    return annotationPresent;
  }
}
