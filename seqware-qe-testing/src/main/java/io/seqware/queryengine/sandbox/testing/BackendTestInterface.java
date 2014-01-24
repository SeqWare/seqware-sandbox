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

import java.io.IOException;
import java.util.Map;

import org.json.*;
/**
 *
 * @author boconnor
 */
public interface BackendTestInterface {
    public static final String DOCS = "docs";
  
  /** 
   * This just returns a string name to identify the backend 
   */
  public String getName();
    
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
  public ReturnValue getIntroductionDocs();
    
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
  public ReturnValue setupBackend(Map<String, String> settings);
  
  /**
   * Point to an input variant file, return the ID to access that data again in subsequent tests
   * The key of "featureSetId" and the value being the featureSetId in the ReturnValue object.
   * 
   * The file type is specified with the extension of the file param:
   * 
   * <ul>
   *   <li>*.vcf is assumed to be a VCF file</li>
   *   <li>*.tsv is assumed to be a TSV (tab separated value) file</li>
   * </ul>
   * 
   * TODO: we only currently support TSV files, look at GATK or another tools
   * to read the VCF file.
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
   * The key of "readSetId" and the value being the readSetID in the ReturnValue object.
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
  ???feature_sets???: {
     	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???features???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???read_sets???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???reads???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???regions???: {
     [
       ???chr22???,
       ???chr20:1-63000000???
     ]
   }
}
   * 
   * In the JSON doc above the "read_sets" and "reads" filters are ignored.
   * 
   * The ReturnValue contains a key called "queryResultFile" and the value is 
   * a file path string that points to a file that contains the search results
   * in TSV format.
   * 
   * @param queryJSON
   * @return 
 * @throws JSONException 
 * @throws IOException 
   */
  public ReturnValue getFeatures(String queryJSON) throws JSONException, IOException;
  
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
  ???feature_sets???: {
     	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???features???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???read_sets???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???reads???: {
	{
        ???key???: ???value???,
        ???key2: ???value2???
     }
   },
   ???regions???: {
     [
       ???chr22???,
       ???chr20:1-63000000???
     ]
   }
}
   * 
   * In the JSON doc above the "feature_sets" and "features" filters are ignored.
   * 
   * The ReturnValue contains a key called "queryResultFile" and the value is 
   * a file path string that points to a file that contains the search results
   * in TSV format.
   * 
   * @param queryJSON
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
   * Run the specified plug-in and return the result as a ReturnValue.
   * 
   * TODO: we need to define this better.  What exactly are the plug-ins?
   * 
   * The ReturnValue value HashMap should have a key called "pluginResultFile"
   * with a value that points to a file that contains the results of this
   * plug-in.
   * 
   * @param pluginClassName
   * @return 
   */
  public ReturnValue runPlugin(String queryJSON, String pluginClassName);
  
  /**
   * This is a simple method that is called just before the backend is torn down.
   * It lets you provide any HTML doc text that you want to and this is added
   * to the end of the HTML report.  Ideas for what this might include:
   * <ul>
   *   <li>any testing summary that you want to provide</li>
   *   <li>information showing system resources used</li>
   *   <li>information any information about manual cleanup that needs to be done</li>
   * </ul>
   * 
   * Put the docs (as an HTML fragment) in the key-value in ReturnValue
   * with the key "docs".
   */
  public ReturnValue getConclusionDocs();
  
  /**
   * Cleans up the backend. Delete any files, databases, etc.  You get the same
   * settings as passed in to the setupBackend method.
   * 
   * @param settings
   * @return 
   */
  public ReturnValue teardownBackend(Map<String, String> settings);
}
