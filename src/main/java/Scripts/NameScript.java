package Scripts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF.APIMolecule;

/**
 * @author TMARTI02
 */
public class NameScript {

	void convertSDF_To_Excel() {

//      System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "INFO");

		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\snapshot-2025-07-30\\";
		String inputFilePath = folder + "50k_chunk_from_1_out.sdf"; // Replace with your actual SDF file path
		String outputFilePath = inputFilePath.replace(".sdf", ".xlsx"); // Replace with your desired Excel file path

		try {

			InputStream inputStream = new FileInputStream(inputFilePath);

			List<APIMolecule> molecules= RunFromSDF.readSDF_to_API_Molecules(inputFilePath, -1, false);

			XSSFWorkbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Molecules");

			// Create header row
			Row headerRow = sheet.createRow(0);
			headerRow.createCell(0).setCellValue("DTXSID");
			headerRow.createCell(1).setCellValue("DTXCID");
			headerRow.createCell(2).setCellValue("PREFERRED_NAME");
			headerRow.createCell(3).setCellValue("CASRN");
			headerRow.createCell(4).setCellValue("INCHIKEY");
			headerRow.createCell(5).setCellValue("INCHI_STRING");
			headerRow.createCell(6).setCellValue("SMILES");
			headerRow.createCell(7).setCellValue("IUPAC_NAME");
			headerRow.createCell(8).setCellValue("INDEX_NAME");

			int rowNum = 1;

			for (APIMolecule molecule : molecules) {
				
				
				if(rowNum%1000==0) System.out.println(rowNum);

				// Extract properties
				String dtxsid = (String) molecule.htProperties.get("DTXSID");
				String dtxcid = (String) molecule.htProperties.get("DTXCID");
				String preferredName = (String) molecule.htProperties.get("PREFERRED_NAME");
				String casrn = (String) molecule.htProperties.get("CASRN");
				String inchiKey = (String) molecule.htProperties.get("INCHIKEY");
				String inchiString = (String) molecule.htProperties.get("INCHI_STRING");
				String smiles = (String) molecule.htProperties.get("SMILES");
				String iupacName = (String) molecule.htProperties.get("IUPAC_NAME");
				String indexName = (String) molecule.htProperties.get("INDEX_NAME");
				
				System.out.println(casrn+"\t"+iupacName);

				// Create a new row in the Excel sheet
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(dtxsid);
				row.createCell(1).setCellValue(dtxcid);
				row.createCell(2).setCellValue(preferredName);
				row.createCell(3).setCellValue(casrn);
				row.createCell(4).setCellValue(inchiKey);
				row.createCell(5).setCellValue(inchiString);
				row.createCell(6).setCellValue(smiles);
				row.createCell(7).setCellValue(iupacName);
				row.createCell(8).setCellValue(indexName);
			}

			// Write the workbook to a file
			FileOutputStream fileOut = new FileOutputStream(outputFilePath);

			workbook.write(fileOut);
			workbook.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		NameScript d = new NameScript();
		d.convertSDF_To_Excel();

	}
}
