package ToxPredictor.Utilities;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class ResourceLoader {
	/**
	 * Finds resource file and returns absolute path 
	 * @param resource - resource name
	 * @return Absolute resource path
	 */
	protected String getResourcePath(String resource)
	{
		ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource(resource);
		File file = new File(url.getFile().replaceAll("%20", " "));
		return file.getAbsolutePath();
	}
}
