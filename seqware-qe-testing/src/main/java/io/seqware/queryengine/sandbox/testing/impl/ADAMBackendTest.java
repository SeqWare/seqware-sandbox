/**
 * This is just a placeholder for the ADAM backend that Brian will eventually
 * implement using the ADAM API for storing/manipulating BAM/VCF files on HDFS
 */
package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import org.apache.hadoop.fs.Path;

import parquet.avro.AvroParquetWriter;
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
    ADAMVariant var = new ADAMVariant();
    VariantContextConverter vcc = new VariantContextConverter();
    
    
    ReturnValue rt = new ReturnValue();
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
  public ReturnValue getFeatures(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }

  @Override
  public ReturnValue getReads(String queryJSON)  {
    ReturnValue rt = new ReturnValue();
    if (null == adamList) {
      rt.setState(ReturnValue.ERROR);
      return rt;
    }
    
    try {
      AvroParquetWriter<ADAMRecord> parquetWriter = new AvroParquetWriter<ADAMRecord>(output, ADAMRecord.SCHEMA$);
      for (ADAMRecord a: adamList) {
        parquetWriter.write(a);
      }
      parquetWriter.close();
    }
    catch (Exception ex) {
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