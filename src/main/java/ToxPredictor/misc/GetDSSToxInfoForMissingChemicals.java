package ToxPredictor.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.io.MDLV2000Reader;

import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.CDKUtilities;


public class GetDSSToxInfoForMissingChemicals {

	
	public void getFromDataSets(Hashtable<String,AtomContainerSet>ht) {
		String strfolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\scidataexperts_webtest_from_master\\data";
		
		File mainFolder=new File(strfolder);
		File [] files=mainFolder.listFiles();
		
		for (File folder:files) {
			if (!folder.isDirectory()) continue;

			if (folder.getName().contains("descriptors")) continue;
			
			File [] files2=folder.listFiles();
		
			for (File file:files2) {
				if (file.getName().contains(".sdf")) {
					System.out.println(file.getName());
					AtomContainerSet acs;
					try {
						acs = WebTEST.LoadFromSDF(file.getAbsolutePath());
						
						for (int i=0;i<acs.getAtomContainerCount();i++) {
							AtomContainer ac=(AtomContainer) acs.getAtomContainer(i);
							ac.setProperty("SDF", file.getName());
														
							String CAS=ac.getProperty("CAS");
							
							if (CAS!=null) {

								if (ht.get(CAS)==null) {
									AtomContainerSet acs2=new AtomContainerSet();
									acs2.addAtomContainer(ac);
									ht.put(CAS, acs2);
								} else {
									AtomContainerSet acs2=ht.get(CAS);
									acs2.addAtomContainer(ac);
								}
							} else {
								System.out.println(file.getName()+"\tNull CAS");
							}
							
						}

						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			}
			
		}
		
	}
	
	public AtomContainer LoadChemicalFromMolFileInJar(String CAS) {
		
		try {
			InputStream ins=this.getClass().getClassLoader().getResourceAsStream("ValidatedStructures2d/"+CAS+".mol");
			MDLV2000Reader mr=new MDLV2000Reader(ins);
			AtomContainer ac=new AtomContainer();
			mr.read(ac);
			return ac;

		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}
	}
	public void loadFromExcel(String excelFilePath) {

		try {

			Hashtable<String,AtomContainerSet>ht=new Hashtable<>();
			getFromDataSets(ht);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheet("not found");

			DataFormatter formatter = new DataFormatter();
			
			Row rowHeader = sheet.getRow(0);

			Vector<String>fieldNames=new Vector<String>();
			
			for (int i=0;i<rowHeader.getLastCellNum();i++) {
				String fieldName=rowHeader.getCell(i).getStringCellValue();
				fieldNames.add(fieldName);
			}
			
			for (int i=1;i<sheet.getLastRowNum();i++) {

				Row row=sheet.getRow(i);
				
				String CAS=formatter.formatCellValue(row.getCell(fieldNames.indexOf("CAS")));
				
//				AtomContainer ac=LoadChemicalFromMolFileInJar(CAS);
				
				String SmilesSDF="";
				String NameSDF="";
				String SDF="";
								
				AtomContainerSet acs=ht.get(CAS);
				
				if (acs!=null) {
					
					for (int j=0;j<acs.getAtomContainerCount();j++) {
						AtomContainer ac=(AtomContainer) acs.getAtomContainer(j);
												
						if (SmilesSDF.isEmpty()) {
							SmilesSDF=CDKUtilities.generateSmiles(ac);
						}
						
						if (NameSDF.isEmpty()) {
							if (ac.getProperty("Name")!=null)				
								NameSDF=ac.getProperty("Name");
							
							if (ac.getProperty("molname")!=null)				
								NameSDF=ac.getProperty("molname");
						}
						
					}
					
				}
				
//				if (NameSDF.isEmpty()) {
//					AtomContainer ac=LoadChemicalFromMolFileInJar(CAS);
//					
//					if (ac!=null && ac.getProperty("molname")!=null) {				
//						NameSDF=ac.getProperty("molname");
//						System.out.println(CAS+"\t"+SmilesSDF+"\t"+NameSDF);
//					}
//					
//				}
			
				System.out.println(CAS+"\t"+SmilesSDF+"\t"+NameSDF);
			}
			
			inputStream.close();
			workbook.close();
			

		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetDSSToxInfoForMissingChemicals g=new GetDSSToxInfoForMissingChemicals ();
		g.loadFromExcel("DSSTox/TEST_2500nohits-in-Prod_20201201.xlsx");
		
	}

}
