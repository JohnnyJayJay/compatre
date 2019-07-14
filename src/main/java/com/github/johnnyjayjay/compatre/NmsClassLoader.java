package com.github.johnnyjayjay.compatre;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsClassLoader extends URLClassLoader {

  public NmsClassLoader() throws MalformedURLException {
    super(ClassLoader.getSystemClassLoader() instanceof URLClassLoader
        ? ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()
        : new URL[] { new File(".").toURI().toURL()},
        ClassLoader.getSystemClassLoader());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> loaded = super.loadClass(name, resolve);
    byte[] classfileBuffer;
    try (InputStream inputStream = getResourceAsStream(asResourcePath(loaded))) {
      if (inputStream == null) {
        throw new ClassFileNotFoundException("Class " + name
            + " could not be transformed by compatre; .class file was not found in resources.");
      }

      classfileBuffer = fromInputStream(inputStream);
    } catch (IOException e) {
      throw new ClassTransformationException("Class " + name
          + " could not be transformed by compatre; I/O Exception occurred", e);
    }
    classfileBuffer = NmsDependentTransformer.transformIfNmsDependent(classfileBuffer);
    return defineClass(name, classfileBuffer, 0, classfileBuffer.length);
  }

  private String asResourcePath(Class<?> clazz) {
    return clazz.getName().replace('.', '/') + ".class";
  }

  private byte[] fromInputStream(InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
      int nextByte;
      while ((nextByte = inputStream.read()) != -1) {
        byteStream.write(nextByte);
      }
      return byteStream.toByteArray();
    }
  }
}
