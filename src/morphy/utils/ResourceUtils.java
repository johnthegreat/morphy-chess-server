package morphy.utils;

import java.io.InputStream;

public class ResourceUtils {

	public static final InputStream getResourceAsInputStream(String resource) {
		final ClassLoader contextClasLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream result = null;
		result = contextClasLoader.getResourceAsStream(resource);
		// resource may be null if it is in a directory and not in jar
		// to locate in jar append "/"
		if (result == null) {
			result = contextClasLoader.getResourceAsStream("/" + resource);
		}
		return result;
	}
}
