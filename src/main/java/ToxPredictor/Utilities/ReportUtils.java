package ToxPredictor.Utilities;

import ToxPredictor.Application.ReportOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Base64;

public class ReportUtils {
	public static String getImageSrc(ReportOptions options, String... parts)
	{
		if ( !options.embedImages )
			return StringUtils.join(parts, '/');
		else
			return Paths.get(options.reportBase, parts).toString();
	}
	
	public static String convertImageToBase64(String url) {
		String imgURL=null;
		try {
			String base64 = null;
			
			BufferedInputStream bis = new BufferedInputStream(new URL(url).openConnection().getInputStream());
			byte[] imageBytes = IOUtils.toByteArray(bis);
			base64 = Base64.getEncoder().encodeToString(imageBytes);
			
			//need to add this or img url won't work (TMM):
			imgURL="data:image/png;base64, "+base64;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return imgURL;
	}
}
