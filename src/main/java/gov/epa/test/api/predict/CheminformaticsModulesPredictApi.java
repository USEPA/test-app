package gov.epa.test.api.predict;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.epa.test.api.predict.CheminformaticsModulesPredictApi.Prediction.ChemicalData;
import gov.epa.test.api.predict.CheminformaticsModulesPredictApi.Prediction.ChemicalData.EndpointData;
import gov.epa.test.api.predict.CheminformaticsModulesPredictApi.Prediction.ChemicalData.EndpointData.Endpoint;
import gov.epa.test.api.predict.CheminformaticsModulesPredictApi.Prediction.ChemicalData.EndpointData.Results;




/**
* @author TMARTI02
*/
public class CheminformaticsModulesPredictApi {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	class Result {
		
		String sid;
		String casrn;
		String name;
		
	}
	
	
	public class Prediction {
	    public String id;
	    public String predictionTime;
	    public String software;
	    public String softwareVersion;
	    public String condition;
	    public List<ChemicalData> chemicals;
	    
	    public class ChemicalData {
		    public String chemicalId;
		    public Chemical chemical;
		    public List<EndpointData> endpoints;
		    
		    public class Chemical {
			    public int gsid;
			    public String cid;
			    public String sid;
			    public String casrn;
			    public String name;
			    public String smiles;
			    public String inchi;
			    public String inchiKey;
			    public String inchiKey1;
			    public String id;
			}
		    
		    public class EndpointData {
			    public Endpoint endpoint;
			    public List<Results> predicted;
			    public List<Results> experimental;
			    
			    public class Endpoint {
				    public String id;
				    public String name;
				    public boolean binary;
				    public String units;
				    public int valueDigits;
				    public int logValueDigits;
				    public String logUnits;
				    public boolean logMolar;
				}
			    
			    public class Results {
				    public String method;
				    public Double logValue;
				    public Double value;
				    public String errorCode;
				    public String error;
				    public Boolean active;
				    public String message;
				}
				
