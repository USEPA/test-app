package ToxPredictor.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MemUtils {
	private static final Logger logger = LogManager.getLogger(MemUtils.class);
	private static final int mb = 1024 * 1024;
	
	public static void printMemoryUsage(String message) {
		Runtime runtime = Runtime.getRuntime();
		logger.info(message);
		logger.info("Used Memory:  {}MB", (runtime.totalMemory() - runtime.freeMemory()) / mb);
		logger.info("Free Memory:  {}MB", runtime.freeMemory() / mb);
		logger.info("Total Memory: {}MB", runtime.totalMemory() / mb);
		logger.info("Max Memory:   {}MB", runtime.maxMemory() / mb);
	}
}
