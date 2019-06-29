package com.github.johnnyjayjay.compatre.agent;

import java.lang.instrument.Instrumentation;

import com.github.johnnyjayjay.compatre.common.NmsDependentTransformer;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class NmsAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
        -> NmsDependentTransformer.transformIfNmsDependent(classfileBuffer));
  }

}
