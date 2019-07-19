package com.github.johnnyjayjay.compatre;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;


/**
 * Compatre's java agent. When applied, its {@code premain} will attach a
 * {@code ClassFileTransformer} to each class that will transform types annotated
 * with {@link NmsDependent} as described {@link NmsDependent here}.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new NmsTransformer());
  }

  private static final class NmsTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
      return NmsDependentTransformer.transformIfNmsDependent(classfileBuffer);
    }
  }

}
