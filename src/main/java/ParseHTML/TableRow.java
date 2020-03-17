package ParseHTML;

import java.util.Iterator;
import java.util.Vector;

public class TableRow {
	private Vector<TableCell>cells=new Vector<TableCell>();
	
	public Vector<TableCell>getCells() {
		return cells;
	}
	
	public String toString() {
		
		String str="";
		
		Iterator<TableCell>itCells=this.getCells().iterator();
		
		while (itCells.hasNext()) {
			
			TableCell tc=itCells.next();
			
//			System.out.print(tc.value);
			str+=tc.getValue();
			
			if (itCells.hasNext()) {
//				System.out.print("\t");
				str+="\t";
			}
		}
		return str;
	}
	
}

