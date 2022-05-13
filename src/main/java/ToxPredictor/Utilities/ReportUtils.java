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
}
