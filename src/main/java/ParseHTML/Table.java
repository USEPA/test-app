package ParseHTML;

import java.util.Iterator;
import java.util.Vector;

public class Table {

	
	private Vector<TableRow>rows=new Vector<TableRow>();
	
	public Vector<TableRow>getRows() {
		return rows;
	}
	
	
	public String toString()  {
		
		Iterator<TableRow> itRows=rows.iterator();
		
		String str="";
		while (itRows.hasNext()) {
			TableRow tr=itRows.next();
			str+=tr.toString()+"\r\n";
//			System.out.print("\r\n");
		}
		return str;
	}
	
	
	static String [] getBetweenTags(String s,String fieldname) {
		
		String openTag="<"+fieldname;
		String closeTag="</"+fieldname;
		
		
//		System.out.println("s="+s);
		
		String s2=s.substring(s.indexOf(openTag)+openTag.length(),s.length());
		
//		System.out.println("s2a="+s2);
		
		s2=s2.substring(s2.indexOf(">")+1,s2.length());				
		
//		System.out.println("s2b="+s2);
		
		s=s2.substring(s2.indexOf(closeTag)+closeTag.length(),s2.length());//save rest of string to original string
		s=s.substring(s.indexOf(">")+1,s.length());
		
//		System.out.println(s);
		
		s2=s2.substring(0,s2.indexOf(closeTag));
		
		String [] result= {s2,s};//s2 is data between tags, s is the remainder of string after closing tag
		
//		System.out.println(s2);
//		System.out.println(s);
		
		return result;
		
	}
	
	static String trimBeginEndTags(String s,String fieldname) {
		String s1="<"+fieldname;
		s=s.substring(s.indexOf(s1)+s1.length(),s.length());
		s=s.substring(s.indexOf(">")+1,s.length());
		s=s.substring(0,s.indexOf("</"+fieldname));
		return s;
	}
	
	static TableRow parseRow(String row) {
		TableRow tr=new TableRow();
		
//		System.out.println(row);
		
		while (row.indexOf("<th")>-1 || row.indexOf("<td")>-1) {
			String [] result=getBetweenTags(row, "t");
			String val=result[0].trim();
			row=result[1];

			TableCell tc=new TableCell();
			tc.setValue(val);
			tr.getCells().add(tc);
			
		}
		
		
		return tr;
	}
	
	
	public static Table parse(String s) {
		
		Table table=new Table();
		
		s=s.replace("\r", "").replace("\n", "").replace("  ", "").replace("> ", ">").replace(" <","<");
		
		
//		System.out.println(s);
		
		//kill off opening and closing table tags:
//		s=trimBeginEndTags(s,"table");

		//get code inside tbody tag:
		if (s.indexOf("<tbody")>-1) {
			String []result=getBetweenTags(s, "tbody");
			s=result[0];
		}
		
		
//		System.out.println(s);
		
		while (s.indexOf("<tr")>-1) {
			
			//if they screw up and miss having <tr> tag:
			if (s.indexOf("<th")==0 || s.indexOf("<td")==0) {
//				System.out.println("here 2 s="+s);
				String s2=s.substring(0, s.indexOf("<tr"));
				TableRow tr=parseRow(s2);
				table.rows.add(tr);
				
				s=s.substring(s.indexOf("<tr"),s.length());
//				System.out.println(s);
				continue;
			}
			
//			System.out.println("here s="+s);
			
			String []result=getBetweenTags(s, "tr");
			String strRow=result[0];
			s=result[1];
			
			TableRow tr=parseRow(strRow);
			table.rows.add(tr);
			
		}
		
		return table;
		
	}
	
	static void parseTable1() {
		String strTable="<table>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        Cas Number:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        766-09-6\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <th>\r\n" + 
				"                    Synonyms:\r\n" + 
				"                </th>\r\n" + 
				"                <td>\r\n" + 
				"                    1-Ethylpiperidine\r\n" + 
				"                </td>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        Molecular Weight:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        113.20\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        Relative Density:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        Water Solubility (mg/l):\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        34600\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        Approval Number:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        HSR001045\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        UN Class:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        Class 3.0;Sub Risk 8; PG II\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>\r\n" + 
				"                        UN Number:\r\n" + 
				"                    </th>\r\n" + 
				"                    <td>\r\n" + 
				"                        2386\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                </table>";
		
		Table table=Table.parse(strTable);
		System.out.println(table);
		
	}
	
