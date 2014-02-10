/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.JSONQueryParser;
import io.seqware.queryengine.sandbox.testing.utils.ReadSearch;

import java.io.File;
import java.io.IOException;
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
import org.json.JSONException;

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
    rt.getKv().put(BackendTestInterface.DOCS, "<p>Introduction</p>");
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
        adamList = new ArrayList<ADAMRecord>();
        //JavaSparkContext sc = new JavaSparkContext("local", "ADAM", "$SPARK_HOME", new String[]{"seqware-qe-testing-1.0.jar"}); 
        //AdamContext ac = new AdamContext(sc);
        
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
    ReturnValue rt = new ReturnValue();
    JSONQueryParser queryParser = new JSONQueryParser(queryJSON);
    
    //Initialize query stores to dump queries from input JSON
    HashMap<String, String> featuresQuery = queryParser.getFeaturesQuery();
    HashMap<String, String> featureSetQuery = queryParser.getFeatureSetQuery();
    HashMap<String, String> regionsQuery = queryParser.getRegionsQuery();
    
    if (null == adamVariantList) {
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
    // Try writing to a parquet file
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
    
    try {
      JSONQueryParser queryParser = new JSONQueryParser(queryJSON);
      
      //Initialize query stores to dump queries from input JSON
      HashMap<String, String> readsQuery = queryParser.getReadsQuery();
      HashMap<String, String> readSetQuery = queryParser.getReadSetQuery();
      HashMap<String, String> regionsQuery = queryParser.getRegionsQuery();
      ReadSearch rs = new ReadSearch(readSetQuery, readsQuery, regionsQuery);
      List<ADAMRecord> searchedADAMList = rs.adamSearch(adamList);
      // Try writing to a parquet file
      AvroParquetWriter<ADAMRecord> parquetWriter = new AvroParquetWriter<ADAMRecord>(output, ADAMRecord.SCHEMA$);
      for (ADAMRecord a: searchedADAMList) {
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
  public ReturnValue runPlugin(String queryJSON, Class pluginClassName) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getConclusionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    rt.getKv().put(BackendTestInterface.DOCS, "<p>Conclusion</p>");
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