package ToxPredictor.Application;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

public class WebTEST_merge_command_line_results {

	static void go() {

		try {

			File Folder=new File("tony");
			FileWriter fw=new FileWriter("tony/output.tsv");
			Vector<List<String>>vecLines=new Vector<>();
			Vector<String>vecEndpoints=new Vector<>();
			
			
			for (File file:Folder.listFiles()) {
				if (!file.getName().contains("tsv")) continue;
				if (file.getName().equals("output.tsv")) continue;
				
				String name=file.getName();
				name=name.substring(name.indexOf("-")+1,name.indexOf("."));
									
				vecEndpoints.add(name);
				
				System.out.println(file.getAbsolutePath());
				
				List<String>lines=Files.readAllLines(Paths.get(file.getAbsolutePath()),Charset.forName("ISO-8859-1"));
				vecLines.add(lines);
			}
			
			List<String>vecLines0=vecLines.get(0);

			for (int row=0;row<vecLines0.size();row++) {
				
				for (int endpoint=0;endpoint<vecLines.size();endpoint++) {
//					String [] values=vecLines.get(endpoint).get(row).split("\t");
										
					String line=vecLines.get(endpoint).get(row);
					
					if (line.substring(line.length()-1,line.length()).equals("\t")) {
						line=line+"OK";
					}
					
					List<String> values = ToxPredictor.Utilities.Utilities.Parse3(line, "\t");
					
					int start=0;
					int stop=values.size();

					if (endpoint!=0) {
						start=2;
						stop=values.size()-1;
					}
					
					for (int k=start;k<stop;k++) {
						if (row==0) {
							fw.write(vecEndpoints.get(endpoint)+"_"+values.get(k)+"\t");
						} else {
							fw.write(values.get(k)+"\t");
						}
					}
				}

				fw.write("\r\n");
				fw.flush();
			}
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		go();
	}

}
