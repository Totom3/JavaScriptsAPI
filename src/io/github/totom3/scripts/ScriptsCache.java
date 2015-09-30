package io.github.totom3.scripts;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.totom3.commons.misc.DataCache;
import io.github.totom3.scripts.internal.JSEngine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 *
 * @author Totom3
 */
public class ScriptsCache extends DataCache<String, Script> {

    public static final File BASE_DIR = new File("./plugins/Scripts");

    static {
	if (!BASE_DIR.isDirectory()) {
	    BASE_DIR.mkdirs();
	}
    }
    
    @Override
    protected LoadingCache<String, Script> makeCache() {
	return CacheBuilder.<String, Script>newBuilder()
		.softValues()
		.build(new ScriptsLoader());
    }

    static class ScriptsLoader extends CacheLoader<String, Script> {

	ScriptsLoader() {
	}

	@Override
	public Script load(String name) throws Exception {
	    String fileName = name.replace('.', File.separatorChar).concat(".js");

	    File file = new File(BASE_DIR, fileName);
	    if (!file.isFile()) {
		throw new FileNotFoundException("missing file for script '" + name + "'");
	    }

	    // Check case
	    String fileName2 = file.getCanonicalFile().getPath();
	    if (!fileName2.contains(fileName)) {
		throw new FileNotFoundException("missing file for script '" + name + "'");
	    }

	    try (FileReader reader = new FileReader(file)) {
		return Script.create(name, file, JSEngine.compile(reader));
	    }
	}
    }

}
