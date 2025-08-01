package ToxPredictor.Utilities;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;

public class GetStructureImagesFromJar {
	
	ToxPredictor.misc.ParseChemidplus p=new ToxPredictor.misc.ParseChemidplus(); 
	
	public void CreateImageFileFromStructure(String CAS,String outputfolder) {
		double time1 = System.currentTimeMillis() / 1000.0;
		IAtomContainer moleculei=p.LoadChemicalFromMolFileInJar("ValidatedStructures/"+CAS+".mol");
		
		
//		ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei,CAS,outputfolder,false,true,true,200);
		ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei, CAS, outputfolder);
		
		
		double time2 = System.currentTimeMillis() / 1000.0;
//		System.out.println(time2-time1);
	}
	
	/**
	 * Method to quickly load an image from a jar and save it to a folder on the harddrive so it can be displayed in a browser
	 * This method is much quicker than creating the image from scratch 
	 * @param CAS
	 * @param outputfolder
	 * @return
	 */
	public int GetImageFileFromJar(String CAS,String outputfolder) {

		try {

			String filepath="images/"+CAS+".png";

			double time1 = System.currentTimeMillis() / 1000.0;

			InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filepath);

			File DestFile=new File(outputfolder+"/"+CAS+".png");
			
			File OF=new File(outputfolder);
			OF.mkdirs();
			
			DataInputStream dis=new DataInputStream(ins);
			DataOutputStream dos=new DataOutputStream(new FileOutputStream(DestFile));

			//			for (int i=1;i<=10;i++) {

			byte [] bytes=new byte[10000]; //arbitrarily pick large size


			try {
				dis.readFully(bytes);

			} catch (Exception ex1) {
				//reach end of file since picked large size
				// or file doesnt exist
			}

			byte [] bytes2=GetBytes(bytes); //get rid of extra bytes

			if(bytes2==null) return -1;

			dos.write(bytes2);

			//			}
			dos.close();

			double time2 = System.currentTimeMillis() / 1000.0;

			return 0;
			//			System.out.println(time2-time1);

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}


	}
	//read 1 byte at a time- slower but works
	
public int GetImageFileFromJar2(String CAS,String outputfolder) {
		
		try {
			
			String filepath="images/"+CAS+".png";
			
			double time1 = System.currentTimeMillis() / 1000.0;
			
			
			InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filepath);

			File DestFile=new File(outputfolder+"/"+CAS+".png");
			DataInputStream dis=new DataInputStream(ins);
			DataOutputStream dos=new DataOutputStream(new FileOutputStream(DestFile));
			
//			for (int i=1;i<=10;i++) {
				
			
			
			try {
				
				int counter=0;
				while (true) {
					byte b=dis.readByte();
					dos.write(b);
//					System.out.println(counter+"\t"+b);
					
					counter++;
					if (counter==100000) break;
					
				}
				
			} catch (Exception ex1) {//end of file
				//reach end of file since picked large size
				// or file doesnt exist
//				System.out.println(ex1);
			}
			
			
//			}
			dos.close();
			
			double time2 = System.currentTimeMillis() / 1000.0;
			
			return 0;
//			System.out.println(time2-time1);
			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		
	}
	
	/**
	 * Loops through bytes and figures out which bytes are filled
	 * @param bytes
	 * @return
	 */
	private byte [] GetBytes(byte [] bytes) {
		int last=-1;
		
		for (int i=bytes.length-1;i>=0;i--) {
			int bob=bytes[i];
//			System.out.println(i+"\t"+bob);
			if (bob!=0) {
				last=i+1;
				break;
			}
		}
		
		if (last==-1) return null;
				
		
		byte [] bytes2=new byte[last];
		
		for (int i=0;i<last;i++) {
			bytes2[i]=bytes[i];
		}
		
		return bytes2;
	}
	
	
	
	private void gothroughlist(String outputfolder,boolean ReadFromJar) {
		
		try {
			double time1 = System.currentTimeMillis() / 1000.0;
			
			BufferedReader br=new BufferedReader(new FileReader("cas listing.txt"));
			
			while (true) {
				String CAS=br.readLine();
				
				if (CAS==null) break;
				
				if (ReadFromJar) GetImageFileFromJar(CAS, outputfolder);
				else CreateImageFileFromStructure(CAS,outputfolder);
				
			}
			
			double time2 = System.currentTimeMillis() / 1000.0;
			
			System.out.println(time2-time1);
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		
	}
	
	void goThroughAllFilesInZip() {
//		try {
//			
//			try {
//	            ZipFile zipFile = new ZipFile("jar/StructureImages.jar");
//	            
//	            Enumeration zipEntries = zipFile.entries();
//	            
//	            while (zipEntries.hasMoreElements()) {
//	                
//	                //Process the name, here we just print it out
//	                System.out.println(((ZipEntry)zipEntries.nextElement()).getName());
//	                
//	            }
//	            
//	        } catch (IOException ex) {
//	            ex.printStackTrace();
//	        }
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		long t1=System.currentTimeMillis();
	try {
		JarFile jar = new java.util.jar.JarFile("jar/StructureImages.jar");
		java.util.Enumeration enum1 = jar.entries();
		
		int counter=0;
		while (enum1.hasMoreElements()) {
			
			counter++;
		    java.util.jar.JarEntry file = (JarEntry) enum1.nextElement();
		    java.io.File f = new java.io.File("bob" + java.io.File.separator + file.getName());
		    if (file.isDirectory()) { // if its a directory, create it
		        if (file.getName().equals("images")) {
		        	f.mkdir();
		        }
		        continue;
		    }
		    
		    if (file.getName().indexOf(".png")==-1) continue;
		    
		    java.io.InputStream is = jar.getInputStream(file); // get the input stream
		    
			DataInputStream dis=new DataInputStream(is);
			DataOutputStream dos=new DataOutputStream(new FileOutputStream(f));

			//			for (int i=1;i<=10;i++) {

			byte [] bytes=new byte[10000]; //arbitrarily pick large size


			try {
				dis.readFully(bytes);

			} catch (Exception ex1) {
				//reach end of file since picked large size
				// or file doesnt exist
			}

			byte [] bytes2=GetBytes(bytes); //get rid of extra bytes

			if(bytes2==null) continue;

			dos.write(bytes2);

			//			}
			dos.close();
		    
		    
//		    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
//		    while (is.available() > 0) {  // write contents of 'is' to 'fos'
//		        fos.write(is.read());
//		    }
//		    fos.close();
//		    is.close();
			
			if (counter==100) break;
		}
		long t2=System.currentTimeMillis();
		
		System.out.println(t2-t1+" milliseconds");
		
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		
}
	
	
	public static void main(String[] args) { 
		
		GetStructureImagesFromJar g=new GetStructureImagesFromJar();
//		g.GetImageFileFromJar("71-43-2", "temp");
//		g.CreateImageFileFromStructure("71-43-2", "temp");
		
//		g.gothroughlist("temp",true);
		g.goThroughAllFilesInZip();
		
//		int bob=g.GetImageFileFromJar("71-43-2", "temp");
//		System.out.println(bob);
		
//		int bob=g.GetImageFileFromJar2("71-43-2", "temp");
		

//		bob=g.GetImageFileFromJar("blob", "temp");
//		System.out.println(bob);

	}
}
