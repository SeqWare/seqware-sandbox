/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author boconnor
 */
public class ADAMBackendTest implements BackendTest {

   /*
   * Not supported 
   */
  public String loadFile(String file) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public ArrayList<String> getRange(String featureSetName, String contig, int start, int stop) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public ArrayList<String> intersect(String featureSetName1, String featureSetName2) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /*
   * Not supported really
   */
  public ArrayList<String> mapReducePluginTest(String mrPluginClassName) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  // here is an example query based on one of the Hue Beeswax sample tables 
        private static final String SQL_STATEMENT = "SELECT id FROM features limit 100";
        private static final String SQL_STATEMENT2 = "use featurestore";
        
        // set the impalad host
        private static final String IMPALAD_HOST = "localhost";
        
        // port 21050 is the default impalad JDBC port 
        private static final String IMPALAD_JDBC_PORT = "21050";

        private static final String CONNECTION_URL = "jdbc:hive2://" + IMPALAD_HOST + ':' + IMPALAD_JDBC_PORT + "/;auth=noSasl";

        private static final String JDBC_DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";

        public static void main(String[] args) {

                System.out.println("\n=============================================");
                System.out.println("Cloudera Impala JDBC Example");
                System.out.println("Using Connection URL: " + CONNECTION_URL);
                System.out.println("Running Query: " + SQL_STATEMENT);

                Connection con = null;

                try {

                        Class.forName(JDBC_DRIVER_NAME);

                        con = DriverManager.getConnection(CONNECTION_URL);

                        Statement stmt = con.createStatement();

                        stmt.execute(SQL_STATEMENT2);

                        ResultSet rs = stmt.executeQuery(SQL_STATEMENT);

                        System.out.println("\n== Begin Query Results ======================");

                        // print the results to the console
                        while (rs.next()) {
                                // the example query returns one String column
                                System.out.println(rs.getString(1));
                        }

                        System.out.println("== End Query Results =======================\n\n");

                } catch (SQLException e) {
                        e.printStackTrace();
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        try {
                                con.close();
                        } catch (Exception e) {
                                // swallow
                        }
                }
        }
  
}
