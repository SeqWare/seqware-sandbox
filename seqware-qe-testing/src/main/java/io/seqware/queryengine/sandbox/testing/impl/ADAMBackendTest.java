/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author boconnor
 */
public class ADAMBackendTest implements BackendTestInterface {

  public ReturnValue getIntrocutionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue setupBackend(HashMap<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue loadFeatureSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue loadReadSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue getFeatures(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue getReads(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue runPlugin(String queryJSON, String pluginClassName) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue getConclusionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public ReturnValue teardownBackend(HashMap<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  public String getName() {
    return("ADAMBackendTest");
  }


  
}
