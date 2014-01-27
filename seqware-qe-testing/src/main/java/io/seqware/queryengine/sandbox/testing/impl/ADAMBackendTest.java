/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author boconnor
 */
public class ADAMBackendTest implements BackendTestInterface {

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
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue loadReadSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getFeatures(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getReads(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue runPlugin(String queryJSON, String pluginClassName) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
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