				public class ExperimentalData {
				    public Double logValue;
				    public Double value;
				}
			}
		    
		}
	}

	
	void setHyperlink(XSSFWorkbook workbook, String url, XSSFCell cell, XSSFCellStyle hlinkstyle) {
		try {
			//TODO: for now use the last link if have multiple
			if ( url.contains("<br>") ) {
				String[] urls = url.split("<br>");
				url = urls[urls.length - 1].trim();
			}

			url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());

			Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
			link.setAddress(url);
			cell.setHyperlink(link);
			cell.setCellStyle(hlinkstyle);
		} catch ( Exception ex ) {
			System.out.println("Bad url:" + url);
		}
	}
	
	private static XSSFCellStyle getStyleBorderWithRotate(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setRotation((short) 90);
		style.setWrapText(true);
		return style;
	}
	
	private static XSSFCellStyle getStyleBorderWrap(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(true);
		return style;
	}

	private static XSSFCellStyle getStyleHyperLink(XSSFWorkbook workbook) {
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont hlinkfont = workbook.createFont();
		hlinkfont.setUnderline(XSSFFont.U_SINGLE);
		hlinkfont.setColor(IndexedColors.BLUE.index);
		style.setFont(hlinkfont);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(true);

		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);

		
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);

		return style;
	}
	
	
	private XSSFCell createCell(XSSFSheet sheet, int row, int col, String value, XSSFCellStyle style) {
		XSSFRow xrow = sheet.getRow(row);
		if ( xrow == null )
			xrow = sheet.createRow(row);

		XSSFCell cell = xrow.getCell(col);
		if ( cell == null )
			cell = sheet.getRow(row).createCell(col);

		cell.setCellValue(value);
		cell.setCellStyle(style);

		return cell;
	}

	void createBatchExcelFile(Prediction prediction,String filepathOut) {
		
		try {
			
			XSSFWorkbook workbook=new XSSFWorkbook();
			XSSFSheet sheet=workbook.createSheet("Predict1.0");
			XSSFCellStyle styleBorderWithRotate = getStyleBorderWithRotate(workbook);
			XSSFCellStyle styleBorderWithWrap = getStyleBorderWrap(workbook);

			Map<String, Endpoint> mapEndpointsById = getEndpointsMap(prediction);
			createHeaderRow(mapEndpointsById, sheet, styleBorderWithRotate, styleBorderWithWrap);
			writeRows(prediction, mapEndpointsById, sheet, styleBorderWithWrap);
			
			sheet.setColumnWidth(0, 40 * 256);
			sheet.getRow(0).setHeight((short)(15*256));
			
			FileOutputStream out = new FileOutputStream(filepathOut); 
			workbook.write(out);
		
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void writeRows(Prediction prediction, Map<String, Endpoint> mapEndpointsById, 
			XSSFSheet sheet, XSSFCellStyle styleBorderWithWrap) {
		
		XSSFCellStyle styleHlink = getStyleHyperLink(sheet.getWorkbook());
		DecimalFormat df=new DecimalFormat("0.00");

		int row=0;
		
		for(ChemicalData chemicalData:prediction.chemicals) {
			row++;
			String casName=chemicalData.chemical.casrn+"\n"+chemicalData.chemical.name;
			
			XSSFCell cellCASName=createCell(sheet, row, 0, casName, styleBorderWithWrap);
			
			String url="https://comptox.epa.gov/dashboard/dsstoxdb/results?search="+chemicalData.chemical.sid;
			setHyperlink(sheet.getWorkbook(), url, cellCASName, styleHlink);
							
			int col=0;
			
			for (String key:mapEndpointsById.keySet()) {
				
				boolean match=false;
				
				//they arent always in the same order so have to loop through them all to find matching endpoint:
				for(EndpointData endpointData:chemicalData.endpoints) {
					
					if(!endpointData.endpoint.id.equals(key)) continue;
					
					match=true;
					
					String experimental=null;
					if(endpointData.experimental!=null) {
						experimental = getValue(df, endpointData.endpoint,endpointData.experimental.get(0));
					}
					
					String predicted = getValue(df, endpointData.endpoint,endpointData.predicted.get(0));

					String val=null;
					if(experimental!=null) {
						val=experimental+"\n"+predicted;
					} else {
						val="\n"+predicted;
					}

//						if(chemicalData.chemical.casrn.equals("103-90-2")) {
//							System.out.println(chemicalData.chemical.casrn+"\t"+endpointData.endpoint.id+"\t"+experimental+"\t"+predicted);
//						}
						
					XSSFCell cell=createCell(sheet, row, ++col, val, styleBorderWithWrap);
					String urlReport="https://hazard-dev.sciencedataexperts.com/api/webtest/report?structure="+chemicalData.chemical.sid+"&endpoint="+endpointData.endpoint.id;
//						setHyperlink(workbook, urlReport, cell, styleHlink);
					
				}
				
				if(!match)col++;
				
			}
			
		}
	}

	private void createHeaderRow(Map<String, Endpoint> mapEndpointsById, XSSFSheet sheet,
			XSSFCellStyle styleBorderWithRotate, XSSFCellStyle styleBorderWithWrap) {
		
		createCell(sheet, 0, 0, "CAS\nName", styleBorderWithWrap);
		
		int col=0;
		for (String key:mapEndpointsById.keySet()) {
			Endpoint endpoint=mapEndpointsById.get(key);
			
			String headerName=endpoint.name;
			
			String units="";
			if(endpoint.logMolar) {
				units=endpoint.logUnits;
			} else if(endpoint.units!=null) {
				units=endpoint.units;
			} else {
				units="Binary";
			}
			headerName+="\n"+units;
			
			createCell(sheet, 0, ++col, headerName, styleBorderWithRotate);	
		}
	}

	private String getValue(DecimalFormat df, Endpoint endpoint, Results results) {

		String value;
		
		if(endpoint.logMolar) {
			if(results.logValue!=null)						
				value=df.format(results.logValue);
			else
				value="";
			
		} else if(endpoint.binary) {
			if(results.active!=null) {
				if(results.active) value="A";
				else value="N";
			} else {
				value="I";
			}
		} else {
			if(results.value!=null) {
				value=df.format(results.value);	
			} else {
				value="";
			}
			
		}
		
		return value;
	}

	private Map<String, Endpoint> getEndpointsMap(Prediction prediction) {
		List<EndpointData>endpointDatas=prediction.chemicals.get(0).endpoints;
		Map<String,Endpoint>mapEndpointsById=new LinkedHashMap<>();//preserves insertion order
		for(EndpointData endpointData:endpointDatas) {
			endpointData.endpoint.name=endpointData.endpoint.name.replace("Â°C","°C");
			if(endpointData.endpoint.units!=null)
				endpointData.endpoint.units=endpointData.endpoint.units.replace("Â°C","°C");
			mapEndpointsById.put(endpointData.endpoint.id,endpointData.endpoint);
		}
		
//		System.out.println(gson.toJson(mapEndpointsById));
		return mapEndpointsById;
	}
	
	
	
	class APIInput {
		String format="JSON";
		List<String>structures;
	}
	
	
	private void runFromCheminformaticsPredictAPI(List<String>structures) {

		Unirest.setTimeouts(0, 0);
		
//		Set<String> artifactoryLoggers = new HashSet<String>(Arrays.asList("org.apache.http", "groovyx.net.http"));
//		for(String log:artifactoryLoggers) {
//			ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(log);
//			artLogger.setLevel(ch.qos.logback.classic.Level.INFO);
//			artLogger.setAdditive(false);
//		}
		
		try {
			
			APIInput ai=new APIInput();
			ai.structures=structures;
			
			HttpResponse<String> response = Unirest.post("https://hcd.rtpnc.epa.gov/api/webtest/predict")
			  .header("Content-Type", "application/json")
			  .body(gson.toJson(ai))
			  .asString();
			
			System.out.println(response.getBody().toString());
			
			Prediction prediction=gson.fromJson(response.getBody().toString(), Prediction.class);
			
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\nta\\";
			String filepathOut=folder+"testBob.xlsx";
			createBatchExcelFile(prediction,filepathOut);
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	

	private void runFromFile() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\nta\\";
		String filepath=folder+"INTERPRET_NTA_confirmed_IDs.json";
		
		try {
			Prediction prediction=gson.fromJson(new FileReader(filepath), Prediction.class);
			
			String filePathOut=filepath.replace(".json",".xlsx");
			createBatchExcelFile(prediction,filePathOut);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CheminformaticsModulesPredictApi c=new CheminformaticsModulesPredictApi();
//		c.runFromFile();
		
		c.runFromCheminformaticsPredictAPI(Arrays.asList("100-44-7","84-74-2","100-41-4"));
		
	}

}
