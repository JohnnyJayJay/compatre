package com.github.johnnyjayjay.compatre.common;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsDependentCheckVisitor extends ClassVisitor {

  private static final String ANNOTATION_NAME = NmsDependent.class.getName().replace('.', '/');

  private boolean annotationPresent;

  NmsDependentCheckVisitor() {
    super(Opcodes.ASM5);
    this.annotationPresent = false;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (ANNOTATION_NAME.equals(descriptor)) {
      annotationPresent = true;
    }
    return null;
  }

  boolean isAnnotationPresent() {
    return annotationPresent;
  }
}
