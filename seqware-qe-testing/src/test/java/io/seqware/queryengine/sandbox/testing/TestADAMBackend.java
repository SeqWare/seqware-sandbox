package io.seqware.queryengine.sandbox.testing;

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
    jsonTxt = null;
  }

  @AfterClass
  public static void tearDown() {
    ab = null;
    bamfile = null;
  } 
  
  @Test
  public void testLoadReadSet() {
    ab.loadReadSet(bamfile);
  }
  
  @Test
  public void testGetReads() {
    ab.loadReadSet(bamfile);
    ab.getReads(jsonTxt);
  } 
}
