package ToxPredictor.Application.GUI.Table.Renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ToxPredictor.Application.GUI.Table.MyTableModelHCD;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class CellRendererHCD extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        //Get the status for the current row.
        MyTableModelHCD tableModel = (MyTableModelHCD) table.getModel();
        
        Chemical chemical=tableModel.getChemical(row);
        
        Score score=chemical.scores.get(col-2);
        
        Font font=l.getFont();
        
        if (score.records.size()>0) {
//        	System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+score.final_score_source+"\t"+score.records.get(0).getAuthorityWeight());
        	
        	if (score.records.get(0).getListType().contentEquals(ScoreRecord.typeAuthoritative)) {
        		l.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
        	} else if (score.records.get(0).getListType().contentEquals(ScoreRecord.typePredicted)) {
        		l.setFont(new Font(font.getFontName(), Font.ITALIC, font.getSize()));
        	}
        }
        

        if (score.final_score.contentEquals("VH")) l.setBackground(Color.red);
        else if (score.final_score.contentEquals("H")) l.setBackground(Color.orange);
        else if (score.final_score.contentEquals("M")) l.setBackground(Color.yellow);
        else if (score.final_score.contentEquals("L")) l.setBackground(Color.green);
        else if (score.final_score.contentEquals("I")) l.setBackground(Color.lightGray);
        else l.setBackground(Color.white);

        //Return the JLabel which renders the cell.
        return l;
    }
}