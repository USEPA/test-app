package caesar.model.DevTox;

import caesar.core.datatype.Description;
import caesar.core.datatype.Version;
import caesar.core.exception.InitFailureException;
import caesar.core.exception.InvalidParameterException;
import caesar.model.common.ModelDataset;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;


/**
 * Main class for the Developmental Toxicity classification model.
 * Extends caesar.core.model.CaesarModel as required in the Caesar framework.
 * 
 * @version 1.0.0.9
 * @author  Antonio Cassano and Alberto Manganaro,
 *          Laboratory of Environmental Toxicology and Chemistry,
 *          Istituto di Ricerche Farmacologiche "Mario Negri", Milano, Italy
 */
public class CaesarModelDevTox  {

    //// Parameters needed for the model

    public final static String KEY_MOLECULEFILE = "MoleculeFile";
    public final static String KEY_MOLECULETYPE = "MoleculeType";
    public final static String KEY_MOLECULENUM = "MoleculeNum";

    // Consts for the definition of the model
    private final static String ModelName = "DevTox model";
    private final static String ModelAuthor = "ist. Mario Negri";
    private final static String ModelDate = "01/03/2009";
    private final static String ModelVer = "1.0.0.9";
    private final static String ModelDescShort = "QSAR classification model for" +
      " Developmental Mutagenicity";
    private final static String ModelDescLong = "QSAR classification model for" +
      " Developmental Mutagenicity based on a Random Forest classification. " +
      "Developed by Istituto Mario Negri, Italy.";

    // Number precision for output
//    private static final String STD_FORMAT = "0.###";

    //// Global private variables

    private Version ModelVersion;
    private Description ModelDescription;

//    private byte[] MoleculeFile;       // content of the molecule file
//    private String MoleculeType;       // extension of the molecule file
//    private int MoleculeNum;           // number of molecules in the file
//    private String[] MoleculeSMILES;   // SMILES of the molecules
//    private Molecule[] MoleculeStruct; // Molecule objects of the set
//    private BitSet[] MoleculeFP;       // FPs of the molecule

//    private boolean[] Results;
//    private ModelResults ResultsObj;
    private ModelDataset modelDataset;
//    private Matrix Descriptors;
    
    public Integer Tox=null;
    public String Warning="";

    private Boolean SetupAvailable;
    private Boolean ModelCalculated;


//    private DragonDevToxExecute Dragon;
    private modelRandomForest Model;


    /**
     * Constructor of the class
     *
     */
    public CaesarModelDevTox() {

        // Builds the current version and description objects

        Date d;
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            d = df.parse(ModelDate);
        } catch (ParseException ex) {
            d = null;
        }
        ModelVersion = new Version(ModelVer, ModelAuthor, d, "none");
        ModelDescription = new Description(ModelDescShort, ModelDescLong);


        // Initializes local variables

        SetupAvailable = false;
        ModelCalculated = false;

        Model = null;
        
        try {
        	initModel(null);
        } catch (Exception e) {
        	e.printStackTrace();
        }
//        Dragon = null;

    }

    public boolean initModel(Hashtable arg0) throws InitFailureException, InvalidParameterException {

        try {

            // Creates the model object
            Model = new modelRandomForest();

            // Reads training set
            URL uTrain, uFunct, uFP, uConst;
            uTrain = getClass().getResource("/caesar/model/DevTox/devtox_descriptors.csv");
            uFunct = getClass().getResource("/caesar/model/DevTox/devtox_functional_groups.csv");
            uConst = getClass().getResource("/caesar/model/DevTox/devtox_constitutional.csv");
            uFP = getClass().getResource("/caesar/model/DevTox/devtox_fp.csv");

            
            modelDataset = new ModelDataset(uTrain.openStream(), 
                uFunct.openStream(), uConst.openStream() , uFP.openStream(), 13);
            

            ModelCalculated = false;
            SetupAvailable = true;

        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new InitFailureException(
                "Unable to load model parameters from local file");
        }

        return true;

    }
    
    
    public void calculateDevTox(Hashtable<String,Object> htDescriptors) throws Exception {

//        Results = new boolean[1];
//        ResultsObj = new ModelResults();
//        ResultsObj.Results = new String[Results.length];
//        ResultsObj.Warnings = new String[Results.length];

        // Calculate descriptors
        String [] selectedDescriptors = {"icycem","BEHm1","BELp3","BELv1","BELv8"
           ,"GATS1p","GATS2m","GATS3v","MATS1p","MATS4p","MATS4v","SdssC","SHssNH"};

        double [] Descriptors = new double [selectedDescriptors.length];
        
        for (int j=0; j<selectedDescriptors.length; j++)
            Descriptors[j]= (Double)htDescriptors.get(selectedDescriptors[j]);

        // Creates the ARFF file
        String header = "@relation model_test"+"\n"+"\n"+"@attribute SdssC numeric"+
            "\n"+"@attribute SHssNH numeric"+"\n"+"@attribute icycem" +
            " numeric"+"\n"+"@attribute BEHm1 numeric"+"\n"+"@attribute " +
            "BELv1 numeric"+"\n"+"@attribute BELv8 numeric"+"\n"+
            "@attribute" +" BELp3 numeric"+"\n"+"@attribute MATS4v numeric"
            +"\n"+"@attribute MATS1p numeric"+"\n"+"@attribute MATS4p" +
            " numeric"+"\n"+"@attribute GATS2m numeric"+"\n"+"@attribute" +
            " GATS3v" +" numeric"+"\n"+"@attribute GATS1p numeric"+"\n"+
            "@attribute" +" Tox {N,D}"+"\n"+"\n"+"@data"+"\n";

        byte[] Input_ARFF = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        BufferedWriter bw = new BufferedWriter(osw);
        
        bw.write(header);
        bw.write(Double.toString((Double)htDescriptors.get("SdssC")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("SHssNH")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("icycem")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("BEHm1")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("BELv1")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("BELv8")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("BELp3")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("MATS4v")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("MATS1p")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("MATS4p")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("GATS2m")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("GATS3v")));
        bw.write(",");
        bw.write(Double.toString((Double)htDescriptors.get("GATS1p")));
        bw.write(",");
        bw.write("?");

        bw.close();

        Input_ARFF = baos.toByteArray();
        
        boolean boolTox;
        
        try {
        	boolTox = Model.ExecuteModel(Input_ARFF);
        }catch (Exception e) {
        	e.printStackTrace();
        	throw new InvalidParameterException("Unable to execute model");
        }
        // Builds and returns the results object

        
        if (boolTox) Tox=1;
        else Tox=0;
        
        Warning="OK";
        for (int j=0; j<13; j++) {
//        	System.out.println(selectedDescriptors[j]+"\t"+modelDataset.Desc_Ranges[0][j]+"\t"+modelDataset.Desc_Ranges[1][j]);
        	
            if ( (Math.round(Descriptors[j]*100) < Math.round((modelDataset.Desc_Ranges[0][j])*100)) ||
                (Math.round(Descriptors[j]*100) > Math.round((modelDataset.Desc_Ranges[1][j])*100)) ) {
            	Warning="Descriptors for this compound have values outside the descriptor range for the compounds of the training set.";
            	
                break;
            }
        }
//        System.out.println(Warning);
        ModelCalculated = true;

    }
    
    
    public String getModelName() {
        return ModelName;
    }

    public Version getModelVersion() {
        return ModelVersion;
    }

    public Description getModelDescription() {
        return ModelDescription;
    }

    
}
