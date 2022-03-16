package ToxPredictor.Application.GUI.Table.Renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.LinkedHashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ToxPredictor.Application.GUI.Table.MyTableModelHCD;
import ToxPredictor.Application.GUI.Table.MyTableModelSimilarChemical;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class CellRendererSimilarChemical extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        //Get the status for the current row.
        MyTableModelSimilarChemical tableModel = (MyTableModelSimilarChemical) table.getModel();
        
        LinkedHashMap<String, Object> map=tableModel.getPrediction(row);

        String color=(String)map.get("backgroundColor");
        
//        System.out.println(color);
        
        l.setBackground(new Color( Integer.parseInt( color.replace("#", ""), 16 )));
        l.setToolTipText(getColor(Double.parseDouble((String)map.get("Similarity"))));
        
        return l;
    }
    
    private String getColor(double SCi) {
    	if (SCi >= 0.9) {
			return "Green indicates similarity ≥ 0.9"; 
		} else if (SCi < 0.9 && SCi >= 0.8) {
			// color=Color.blue;
			return "Blue indicates 0.8 ≤ similarity < 0.9";
		} else if (SCi < 0.8 && SCi >= 0.7) {
			return "Yellow indicates 0.7 ≤ similarity < 0.8";
		} else if (SCi < 0.7 && SCi >= 0.6) {
			return "Orange indicates 0.6 ≤ similarity < 0.7";
		} else if (SCi < 0.6) {
			// color=Color.red;//255,153,153
			return "Red indicates similarity < 0.6";
		} else {
			return "";
		}
    	
    }
}