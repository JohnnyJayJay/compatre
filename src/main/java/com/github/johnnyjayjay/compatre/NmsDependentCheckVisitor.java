package com.github.johnnyjayjay.compatre;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
final class NmsDependentCheckVisitor extends ClassVisitor {

  private static final String ANNOTATION_DESCRIPTOR = NmsDependent.class.getName().replace('.', '/');

  private boolean annotationPresent;

  NmsDependentCheckVisitor() {
    super(Opcodes.ASM5);
    this.annotationPresent = false;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (ANNOTATION_DESCRIPTOR.equals(descriptor)) {
      annotationPresent = true;
    }
    return null;
  }

  boolean isAnnotationPresent() {
    return annotationPresent;
  }
}