	static void parseTable3() {
		
		String strTable="<table class=\"results\" border=1>\r\n" + 
				"                    <thead>\r\n" + 
				"                        <tr>\r\n" + 
				"                            <th colspan=\"2\">\r\n" + 
				"                                Classification Data\r\n" + 
				"                            </th>\r\n" + 
				"                        </tr>\r\n" + 
				"                    </thead>\r\n" + 
				"                    <tbody>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        3.1B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Flashpoint Value : °C <br/>Test Method :Closed cup<br/>Boiling Point :131 °C\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        8.2B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        SPECIES:\r\n" + 
				"<br>RESULT: Severe burns.\r\n" + 
				"<br>REEFERENCE SOURCE: [MSDS]\r\n" + 
				"<br>\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        8.3A\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        SPECIES:\r\n" + 
				"<br>RESULT: Can cause severe burns leading to permanent damage.\r\n" + 
				"<br>REFERENCE SOURCE: [NJHS]\r\n" + 
				"<br>\r\n" + 
				"<br>\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B (fish)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        SPECIES: Fish\r\n" + 
				"<br>TYPE OF EXPOSURE:\r\n" + 
				"<br>DURATION: 96 hr\r\n" + 
				"<br>ENDPOINT: LC50\r\n" + 
				"<br>VALUE: 36.666 mg/l\r\n" + 
				"<br>REFERENCE SOURCE: [ECOSAR]\r\n" + 
				"<br>\r\n" + 
				"<br>REMARK: Ecosar Class: Aliphatic Amines\r\n" + 
				"<br>\r\n" + 
				"<br><br><br>Bioccumulative: No<br><br><br>Rapidly Degradable: No<br>SMILES : N(CCCC1)(C1)CC\r\n" + 
				"<br>CHEM   : Piperidine, 1-ethyl-\r\n" + 
				"<br>MOL FOR: C7 H15 N1\r\n" + 
				"<br>MOL WT : 113.20\r\n" + 
				"<br>--------------------------- BIOWIN v4.00 Results ----------------------------\r\n" + 
				"<br>\r\n" + 
				"<br>    Linear Model Prediction    :  Does Not Biodegrade Fast\r\n" + 
				"<br>    Non-Linear Model Prediction:  Does Not Biodegrade Fast\r\n" + 
				"<br>    Ultimate Biodegradation Timeframe:  Weeks-Months\r\n" + 
				"<br>    Primary  Biodegradation Timeframe:  Days-Weeks\r\n" + 
				"<br>    MITI Linear Model Prediction    :  Not Readily Degradable\r\n" + 
				"<br>    MITI Non-Linear Model Prediction:  Readily Degradable\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2053 | -0.2053\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.0539\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7475\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |     LINEAR BIODEGRADATION PROBABILITY      |         |  0.4884\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -2.2229 | -2.2229\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -1.6075\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   NON-LINEAR BIODEGRADATION PROBABILITY    |         |  0.3054\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Biodegrades Fast\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> Does NOT Biodegrade Fast\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2548 | -0.2548\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.2502\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.1992\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - ULTIMATE BIODEGRADATION   |         |  2.6942\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2880 | -0.2880\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.1633\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.8477\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - PRIMARY BIODEGRADATION    |         |  3.3964\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> Result Classification:   5.00 -> hours     4.00 -> days    3.00 -> weeks\r\n" + 
				"<br>  (Primary & Ultimate)    2.00 -> months    1.00 -> longer\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.0848 | -0.0848\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0004 |  0.0004\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.0494 |  0.0494\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.0197 |  0.0986\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.3368\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7121\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |  MITI LINEAR BIODEGRADATION PROBABILITY    |         |  0.4390\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.8396 | -0.8396\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0194 |  0.0194\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.4295 |  0.4295\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.2365 |  1.1826\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -3.2681\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   | MITI NON-LINEAR BIODEGRADATION PROBABILITY |         |  0.5124\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Readily Degradable\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> NOT Readily Degradable[BIOWIN]\r\n" + 
				"<br>\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B (crustacean)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        SPECIES: Daphnid\r\n" + 
				"<br>TYPE OF EXPOSURE:\r\n" + 
				"<br>DURATION: 48 hr\r\n" + 
				"<br>ENDPOINT: LC50\r\n" + 
				"<br>VALUE: 2.667 mg/l\r\n" + 
				"<br>REFERENCE SOURCE: [ECOSAR]\r\n" + 
				"<br>\r\n" + 
				"<br>REMARK: Ecosar Class: Aliphatic Amines\r\n" + 
				"<br><br><br>Bioccumulative: No<br><br><br>Rapidly Degradable: No<br>SMILES : N(CCCC1)(C1)CC\r\n" + 
				"<br>CHEM   : Piperidine, 1-ethyl-\r\n" + 
				"<br>MOL FOR: C7 H15 N1\r\n" + 
				"<br>MOL WT : 113.20\r\n" + 
				"<br>--------------------------- BIOWIN v4.00 Results ----------------------------\r\n" + 
				"<br>\r\n" + 
				"<br>    Linear Model Prediction    :  Does Not Biodegrade Fast\r\n" + 
				"<br>    Non-Linear Model Prediction:  Does Not Biodegrade Fast\r\n" + 
				"<br>    Ultimate Biodegradation Timeframe:  Weeks-Months\r\n" + 
				"<br>    Primary  Biodegradation Timeframe:  Days-Weeks\r\n" + 
				"<br>    MITI Linear Model Prediction    :  Not Readily Degradable\r\n" + 
				"<br>    MITI Non-Linear Model Prediction:  Readily Degradable\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2053 | -0.2053\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.0539\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7475\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |     LINEAR BIODEGRADATION PROBABILITY      |         |  0.4884\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -2.2229 | -2.2229\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -1.6075\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   NON-LINEAR BIODEGRADATION PROBABILITY    |         |  0.3054\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Biodegrades Fast\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> Does NOT Biodegrade Fast\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2548 | -0.2548\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.2502\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.1992\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - ULTIMATE BIODEGRADATION   |         |  2.6942\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2880 | -0.2880\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.1633\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.8477\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - PRIMARY BIODEGRADATION    |         |  3.3964\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> Result Classification:   5.00 -> hours     4.00 -> days    3.00 -> weeks\r\n" + 
				"<br>  (Primary & Ultimate)    2.00 -> months    1.00 -> longer\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.0848 | -0.0848\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0004 |  0.0004\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.0494 |  0.0494\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.0197 |  0.0986\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.3368\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7121\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |  MITI LINEAR BIODEGRADATION PROBABILITY    |         |  0.4390\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.8396 | -0.8396\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0194 |  0.0194\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.4295 |  0.4295\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.2365 |  1.1826\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -3.2681\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   | MITI NON-LINEAR BIODEGRADATION PROBABILITY |         |  0.5124\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Readily Degradable\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> NOT Readily Degradable[BIOWIN]\r\n" + 
				"<br>\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B (algal)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        SPECIES: Green Algae\r\n" + 
				"<br>TYPE OF EXPOSURE:\r\n" + 
				"<br>DURATION: 96 hr\r\n" + 
				"<br>ENDPOINT: EC50\r\n" + 
				"<br>VALUE: 4.848 mg/l\r\n" + 
				"<br>REFERENCE SOURCE: [ECOSAR]\r\n" + 
				"<br>\r\n" + 
				"<br>REMARK: Ecosar Class: Aliphatic Amines\r\n" + 
				"<br>\r\n" + 
				"<br><br><br>Bioccumulative: No<br><br><br>Rapidly Degradable: No<br>SMILES : N(CCCC1)(C1)CC\r\n" + 
				"<br>CHEM   : Piperidine, 1-ethyl-\r\n" + 
				"<br>MOL FOR: C7 H15 N1\r\n" + 
				"<br>MOL WT : 113.20\r\n" + 
				"<br>--------------------------- BIOWIN v4.00 Results ----------------------------\r\n" + 
				"<br>\r\n" + 
				"<br>    Linear Model Prediction    :  Does Not Biodegrade Fast\r\n" + 
				"<br>    Non-Linear Model Prediction:  Does Not Biodegrade Fast\r\n" + 
				"<br>    Ultimate Biodegradation Timeframe:  Weeks-Months\r\n" + 
				"<br>    Primary  Biodegradation Timeframe:  Days-Weeks\r\n" + 
				"<br>    MITI Linear Model Prediction    :  Not Readily Degradable\r\n" + 
				"<br>    MITI Non-Linear Model Prediction:  Readily Degradable\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2053 | -0.2053\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.0539\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7475\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |     LINEAR BIODEGRADATION PROBABILITY      |         |  0.4884\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -2.2229 | -2.2229\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -1.6075\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   NON-LINEAR BIODEGRADATION PROBABILITY    |         |  0.3054\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Biodegrades Fast\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> Does NOT Biodegrade Fast\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2548 | -0.2548\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.2502\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.1992\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - ULTIMATE BIODEGRADATION   |         |  2.6942\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.2880 | -0.2880\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.1633\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  3.8477\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |   SURVEY MODEL - PRIMARY BIODEGRADATION    |         |  3.3964\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> Result Classification:   5.00 -> hours     4.00 -> days    3.00 -> weeks\r\n" + 
				"<br>  (Primary & Ultimate)    2.00 -> months    1.00 -> longer\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.0848 | -0.0848\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0004 |  0.0004\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.0494 |  0.0494\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.0197 |  0.0986\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -0.3368\r\n" + 
				"<br> Const|  *  |  Equation Constant                         |         |  0.7121\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   |  MITI LINEAR BIODEGRADATION PROBABILITY    |         |  0.4390\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> TYPE | NUM |        BIOWIN FRAGMENT DESCRIPTION         |  COEFF  |  VALUE\r\n" + 
				"<br>------+-----+--------------------------------------------+---------+---------\r\n" + 
				"<br> Frag |  1  |  Tertiary amine                            | -0.8396 | -0.8396\r\n" + 
				"<br> Frag |  1  |  Methyl  [-CH3]                            |  0.0194 |  0.0194\r\n" + 
				"<br> Frag |  1  |  -CH2-  [linear]                           |  0.4295 |  0.4295\r\n" + 
				"<br> Frag |  5  |  -CH2-  [cyclic]                           |  0.2365 |  1.1826\r\n" + 
				"<br> MolWt|  *  |  Molecular Weight Parameter                |         | -3.2681\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>   RESULT   | MITI NON-LINEAR BIODEGRADATION PROBABILITY |         |  0.5124\r\n" + 
				"<br>============+============================================+=========+=========\r\n" + 
				"<br>\r\n" + 
				"<br> A Probability Greater Than or Equal to 0.5 indicates --> Readily Degradable\r\n" + 
				"<br> A Probability Less Than 0.5 indicates --> NOT Readily Degradable[BIOWIN]\r\n" + 
				"<br>\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                    </tbody>\r\n" + 
				"                </table>";
		
		Table table=parse(strTable);
		System.out.println(table);

		
		
	}
	
