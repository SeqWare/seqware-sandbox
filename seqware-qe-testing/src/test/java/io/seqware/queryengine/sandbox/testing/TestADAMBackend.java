package io.seqware.queryengine.sandbox.testing;

import static org.junit.Assert.fail;
import io.seqware.queryengine.sandbox.testing.impl.ADAMBackendTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestADAMBackend {
  static ADAMBackendTest ab;
  static String bamfile;
  static String vcffile;
  static String jsonTxt;
  
  @BeforeClass
  public static void setUp() throws Exception {
    ab = new ADAMBackendTest();
    bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
    vcffile = "src/main/resources/testdata/exampleVCFinput.vcf";
    File jsonQuery = new File("src/main/resources/testdata/query.json");
    InputStream is = new FileInputStream(jsonQuery);
    jsonTxt = IOUtils.toString(is);
  }

  @AfterClass
  public static void tearDown() {
    ab = null;
    bamfile = null;
  } 
  
  @Test
  public void testGetReads() {
    try {
      ab.loadReadSet(bamfile);
      ab.getReads(jsonTxt);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  } 
  
  @Test
  public void testGetFeatures() {
    try {
      ab.loadFeatureSet(vcffile);
      ab.getFeatures(jsonTxt);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }
}
