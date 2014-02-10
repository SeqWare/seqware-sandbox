package io.seqware.queryengine.sandbox.testing;

import io.seqware.queryengine.sandbox.testing.impl.GATK_Picard_BackendTest;
import io.seqware.queryengine.sandbox.testing.utils.Global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;

import net.sf.samtools.SAMFileReader;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GATK_Picard_Test {
    @Test
    public void testAll() throws IOException, JSONException{
    	 ReturnValue rv = new ReturnValue();
         

         //Point to local VCF file to be read
         rv = gb.loadFeatureSet(vcffile);  
 		 
         //Point to TSV output file to be written to
         Global.outputFilePath = File.createTempFile("output", "txt").getAbsolutePath();
         
 		//Obtain matched features
         rv = gb.getFeatures(jsonTxt);    	
    }

    static GATK_Picard_BackendTest pb;
    static String bamfile;
    static String jsonTxt;
    
    static GATK_Picard_BackendTest gb; 
    static String vcffile;
    static String jsonTxt2 = new String(); //Need to combine these JSON's after
    
    @BeforeClass
    public static void setUp() throws Exception {
      File jsonQuery = new File("src/main/resources/testdata/query.json");
	  InputStream is = new FileInputStream(jsonQuery);
	  jsonTxt = IOUtils.toString(is);
	    
      pb = new GATK_Picard_BackendTest();
      bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
      
      gb = new GATK_Picard_BackendTest();
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
      pb.getIntroductionDocs();
      HTMLDocument expHtmlReport = new HTMLDocument();
      JEditorPane p = new JEditorPane();
      p.setContentType("text/html");
      p.setText(""
          + " <html>"
          + "   <head>"
          + "     <title>SeqWare Query Engine: GATK_Picard_BackendTest</title>"
          + "     <style type=\"text/css\">"
          + "       body { background-color: #EEEEEE; }"
          + "       h3  { color: red; }"
          + "     </style>"
          + "     </head>" 
          + "   <body>"
          + "     <h1>SeqWare Query Engine: GATK_Picard_BackendTest</h1>"
          + "   </body>"
          + "</html>"
       );
      expHtmlReport = (HTMLDocument) p.getDocument();  
      try {
      Assert.assertEquals(pb.getHTMLReport().getText(0, pb.getHTMLReport().getLength()), expHtmlReport.getText(0, expHtmlReport.getLength()));
      } catch (Exception ex) {
        Assert.fail();
      }
    }

    @Test
    public void testLoadReadSet() {
      pb.loadReadSet(bamfile);
      Assert.assertNotNull(pb.getFileReader());
    }
    
    @Test
    public void testGetReads() {
      pb.getIntroductionDocs();
      pb.loadReadSet(bamfile);
      pb.getReads(jsonTxt);
      File actualFile = new File("testOutput.bam");
      SAMFileReader actualBAM = new SAMFileReader(actualFile);
      Assert.assertNotNull(actualBAM);
    }
    
    @Test
    public void testGetConclusionDocs() {
      pb.getConclusionDocs();

      Assert.assertNotNull(pb.getHTMLReport());
    }
    
}
