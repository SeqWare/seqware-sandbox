/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
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
public class SamtoolsBackendTest implements BackendTestInterface {

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
  
}
