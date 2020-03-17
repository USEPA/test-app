package ToxPredictor.misc;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileSearch {

	void findCASinZipFiles () {
		
		String folder="R:\\javax\\WebTEST\\NCCT Results 11_16_17";
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		String CAS="53606-41-0";
		
		for (int i=0;i<files.length;i++) {

			String filename = files[i].getName();

			//			if (!filename.equals("TEST_2017-07-31_0.zip")) continue;

			if (filename.indexOf(".zip")==-1 || filename.indexOf("web-reports")==-1) continue;

			String filepath = folder + "/" + filename;

			System.out.println(filename);

			try {
				final ZipFile file = new ZipFile(files[i]);

				final Enumeration<? extends ZipEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					final ZipEntry entry = entries.nextElement();

					if (entry.getName().indexOf(CAS)>-1) {
						System.out.println(entry.getName());	
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}


		}
		
	}
	
	public static void main(String[] args) {
		ZipFileSearch zfs=new ZipFileSearch();
		
		zfs.findCASinZipFiles();
		
	}
}
