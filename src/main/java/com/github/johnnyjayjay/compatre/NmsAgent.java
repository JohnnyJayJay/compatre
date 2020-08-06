package com.github.johnnyjayjay.compatre;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Compatre's java agent. When applied, its {@code premain} will attach a
 * {@code ClassFileTransformer} to each class that will transform types annotated
 * with {@link NmsDependent} as described {@link NmsDependent here}.
 * <p>
 * The recommended usage is to <a href="https://github.com/johnnyjayjay/compatre/releases">download the compatre jar</a>,
 * put it in the {@code ./lib} directory of your server and then add {@code -javaagent:"./lib/name-of-compatre.jar"}
 * as a JVM argument when starting the server.
 * <p>
 * Compatre currently does <strong>not</strong> support dynamic agent attachment itself.
 * It does however define an {@code agentmain} as well as the {@code Agent-Class} attribute
 * in its manifest, meaning you can add support to attach it at runtime without modifying compatre.
 * <br>
 * Note that whether and how this is possible strongly depends on the implementation and the version of
 * the JVM, so it is not recommended.
 * See <a href="https://github.com/electronicarts/ea-agent-loader">the ea agent loader library</a> for more info.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    agentmain(agentArgs, inst);
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new NmsTransformer());
  }

  private static final class NmsTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
      return NmsDependentTransformer.isNmsDependent(classfileBuffer)
          ? NmsDependentTransformer.transform(classfileBuffer)
          : classfileBuffer;
    }
  }

}
