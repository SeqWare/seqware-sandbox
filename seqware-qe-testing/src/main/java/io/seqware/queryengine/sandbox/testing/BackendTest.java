/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing;

import java.util.ArrayList;

/**
 *
 * @author boconnor
 */
public interface BackendTest {
  
  /**
   * Point to a tsv input file, return the ID to access that data again
   * @param file
   * @return 
   */
  public String loadFile(String file);
  
  /**
   * Return array list of "chr:start-stop" for the features that overlap that range in that featureSet
   * @param featureSetName
   * @param start
   * @param stop
   * @return 
   */
  public ArrayList<String> getRange(String featureSetName, String contig, int start, int stop);
  
  /**
   * Return arrayList of strings of the same format as above from featureSet1 that overlap with featureSet2 features
   * @param featureSetName1
   * @param featureSetName2
   * @return 
   */
  public ArrayList<String> intersect(String featureSetName1, String featureSetName2);
  
  /**
   * Run the specified M/R plugin and return the result
   * @param mrPluginClassName
   * @return 
   */
  public ArrayList<String> mapReducePluginTest(String mrPluginClassName);
  
}
