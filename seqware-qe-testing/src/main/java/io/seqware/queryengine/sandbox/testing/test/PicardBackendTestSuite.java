package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import net.sf.samtools.SAMFileReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PicardBackendTestSuite extends TestCase {
  PicardBackendTest pb;
  String bamfile;
  String jsonTxt;
  
  protected void setUp() throws Exception {
    pb = new PicardBackendTest();
    bamfile = "src/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
    File jsonQuery = new File("src/resources/testdata/query.json");
    InputStream is = new FileInputStream(jsonQuery);
    jsonTxt = IOUtils.toString(is);
  }
  
  protected void tearDown() throws Exception {
    pb = null;
    bamfile = null;
    jsonTxt = null;
  }

  @Test
  public void testGetIntroductionDocs() {
    assertNull(pb.getHTMLReport());
    pb.getIntroductionDocs();
    assertNotNull(pb.getHTMLReport());
  }

  @Test
  public void testLoadReadSet() {
    pb.loadReadSet(bamfile);
    assertNotNull(pb.getFileReader());
  }
  
  @Test
  public void testGetReads() {
    pb.getReads(jsonTxt);
    File actualFile = new File("output.bam");
    SAMFileReader actualBAM = new SAMFileReader(actualFile);
    assertNotNull(actualBAM);
  }
  
  @Test
  public void testGetConclusionDocs() {
    pb.getConclusionDocs();
    assertNotNull(pb.getHTMLReport());
  }
}
