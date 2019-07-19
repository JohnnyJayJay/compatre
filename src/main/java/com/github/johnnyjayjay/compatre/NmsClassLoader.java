package com.github.johnnyjayjay.compatre;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * An implementation of {@code ClassLoader} that transforms types loaded with this class
 * and annotated with {@link NmsDependent} as described {@link NmsDependent here}.
 *
 * Note that this ClassLoader only works for class files that are present on the machine the JVM is running on.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class NmsClassLoader extends URLClassLoader {

  public NmsClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
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

  /**
   * Scans the classpath and loads every class annotated with {@link NmsDependent}
   * using an instance of this ClassLoader obtained using {@link #fromSystemClassLoader()}.
   */
  public static void loadAllInClasspath() {
    ClassLoader nmsLoader = fromSystemClassLoader();
    try (ScanResult result = new ClassGraph()
        .enableAnnotationInfo()
        .addClassLoader(nmsLoader)
        .scan()) {
      result.getClassesWithAnnotation(NmsDependent.class.getName()).loadClasses();
    }
  }

  /**
   * Creates a new NmsClassLoader based on the system ClassLoader.
   * If the system class loader is an {@code URLClassLoader}, its urls will be used for this ClassLoader too.
   *
   * @return a new NmsClassLoader instance with the system class loader as its parent.
   */
  public static NmsClassLoader fromSystemClassLoader() {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    URL[] urls;
    try {
      urls = systemClassLoader instanceof URLClassLoader
          ? ((URLClassLoader) systemClassLoader).getURLs()
          : new URL[] { new File(".").toURI().toURL() };

    } catch (MalformedURLException e) {
      throw new AssertionError("URL is unexpectedly malformed", e);
    }
    return new NmsClassLoader(urls, systemClassLoader);
  }
}
