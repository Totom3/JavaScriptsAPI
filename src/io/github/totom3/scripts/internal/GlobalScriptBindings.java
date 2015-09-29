package io.github.totom3.scripts.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import jdk.internal.dynalink.beans.StaticClass;

/**
 *
 * @author Totom3
 */
public class GlobalScriptBindings implements Bindings {

    private static GlobalScriptBindings instance;

    public static GlobalScriptBindings get() {
	if (instance == null) {
	    instance = new GlobalScriptBindings();
	}
	return instance;
    }

    private final Map<String, Object> map;

    private GlobalScriptBindings() {
	this.map = new HashMap<>();
    }

    private String checkKey(Object key) {
	if (key == null) {
	    throw new NullPointerException("key cannot be null");
	}

	if (!(key instanceof String)) {
	    throw new ClassCastException("Expected String, got instead " + key.getClass().getName());
	}

	String str = (String) key;
	if ((str).isEmpty()) {
	    throw new IllegalArgumentException("key cannot be empty");
	}

	return str;
    }

    @Override
    public Object put(String name, Object value) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
	String str = checkKey(key);
	return get(str) != null || map.containsKey(str);
    }

    @Override
    public Object get(Object key) {
	String str = checkKey(key);

	Object value = map.get(str);
	if (value != null) {
	    return value;
	}

	Class<?> clazz = ScriptImports.loadClass(str);
	if (clazz == null) {
	    return null;
	}

	StaticClass staticClass = StaticClass.forClass(clazz);
	map.put(str, staticClass);
	return staticClass;
    }

    @Override
    public Object remove(Object key) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
	return map.size();
    }

    @Override
    public boolean isEmpty() {
	return map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
	return map.keySet();
    }

    @Override
    public Collection<Object> values() {
	return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
	return map.entrySet();
    }

}
