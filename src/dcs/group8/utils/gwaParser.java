package dcs.group8.utils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class gwaParser {

	public static void main(String[] args) {
		Connection c = null;
		Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:anon_jobs.db3");
	      c.setAutoCommit(false);
	      
	      PrintWriter u1 = new PrintWriter("u1.txt", "UTF-8");
	      PrintWriter u2 = new PrintWriter("u2.txt", "UTF-8");
	      
	      int startTime = 1133308800;
	      int endTime = startTime + 60*60*2;  // 2 hours
	      int totalJobs = 0;
	      stmt = c.createStatement();
	      // Jobs less than 15 minutes
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM Jobs where Jobs.RunTime <= 900 and Jobs.SubmitTime > " + startTime + " and Jobs.SubmitTime < "+endTime+";" );
	      while ( rs.next() ) {
	    	 totalJobs++;
	         int submitTime = rs.getInt("SubmitTime") - startTime;
	         int runTime = rs.getInt("RunTime");
         
	         if (Math.random() < 0.5 ) {
	        	 u1.println(submitTime + ";" + runTime);
	         } else {
	        	 u2.println(submitTime + ";" + runTime);
	         }
	         
	      }
	      
	      rs.close();
	      stmt.close();
	      c.close();
	      u1.close();
	      u2.close();
	      System.out.println(totalJobs);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}

}
