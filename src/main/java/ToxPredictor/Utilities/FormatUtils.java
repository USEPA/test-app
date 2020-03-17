package ToxPredictor.Utilities;

import java.text.DecimalFormat;

public class FormatUtils {
	static DecimalFormat d3 = new DecimalFormat("0.000");
    static DecimalFormat d2 = new DecimalFormat("0.00");
    static DecimalFormat d1 = new DecimalFormat("0.0");
	static DecimalFormat ds = new DecimalFormat("0.0##E0#");
	
	static public String asString(Double f)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 ) {
			return "N/A";
		} else if (Math.abs(f) < 0.001) {
		    ds.setMaximumFractionDigits(3);
			return ds.format(f).replace("E", "*10^");
		} else {
			return d3.format(f);
		}
	}
	
	static public String toD3(Double f)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 )
			return "N/A";
		else
			return d3.format(f);
	}
	
	static public String toD2(Double f)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 )
			return "N/A";
		else
			return d2.format(f);
	}
	
	static public String toD1(Double f)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 )
			return "N/A";
		else
			return d1.format(f);
	}
	
	static public String toDecimalString(Double f, String decimalFormat)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 )
			return "N/A";
		else {
			DecimalFormat df = new DecimalFormat(decimalFormat);
			return df.format(f);
		}
	}
	
	static public Boolean isMaterial(Double f)
	{
		if ( f == null || Double.isNaN(f) || Math.abs(f - (-9999.000)) < 0.001 )
			return false;
		else
			return true;
	}
}
