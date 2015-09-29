package io.github.totom3.commons.misc;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Totom3
 * @param <K>
 * @param <V>
 */
public abstract class DataCache<K, V> {
    private static final Logger LOGGER = Logger.getLogger(DataCache.class.getName());

    protected final LoadingCache<K, V> cache;

    public DataCache() {
	cache = makeCache();
    }

    public V getLoaded(K key) {
	return cache.getIfPresent(key);
    }

    public V getOrLoad(K key) throws ExecutionException {
	V element = cache.getIfPresent(key);
	if (element == null) {
	    element = cache.get(key);
	    onLoad(key, element);
	}
	return element;
    }

    public V getOrLoadUnchecked(K key) throws UncheckedExecutionException {
	V element = cache.getIfPresent(key);
	if (element == null) {
	    element = cache.getUnchecked(key);
	    onLoad(key, element);
	}
	return element;
    }

    public V tryGetOrLoad(K key) {
	try {
	    return getOrLoad(key);
	} catch (ExecutionException ex) {
	    LOGGER.log(Level.SEVERE, "Could not load value for key " + ex, ex);
	    return null;
	}
    }

    public void set(K key, V value) {
	if (value == null) {
	    onRemove(key, null);
	    cache.invalidate(key);
	} else {
	    onSet(key, value);
	    cache.put(key, value);
	}
    }

    public V unload(K key) {
	ConcurrentMap<K, V> map = cache.asMap();

	V element = map.get(key);
	onRemove(key, element);
	map.remove(key);

	return element;
    }

    public void clear() {
	for (Iterator<Entry<K, V>> it = cache.asMap().entrySet().iterator(); it.hasNext();) {
	    Entry<K, V> entry = it.next();

	    K key = entry.getKey();
	    V value = entry.getValue();

	    onRemove(key, value);
	    it.remove();
	}
    }

    public int size() {
	return (int) cache.size();
    }

    public boolean isLoaded(K key) {
	return cache.getIfPresent(key) != null;
    }

    public Map<K, V> all() {
	return cache.asMap();
    }

    protected abstract LoadingCache<K, V> makeCache();

    /**
     * Called when an element is loaded in this {@code DataCache}. It is not
     * possible to cancel the load without manually invoking
     * {@code cache.invalidate(key)}. The default implementation calls
     * {@link #onSet(K, Object)}.
     * <p>
     * @param key
     * @param element
     */
    protected void onLoad(K key, V element) {
	onSet(key, element);
    }

    /**
     * Called when an element is manually inserted in this {@code DataCache}, by
     * invoking {@link #set(K, Object)}. Since this method is called
     * <em>before</em> the changes actually take place, it is possible to
     * directly cancel them by throwing an exception.
     * <p>
     * @param key
     * @param element
     */
    protected void onSet(K key, V element) {
    }

    /**
     * Called when an element is removed from this {@code DataCache}. Since this
     * method is called <em>before</em> the changes actually take place, it is
     * possible to directly cancel them by throwing an exception.
     * <p>
     * @param key
     * @param element
     */
    protected void onRemove(K key, V element) {
    }
}
