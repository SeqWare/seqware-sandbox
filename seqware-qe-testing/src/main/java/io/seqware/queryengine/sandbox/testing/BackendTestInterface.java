/**
 * This interface defines the things we want any backend to be able to support.
 * The relative challenge for making backend objects that implement this interface
 * should be really helpful in deciding what tools we will use in long run.
 * 
 * Right now the backend just talks about FeatureSets (see sample TSV file).
 * In the future we'll support both FeatureSets (via VCF files) and ReadSets (via BAM files).
 * 
 * Keep in mind a few general points about this interface:
 * 
 * <ul>
 *   <li>all methods return a ReturnValue</li>
 *   <li>ReturnValue contains a HashMap used to return key-values that encode info used by the testing system</li>
 *   <li>see the Javadoc below for the particular kev-values that should be populated for each method</li>
 * </ul>
 * 
 * The testing process is triggered by the TestBackends object in the following order:
 * 
 * <ul>
 *   <li>getDocs(): these are added to the report</li>
 *   <li>setupBackend(): this is where you check to make sure all you dependencies are installed, then you setup</li>
 *   <li>loadFile(): this is called several times by the TestBackends object to load multiple featureSets</li>
 *   <li></li>
 *   <li>teardownBackend(): this is where you cleanup</li>
 * </ul>
 * 
 * Notes about this interface and how to implement it are included in the comments below.
 */
package io.seqware.queryengine.sandbox.testing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author boconnor
 */
public interface BackendTestInterface {
    
  /**
   * This is a simple method that returns documentation about the particular backend.
   * This is your place to add docs about:
   * <ul>
   *   <li>how to setup the backend (on Ubuntu 12.04 like we use for SeqWare)</li>
   *   <li>what you felt the backend was good at?</li>
   *   <li>what you felt the backend was bad at?</li>
   *   <li>do you think we should invest more time in this backend?</li>
   *   <li>if yes to the previous. what's the next step for this backend?</li>
   * </ul>
   * 
   * Put the docs (as an HTML fragment) in the key-value in ReturnValue
   * with the key-value "docs=content".
   */
  public ReturnValue getDocs();
    
  /**
   * This method is called at the beginning of the test process.  It's where you
   * should test that the backend is installed correctly e.g. HBase is running, you can
   * connect, etc.  The settings hashmap will be passed in by the TestBackends object, 
   * you can customize it there.
   * 
   * ReturnValue should just contain a state value, no key-value hash needed.
   * 
   * If the dependencies for this backend are not setup e.g. the DB daemon is not 
   * running return BACKEND_NOT_SETUP
   * 
   * If the setup for this backend test failed return BACKEND_SETUP_FAILURE
   */
  public ReturnValue setupBackend(HashMap<String, String> settings);
  
  /**
   * Point to an input variant file, return the ID to access that data again in subsequent tests
   * with kv of "featureSetId=<string>" in the ReturnValue object.
   * 
   * The file type is specified with the extension of the file param:
   * 
   * <ul>
   *   <li>*.vcf is assumed to be a VCF file</li>
   *   <li>*.tsv is assumed to be a TSV (tab separated value) file</li>
   * </ul>
   * 
   * TODO: we only currently support TSV files
   * 
   * ReturnValue state should be BACKEND_FILE_IMPORT_NOT_SUPPORTED if the file
   * passed in is not supported.
   * 
   * @param file
   * @return ReturnValue
   */
  public ReturnValue loadFeatureSet(String filePath);
  
  /**
   * Point to an input read file, return the ID to access that data again in subsequent tests
   * with kv of "readSetId=<string>" in the ReturnValue object.
   * 
   * The file type is specified with the extension of the file param:
   * 
   * <ul>
   *   <li>*.bam is assumed to be a BAM file</li>
   *   <li>*.sam is assumed to be a SAM files</li>
   * </ul>
   * 
   * You should use the Picard API to read SAM/BAM files.
   * 
   * ReturnValue state should be BACKEND_FILE_IMPORT_NOT_SUPPORTED if the file
   * passed in is not supported.
   * 
   * @param file
   * @return ReturnValue
   */
  public ReturnValue loadReadSet(String filePath);
  
  /**
   * This is the heart of the test process. This method allows searches for features
   * based on the criteria explained by the Global Alliance use cases document:
   * 
   * https://docs.google.com/document/d/1nkPkYJwks7WY7AAo5EOLVXUj684Zf06ornbEEOj20Og/edit?usp=sharing
   * 
   * This document defines the way in which you search for features.
   * The query is defined in a JSON:
   * 
{
  “feature_sets”: {
     	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “features”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “read_sets”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “reads”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “regions”: {
     [
       “chr22”,
       “chr20:1-63000000”
     ]
   }
}
   * 
   * In the JSON doc above the "read_sets" and "reads" filters are ignored.
   * 
   * The ReturnValue contains a key called "features" and the value is a list of featureIds that matches
   * the search.
   * 
   * @param featureSetId
   * @param start
   * @param stop
   * @return 
   */
  public ReturnValue getFeatures(String queryJSON);
  
  /**
   * This is the heart of the test process. This method allows searches for reads
   * based on the criteria explained by the Global Alliance use cases document:
   * 
   * https://docs.google.com/document/d/1nkPkYJwks7WY7AAo5EOLVXUj684Zf06ornbEEOj20Og/edit?usp=sharing
   * 
   * This document defines the way in which you search for reads.
   * The query is defined in a JSON:
   * 
{
  “feature_sets”: {
     	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “features”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “read_sets”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “reads”: {
	{
        “key”: “value”,
        “key2: “value2”
     }
   },
   “regions”: {
     [
       “chr22”,
       “chr20:1-63000000”
     ]
   }
}
   * 
   * In the JSON doc above the "feature_sets" and "features" filters are ignored.
   * 
   * The ReturnValue contains a key called "reads" and the value is a list of readIds that matches
   * the search.
   * 
   * @param readSetId
   * @param start
   * @param stop
   * @return 
   */
  public ReturnValue getReads(String queryJSON);
  
  /**
   * Return arrayList of strings of the same format as above from featureSet1 that overlap with featureSet2 features
   * @param featureSetName1
   * @param featureSetName2
   * @return 
   *
  public ArrayList<String> intersect(String featureSetName1, String featureSetName2);
  */
  
  /**
   * Run the specified plugin and return the result as a ReturnValue.
   * 
   * TODO: we need to define this better.  What exactly are the plugins?
   * 
   * The ReturnValue 
   * 
   * @param mrPluginClassName
   * @return 
   */
  public ReturnValue runPlugin(String queryJSON, String pluginClassName);
  
}
