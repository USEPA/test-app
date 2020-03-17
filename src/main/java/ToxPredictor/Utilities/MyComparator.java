package ToxPredictor.Utilities;

import java.util.Comparator;

/** 
 * Allows one to sort objects like hashtables in descending order
 * @author TMARTI02
 *
 */
public class MyComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {

		double d1 = (Double) o1;
		double d2 = (Double) o2;
		if (d1 > d2)
			return -1;
		else if (d1 < d2)
			return 1;
		else
			return 0;

	}

}


