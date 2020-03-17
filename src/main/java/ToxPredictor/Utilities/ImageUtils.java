package ToxPredictor.Utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Base64;

public class ImageUtils {
	public static String dataUriFromImageFile(String imagePath) throws IOException
	{
		Path path = Paths.get(imagePath);
		byte[] data = Files.readAllBytes(path);
		String base64bytes = Base64.encodeBase64String(data);
		String src = "data:image/png;base64," + base64bytes;
		return src;
	}
}
