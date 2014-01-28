package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.sf.samtools.SAMFileReader;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPicardBackend  {
  static PicardBackendTest pb;
  static String bamfile;
  static String jsonTxt;
  
  @BeforeClass
  public static void setUp() throws Exception {
    pb = new PicardBackendTest();
    bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
    File jsonQuery = new File("src/main/resources/testdata/query.json");
    InputStream is = new FileInputStream(jsonQuery);
    jsonTxt = IOUtils.toString(is);
  }
  
  @AfterClass
  public static void tearDown() throws Exception {
    pb = null;
    bamfile = null;
    jsonTxt = null;
  }

  @Test
  public void testGetIntroductionDocs() {
    Assert.assertNull(pb.getHTMLReport());
    pb.getIntroductionDocs();
    Assert.assertNotNull(pb.getHTMLReport());
  }

  @Test
  public void testLoadReadSet() {
    pb.loadReadSet(bamfile);
    Assert.assertNotNull(pb.getFileReader());
  }
  
  @Test
  public void testGetReads() {
    pb.getReads(jsonTxt);
    File actualFile = new File("output.bam");
    SAMFileReader actualBAM = new SAMFileReader(actualFile);
    Assert.assertNotNull(actualBAM);
  }
  
  @Test
  public void testGetConclusionDocs() {
    pb.getConclusionDocs();
    Assert.assertNotNull(pb.getHTMLReport());
  }
}
