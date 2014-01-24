package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class PicardBackendTest implements BackendTestInterface {
	
 
	public static SAMFileReader samReader; //Used to read the SAM/BAM file.
	public static HTMLDocument htmlReport; // The HTML Report to be written -- don't need to worry about this too much

	public String getName(){
	  return "Samtools Picard API";
	}
	
	public ReturnValue getIntroductionDocs() {
	  htmlReport = new HTMLDocument();
	  JEditorPane p = new JEditorPane();
	  p.setContentType("text/html");
	  p.setText(""
	      + " <html>"
        + "   <head>"
        + "     <title>SeqWare Query Engine: PicardBackendTest</title>"
        + "     <style type=\"text/css\">"
        + "       body { background-color: #EEEEEE; }"
        + "       h3  { color: red; }"
        + "     </style>"
        + "     </head>" 
        + "   <body>"
        + "     <h1>SeqWare Query Engine: PicardBackendTest</h1>"
        + "     <div>" 
        + "     </div>"
        + "   </body>"
        + "</html>"
     );
	  htmlReport = (HTMLDocument) p.getDocument();
		//Parse connectionSettings and output configuration to HTML Report
	  
    ReturnValue r = new ReturnValue();
    r.setState(2);
    return r;
	}
	
	public ReturnValue loadFeatureSet(String filePath) { 
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; 		/* NOT_IMPLEMENTED */ 
  }

  // Places file into SAMFileReader attribute to prepare for queries
	public ReturnValue loadReadSet(String filePath) {
		// Check if file is SAM/BAM!
		ReturnValue r = new ReturnValue();
		if (filePath.endsWith(".sam") || filePath.endsWith(".bam")) {
		  long elapsedTime = System.nanoTime();
      // Convert Reads to JSON and insert to Backend db
			File samfile = new File(filePath);
			samReader = new SAMFileReader(samfile);
	    elapsedTime = (System.nanoTime() - elapsedTime) / 1000000;
	    System.out.println("Loaded file in time: "+ elapsedTime + " milliseconds" );
	    
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
	  //First, parse the query for related fields
	  try {
	    JSONObject jsonObOuter = new JSONObject(queryJSON);
	    Iterator<String> OuterKeys = jsonObOuter.keys();
	    JSONObject query = new JSONObject(queryJSON);

      JSONArray regionArray = new JSONArray(); 
      HashMap<String, String> readSetMap = new HashMap<String, String>();
      HashMap<String, String> readsQuery = new HashMap<String, String>();      
      ArrayList<String> chQuery = new ArrayList<String>();
      
      while (OuterKeys.hasNext()) {
        String OutKey = OuterKeys.next();
        if (query.get(OutKey) instanceof JSONObject) {
          JSONObject jsonObInner = query.getJSONObject(OutKey);
          Iterator<String> InnerKeys = jsonObInner.keys();
          while (InnerKeys.hasNext()) {
            String InKey = InnerKeys.next();
            //Save values of 
            if (OutKey.equals("read_sets")) {
              readSetMap.put(InKey, jsonObInner.getString(InKey));
            }
            if (OutKey.equals("reads")) {
              readsQuery.put(InKey, jsonObInner.getString(InKey));
            }
          }
          InnerKeys = null;
        } else if (query.get(OutKey) instanceof JSONArray) {
          if(OutKey.equals("regions")) {
            regionArray = query.getJSONArray(OutKey);
            for (int i=0; i< regionArray.length(); i++) {
              chQuery.add(regionArray.getString(i));
            }
          }
        } 
      }
	    
	    String querySampleIds = new String();
      if (!readSetMap.get("sample").isEmpty()) {
        querySampleIds = readSetMap.get("sample"); //Get Sample IDs, figure out how they are delimited
        readSetMap.remove("sample");
      }
      
      //Single Read: SAMRecord
      //Fields: ID, Tags, Region, Read Attributes
      //ID: @RG.SM -querySampleIds
      //Tags: SAMRecord.getAttribute() - will receive tag for read - rmapQuery
      //Regions: query with SAMRecord alignment start/end - chQuery
      //Set Order: sample_id(tags(region(read_attributes())))
      
      // Display Query to user -- todo: place in HTML Report
      System.out.println();
      System.out.println("__Query__");
      System.out.println("Sample IDs: " + querySampleIds);
      System.out.println("Tags: " +  readSetMap.toString());
      System.out.println("Read Attributes: " + readsQuery.toString());
      System.out.println("Regions: " + chQuery);
      System.out.println();
      
      //Next, Query the BAM file for such entries
      SAMFileHeader bamHeader = samReader.getFileHeader();
      List<SAMReadGroupRecord> bamReadGroups = bamHeader.getReadGroups();

      File output = new File("output.bam");
      SAMFileWriterFactory samFactory = new SAMFileWriterFactory();
      SAMFileWriter bfw = samFactory.makeBAMWriter(bamHeader, true, output);
      
      // First, query the header of .bam:
      if (!querySampleIds.isEmpty()) {
        boolean sampleMatch = false; 
        for (SAMReadGroupRecord rec: bamReadGroups) {          
          if (rec.getAttribute("SM").equals(querySampleIds)) {
            sampleMatch = true;
          }
        }         
        // No Sample ID match
        if (!sampleMatch) {
          System.out.println("No Results");
          ReturnValue r = new ReturnValue();
          r.setState(2);
          return r;
        }
      }
      
      // Oragnize and query read_set tags
      // Not working, to fix..
      /*if (!readSetMap.containsValue(null)) {
        boolean readSetMatch = false;
        System.out.println(readSetMap);
        
        //This part of query is only good for the header line: @HD SO and VN
        //todo: Needs to be fixed for the rest of the header
        for (Entry<String, String> entry : readSetMap.entrySet()) {
          System.out.println(bamHeader.getAttribute(entry.getKey()));
          if (bamHeader.getAttribute(entry.getKey()).equals(entry.getValue())){
            readSetMatch = true;
          }
        }
        if (!readSetMatch) {
          System.out.println("No Results");
          ReturnValue r = new ReturnValue();
          r.setState(2);
          return r;
        }
      }*/
      
      // Narrow down Regions
      if (regionArray.length() !=0) {
        
        for (int i=0; i < regionArray.length(); i++) {
          String region = regionArray.get(i).toString();
          if (region.contains(":")) {
            //int regionIndex = Integer.parseInt(region.substring(3, region.indexOf(":")));
            int regionStart = Integer.parseInt(region.substring(region.indexOf(":") + 1, region.indexOf("-")));
            int regionEnd = Integer.parseInt(region.substring(region.indexOf("-") + 1, region.length()));
            
            SAMRecordIterator iter = samReader.queryOverlapping(region, regionStart, regionEnd);
            //todo:  Add SAMRecord to query pool..
            System.out.println(iter.toString());
            iter.close();
          }
        }
      }
      
      // Check Attributes
      if (!readsQuery.isEmpty()) {
        for (SAMRecord r: samReader) { 
          for (Entry<String, String> e : readsQuery.entrySet()) {
            if (e.getKey().equals("qname")) {
              if (r.getReadName().equals(e.getValue()))
                bfw.addAlignment(r);
            }
            //System.out.println(r.getAttributes().toString());
            /*if (r.getAttribute(e.getKey()).equals(e.getValue())) {
              bfw.addAlignment(r);
              System.out.println(r.getReadName());
            }*/
          }
        }
      } else {
        for (SAMRecord r: samReader) {
          bfw.addAlignment(r);
        }
      }
      
      //Write a .bam file with result of query
      bfw.close();
      System.out.println("done");
      
	  } catch (Exception ex) {
	    System.out.println(ex.toString());
	  }  
	  
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
	  // Nothing to do..
		// 
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}

	public ReturnValue teardownBackend(HashMap<String, String> settings) {
		// Clean up, delete any files.
		// Connect to DBs, Drop Tables
	  
	  // Drop temporary variables
	  htmlReport = null;
	  samReader = null;
		
		ReturnValue r = new ReturnValue(); 
		r.setState(2); 
		return r; /* NOT_IMPLEMENTED */ 
	}
}
