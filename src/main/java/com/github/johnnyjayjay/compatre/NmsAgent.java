package com.github.johnnyjayjay.compatre;

import java.lang.instrument.Instrumentation;


/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
        -> NmsDependentTransformer.transformIfNmsDependent(classfileBuffer));
  }

}
