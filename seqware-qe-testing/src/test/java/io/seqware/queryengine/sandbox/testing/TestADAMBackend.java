package io.seqware.queryengine.sandbox.testing;

import static org.junit.Assert.fail;
import io.seqware.queryengine.sandbox.testing.impl.ADAMBackendTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestADAMBackend {
  static ADAMBackendTest ab;
  static String bamfile;
  static String vcffile;
  static String jsonTxt;
  
  @BeforeClass
  public static void setUp() {
    ab = new ADAMBackendTest();
    bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
    vcffile = "src/main/resources/testdata/exampleVCFinput.vcf";
    jsonTxt = "src/main/resources/testdata/query.json";
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
