package io.seqware.queryengine.sandbox.testing;

import static org.junit.Assert.fail;
import io.seqware.queryengine.sandbox.testing.impl.ADAMBackendTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.cs.amplab.adam.avro.ADAMRecord;

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
    fail("Not yet implemented");
  }
  
  @Test
  public void testGetReads() {
    ab.loadReadSet(bamfile);
    ab.getReads(jsonTxt);
    fail("Not yet implemented");
  } 

  @Test
  public void testADAMRecord() {
    ADAMRecord ar = new ADAMRecord();
    ar.getStart();
    fail();
  }
  
}
