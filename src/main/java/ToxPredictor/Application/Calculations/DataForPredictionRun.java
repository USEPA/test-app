package ToxPredictor.Application.Calculations;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import QSAR.qsarOptimal.AllResults;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.Lookup.ExpRecord;
import wekalite.Instance;
import wekalite.Instances;

public class DataForPredictionRun {	
	public String DescriptorSet;
	
//	Instances evalInstances2d;
//	Instances trainingDataSet2d;
//	AllResults allResults;
//
//	Instances evalInstancesFrag;
//	Instances trainingDataSetFrag;
//	AllResults allResultsFrag;
	
	public String endpoint;
	public String abbrev;
	public boolean isBinaryEndpoint;	
	public boolean isLogMolarEndpoint;
	public boolean useFragmentsConstraint;

//	String method;
//	ArrayList <String>methods;
	
	public String CAS;
//	public String gsid;
	public String dtxcid;
	public String dtxsid;

	public Lookup.ExpRecord er;
	public double MW;
	public double MW_Frag;
	public Hashtable<Double, Instance> htTestMatch;
	public Hashtable<Double, Instance> htTrainMatch;

	public boolean createDetailedReport;
	public ReportOptions reportOptions;
	public Set<WebReportType> reportTypes;
//	public Statement statNCCT_ID;
	
	public DataForPredictionRun(String descriptorSet, 
//			Instances evalInstances2d, Instances trainingDataSet2d,	AllResults allResults, 
//			Instances evalInstancesFrag, Instances trainingDataSetFrag,	AllResults allResultsFrag, 
			String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, boolean useFragmentsConstraint, 
			String CAS, String dtxcid,String dtxsid, ExpRecord er,
			double MW, double MW_Frag, Hashtable<Double, Instance> htTestMatch,
			Hashtable<Double, Instance> htTrainMatch, boolean createDetailedReport, ReportOptions reportOptions,
			Set<WebReportType> reportTypes) {
		super();
		DescriptorSet = descriptorSet;
//		this.evalInstances2d = evalInstances2d;
//		this.trainingDataSet2d = trainingDataSet2d;
//		this.allResults = allResults;
//		this.evalInstancesFrag = evalInstancesFrag;
//		this.trainingDataSetFrag = trainingDataSetFrag;
//		this.allResultsFrag = allResultsFrag;
		this.endpoint = endpoint;
		this.abbrev = abbrev;
		this.isBinaryEndpoint = isBinaryEndpoint;
		this.isLogMolarEndpoint = isLogMolarEndpoint;
		this.useFragmentsConstraint = useFragmentsConstraint;
		this.CAS = CAS;
		this.dtxcid = dtxcid;
		this.dtxsid = dtxsid;
		this.er = er;
		this.MW = MW;
		this.MW_Frag = MW_Frag;
		this.htTestMatch = htTestMatch;
		this.htTrainMatch = htTrainMatch;
		this.createDetailedReport = createDetailedReport;
		this.reportOptions = reportOptions;
		this.reportTypes = reportTypes;
//		this.statNCCT_ID = statNCCT_ID;
	}
	

}
