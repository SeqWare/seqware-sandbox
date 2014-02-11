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
import java.util.Map.Entry;
import java.util.UUID;

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
  public static ArrayList<ADAMRecord> adamList = new ArrayList<ADAMRecord>();
  public static List<List<ADAMVariant>> adamVariantList;
  public static HashMap<String, String> READ_SETS = new HashMap<String, String>();

  @Override
  public ReturnValue getIntroductionDocs() {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    rt.getKv().put(BackendTestInterface.DOCS, "<h2>Introduction</h2>");
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
    ReturnValue rt = new ReturnValue();
    try {
      if (filePath.endsWith(".bam") || filePath.endsWith("sam")) {
        UUID id = UUID.randomUUID();
        READ_SETS.put(id.toString(), filePath);
        
        rt.storeKv(BackendTestInterface.READ_SET_ID, id.toString());
        rt.setState(ReturnValue.SUCCESS);
        return rt;
      } else if (filePath.endsWith(".bai")){
        boolean hasBam = false;
        String bamId = "";
        for (Entry<String, String> e: READ_SETS.entrySet()) {
          if ((e.getValue() + ".bai").equals(filePath)) {
            bamId = e.getKey();
            hasBam = true;
          }
        }
        if (hasBam){
          READ_SETS.put(bamId + "index", filePath);
          rt.storeKv(BackendTestInterface.READ_SET_ID, bamId + "index");
        } else {
          UUID id = UUID.randomUUID();
          READ_SETS.put(id.toString(), filePath);
          rt.storeKv(BackendTestInterface.READ_SET_ID, id.toString());
        }
        rt.setState(ReturnValue.SUCCESS);
        return(rt);
      } else {
        System.out.println("Read file is not a .bam/.sam file.");
        rt.setState(ReturnValue.NOT_SUPPORTED);
        return(rt);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      rt.setState(ReturnValue.ERROR);
      return(rt);
    }    
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
    SAMRecordConverter samConverter = new SAMRecordConverter();
    for (Entry<String, String> e: READ_SETS.entrySet()) {
      if (e.getValue().endsWith(".bai"))
        continue;
      
      if (null != READ_SETS.get(e.getKey() + "index")) {
        File bamfile = new File(e.getValue());
        File baifile = new File(READ_SETS.get(e.getKey() + "index"));
        SAMFileReader samReader = new SAMFileReader(bamfile, baifile, true);
        
        for (SAMRecord r: samReader) {
          try {
            adamList.add(samConverter.convert(r, SequenceDictionary.fromSAMReader(samReader), RecordGroupDictionary.fromSAMReader(samReader)));
          } catch (Exception ex) {
            System.out.println(ex.getMessage());
          }
        }
      }
    }
    if (queryJSON.isEmpty()) {
      try {
        // Try writing to a parquet file
        AvroParquetWriter<ADAMRecord> parquetWriter = new AvroParquetWriter<ADAMRecord>(output, ADAMRecord.SCHEMA$);
        for (ADAMRecord a: adamList) {
          parquetWriter.write(a);
        }
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    } else {
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
    }
    rt.storeKv(BackendTestInterface.QUERY_RESULT_FILE, output.toString());
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue runPlugin(String queryJSON, Class pluginClassName) {
    
    ReturnValue rt = new ReturnValue();
    rt.storeKv(BackendTestInterface.PLUGIN_RESULT_FILE, output.toString());
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

  
}