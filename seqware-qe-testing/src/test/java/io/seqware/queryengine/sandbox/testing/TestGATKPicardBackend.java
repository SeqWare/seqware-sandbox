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
    	   backend.loadFeatureSet(vcffile);  
 		
         //Point to TSV output file to be written to
         Global.outputFilePath = File.createTempFile("output", "txt").getAbsolutePath();
         
         //Obtain matched features
         returned = backend.getFeatures(jsonTxt);    	
    }

    static GATKPicardBackendTest backend; 
    static String vcffile;
    static String bamfile;
    static String jsonTxt;
    static String jsonTxt2 = new String(); //Need to combine these JSON's after
    
    
    @BeforeClass
    public static void setUp() throws Exception {
      File jsonQuery = new File("src/main/resources/testdata/query.json");
  	  InputStream is = new FileInputStream(jsonQuery);
  	  jsonTxt = IOUtils.toString(is);
	    
  	  backend = new GATKPicardBackendTest();
      bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
      vcffile = "src/main/resources/testdata/exampleVCFinput.vcf";
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
      backend = null;
      bamfile = null;
      jsonTxt = null;
    }

    @Test
    public void testGetIntroductionDocs() {
      String expHtmlReport = "<h2>GATKPicardBackend: Introduction<h2>";  
      assertEquals(expHtmlReport, backend.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
    }

    @Test
    public void testLoadReadSet() {
      Assert.assertNotNull(backend.loadReadSet(bamfile).getKv().get(BackendTestInterface.READ_SET_ID));
    }
    
    @Test
    public void testGetReads() {
      fail("Not implemented yet.");
    }
    
    @Test
    public void testGetConclusionDocs() {
      String expHtmlReport = "<h2>Conclusion</h2>"; 
      assertEquals(expHtmlReport, backend.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));
    }
    
}
