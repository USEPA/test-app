package ToxPredictor.Utilities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FormatUtils {
	static DecimalFormat d3 = new DecimalFormat("0.000");
    static DecimalFormat d2 = new DecimalFormat("0.00");
    static DecimalFormat d1 = new DecimalFormat("0.0");
	static DecimalFormat ds = new DecimalFormat("0.0##E0#");
	
	
	public static String setSignificantDigits(Double value, int significantDigits) {
	    if (significantDigits < 0) throw new IllegalArgumentException();
	    
	    
	    if(value==null) return "N/A";

	    // this is more precise than simply doing "new BigDecimal(value);"
	    BigDecimal bd = new BigDecimal(value, MathContext.DECIMAL64);
	    bd = bd.round(new MathContext(significantDigits, RoundingMode.HALF_UP));
	    final int precision = bd.precision();
	    if (precision < significantDigits)
	    bd = bd.setScale(bd.scale() + (significantDigits-precision));
	    return bd.toPlainString();
	}    

	
	
	
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
