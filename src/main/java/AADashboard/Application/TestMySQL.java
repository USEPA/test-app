package AADashboard.Application;

import java.sql.*;
import java.util.Calendar;

public class TestMySQL {


	/**
	 * A Java MySQL PreparedStatement INSERT example.
	 * Demonstrates the use of a SQL INSERT statement against a
	 * MySQL database, called from a Java program, using a
	 * Java PreparedStatement.
	 * 
	 * Created by Alvin Alexander, http://alvinalexander.com
	 */
	
	  public static void main(String[] args)
	  {
	    try
	    {
	    	
	    Connection Conn = DriverManager.getConnection 
	    			("jdbc:mysql://localhost/test"); 

 
	    Statement s=Conn.createStatement(); 

	    int Result=s.executeUpdate("CREATE DATABASE bob");

	    	
//	      // create a mysql database connection
//	      String myDriver = "com.mysql.jdbc.Driver";
//	      String myUrl = "jdbc:mysql://localhost/test";
//	      Class.forName(myDriver);
//	      Connection conn = DriverManager.getConnection(myUrl, "root", "");
//	    
//	      // create a sql date object so we can use it in our INSERT statement
//	      Calendar calendar = Calendar.getInstance();
//	      java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());
//
//	      // the mysql insert statement
//	      String query = " insert into users (first_name, last_name, date_created, is_admin, num_points)"
//	        + " values (?, ?, ?, ?, ?)";
//
//	      // create the mysql insert preparedstatement
//	      PreparedStatement preparedStmt = conn.prepareStatement(query);
//	      preparedStmt.setString (1, "Barney");
//	      preparedStmt.setString (2, "Rubble");
//	      preparedStmt.setDate   (3, startDate);
//	      preparedStmt.setBoolean(4, false);
//	      preparedStmt.setInt    (5, 5000);
//
//	      // execute the preparedstatement
//	      preparedStmt.execute();
//	      
//	      conn.close();
	    }
	    catch (Exception e)
	    {
	      System.err.println("Got an exception!");
	      System.err.println(e.getMessage());
	    }
	  }
	
}
