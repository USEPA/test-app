package ToxPredictor.StructureDataDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetChemIDplusData {
	
	
	
	RecordChemIDplus scrapeWebPage(String filePath) {
		
		try {
			File inputFile=new File(filePath);
			Document doc = Jsoup.parse(inputFile, "utf-8");
			
			RecordChemIDplus cr=new RecordChemIDplus();
			
			getStructureDescs(doc, cr);
			getNames(doc,cr);

//			System.out.println(cr.toString("\t"));

			return cr;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}


	private void getNames(Document doc, RecordChemIDplus cr) {
		
		Elements elementsH1=doc.select("h1");
		
		for (Element element:elementsH1) {
			if (element.text().contains("Substance Name")) {
				String [] vals=element.html().split("<br>");
				if (vals[0].contains("Substance Name:&nbsp;")) {
					cr.Substance_Name=vals[0].replace("Substance Name:&nbsp;", "");
				}
				
			}
		}
		
		Elements elements=doc.select("h3");

		for (Element element:elements) {
			

			if (element.text().contains("CAS Registry Number")) {
				Element elementNext=element.nextElementSibling();
				Element elementLi=elementNext.select("li").first();
				cr.CAS_Registry_Number=elementLi.text();
				
			}else if (element.text().contains("Other Registry Number")) {
				Element elementNext=element.nextElementSibling();
								
				Elements elementsLi=elementNext.select("li");
				
				for (Element elementLi:elementsLi) {
					if (cr.Other_Registry_Numbers==null) {
						cr.Other_Registry_Numbers=elementLi.text();
					} else {
						cr.Other_Registry_Numbers+="| "+elementLi.text();
					}
				}
			
			}else if (element.text().contains("Synonym")) {
				Element elementNext=element.nextElementSibling();
								
				Elements elementsLi=elementNext.select("li");
				
				for (Element elementLi:elementsLi) {
					if (cr.Synonyms==null) {
						cr.Synonyms=elementLi.text();
					} else {
						cr.Synonyms+="| "+elementLi.text();
					}
				}
				
				
//			}else if (element.text().contains("Name of Substance")) {
//					Element elementNext=element.nextElementSibling();
//					Element elementsLi=elementNext.select("li").first();
//					cr.Name_of_Substance=elementsLi.text();

			}else if (element.text().contains("Systematic Name")) {
				Element elementNext=element.nextElementSibling();
				Element elementsLi=elementNext.select("li").first();
				cr.Systematic_Name=elementsLi.text();
				
			} else {
//				System.out.println(element.text()+"\n");	
			}
		}
		
	}
	
	private void getStructureDescs(Document doc, RecordChemIDplus cr) {
		Element elementStructureDescs=doc.select("div#structureDescs").first();

		if (elementStructureDescs==null) return;
		Elements elementsDiv=elementStructureDescs.select("div");
		
		for (int i=1;i<elementsDiv.size();i++) {
			Element elementDiv=elementsDiv.get(i);
			Element elementH3=elementDiv.selectFirst("h3");
			
			String header=elementH3.text().trim();
			String text=elementDiv.text();
			
//			System.out.println(header);
			
			//TODO need better way to parse the html than by taking the text and deleting the extra things like Download
			text=text.replace("Download", "").replace("Search the web for this InChIKey", "");
			text=text.replace(header, "").trim();

			if (header.contentEquals("InChI")) {
				cr.InChI=text;
			} else if (header.contentEquals("InChIKey")) {
				cr.InChIKey=text;
			} else if (header.contentEquals("Smiles")) {
				cr.Smiles=text;
			}
//				String strHTML=elementDiv.html();				
//				String cleanedHTML = Jsoup.clean(strHTML, Whitelist.none());
//				System.out.println(cleanedHTML+"\r\n");
		}
	}
	

	void downloadWebPagesFromCASList(String textFilePath,String destFolderPath) {
		int waitSeconds=3;//time to wait in seconds
				
		try {								
			BufferedReader br=new BufferedReader(new FileReader(textFilePath));			
			Vector<String>casList=new Vector<String>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				Line=Line.trim();
				if (!casList.contains(Line)) casList.add(Line);
			}
			br.close();
			
			for (String CAS:casList) {
				String strURL="https://chem.nlm.nih.gov/chemidplus/rn/"+CAS;
				String outputFilePath=destFolderPath+File.separator+CAS+".html";
				FileUtilities.downloadFileByBytes(strURL, outputFilePath);
				TimeUnit.SECONDS.sleep(waitSeconds);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void downloadWebPagesFromNameList(String textFilePath,String destFolderPath) {
		int waitSeconds=3;//time to wait in seconds
				
		try {								
			BufferedReader br=new BufferedReader(new FileReader(textFilePath));			
			Vector<String>nameList=new Vector<String>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				Line=Line.trim();
				if (!nameList.contains(Line)) nameList.add(Line);
			}
			br.close();
			
			for (String name:nameList) {
				String strURL="https://chem.nlm.nih.gov/chemidplus/name/"+name.replace(" ", "%20");
				String outputFilePath=destFolderPath+File.separator+name+".html";
				FileUtilities.downloadFileByBytes(strURL, outputFilePath);
//				System.out.println(name);
				TimeUnit.SECONDS.sleep(waitSeconds);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void goThroughHTMLFolder(String folderPath) {
		File Folder=new File(folderPath);
		File [] files=Folder.listFiles();
		
		System.out.println(RecordChemIDplus.getHeader("\t"));
		for (File file:files) {
			RecordChemIDplus cr=scrapeWebPage(file.getAbsolutePath());
			
			System.out.println(cr.toString("\t"));
		}
		
	}
	
	public static void main(String[] args) {
		
		GetChemIDplusData g=new GetChemIDplusData();
		
		String folderStructureData="data\\StructureData\\";
//		String textFilePath=folderStructureData+"test.txt";
		String textFilePathCAS=folderStructureData+"unique cas list no hit LLNA_echemportal.txt";
		String textFilePathName=folderStructureData+"unique name list no hit LLNA_echemportal.txt";

		String source="chemidplus";
		String folderSource=folderStructureData+source+"\\";

		//		String destFolderPath=folderSource+"html pages";
//		g.downloadWebPagesFromCASList(textFilePathCAS, destFolderPath);

		String destFolderPath=folderSource+"html pages from name";
//		g.downloadWebPagesFromNameList(textFilePathName, destFolderPath);
		
//		g.scrapeWebPage(destFolderPath+"\\"+"71-43-2.html");
//		g.scrapeWebPage(destFolderPath+"\\"+"23356-96-9.html");
		
		g.goThroughHTMLFolder(destFolderPath);		
		
//		g.scrapeWebPage(destFolderPath+"\\"+"91-20-3.html");
	}
}
