package com.github.johnnyjayjay.compatre;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * A class that allows you to apply Compatre's transformations to your nms dependent classes
 * without a java agent.
 * <p>
 * The java agent is the recommended way to use compatre, as this class uses many
 * unsafe hacks and workarounds to make it work. Unless Spigot introduces an API for custom
 * class loading, this class will thus remain unstable.
 * <p>
 * While this class implements {@code ClassLoader}, it is not meant to be used like one.
 * In fact, this inheritance only exists to provide access to
 * {@link ClassLoader#defineClass(String, byte[], int, int) class definition} for itself.
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
public final class NmsClassLoader extends ClassLoader {

  private static final boolean classProcessingAvailable;

  static {
    boolean exists;
    try {
      Bukkit.getServer().getUnsafe().getClass().getDeclaredMethod("processClass", PluginDescriptionFile.class, String.class, byte[].class);
      exists = true;
    } catch (NoSuchMethodException e) {
      exists = false;
    }
    classProcessingAvailable = exists;
  }

  private static final NmsClassLoader LOADER = new NmsClassLoader();

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
   *           <li>Define the {@code java.lang.Class} object</li>
   *           <li>Put it in the plugin class loader's cache</li>
   *         </ol>
   *       </li>
   *       <li>Otherwise, ignore the class and leave it to regular class loading.</li>
   *     </ul>
   *   </li>
   * </ol>
   */
  public static void loadNmsDependents(Class<? extends JavaPlugin> plugin) {
    ClassLoader pluginClassLoader = plugin.getClassLoader();
    if (!"org.bukkit.plugin.java.PluginClassLoader".equals(pluginClassLoader.getClass().getName())) {
      throw new AssertionError("Plugin class was not loaded using a PluginClassLoader");
    }
    try {
      Map<String, Class<?>> classes = get(pluginClassLoader, "classes");
      PluginDescriptionFile description = get(pluginClassLoader, "description");
      File jarFile = get(pluginClassLoader, "file");
      JarFile pluginJar = new JarFile(jarFile);
      pluginJar.stream()
          .filter((entry) -> entry.getName().endsWith(".class"))
          .map((entry) -> loadOrIgnoreClass(description, pluginJar, entry))
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

  private static Class<?> loadOrIgnoreClass(PluginDescriptionFile description, JarFile pluginJar, ZipEntry entry) {
    try {
      String className = entry.getName().substring(0, entry.getName().length() - ".class".length()).replace('/', '.');
      byte[] classfileBuffer = ByteStreams.toByteArray(pluginJar.getInputStream(entry));
      if (NmsDependentTransformer.isNmsDependent(classfileBuffer)) {
        if (classProcessingAvailable) {
          classfileBuffer = Bukkit.getServer().getUnsafe().processClass(description, entry.getName(), classfileBuffer);
        }
        classfileBuffer = NmsDependentTransformer.transform(classfileBuffer);
        return LOADER.defineClass(className, classfileBuffer, 0, classfileBuffer.length);
      }
    } catch (IOException e) {
      throw new RuntimeException("An IO exception occurred while reading a class file.", e);
    }
    return null;
  }
}
