/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import static io.seqware.queryengine.sandbox.testing.BackendTestInterface.DOCS;
import static io.seqware.queryengine.sandbox.testing.BackendTestInterface.FEATURE_SET_ID;
import static io.seqware.queryengine.sandbox.testing.BackendTestInterface.PLUGIN_RESULT_FILE;
import static io.seqware.queryengine.sandbox.testing.BackendTestInterface.QUERY_RESULT_FILE;
import static io.seqware.queryengine.sandbox.testing.BackendTestInterface.READ_SET_ID;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a silly back-end just for me to code the testing framework against
 * @author dyuen
 */
public class NoOpBackendTest implements BackendTestInterface {

  @Override
  public ReturnValue getIntroductionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    rt.getKv().put(DOCS, "<p>Introduction</p>");
    return(rt);
  }

  @Override
  public ReturnValue setupBackend(Map<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue loadFeatureSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    rt.getKv().put(FEATURE_SET_ID,  String.valueOf((new Random()).nextInt()));
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue loadReadSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    rt.getKv().put(READ_SET_ID,  String.valueOf((new Random()).nextInt()));
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getFeatures(String queryJSON) {
    ReturnValue rt = new ReturnValue();
      File createTempFile;
      try {
          createTempFile = File.createTempFile("test", "out");
      } catch (IOException ex) {
          throw new RuntimeException(ex);
      }
      rt.getKv().put(QUERY_RESULT_FILE, createTempFile.getAbsolutePath());
      rt.setState(ReturnValue.SUCCESS);
      return (rt);
  }

  @Override
  public ReturnValue getReads(String queryJSON) {
    ReturnValue rt = new ReturnValue();
      File createTempFile;
      try {
          createTempFile = File.createTempFile("test", "out");
      } catch (IOException ex) {
          throw new RuntimeException(ex);
      }
      rt.getKv().put(QUERY_RESULT_FILE, createTempFile.getAbsolutePath());
      rt.setState(ReturnValue.SUCCESS);
      return (rt);
  }

  @Override
  public ReturnValue runPlugin(String queryJSON, Class pluginClass) {
      ReturnValue rt = new ReturnValue();
      File createTempFile;
      try {
          createTempFile = File.createTempFile("test", "out");
      } catch (IOException ex) {
          throw new RuntimeException(ex);
      }
      rt.getKv().put(PLUGIN_RESULT_FILE, createTempFile.getAbsolutePath());
      rt.setState(ReturnValue.SUCCESS);
      return (rt);
  }

  @Override
  public ReturnValue getConclusionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    rt.getKv().put(DOCS, "<p>Conclusion</p>");
    return(rt);
  }

  @Override
  public ReturnValue teardownBackend(Map<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public String getName() {
    return("ADAMBackendTest");
  }

  public ReturnValue teardownBackend(HashMap<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }


  
}