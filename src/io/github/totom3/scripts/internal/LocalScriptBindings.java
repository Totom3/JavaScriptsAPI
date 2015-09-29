package io.github.totom3.scripts.internal;

import java.util.Map;
import javax.script.SimpleBindings;

/**
 *
 * @author Totom3
 */
public class LocalScriptBindings extends SimpleBindings {

    private final JSContext context;

    public LocalScriptBindings(JSContext context) {
	this.context = context;
	super.put("world", context.getWorld());
    }

    public JSContext getContext() {
	return context;
    }

    @Override
    public Object remove(Object key) {
	return super.remove(checkKey(key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
	for (Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
	    put(checkKey(entry.getKey()), entry.getValue());
	}
    }

    @Override
    public Object put(String name, Object value) {
	return super.put(checkKey(name), value);
    }

    private String checkKey(Object key) {
	if ("world".equals(key)) {
	    throw new IllegalArgumentException("cannot modify 'world' property");
	}
	return (String) key;
    }
}
