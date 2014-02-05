package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JEditorPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;

import org.json.JSONArray;
import org.json.JSONObject;

public class PicardBackendTest implements BackendTestInterface {
  
 
  public static SAMFileReader samReader; // Used to read the SAM/BAM file.
  public static HTMLDocument htmlReport; // The HTML Report to be written 

  @Override
  public String getName(){
    return "PicardBackendTest";
  }
  
  public HTMLDocument getHTMLReport() {
    return htmlReport;
  }
  
  public SAMFileReader getFileReader() {
    return samReader;
  }
  /** getIntroductionDocs()
   *  Creates an HTMLDocument object to use as a log
   */
  @Override
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
        + "   </body>"
        + "</html>"
     );
    htmlReport = (HTMLDocument) p.getDocument();    
    ReturnValue r = new ReturnValue();
    r.setState(ReturnValue.SUCCESS);
    return r;
  }
  
  @Override
  public ReturnValue loadFeatureSet(String filePath) { 
    ReturnValue r = new ReturnValue(); 
    r.setState(ReturnValue.NOT_SUPPORTED); 
    return r;  
  }

  /** loadReadSet
   * Prepares to read a .sam/.bam file. Points reader to a filePath on disk.
   * 
   * @param filePath
   */
  // Places file into SAMFileReader attribute to prepare for queries
  @Override
  public ReturnValue loadReadSet(String filePath) {
    ReturnValue rt = new ReturnValue();
    
    // Check if file is SAM/BAM!
    if (filePath.endsWith(".sam") || filePath.endsWith(".bam")) {
      long elapsedTime = System.nanoTime();
      
      // Point to file on disk
      File samfile = new File(filePath);
      samReader = new SAMFileReader(samfile);
      elapsedTime = (System.nanoTime() - elapsedTime) / 1000000;
      
      // Write status to HTML Report
      try {
        htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<div><h3>loadReadSet</h3><p>Loaded file in time: "+ elapsedTime + " milliseconds</p></div>");
      } catch (Exception ex) {
        rt.setState(ReturnValue.ERROR);
        return rt;
      }
      rt.setState(ReturnValue.SUCCESS);
      return rt; 
    } else {
      rt.setState(ReturnValue.BACKEND_FILE_IMPORT_NOT_SUPPORTED);
      return rt; 
    }
  }
         
  @Override
  public ReturnValue getFeatures(String queryJSON) { 
    ReturnValue rt = new ReturnValue(); 
    rt.setState(ReturnValue.NOT_SUPPORTED); 
    return rt;
  }
    
  /** getReads
   * Queries the .sam/.bam file in question for results specified by the JSON query
   * @param queryJSON
   */
  @Override
  public ReturnValue getReads(String queryJSON) {
    ReturnValue rt = new ReturnValue();
    //First, parse the query for related fields
    try {
      JSONObject query = new JSONObject(queryJSON);
      Iterator<String> OuterKeys = query.keys();

      JSONArray regionArray = new JSONArray(); 
      HashMap<String, String> readSetMap = new HashMap<>();
      HashMap<String, String> readsQuery = new HashMap<>();      
      ArrayList<String> chQuery = new ArrayList<>();
      
      while (OuterKeys.hasNext()) {
        String OutKey = OuterKeys.next();
        if (query.get(OutKey) instanceof JSONObject) {
          JSONObject jsonObInner = query.getJSONObject(OutKey);
          Iterator<String> InnerKeys = jsonObInner.keys();
          while (InnerKeys.hasNext()) {
            String InKey = InnerKeys.next();
            //Save key-values of JSON query
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
     
      // Obtain Sample ID
      // Need to modify for more than one ID
      String querySampleIds = new String();
      if (!readSetMap.isEmpty()) {
        if (!readSetMap.get("sample").isEmpty()) {
          querySampleIds = readSetMap.get("sample"); 
          readSetMap.remove("sample");
        }
      }
      
      //Single Read: SAMRecord
      //Fields: ID, Tags, Region, Read Attributes
      //ID: @RG.SM -querySampleIds
      //Tags: SAMRecord.getAttribute() - will receive tag for read - rmapQuery
      //Regions: query with SAMRecord alignment start/end - chQuery
      //Set Order: sample_id(tags(region(read_attributes())))
      // Working fields: Readset: Sample ID, Read: qname, flag, cigar
      long elapsedTime = System.nanoTime();
      try {
        htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<div><h3>getReads</h3><h4>Query Details</h4><ul>"
            + "<li>Sample IDs: " + querySampleIds + "</li>"
            + "<li>Tags: " + readSetMap.toString() + "</li>"
            + "<li>Read Attributes: " + readsQuery.toString() + "</li>"
            + "<li>Regions " + chQuery + "</li></ul>");
      } catch (Exception ex) {
        // Keep going, should not depend on success of htmlReport
      }

      //Next, Query the BAM file for such entries
      SAMFileHeader bamHeader = samReader.getFileHeader();
      List<SAMReadGroupRecord> bamReadGroups = bamHeader.getReadGroups();

      File output = new File("testOutput.bam");
      SAMFileWriterFactory samFactory = new SAMFileWriterFactory();
      SAMFileWriter bfw = samFactory.makeSAMWriter(bamHeader, true, output);
      
      // Query for the Sample IDs
      if (!querySampleIds.isEmpty()) {
        boolean sampleMatch = false; 
        for (SAMReadGroupRecord rec: bamReadGroups) {          
          if (rec.getAttribute("SM").equals(querySampleIds)) {
            sampleMatch = true;
          }
        }         
        if (!sampleMatch) {
          htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>No Results.</p></div>");
          rt.setState(ReturnValue.SUCCESS);
          return rt;
        }
      }
      
      // Organize and query read_set tags
      // Not completely working for all the tags (there's lots of them)
      if (!readSetMap.isEmpty()) {
        boolean readSetMatch = false;
        
        //This part of query is only good for the header line: @HD SO and VN
        //todo: Needs to be fixed for the rest of the header
        for (Entry<String, String> entry : readSetMap.entrySet()) {
          System.out.println(bamHeader.getAttribute(entry.getKey()));
          if (bamHeader.getAttribute(entry.getKey()).equals(entry.getValue())){
            readSetMatch = true;
          }
        }
        if (!readSetMatch) {
          htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>No Results.</p></div>");
          rt.setState(ReturnValue.SUCCESS);
          return rt;
        }
      }
      
      // Narrow down Regions -- currently not working, need to find out how to 
      //   work with the API for this query
      /*
      if (regionArray.length() !=0) {
        
        for (int i=0; i < regionArray.length(); i++) {
          String region = regionArray.get(i).toString();
          if (region.contains(":")) {
            //int regionIndex = Integer.parseInt(region.substring(3, region.indexOf(":")));
            int regionStart = Integer.parseInt(region.substring(region.indexOf(":") + 1, region.indexOf("-")));
            int regionEnd = Integer.parseInt(region.substring(region.indexOf("-") + 1, region.length()));
            
            SAMRecordIterator iter = samReader.queryOverlapping(region, regionStart, regionEnd);
            if (!iter.hasNext()) {
              //if empty, return
              htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>No Results.</p></div>");
              rt.setState(ReturnValue.SUCCESS);
              return rt;
            }
            System.out.println(iter.next().toString());
            iter.close();
          }
        }
      }
      */

      // Check Attributes -- a little redundant here..
      //   to do: finish for the rest of the read attributes
      if (!readsQuery.isEmpty()) {
        boolean qname = false;
        boolean flag = false;
        boolean rname = false;
        boolean pos = false;
        boolean mapq = false;
        boolean cigar = false;
        boolean rnext = false;
        boolean pnext = false;
        boolean tlen = false;
        boolean seq = false;
        boolean qual = false;
        
        for (Entry<String, String> e : readsQuery.entrySet()) {
          switch (e.getKey()) {
            case "qname": qname = true; break;
            case "flag": flag = true; break;
            case "rname": rname = true; break;
            case "pos": pos = true; break;
            case "mapq": mapq = true; break;
            case "cigar": cigar = true; break;
            case "rnext": rnext = true; break;
            case "pnext": pnext = true; break;
            case "tlen": tlen = true; break;
            case "seq": seq = true; break;
            case "qual": qual = true; break;    
          }
        }
        
        for (SAMRecord r: samReader) {
          if (qname) {
            if (!readsQuery.get("qname").equals(r.getReadName()))
              continue;
          } 
          if (flag) {
            if (!readsQuery.get("flag").equals(r.getFlags()))
              continue;
          }
          if (rname) {
            if (!readsQuery.get("rname").equals(r.getReferenceName()))
              continue;
          }
          if (pos) {
            if (!readsQuery.get("pos").equals(r.getAlignmentStart()))
              continue;
          }
          if (mapq) {
            if (!readsQuery.get("mapq").equals(r.getMappingQuality()))
              continue; 
          }
          if (cigar) {
            if (!readsQuery.get("cigar").equals(r.getCigarString()))
              continue;
          }
          if (seq) {
            if (!readsQuery.get("seq").equals(r.getReadString()))
              continue;
          }
          if (qual) {
            if (!readsQuery.get("qual").equals(r.getBaseQualityString()))
              continue;
          }
          bfw.addAlignment(r);
        }
      } else {
        for (SAMRecord r: samReader) {
          bfw.addAlignment(r);
        }
      }
      
      
      // Finally, write a .bam file with result of query
      // Also write to htmlReport
      bfw.close();
      elapsedTime = (System.nanoTime() - elapsedTime) / 1000000;
      try {
        htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>Finished in " + elapsedTime + " milliseconds.</p></div>");
      } catch (Exception ex) {
        rt.setState(ReturnValue.ERROR);
        return rt;
      }
    } catch (Exception ex) {
      System.out.println(ex.toString());
      rt.setState(ReturnValue.ERROR); 
      return rt; 
    }  
    
    rt.setState(ReturnValue.SUCCESS); 
    return rt;  
  }
    
  @Override
  public ReturnValue runPlugin(String queryJSON, Class pluginClass) {
    ReturnValue rt = new ReturnValue(); 
    rt.setState(ReturnValue.NOT_IMPLEMENTED); 
    return rt; 
  }   

  /** getConclusionDocs
   *  Writes htmlReport to file
   */
  @Override
  public ReturnValue getConclusionDocs() {
    ReturnValue rt = new ReturnValue(); 
    // Write HTMLDocuments to file
    try {
      HTMLEditorKit kit = new HTMLEditorKit();
      StringWriter writer = new StringWriter();
      kit.write(writer, htmlReport, 0, htmlReport.getLength());
      String s = writer.toString();
      PrintWriter out = new PrintWriter("htmlReport.html");
      out.print(s);
      out.close();
    } catch (Exception ex) { 
      rt.setState(ReturnValue.ERROR); 
      return rt; 
    } 
     
    rt.setState(ReturnValue.SUCCESS); 
    return rt;
  }
   
  public ReturnValue setupBackend(HashMap<String, String> settings) {
    ReturnValue rt = new ReturnValue(); 
    rt.setState(ReturnValue.NOT_IMPLEMENTED); 
    return rt; 
  }

  /** teardownBackend
   *  Cleans up any variables stored by object
   */
  public ReturnValue teardownBackend(HashMap<String, String> settings) {
    // Close fileReader and drop temporary variables
    samReader.close();
    htmlReport = null;
    samReader = null;
    
    ReturnValue rt = new ReturnValue(); 
    rt.setState(ReturnValue.SUCCESS); 
    return rt; 
  }

    @Override
    public ReturnValue setupBackend(Map<String, String> settings) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnValue teardownBackend(Map<String, String> settings) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
