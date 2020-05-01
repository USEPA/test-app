/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 12:26:00 -0500 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2007  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sf.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ToxPredictor.Application.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import ToxPredictor.Application.TESTConstants;




/**
 * About dialog for ToxPredictor
 *
 * @author        Todd Martin
 * @cdk.created       1/30/08
 *
 */
public class DialogSplash extends javax.swing.JDialog {


	/** Displays the About Dialog for JChemPaint.  */
	public DialogSplash() {
			
		doInit();
	}

	/**  Description of the Method */
	public void doInit() {
		String s1 = "Loading T.E.S.T. software..."+"\n";
		String s2 = "Written by Todd Martin, Paul Harten, Raghuraman Venkatapathy, and Douglas Young.\n";
		s2 += "See the user's guide for more information.";

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(Color.white);

		JLabel label1 = new JLabel();

		try {
//			JCPPropertyHandler jcpph = JCPPropertyHandler.getInstance();
//			URL url = jcpph.getResource("jcplogo" + JCPAction.imageSuffix);
						
			URL url=this.getClass().getClassLoader().getResource("EPA logo.jpg");
			
			ImageIcon icon = new ImageIcon(url);
			//ImageIcon icon = new ImageIcon(../resources/);
			label1 = new JLabel(icon);
		} catch (Exception exception) {
		}
		
		this.setTitle("T.E.S.T. Version " +TESTConstants.SoftwareVersion);
		
		
		try {
			URL url=this.getClass().getClassLoader().getResource("epa_logo_icon.jpg");
			this.setIconImage(new ImageIcon(url).getImage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

//		this.getContentPane().setBackground(Color.black);
		
		label1.setBackground(Color.white);

		Border lb = BorderFactory.createLineBorder(Color.white, 5);
		JTextArea jtf1 = new JTextArea(s1);
		
//		System.out.println(jtf1.getFont().getName());
//		System.out.println(jtf1.getFont().getSize());
		
		Font myFont = new java.awt.Font("Dialog", Font.ITALIC, 14);
		jtf1.setFont(myFont);
		jtf1.setForeground(Color.BLUE);
		jtf1.setBorder(lb);
		jtf1.setEditable(false);
		JTextArea jtf2 = new JTextArea(s2);
		jtf2.setEditable(false);
		jtf2.setBorder(lb);		
		getContentPane().add("Center", label1);
		getContentPane().add("North", jtf1);
		getContentPane().add("South", jtf2);
		pack();
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		ToxPredictor.Utilities.Utilities.CenterFrame(this); //applet-omit
		
		setVisible(false);
	}
	
	public static void main(String[] args) { //applet-omit
		// TODO Auto-generated method stub
		
		DialogSplash fs=new DialogSplash();
		fs.setVisible(true);
	}
}

