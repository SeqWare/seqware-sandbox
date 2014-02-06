/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;

import java.io.File;
<<<<<<< HEAD
import java.io.IOException;
=======
>>>>>>> origin/feature/ADAMBackend
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import org.apache.hadoop.fs.Path;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
<<<<<<< HEAD
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
=======
>>>>>>> origin/feature/ADAMBackend

import parquet.avro.AvroParquetWriter;
import scala.collection.JavaConverters;
import edu.berkeley.cs.amplab.adam.avro.ADAMRecord;
import edu.berkeley.cs.amplab.adam.avro.ADAMVariant;
import edu.berkeley.cs.amplab.adam.converters.SAMRecordConverter;
import edu.berkeley.cs.amplab.adam.converters.VariantContextConverter;
import edu.berkeley.cs.amplab.adam.models.RecordGroupDictionary;
import edu.berkeley.cs.amplab.adam.models.SequenceDictionary;


/**
 *
 * @author boconnor
 */
public class ADAMBackendTest implements BackendTestInterface {
  public static Path output = new Path("testOutput.adam"); 
  public static SAMFileReader samReader;
  public static ArrayList<ADAMRecord> adamList;
  public static List<List<ADAMVariant>> adamVariantList;

  @Override
  public ReturnValue getIntroductionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    rt.getKv().put(DOCS, "<p>Introduction</p>");
    return(rt);
  }

  @Override
  public ReturnValue setupBackend(Map<String, String> settings) {
    //Either output file or db backend?
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue loadFeatureSet(String filePath) {
    if (!filePath.endsWith("vcf")) {
      System.out.println("Read file is not a .vcf file");
    }
    try {
      VariantContextConverter vcc = new VariantContextConverter();
      VCFCodec vcfCodec = new VCFCodec();
      FeatureReader<VariantContext> reader = AbstractFeatureReader.getFeatureReader(filePath, vcfCodec, false);
      Iterator<VariantContext> iter = reader.iterator();
      adamVariantList = new ArrayList<List<ADAMVariant>>();
      
      while (iter.hasNext()) {
        VariantContext vc = iter.next();
        adamVariantList.add((List<ADAMVariant>) JavaConverters.bufferAsJavaListConverter(vcc.convertVariants(vc).toBuffer()));        
      }
    } catch (Exception ex) {
      System.out.println("Error" + ex.getMessage());
    }
    
    ReturnValue rt = new ReturnValue();
    if (!filePath.endsWith("vcf")) {
      System.out.println("Read file is not a .vcf file");
    }
    try {
      VariantContextConverter vcc = new VariantContextConverter();
      VCFCodec vcfCodec = new VCFCodec();
      FeatureReader<VariantContext> reader = AbstractFeatureReader.getFeatureReader(filePath, vcfCodec, false);
      Iterator<VariantContext> iter = reader.iterator();
      adamVariantList = new ArrayList<List<ADAMVariant>>();
      
      // Converts a Scala List to a Scala Buffer to a Java List (No workaround)
      while (iter.hasNext()) {
        VariantContext vc = iter.next();
        adamVariantList.add((List<ADAMVariant>) JavaConverters.bufferAsJavaListConverter(vcc.convertVariants(vc).toBuffer()));        
      }
    } catch (Exception ex) {
      System.out.println("Error" + ex.getMessage());
      rt.setState(ReturnValue.ERROR);
      return(rt);
    }
    
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue loadReadSet(String filePath) {
    try {
      if (filePath.endsWith(".bam") || filePath.endsWith("sam")) {
        SAMRecordConverter sr = new SAMRecordConverter();
        File bamFile = new File(filePath);
        samReader = new SAMFileReader(bamFile, null, true);
<<<<<<< HEAD
        adamList = new ArrayList<ADAMRecord>();
        //JavaSparkContext sc = new JavaSparkContext("local", "ADAM", "$SPARK_HOME", new String[]{"seqware-qe-testing-1.0.jar"}); 
        //AdamContext ac = new AdamContext(sc);
        
=======
        
        adamList = new ArrayList<ADAMRecord>();
>>>>>>> origin/feature/ADAMBackend
        for (SAMRecord r: samReader) {
          adamList.add(sr.convert(r, SequenceDictionary.fromSAMReader(samReader), RecordGroupDictionary.fromSAMReader(samReader)));
        }
      } else {
        System.out.println("Read file is not a .bam/.sam file.");
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
    
  }

  @Override
  public ReturnValue getFeatures(String queryJSON) throws JSONException, IOException {
  //Read the input JSON file to seperate ArrayLists for parsing
    ReturnValue finished = new ReturnValue();
    JSONObject jsonObOuter = new JSONObject(queryJSON);
    JSONArray regionArray;
    Iterator<String> OutterKeys = jsonObOuter.keys();
    
    //Initialize query stores to dump queries from input JSON
    HashMap<String, String> FEATURE_MAP_QUERY = new HashMap<String,String>();
    HashMap<String, String> FEATURE_SET_MAP_QUERY = new HashMap<String,String>();
    HashMap<String, String> REGION_MAP_QUERY = new HashMap<String,String>();

    //READ THE JSON INPUT FILE
    /** "OutKey":
    {
      "InKey": "jsonObInner.get(InKey)"
    }*/
    while (OutterKeys.hasNext()){
      String OutKey = OutterKeys.next();
      if (jsonObOuter.get(OutKey) instanceof JSONObject){
        JSONObject jsonObInner = jsonObOuter.getJSONObject(OutKey);
        Iterator<String> InnerKeys = jsonObInner.keys();
        
        while (InnerKeys.hasNext()){
          String InKey = InnerKeys.next();
          
          if (OutKey.equals("feature_sets")){
            FEATURE_SET_MAP_QUERY.put(InKey.toString(), 
                    jsonObInner.getString(InKey));
          }
          
          if (OutKey.equals("features")){
            FEATURE_MAP_QUERY.put(InKey.toString(), 
                    jsonObInner.getString(InKey));
          }
        }
      } else if (jsonObOuter.get(OutKey) instanceof JSONArray){
        JSONArray jsonArInner = jsonObOuter.getJSONArray(OutKey);
          if(OutKey.equals("regions")){
            regionArray = jsonObOuter.getJSONArray(OutKey);
            
            for (int i=0; i< regionArray.length(); i++){
              String region = regionArray
                      .get(i)
                      .toString();
              
              if (region.contains(":") == false){
                
                //i.e. selects "22" from "chr22"
                String chromosomeID = region.substring(
                    region.indexOf("r")+1,
                    region.length());
                
                REGION_MAP_QUERY.put(chromosomeID.toString(), 
                        ".");
                
              } else if (region.contains(":") == true){
                
                //i.e. selects "22" from "chr22:1-99999"
                String chromosomeID = region.substring(
                    region.indexOf("r")+1,
                    region.indexOf(":"));
                
                String range = region.substring(
                    region.indexOf(":")+1,
                    region.length());
                
                REGION_MAP_QUERY.put(chromosomeID.toString(), 
                        range.toString());
              }
            }
          }
      }
    }
    ReturnValue rt = new ReturnValue();
    if (null == adamVariantList) {
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
<<<<<<< HEAD
    // Try writing to a parquet file
=======
>>>>>>> origin/feature/ADAMBackend
    try {
      AvroParquetWriter<ADAMVariant> parquetWriter = new AvroParquetWriter<ADAMVariant>(output, ADAMVariant.SCHEMA$);
      for (List<ADAMVariant> l: adamVariantList) {
        for (ADAMVariant a: l) {
          parquetWriter.write(a);
        }
      }
      parquetWriter.close();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getReads(String queryJSON)  {
    ReturnValue rt = new ReturnValue();
<<<<<<< HEAD
    
    try {
      JSONObject query = new JSONObject(queryJSON);
      Iterator<String> OuterKeys = query.keys();

      JSONArray regionArray = new JSONArray(); 
      HashMap<String, String> readSetMap = new HashMap<>();
      HashMap<String, String> readsQuery = new HashMap<>();      
      ArrayList<String> chQuery = new ArrayList<>();
      
      while (OuterKeys.hasNext()) {
        String OutKey = OuterKeys.next();
        if (query.get(OutKey) instanceof JSONObject) {
          JSONObject jsonObInner = query.getJSONObject(OutKey);
          Iterator<String> InnerKeys = jsonObInner.keys();
          while (InnerKeys.hasNext()) {
            String InKey = InnerKeys.next();
            //Save key-values of JSON query
            if (OutKey.equals("read_sets")) {
              readSetMap.put(InKey, jsonObInner.getString(InKey));
            }
            if (OutKey.equals("reads")) {
              readsQuery.put(InKey, jsonObInner.getString(InKey));
            }
          }
          InnerKeys = null;
        } else if (query.get(OutKey) instanceof JSONArray) {
          if(OutKey.equals("regions")) {
            regionArray = query.getJSONArray(OutKey);
            for (int i=0; i< regionArray.length(); i++) {
              chQuery.add(regionArray.getString(i));
            }
          }
        } 
      }
    
      if (null == adamList) {
        rt.setState(ReturnValue.ERROR);
        return rt;
      }
      // Try writing to a parquet file
=======
    if (null == adamList) {
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
    
    try {
>>>>>>> origin/feature/ADAMBackend
      AvroParquetWriter<ADAMRecord> parquetWriter = new AvroParquetWriter<ADAMRecord>(output, ADAMRecord.SCHEMA$);
      for (ADAMRecord a: adamList) {
        parquetWriter.write(a);
      }
      parquetWriter.close();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
    
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