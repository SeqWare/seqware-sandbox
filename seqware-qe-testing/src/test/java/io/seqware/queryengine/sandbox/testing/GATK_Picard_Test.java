package io.seqware.queryengine.sandbox.testing;

import io.seqware.queryengine.sandbox.testing.impl.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

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
//         Global.outputFilePath = File.createTempFile("output", "txt").getAbsolutePath();
         
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
      vcffile = "src/main/resources/NA12156_lcl_SRR801819.vcf";
      
      GATK_Picard_BackendTest b = new GATK_Picard_BackendTest();
      PrintWriter writer = new PrintWriter("/Users/bso/Report.html", "UTF-8");
      writer.println(b.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
      writer.println(b.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));
      writer.close();
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
      pb = null;
      bamfile = null;
      jsonTxt = null;
    }


    public void testLoadReadSet() {
      pb.loadReadSet(bamfile);
      Assert.assertNotNull(pb.getFileReader());
    }
    

    public void testGetReads() {
      pb.getIntroductionDocs();
      pb.loadReadSet(bamfile);
      pb.getReads(jsonTxt);
      File actualFile = new File("testOutput.bam");
      SAMFileReader actualBAM = new SAMFileReader(actualFile);
      Assert.assertNotNull(actualBAM);
    }
    
    
    public void testGetConclusionDocs() {
      pb.getConclusionDocs();

      Assert.assertNotNull(pb.getHTMLReport());
    }
    
}
