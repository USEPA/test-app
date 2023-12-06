package ToxPredictor.Application;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.AtomContainer;

import ToxPredictor.Utilities.TESTPredictedValue;

public class Task_TEST_Prediction_All_Endpoints implements Runnable {

//	SwingWorker worker;

	AtomContainer ac=null;
	List<String> endpoints=null;
	List<String> methods=null;
	Set<WebReportType> wrt=null;
	Connection conn=null;
	
	public void init(AtomContainer ac,List<String> endpoint,List<String> method,Set<WebReportType> wrt,Connection conn) throws Exception {

        try {
        	this.ac=ac;
        	this.endpoints=endpoint;
        	this.methods=method;
        	this.wrt=wrt;
        	this.conn=conn;
        	
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	@Override
	public void run() {

		try {
			CalculationParameters cp=new CalculationParameters(null, null, endpoints, methods, wrt);
			
			List<TESTPredictedValue> listTPV = WebTEST5.go3(ac,cp,conn);

    	} catch (Exception ex){
    		ex.printStackTrace();
    	}

	}
	
	

	


}
