package io.seqware.queryengine.sandbox.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import io.seqware.queryengine.sandbox.testing.impl.GATKPicardBackendTest;
import io.seqware.queryengine.sandbox.testing.utils.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGATKPicardBackend {
    @Test
    public void testAll() throws IOException, JSONException{
    	 ReturnValue returned = new ReturnValue();
         

         //Point to local VCF file to be read
         gb.loadFeatureSet(vcffile);  
 		
         //Point to TSV output file to be written to
         Global.outputFilePath = File.createTempFile("output", "txt").getAbsolutePath();
         
 		//Obtain matched features
         returned = gb.getFeatures(jsonTxt);    	
    }

    static GATKPicardBackendTest pb;
    static String bamfile;
    static String jsonTxt;
    
    static GATKPicardBackendTest gb; 
    static String vcffile;
    static String jsonTxt2 = new String(); //Need to combine these JSON's after
    
    @BeforeClass
    public static void setUp() throws Exception {
      File jsonQuery = new File("src/main/resources/testdata/query.json");
	  InputStream is = new FileInputStream(jsonQuery);
	  jsonTxt = IOUtils.toString(is);
	    
      pb = new GATKPicardBackendTest();
      bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
      
      gb = new GATKPicardBackendTest();
      vcffile = "src/main/resources/testdata/exampleVCFinput.vcf";
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
      pb = null;
      bamfile = null;
      jsonTxt = null;
    }

    @Test
    public void testGetIntroductionDocs() {
      String expHtmlReport = "<p>Introduction</p>";  
      assertEquals(expHtmlReport, pb.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
    }

    @Test
    public void testLoadReadSet() {
      Assert.assertNotNull(pb.loadReadSet(bamfile).getKv().get(BackendTestInterface.READ_SET_ID));
    }
    
    @Test
    public void testGetReads() {
      fail("Not implemented yet.");
    }
    
    @Test
    public void testGetConclusionDocs() {
      Assert.assertNotNull(pb.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));
    }
    
}