	static void parseTable2() {
		String strTable="<table class=\"results\">\r\n" + 
				"                    <thead>\r\n" + 
				"                        <tr>\r\n" + 
				"                            <th colspan=\"3\">\r\n" + 
				"                                Classification\r\n" + 
				"                            </th>\r\n" + 
				"                        </tr>\r\n" + 
				"                    </thead>\r\n" + 
				"                    <tbody>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        3.1B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Flammable Liquids: high hazard\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        8.2B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Corrosive to dermal tissue\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        8.3A\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Corrosive to ocular tissue\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                         (fish)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Very ecotoxic in the aquatic environment\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr>\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                         (crustacean)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Very ecotoxic in the aquatic environment\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                <tr class=\"alt\">\r\n" + 
				"                    <td>\r\n" + 
				"                        9.1B\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                         (algal)\r\n" + 
				"                    </td>\r\n" + 
				"                    <td>\r\n" + 
				"                        Very ecotoxic in the aquatic environment\r\n" + 
				"                    </td>\r\n" + 
				"                </tr>\r\n" + 
				"\r\n" + 
				"                    </tbody>\r\n" + 
				"                </table>";
		
		
		
		Table table=parse(strTable);
		System.out.println(table);
		
	}
	
	 
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Table.parseTable1();
		Table.parseTable2();
		Table.parseTable3();
		
	}

}
