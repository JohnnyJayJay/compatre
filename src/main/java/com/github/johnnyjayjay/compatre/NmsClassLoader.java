package com.github.johnnyjayjay.compatre;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static com.github.johnnyjayjay.compatre.Compatre.LOGGER;

/**
 * A class that allows you to apply Compatre's transformations to your nms dependent classes
 * without a java agent.
 * <p>
 * The java agent is the recommended way to use compatre, as this class uses many
 * unsafe hacks and workarounds to make it work. Unless Spigot introduces an API for custom
 * class loading, this class will thus remain unstable.
 * <p>
 * This class is not a "real" class loader - it only acts as a way to inject custom logic into
 * Bukkit's {@code PluginClassLoader}.
 *
 * @implNote To understand how this class operates, you need to understand how Bukkit's
 * class loading system works. Bukkit uses a separate instance of
 * <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/PluginClassLoader.java">PluginClassLoader</a>
 * for each plugin. This class loader is used to load the plugin main class, which results in it being used
 * for all plugin classes. There is currently no way to change this behaviour.
 * <p>
 * However, this particular class loader uses an internal cache (a map of class names to class objects)
 * that keeps track of already loaded classes. On each load request, a lookup on the cache is performed
 * first to return the previously loaded class if one exists.
 * <p>
 * This implementation detail is exploited by this class. The classes of interest are read,
 * transformed and defined manually and then put into the plugin's class loader's cache as if
 * they had been loaded by that class loader. For more info on the exact procedure,
 * see the implementation notes of {@link #loadNmsDependents(Class)}.
 *
 * @see #loadNmsDependents(Class)
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
@Beta
public final class NmsClassLoader {

  private static final boolean classProcessingAvailable;
  private static final MethodHandle defineClass;

  static {
    boolean exists;
    LOGGER.fine("Checking if class processing for legacy support is available...");
    try {
      Bukkit.getServer().getUnsafe().getClass().getDeclaredMethod("processClass", PluginDescriptionFile.class, String.class, byte[].class);
      exists = true;
      LOGGER.fine("Class processing is available.");
    } catch (NoSuchMethodException e) {
      exists = false;
      LOGGER.fine("Class processing is not available.");
    }
    classProcessingAvailable = exists;
    try {
      Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
      defineClassMethod.setAccessible(true);
      defineClass = MethodHandles.lookup().unreflect(defineClassMethod);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("defineClass method could not be found", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Could not access defineClass method.", e);
    }
  }

  private NmsClassLoader() {}

  /**
   * Finds and loads all classes annotated with {@link NmsDependent @NmsDependent}
   * in the jar of the given plugin. Applies compatre's transformations to them.
   *
   * @param plugin Your plugin class. Must have been loaded by a {@code PluginClassLoader}.
   *
   * @apiNote You should call this method only once per plugin being loaded.
   * Optimally, you run it in a static initialiser of your plugin main.
   * That way you ensure that the nms dependencies are respected for every class (except the plugin main itself).
   *
   * @implNote The procedure of this method is as follows:
   * <ol>
   *   <li>Via reflection, get the class cache, plugin description and plugin file from the plugin class loader</li>
   *   <li>Scan the plugin jar for .class files</li>
   *   <li>
   *     Check if the class is nms dependent
   *     <ul>
   *       <li>
   *         If so:
   *         <ol>
   *           <li>Apply Spigot's legacy transformations if applicable</li>
   *           <li>Apply compatre's transformations</li>
   *           <li>Define the {@code java.lang.Class} object using the plugin class loader</li>
   *           <li>Put it in the plugin class loader's cache</li>
   *         </ol>
   *       </li>
   *       <li>Otherwise, ignore the class and leave it to regular class loading.</li>
   *     </ul>
   *   </li>
   * </ol>
   */
  public static void loadNmsDependents(Class<? extends JavaPlugin> plugin) {
    LOGGER.info("Loading NMS depent classes of plugin " + plugin);
    ClassLoader pluginClassLoader = plugin.getClassLoader();
    LOGGER.fine("Plugin " + plugin + " uses class loader " + pluginClassLoader);
    if (!"org.bukkit.plugin.java.PluginClassLoader".equals(pluginClassLoader.getClass().getName())) {
      throw new AssertionError("Plugin class was not loaded using a PluginClassLoader");
    }
    try {
      Map<String, Class<?>> classes = get(pluginClassLoader, "classes");
      PluginDescriptionFile description = get(pluginClassLoader, "description");
      LOGGER.fine("Processing plugin jar file");
      File jarFile = get(pluginClassLoader, "file");
      JarFile pluginJar = new JarFile(jarFile);
      pluginJar.stream()
          .filter((entry) -> entry.getName().endsWith(".class"))
          .map((entry) -> loadOrIgnoreClass(pluginClassLoader, description, pluginJar, entry))
          .filter(Objects::nonNull)
          .forEach((loaded) -> classes.put(loaded.getName(), loaded));
    } catch (NoSuchFieldException e) {
      throw new AssertionError("classes field does not exist", e);
    } catch (IllegalAccessException e) {
      throw new AssertionError("field was inaccessible after access had been enabled", e);
    } catch (IOException e) {
      throw new RuntimeException("An I/O exception occurred while trying to access plugin jar file", e);
    }
  }

  private static <T> T get(Object instance, String fieldName)
      throws IllegalAccessException, NoSuchFieldException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return (T) field.get(instance);
  }

  private static Class<?> loadOrIgnoreClass(ClassLoader pluginClassLoader, PluginDescriptionFile description, JarFile pluginJar, ZipEntry entry) {
    try (InputStream classFileStream = pluginJar.getInputStream(entry)) {
      String className = entry.getName().substring(0, entry.getName().length() - ".class".length()).replace('/', '.');
      byte[] classfileBuffer = ByteStreams.toByteArray(classFileStream);
      if (NmsDependentTransformer.isNmsDependent(classfileBuffer)) {
        LOGGER.fine("Found NMS dependent class " + className);
        if (classProcessingAvailable) {
          LOGGER.fine("Applying Spigot transformations to " + className);
          classfileBuffer = Bukkit.getServer().getUnsafe().processClass(description, entry.getName(), classfileBuffer);
        }
        classfileBuffer = NmsDependentTransformer.transform(classfileBuffer);
        return (Class<?>) defineClass.invoke(pluginClassLoader, className, classfileBuffer, 0, classfileBuffer.length);
      }
    } catch (IOException e) {
      throw new RuntimeException("An IO exception occurred while reading a class file.", e);
    } catch (Throwable throwable) {
      throw new RuntimeException("Something went wrong while defining a class", throwable);
    }
    return null;
  }
}
