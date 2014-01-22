package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;

import org.json.JSONObject;

/**
 * TestBackends
 * 
 * This tool is designed to iterate over the available backends and call them
 * one-by-one, producing data structures that can be turned into reports.
 *
 */
public class TestBackends implements BackendTestInterface {
	//Test Sandbox Configuration
	//Path configPath = Paths.get("./TestConfig.xml");
	
	List<String> backendList;
	HashMap<String, String> connectionSettings; // Connection settings for each backend
	HTMLDocument htmlReport; // The HTML Report to be written
	HashMap<String, String> htmlReportFields;

	public ReturnValue getIntroductionDocs() {
    // Connect to current backend with current features
	  htmlReport = new HTMLDocument();
	  JEditorPane p = new JEditorPane();
	  p.setContentType("text/html");
	  p.setText(""
	      + " <html>"
        + "   <head>"
        + "     <title>An example HTMLDocument</title>"
        + "     <style type=\"text/css\">"
        + "       body { background-color: #EEEEEE; }"
        + "       h3  { color: red; }"
        + "     </style>"
        + "     </head>" 
        + "   <body>"
        + "     <h1>SeqWare Query Engine Report</h1>"
        + "     <div>" 
        + "     </div>"
        + "   </body>"
        + "</html>"
     );
	  htmlReport = (HTMLDocument) p.getDocument();
		//Parse connectionSettings and output configuration to HTML Report
	  
	  // NOT_IMPLEMENTED
    ReturnValue r = new ReturnValue();
    r.setState(2);
    return r;
	}
	
	public ReturnValue loadFeatureSet(String filePath) { 
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; 		/* NOT_IMPLEMENTED */ 
  }

	// To load a read into the db, need reference, chromosome, start pos..
	// compression in bytes.
	public ReturnValue loadReadSet(String filePath) {
		// Check if file is SAM/BAM!
		ReturnValue r = new ReturnValue();
		PicardReader reader = new PicardReader();
		JSONObject jsonDocument = new JSONObject();
		
		try {
		  //htmlReport.setInnerHTML(elem, "");
		} catch (Exception ex) {
		  
		}
		
		if (filePath.endsWith(".sam")) {
		  long elapsedTime = System.nanoTime();
      // Convert Reads to JSON and insert to Backend db
			File samfile = new File(filePath);
	    System.out.println("Detected SAM file");
	    jsonDocument = reader.parseSAM(samfile);
	    
	    //Iterate over JSON document for: Rowkey: reference, 
	    //                                value: sequence, 
	    //                                Qualifier: UUID for ReadSet
	    // Set ColumnFamily to test
	    
	    
	    elapsedTime = System.nanoTime() - elapsedTime;
	    
			r.setState(2); // NOT_IMPLEMENTED
			return r;
		} else if (filePath.endsWith(".bam")) {
		  long elapsedTime = System.nanoTime();
      // Convert Reads to JSON and insert to Backend db
	    File bamfile = new File(filePath);
	    System.out.println("Detected BAM file");
	    jsonDocument = reader.parseSAM(bamfile);
	    
	    elapsedTime = System.nanoTime() - elapsedTime;
	    
			r.setState(2); // NOT_IMPLEMENTED
			return r; 
		} else {
			r.setState(6); // BACKEND_FILE_IMPORT_NOT_SUPPORTED
			return r; 
		}
	}
	  	   
	public ReturnValue getFeatures(String queryJSON) { 
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */  
	}
	  
	
	public ReturnValue getReads(String queryJSON) {
		//Parse the query for instructions
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}
	  

	public ReturnValue runPlugin(String queryJSON, String pluginClassName) {
		//Parse the query for instructions
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}
	  

	public ReturnValue getConclusionDocs() {
		// Write HTMLDocuments to file
	  try {
	    PrintWriter out = new PrintWriter("htmlReport.html");
      out.print(htmlReport.toString());
      out.close();
	  } catch (Exception ex) {
	    ReturnValue r = new ReturnValue(); 
	    r.setState(1); 
	    return r; /* ERROR */ 
    } 
	  
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}
	  
	/* Setup and Teardown Methods */
	public ReturnValue setupBackend(HashMap<String, String> settings) {
    // todo: Setup Documentation
	  long elapsedTime = System.nanoTime();
	  
	  if (settings.get("backend") == "HBase") {
	    try {
        int testResult = HBaseOperations.test();        
      } catch (Exception ex) {
        System.out.println("Error: " + ex.getMessage());
      } 
      System.out.println("Finished!");
	  }
		//Parse the query for instructions
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}

	public ReturnValue teardownBackend(HashMap<String, String> settings) {
		// Clean up, delete any files.
		// Connect to DBs, Drop Tables
	  
	  // Drop temporary variables
	  htmlReport = null; 
	  htmlReportFields = null; 
	  connectionSettings = null; 
		
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}
	  
	//Currently used to test components. Should refactor.
  public static void main(String[] args) {
    File testBam = new File("src/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam");
    if (!testBam.exists())
    {
      System.out.println("File does not Exist.");
      throw new IllegalArgumentException("Error: File does not exist.");
    }
    PicardReader pr = new PicardReader();
    JSONObject jsonData = new JSONObject();
    jsonData = pr.parseTest(testBam);
    
    System.out.println(jsonData);
  }  
  
}
