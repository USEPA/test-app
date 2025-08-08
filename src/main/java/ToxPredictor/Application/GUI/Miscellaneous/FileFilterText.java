/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-04-16 04:40:19 -0400 (Mon, 16 Apr 2007) $
 *  $Revision: 8201 $
 *
 *  Copyright (C) 1997-2007  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sourceforge.net
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
package ToxPredictor.Application.GUI.Miscellaneous;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

/**
 * A file filter for JCP
 *
 * @cdk.module jchempaint
 *@author     steinbeck
 */
public class FileFilterText extends javax.swing.filechooser.FileFilter 
{

	/**
	 *  Description of the Field
	 */
	public final static String txt = "txt";
	public final static String smi = "smi";
	public final static String csv = "csv";
	public final static String xlsx = "xlsx";
	public final static String html = "html";
	/**

	/**
	 *  Description of the Field
	 */
	protected List<String> types;


	/**
	 *  Constructor for the JCPFileFilter object
	 *
	 *@param  type  Description of the Parameter
	 */
	public FileFilterText(String type)
	{
		super();
		types = new ArrayList<>();
		types.add(type);
	}


	/**
	 *  Adds an additional file type to the list
	 *
	 *@param  type  The feature to be added to the Type attribute
	 */
	public void addType(String type)
	{
		types.add(type);
	}


	/**
	 *  Adds the JCPFileFilter to the JFileChooser object.
	 *
	 *@param  chooser  The feature to be added to the ChoosableFileFilters
	 *      attribute
	 */
	public static void addChoosableFileFilters(JFileChooser chooser)
	{
		chooser.addChoosableFileFilter(new FileFilterText(FileFilterText.csv));		
		chooser.addChoosableFileFilter(new FileFilterText(FileFilterText.smi));
		chooser.addChoosableFileFilter(new FileFilterText(FileFilterText.txt));
		chooser.addChoosableFileFilter(new FileFilterText(FileFilterText.xlsx));
		chooser.addChoosableFileFilter(new FileFilterText(FileFilterText.html));
		
	}


	/*
	 *  Get the extension of a file.
	 */
	/**
	 *  Gets the extension attribute of the JCPFileFilter class
	 *
	 *@param  f  Description of the Parameter
	 *@return    The extension value
	 */
	public static String getExtension(File f)
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1)
		{
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}


	// Accept all directories and all gif, jpg, or tiff files.
	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}

		String extension = getExtension(f);
		if (extension != null)
		{
			if (types.contains(extension))
			{
				return true;
			} else
			{
				return false;
			}
		}
		return false;
	}


	// The description of this filter
	/**
	 *  Gets the description attribute of the JCPFileFilter object
	 *
	 *@return    The description value
	 */
	public String getDescription()
	{
		String type = (String) types.get(0);
		if (type.equals(txt))
		{
			return "Text file (*.txt)";
		}
		
		if (type.equals(smi))
		{
			return "Smiles text file (*.smi)";
		}

		if (type.equals(csv))
		{
			return "Comma separated text file (*.csv)";
		}
		
		if (type.equals(xlsx))
		{
			return "Microsoft Excel file (*.xlsx)";
		}

		if (type.equals(html))
		{
			return "HTML file (*.html)";
		}

		return null;
	}


	/**
	 *  Gets the type attribute of the JCPFileFilter object
	 *
	 *@return    The type value
	 */
	public String getType()
	{
		return (String) types.get(0);
	}


	/**
	 *  Sets the type attribute of the JCPFileFilter object
	 *
	 *@param  type  The new type value
	 */
	public void setType(String type)
	{
		types.add(type);
	}
}


