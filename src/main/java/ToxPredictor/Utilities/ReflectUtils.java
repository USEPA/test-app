package ToxPredictor.Utilities;

import java.lang.reflect.Method;

import ToxPredictor.Application.WebTEST;

public class ReflectUtils {
	/**
	 * Utility function to get the calling method name
	 * 
	 * @return calling method name
	 */
	public static String getMethodName()
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[2].getMethodName();
	}
	
}
