package wekalite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ToxPredictor.Utilities.Utilities;
import QSAR.validation2.ArrayConverter;
import wekalite.*;

public class CSVLoader {

	
	
	/**
	 * Load Instances from input stream
	 * Note: CAS and Tox arent stored in descriptor list
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public Instances getDatasetFromInputStream(InputStream is,String del) throws IOException {
		Instances instances=new Instances();
		
		BufferedReader br=new BufferedReader(new java.io.InputStreamReader(is));

		String cas = null;
		String header = br.readLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=Utilities.Parse3toArrayList(header, del);
		attributesAL.remove(0);//remove CAS attribute
		attributesAL.remove(0);//remove Tox attribute
		
		String[]attributes=ArrayConverter.convertStringArrayListToStringArray(attributesAL);
		
		
		instances.setAttributes(attributes);
		
		while (true) {
			String Line=br.readLine();
			
			if (Line==null)	break;

			String CAS=Line.substring(0,Line.indexOf(del));
			Line=Line.substring(Line.indexOf(del)+1,Line.length());//remove CAS
			
			String strTox=Line.substring(0,Line.indexOf(del));
			double Tox=Double.parseDouble(strTox);
			Line=Line.substring(Line.indexOf(del)+1,Line.length());//remove Tox
			
			ArrayList<Double> llDescriptors=Utilities.ParseToDoubleArrayListWithTokenizer(Line, del);

			double []descriptors=ArrayConverter.convertDoubleArrayListToDoubleArray(llDescriptors);
			
			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instance.setName(CAS);
			instance.setToxicity(Tox);
		
			instances.addInstance(instance);
			
		}
		
		is.close();
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
	}
	
	
	public Instances getDatasetFromInputStreamNoTox(InputStream is,String del) throws IOException {
		Instances instances=new Instances();
		
		BufferedReader br=new BufferedReader(new java.io.InputStreamReader(is));

		String cas = null;
		String header = br.readLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=Utilities.Parse3toArrayList(header, del);
		attributesAL.remove(0);//remove CAS attribute
		
		String[]attributes=ArrayConverter.convertStringArrayListToStringArray(attributesAL);
		
		
		instances.setAttributes(attributes);
		
		while (true) {
			String Line=br.readLine();
			
			if (Line==null)	break;
			
			if (Line.toLowerCase().indexOf("error")>-1) {
				System.out.println(Line);
				continue;
			}

			String CAS=Line.substring(0,Line.indexOf(del));
			Line=Line.substring(Line.indexOf(del)+1,Line.length());//remove CAS
			
			ArrayList<Double> llDescriptors=Utilities.ParseToDoubleArrayListWithTokenizer(Line, del);

			double []descriptors=ArrayConverter.convertDoubleArrayListToDoubleArray(llDescriptors);
			
			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instance.setName(CAS);
		
			instances.addInstance(instance);
			
		}
		
		is.close();
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
	}

	public Instances getDataSetFromFile(String filePath) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStream(fis,",");
	}

	
	public Instances getDataSetFromFile(String filePath,String del) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStream(fis,del);
	}
	
	public Instances getDataSetFromFileNoTox(String filePath,String del) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStreamNoTox(fis,del);
	}
	
	  /**
	   * Method added by TMM to load csv dataset from file in jar file
	   * @return
	   * @throws IOException
	   */
	public Instances getDataSetFromJarFile(String filePathInJar) throws IOException {
		java.io.InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(filePathInJar);

		return getDatasetFromInputStream(is,",");
	}
	
//	void testWekaLoadTime() {
//		
//		String endpoint="LD50";
//		
//		String folder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
//		String filename=endpoint+"_training_set-2d.csv";
//		
//		for (int i=1;i<=5;i++) {
//		try {
//			long t1=System.currentTimeMillis();
//			QSAR.validation.CSVLoader c= new QSAR.validation2.CSVLoader();
//			c.setSource(new File(folder+"/"+filename));
//			c.getDataSet();
//			long t2=System.currentTimeMillis();
//			System.out.println("Time to load 1st time: "+(t2-t1)/1000.0+" secs");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		}
//	}
	
	void testLoad() {

		String endpoint="LD50";
		
		String folder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
		String filename=endpoint+"_training_set-2d.csv";

//		for (int i=1;i<=5;i++) {
		long tstart=System.currentTimeMillis();
		try {
			CSVLoader c=new CSVLoader();
			Instances instances=c.getDataSetFromFile(folder+"/"+filename,",");
			
//			instances.printInstances();
//			instances.printMetaData();
	
//			System.out.println(instance0.stringValue(0));
//			System.out.println(instance0.getName());
//			System.out.println(instance0.getToxicity());
//			System.out.println(instance0.value(2));
//			System.out.println(instance0.value("x0"));
//			System.out.println(instance0.attribute(2));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		long tend=System.currentTimeMillis();
		System.out.println("Overal time to load: "+(tend-tstart)/1000.0+" secs");
		
//		}//end loop over i
		
	}
	
	public static void main(String [] args) {

		CSVLoader c=new CSVLoader();
		c.testLoad();
//		c.testWekaLoadTime();
	}
}
