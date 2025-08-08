package ToxPredictor.Application.Calculations.CreateLookups;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF.APIMolecule;

/**
 * @author TMARTI02
 */
public class CreateSnapshotDatabaseFromSDFs {

	void createDB() {

		String snapshot = "snapshot-2025-07-30";
		String folderPath = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\"
				+ snapshot; // Replace with your actual folder path
		String dbUrl = "databases\\" + snapshot + ".db";

		// Create table if it doesn't exist
		try {

			Connection connection = MySQL_DB.getConnection(dbUrl);

			// Start transaction
			connection.setAutoCommit(false);

			
			Statement stat=connection.createStatement();
			
			String tableName="substances";
			
			stat.executeUpdate("drop table if exists "+tableName+";");

			String sqlTableCreate="CREATE TABLE IF NOT EXISTS "+tableName+
					" (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"sid TEXT, "
					+"cid TEXT, "
					+ "name TEXT, "
					+ "casrn TEXT, "
					+ "inchiKey TEXT, "
					+ "inchiKey1 TEXT, "
					+"inchi TEXT, "
					+ "smiles TEXT, "
					+ "mol TEXT);";
			
			stat.executeUpdate(sqlTableCreate);
			
			String sqlIndexes="CREATE INDEX IF NOT EXISTS idx_sid ON "+tableName+"(sid);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_cid ON "+tableName+"(cid);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_casrn ON "+tableName+"(casrn);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_name ON "+tableName+"(name);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_inchiKey ON "+tableName+"(inchiKey);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_inchiKey1 ON "+tableName+"(inchiKey1);\n" +
                    "CREATE INDEX IF NOT EXISTS idx_smiles ON "+tableName+"(smiles);"; 
//			System.out.println(sqlIndexes);
			stat.executeUpdate(sqlIndexes);
			

			PreparedStatement prepInsert = connection.prepareStatement(
					"INSERT INTO "+tableName+" (sid, cid, name, casrn, inchiKey, inchiKey1, inchi, smiles, mol) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			int batchSize = 1000;
			int batchCount = 0;

			// Iterate through all .sdf files in the folder
			File folder = new File(folderPath);
			
			int countAll=0;
			

			for (File file : Objects.requireNonNull(folder.listFiles((dir, name) -> name.endsWith(".sdf")))) {

				System.out.println(file.getAbsolutePath());

				List<APIMolecule> mols = RunFromSDF.readSDF_to_API_Molecules(file.getAbsolutePath(), -1, false);

				for (APIMolecule molecule : mols) {

					// Extract properties
					String sid = (String) molecule.htProperties.get("DTXSID");
					String cid = (String) molecule.htProperties.get("DTXCID");
					String name = (String) molecule.htProperties.get("PREFERRED_NAME");
					String casrn = (String) molecule.htProperties.get("CASRN");

					String inchiKey = (String) molecule.htProperties.get("INCHIKEY");
					
					if(inchiKey==null) inchiKey="NA";
					
					if(casrn==null) {
						System.out.println(file.getName()+"\t"+sid);
					}
					
					
					String inchiKey1 = null;
					if (inchiKey.length() <= 14) {
//						System.out.println(sid + "\t" + inchiKey);
						inchiKey1 = "NA";
					} else {
						inchiKey1 = inchiKey.substring(0, 14);
					}

					String inchi = (String) molecule.htProperties.get("INCHI_STRING");
					String smiles = (String) molecule.htProperties.get("SMILES");
					String mol = molecule.strStructure;

//					System.out.println(sid + "\t" + casrn);

					// Insert molecule into the database

					prepInsert.setString(1, sid);
					prepInsert.setString(2, cid);
					prepInsert.setString(3, name);
					prepInsert.setString(4, casrn);
					prepInsert.setString(5, inchiKey);
					prepInsert.setString(6, inchiKey1); // Insert inchiKey1
					prepInsert.setString(7, inchi);
					prepInsert.setString(8, smiles);
					prepInsert.setString(9, mol);
					prepInsert.addBatch();

					batchCount++;
					countAll++;
					
					if(countAll%10000==0) System.out.println(countAll);

					if (batchCount == batchSize) {
						prepInsert.executeBatch();
						connection.commit();
//						System.out.println("Committed " + batchCount + " records.");
						batchCount = 0;
					}

				}

				prepInsert.executeBatch();
				System.out.println("Committed remaining " + batchCount % batchSize + " records.");

			}

			connection.commit();
			
			connection.setAutoCommit(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		CreateSnapshotDatabaseFromSDFs c = new CreateSnapshotDatabaseFromSDFs();
		c.createDB();

	}

}
