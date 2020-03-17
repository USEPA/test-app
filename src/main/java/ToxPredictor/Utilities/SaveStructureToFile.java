package ToxPredictor.Utilities;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveStructureToFile {

	private static double determineMaxDiff(IAtomContainer atomContainer) {

		double MinX = 99999999;
		double MaxX = -99999999;
		double MinY = 99999999;
		double MaxY = -99999999;

		double MaxDiff = 0;
		double DiffX = 0;
		double DiffY = 0;

		for (int i = 0; i < atomContainer.getAtomCount(); i++) {
			double X = atomContainer.getAtom(i).getPoint2d().x;
			double Y = atomContainer.getAtom(i).getPoint2d().y;
			if (X < MinX)
				MinX = X;
			if (X > MaxX)
				MaxX = X;
			if (Y < MinY)
				MinY = Y;
			if (Y > MaxY)
				MaxY = Y;
		}
		DiffX = Math.abs(MaxX - MinX);
		DiffY = Math.abs(MaxY - MinY);
		MaxDiff = Math.max(DiffX, DiffY);
		return MaxDiff;

	}

	/**
	 * 
	 * @param atomContainer
	 * @param filename
	 * @param foldername
	 */
	public static void CreateImageFileWithNumbers(IAtomContainer atomContainer, String filename, String foldername) {
		try {
			File myFile = new File(foldername + File.separator + filename + ".png");
			new DepictionGenerator().withAtomColors().withAtomNumbers().withZoom(1.5).depict(atomContainer)
					.writeTo(myFile.getAbsolutePath());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void CreateImageFileFromSmiles(String smiles, String filename, String foldername) {

		try {
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			AtomContainer m = (AtomContainer) sp.parseSmiles(smiles);
			CreateImageFile(m, filename, foldername);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 
	 * @param atomContainer
	 * @param filename
	 * @param foldername
	 */
	public static void CreateImageFile(IAtomContainer atomContainer, String filename, String folderPath) {
		
		try {
			Path path = Paths.get(folderPath);
			Files.createDirectories(path);
			File myFile = new File(folderPath + File.separator + filename + ".png");
			new DepictionGenerator().withAtomColors().withZoom(1.5).depict(atomContainer)
					.writeTo(myFile.getAbsolutePath());

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * 
	 * @param atomContainer
	 * @param filename
	 * @param foldername
	 */
	public static BufferedImage CreateBufferedImage(IAtomContainer atomContainer) {
		try {

			return new DepictionGenerator().withAtomColors().withZoom(1.5).depict(atomContainer).toImg();

		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	/**
	 * Using my old scaling system for making larger images for large molecules
	 * 
	 * @param atomContainer
	 * @param filename
	 * @param foldername
	 * @param Factor
	 * @param minSize
	 */
	public static void CreateScaledImageFileWithNumbers(IAtomContainer atomContainer, String filename,
			String foldername, double Factor, int minSize) {
		try {
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.generateCoordinates(atomContainer);
		} catch (Exception e) {
			// System.out.println(e);
			e.printStackTrace();
			return;
		}

		double MaxDiff = determineMaxDiff(atomContainer);
		int w = (int) (MaxDiff * Factor);
		// System.out.println(w);
		if (w < minSize)
			w = minSize;
		int h = w;

		try {
			File myFile = new File(foldername + File.separator + filename + ".png");

			new DepictionGenerator().withSize(w, w).withAtomColors().withAtomNumbers().depict(atomContainer)
					.writeTo(myFile.getAbsolutePath());

			// if (myFile.exists()) {
			// long fileModTime=myFile.lastModified();
			//
			// long currentTime = System.currentTimeMillis();
			// long diff = currentTime - fileModTime;
			// diff_in_sec = diff / 1000.0;
			//
			// if (diff_in_sec < 1) {//only write model files if they are more
			// than 1 hr old
			//// System.out.println("Hi!");
			// return;
			// }
			//
			// }
			//
			// System.out.println("time="+diff_in_sec);

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String filename = "bob";
		String foldername = "ToxPredictor/temp";
		String smiles = "COc7ccc(N4C(=O)c5ccc6c1ccc3c2c1c(ccc2C(=O)N(c1ccc(OC)cc1)C3=O)c1ccc(C4=O)c5c61)cc7";
		smiles = "CC(O)c1c(C)c2cc3[nH]c(cc4nc(cc5[nH]c(cc1n2)C(=C5CCC(=O)O)C)c(CCC(=O)O)c4C)c(C)c3C(C)O";
		SaveStructureToFile.CreateImageFileFromSmiles(smiles, filename, foldername);
	}

}
