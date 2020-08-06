# Compatre
Compatre is a very small and fast tool to assist with version-dependent Bukkit programming. 
It makes "version-checkers" and multiple implementations for different versions (but the same functionality)
redundant by replacing the required nms/craftbukkit types at runtime.

**Features include:**
- A java agent to dynamically replace types (no boilerplate)
- A custom ClassLoader as an alternative to the agent (very little boilerplate)
- A library to support cases where more than just a type replacement is needed *coming soon...*

## Dependency
### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.johnnyjayjay</groupId>
        <artifactId>compatre</artifactId>
        <version>0.2.0-alpha</version>
    </dependency>
</dependencies>
```
### Gradle
```groovy
repositories {
    maven {
        url("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.johnnyjayjay:compatre:0.2.0-alpha")
}
```

## Note
This project is still in early development/alpha. This mainly needs testing now and breaking 
changes may occur at any time. Please leave requests, ideas and bug reports in the 
[issues section](https://github.com/johnnyjayjay/compatre/issues) of this repository.

## Agent Setup
Setting up the agent is fairly easy. Head over to the [releases](/releases) and download 
the latest `all` jar file from there. Now place the file somewhere near your server jar, e.g. in the `lib` 
directory of your Minecraft server directory.

Now add the the agent to your JVM arguments when starting the server:
```batch
java -jar -javaagent:"./lib/compatre.jar" "spigot-1.12.2.jar"
```
(Replace the `javaagent` path and name as well as the server jar with your equivalents)

## Usage (Code)
The usage of compatre in your code differs depending on what parts of the library you use.
### Agent
The agent has the simplest usage. Since it is defined in the JVM arguments, you don't have 
to write any additional code. Just annotate classes that depend on version specific nms / craftbukkit
types with `@NmsDependent`:
```java
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.johnnyjayjay.compatre.NmsDependent; // the annotation

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer; // <-- Version specific - usually wouldn't work on any other version

@NmsDependent // makes the agent replace v1_12_R1 CraftPlayer with the correct CraftPlayer at runtime
public class VersionIndependentCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      CraftPlayer player = (CraftPlayer) sender;
      // do something with it
    } 
    return true;
  }
}
```

### ClassLoader
Just like with the agent, you simply need to annotate version dependent classes as `@NmsDependent`. 
You just need to do one more thing though: manually load these classes.
That's much simpler than it sounds; just modify your plugin class like this:
```java
import org.bukkit.plugin.JavaPlugin;
import com.github.johnnyjayjay.compatre.NmsClassLoader;

public class MyPlugin extends JavaPlugin {
  // this will only run once when MyPlugin is loaded
  static {
    NmsClassLoader.loadNmsDependents(MyPlugin.class); // loads all nms dependents of this plugin
  }
  
  // etc.
}
```
Note that there is no way to modify the class this method is invoked in. So `MyPlugin` here could 
not depend on version specific classes.

