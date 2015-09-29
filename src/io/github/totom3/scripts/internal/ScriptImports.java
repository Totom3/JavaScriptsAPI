package io.github.totom3.scripts.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import io.github.totom3.scripts.ScriptsMain;
import io.github.totom3.scripts.internal.javascript.JavaEngine;
import io.github.totom3.scripts.internal.javascript.JavaEvents;
import io.github.totom3.scripts.internal.javascript.JavaSchedule;
import io.github.totom3.scripts.internal.javascript.JavaUnschedule;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;

/**
 *
 * @author Totom3
 */
public class ScriptImports {

    private static Set<String> rawPackages;
    private static Map<String, ImportInfo> rawClasses;
    private static boolean initiated;

    public static void initClasses() throws IOException {
	if (initiated) {
	    throw new IllegalStateException("classes already initiated");
	}

	Map<String, ImportInfo> map = new HashMap<>(100);

	ClassLoader mythicubeLoader = ScriptsMain.class.getClassLoader();
	ClassLoader bukkitLoader = Bukkit.class.getClassLoader();

	// Bukkit API classes
	for (ClassInfo info : ClassPath.from(bukkitLoader).getTopLevelClasses()) {
	    if (!rawPackages.contains(info.getPackageName())) {
		continue;
	    }

	    map.put(info.getSimpleName(), new GuavaImportInfo(info));
	}

	// Mythicube API classes
	if (mythicubeLoader != bukkitLoader) {
	    for (ClassInfo info : ClassPath.from(mythicubeLoader).getTopLevelClasses()) {
		if (!rawPackages.contains(info.getPackageName())) {
		    continue;
		}

		map.put(info.getSimpleName(), new GuavaImportInfo(info));
	    }
	}

	map.put("Events", new LoadedImportInfo(JavaEvents.class));
	map.put("JSEngine", new LoadedImportInfo(JavaEngine.class));
	map.put("Schedule", new LoadedImportInfo(JavaSchedule.class));
	map.put("Unschedule", new LoadedImportInfo(JavaUnschedule.class));

	rawClasses = map;
	initiated = true;
    }

    public static void initPackages() {
	Set<String> set = new HashSet<>(16);

	set.add("org.bukkit");
	set.add("org.bukkit.block");
	set.add("org.bukkit.entity");
	set.add("org.bukkit.inventory");
	set.add("org.bukkit.metadata");
	set.add("org.bukkit.scoreboard");
	set.add("org.bukkit.util");

	set.add("org.bukkit.event");
	set.add("org.bukkit.event.block");
	set.add("org.bukkit.event.entity");
	set.add("org.bukkit.event.enchantment");
	set.add("org.bukkit.event.hanging");
	set.add("org.bukkit.event.player");
	set.add("org.bukkit.event.inventory");
	set.add("org.bukkit.event.painting");
	set.add("org.bukkit.event.server");
	set.add("org.bukkit.event.vehicle");
	set.add("org.bukkit.event.weather");
	set.add("org.bukkit.event.world");

	/*
	 set.add("net.mythicube.combat");
	 set.add("net.mythicube.dialogue");
	 set.add("net.mythicube.doors");
	 set.add("net.mythicube.items");
	 set.add("net.mythicube.npc");
	 set.add("net.mythicube.playerdata");
	 set.add("net.mythicube.quest");
	 set.add("net.mythicube.skills");
	 */
	rawPackages = set;
    }

    // ------------===[ Editing Methods ]===------------
    public static void importClass(Class<?> clazz) {
	importClass(clazz.getSimpleName(), clazz);
    }

    public static void importClass(String name, Class<?> clazz) {
	ImportInfo oldImport = rawClasses.get(name);
	if (oldImport != null) {
	    throw new IllegalArgumentException("Class '" + name + "' is already imported: " + oldImport.getName());
	}
	rawClasses.put(name, new LoadedImportInfo(clazz));
    }

    public static void importPackage(Package pkg) {
	rawPackages.add(pkg.getName());
    }

    public static void clear() {
	rawClasses.clear();
	rawPackages.clear();
	rawClasses = null;
	rawPackages = null;
	initiated = false;
    }

    // ------------===[ Check Methods ]===------------
    public static Set<String> getRawPackages() {
	return Collections.unmodifiableSet(rawPackages);
    }

    public static Map<String, ImportInfo> getRawClasses() {
	checkInit();
	return Collections.unmodifiableMap(rawClasses);
    }

    public static boolean containsPackage(String pkg) {
	return rawPackages.contains(pkg);
    }

    public static boolean containsClass(String simpleClassName) {
	checkInit();
	return rawClasses.containsKey(simpleClassName);
    }

    public static boolean containsClass(Class<?> clazz) {
	checkInit();
	ImportInfo info = rawClasses.get(clazz.getSimpleName());
	return info != null && clazz.getName().equals(info.getName());
    }

    public static String getFullClassName(String simpleName) {
	checkInit();
	ImportInfo info = rawClasses.get(simpleName);
	return (info != null) ? info.getName() : null;
    }

    public static Class<?> loadClass(String simpleName) {
	checkInit();
	ImportInfo info = rawClasses.get(simpleName);
	return (info != null) ? info.loadClass() : null;
    }

    private static void checkInit() {
	if (!initiated) {
	    throw new IllegalStateException("classes not initiated");
	}
    }

    private ScriptImports() {
    }

    public static interface ImportInfo {

	String getPackageName();

	String getSimpleName();

	String getName();

	Class<?> loadClass();

    }

    private static class LoadedImportInfo implements ImportInfo {

	final Class<?> clazz;

	LoadedImportInfo(Class<?> clazz) {
	    this.clazz = checkNotNull(clazz);
	}

	@Override
	public String getPackageName() {
	    return clazz.getPackage().getName();
	}

	@Override
	public String getSimpleName() {
	    return clazz.getSimpleName();
	}

	@Override
	public String getName() {
	    return clazz.getName();
	}

	@Override
	public Class<?> loadClass() {
	    return clazz;
	}

    }

    private static class GuavaImportInfo implements ImportInfo {

	final ClassInfo info;

	GuavaImportInfo(ClassInfo info) {
	    this.info = info;
	}

	@Override
	public String getPackageName() {
	    return info.getPackageName();
	}

	@Override
	public String getSimpleName() {
	    return info.getSimpleName();
	}

	@Override
	public String getName() {
	    return info.getName();
	}

	@Override
	public Class<?> loadClass() {
	    return info.load();
	}
    }
}
