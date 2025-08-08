package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.io.File;
import java.text.DecimalFormat;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.util.FileUtils;

import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.AtomContainer;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.TaskCalculations2;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.HueckelAromaticityDetector;

public class RunFromCommandLine {

	private static final Logger logger = LogManager.getLogger(RunFromCommandLine.class);
	DescriptorFactory df=new DescriptorFactory(false);
	static DecimalFormat d2 = new DecimalFormat("0.00");
	
	public void go(CommandLine commandLine) {

		String inputFilePath=commandLine.getOptionValue("i");
		String outputFilePath=commandLine.getOptionValue("o");
		String endpointAbbrev=commandLine.getOptionValue("e");
		String methodAbbrev=commandLine.getOptionValue("m");
		
		boolean hasShow=commandLine.hasOption("s");		
		
//		String reportType=commandLine.getOptionValue("r");

		System.out.println("hasShow="+hasShow);
		System.out.println("inputFilePath="+inputFilePath);
		System.out.println("outputFilePath="+outputFilePath);
		System.out.println("endpointAbbrev="+endpointAbbrev);
		System.out.println("methodAbbrev="+methodAbbrev);
//		System.out.println("reportType="+reportType);
		
		
		String ext=FileUtils.getFilenameExt(inputFilePath).toLowerCase();
		HueckelAromaticityDetector.debug=false;//dont print a ton of messages
		IAtomContainerSet acs=null;
		
		if (ext.contentEquals("smi")) {
			acs=TaskStructureSearch.LoadFromList(inputFilePath,TaskStructureSearch.TypeSmiles);
		} else if (ext.contentEquals("sdf") || ext.contentEquals("mol")) {
			acs=TaskStructureSearch.LoadFromSDF(inputFilePath);				
		} else if (ext.contentEquals("txt")) {
			acs=TaskStructureSearch.LoadFromList(inputFilePath,TaskStructureSearch.TypeAny);
		}

		if (acs==null || acs.getAtomContainerCount()==0) {
			logger.error("error loading molecules from "+inputFilePath);	
			return;
		} else {
			logger.info(acs.getAtomContainerCount()+" molecules loaded from file");			
		}
		

		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			ac.setProperty("Index", i+1);
//			System.out.println(ac.getProperty("Index")+"");
		}
		
		String method=TESTConstants.getFullMethod(methodAbbrev);
		String endpoint=TESTConstants.getFullEndpoint(endpointAbbrev);
		
		TaskCalculations2 tc2=new TaskCalculations2();
		boolean useFragmentsConstraint=false;
		
//		reportType=TESTConstants.strExcel;
		
		int taskType=TESTConstants.typeTaskBatch;
		int runType=TESTConstants.typeRunEndpoint;
		
		File fileOutputFolder=new File(outputFilePath);
		
		if (fileOutputFolder.getName().contains(".")) fileOutputFolder=fileOutputFolder.getParentFile();
		
		boolean runCTS=false;
		String libraryCTS="";

		TESTApplication f=new TESTApplication();
		
		
		String extOut=FileUtils.getFilenameExt(outputFilePath).toLowerCase();
		
		boolean createReports=false;
		boolean createDetailedReports=false;
		boolean generateWebpages=false;
		
		if (extOut.contains(".htm")) {
			createReports=true;
			generateWebpages=true;
		}
		
		tc2.init(acs, useFragmentsConstraint, createReports,createDetailedReports,generateWebpages,fileOutputFolder, 
				f, endpoint, method, taskType, runType, runCTS, libraryCTS);
		
		if (hasShow) f.panelResults.setVisible(true);
		f.panelResults.jbSaveToExcel.setVisible(false);
		f.panelResults.jbSaveToText.setVisible(false);
//		f.panelResults.jbSaveToHTML.setVisible(false);
		
		if (!endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			f.panelResults.setTitle("Prediction results: "+endpoint);
			f.panelResults.initTableModel(endpoint, method,true);		
		} else {
			f.panelResults.setTitle("Descriptor values");
			f.panelResults.initTableModelDescriptors();
		}

		TaskCalculations2.commandLineOutputFile=new File(outputFilePath);
		tc2.go();
		
	}
	
	
	
	public static Options createOptions() {
		Options options = new Options();

		Option o = new Option("i", "input", true, "input file (MOL, SDF, SMI, or TXT)");
		o.setRequired(true);
		options.addOption(o);

		options.addOption("o", "output", true, "output tab delimited text file");

		o = new Option("m", "method", true, "abbreviated QSAR method (hc, fda, sm, nn, gc, rf, lda, consensus)");
		// o.setArgs(8);
		options.addOption(o);
		
//		o = new Option("r", "Report type", true, "report type (XLSX,CSV,HTML)");
//		// o.setArgs(8);
//		options.addOption(o);
				

		o = new Option("e", "endpoint", true,
				"abbreviated endpoint (LC50, LC50DM, IGC50, LD50, EC50GA, BCF, DevTox, Mutagenicity, ER_Binary, ER_LogRBA, BP, VP, MP, Density, FP, ST, TC, Viscosity, WS)");
		// o.setArgs(19);
		options.addOption(o);
		
		
		o = new Option("s", "show", false,
				"show calculation table");
		o.setRequired(false);
		options.addOption(o);


		return options;
	}
	
	public static void main(String[] args) {

		CommandLine commandLine;
        Options options = createOptions();

        try
        {
//			for (int i=0;i<args.length;i++) {
//				System.out.println(i+"\t"+args[i]);
//			}
        	CommandLineParser parser = new DefaultParser();			
            commandLine = parser.parse(options, args);
            
            RunFromCommandLine r=new RunFromCommandLine();
            r.go(commandLine);
//            System.exit(0);

        }
        catch (ParseException exception)
        {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
	}

}
