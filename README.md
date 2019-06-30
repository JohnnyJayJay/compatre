# Compatre
Compatre is a very small and fast library to assist with version-dependent Bukkit programming. 
It makes "version-checkers" and multiple implementations for different versions (but the same functionality)
redundant by replacing the required nms/craftbukkit types at runtime.

**Features include:**
- A java agent to dynamically replace types (no boilerplate)
- A custom ClassLoader as an alternative to the agent (very little boilerplate) *coming soon...*
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
        <version>master-SNAPSHOT</version>
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
    implementation("com.github.johnnyjayjay:compatre:master-SNAPSHOT")
}
```

## Note
This project is still in early development/alpha. This mainly needs testing now and breaking 
changes may occur at any time. Please leave requests, ideas and bug reports in the 
[issues section](https://github.com/johnnyjayjay/compatre/issues) of this repository.

## Agent Setup
Setting up the agent is fairly easy. Head over to the [agent builds](./agent-build) and download 
the latest jar file from there. Now place the file somewhere near your server jar, e.g. in the `lib` 
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
  }
}
```

