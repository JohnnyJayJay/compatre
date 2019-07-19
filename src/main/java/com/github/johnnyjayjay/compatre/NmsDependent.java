package com.github.johnnyjayjay.compatre;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Types annotated with this annotation will be recognised as version dependent types
 * by Compatre and will be adjusted at runtime if the Compatre java agent is used or the
 * type is loaded using {@link NmsClassLoader}.
 * This means that every {@code net.minecraft.server} or {@code org.bukkit.craftbukkit}
 * type descriptor found in the class will be replaced with its correct version just before
 * the class is loaded.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface NmsDependent {}
