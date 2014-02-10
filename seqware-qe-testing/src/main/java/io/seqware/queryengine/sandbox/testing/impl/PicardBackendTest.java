package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.JSONQueryParser;
import io.seqware.queryengine.sandbox.testing.utils.ReadSearch;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;

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
    ReturnValue rt = new ReturnValue();
    rt.storeKv(BackendTestInterface.DOCS, p.getText());
    rt.setState(ReturnValue.SUCCESS);
    return rt;
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
      JSONQueryParser jsonParser = new JSONQueryParser(queryJSON);
      HashMap<String, String> readSetQuery = jsonParser.getReadSetQuery();
      HashMap<String, String> readsQuery = jsonParser.getReadsQuery();
      HashMap<String, String> chQuery = jsonParser.getRegionsQuery();
      
      long elapsedTime = System.nanoTime();
      /*try {
        htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<div><h3>getReads</h3><h4>Query Details</h4><ul>"
            + "<li>Sample IDs: " + querySampleIds + "</li>"
            + "<li>Tags: " + readSetQuery.toString() + "</li>"
            + "<li>Read Attributes: " + readsQuery.toString() + "</li>"
            + "<li>Regions " + chQuery + "</li></ul>");
      } catch (Exception ex) {
        // Keep going, should not depend on success of htmlReport
      }*/
      ReadSearch rs = new ReadSearch(readSetQuery, readsQuery, chQuery);
      SAMFileWriter bfw = rs.bamSearch(samReader, "testOutput.bam");
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
  public ReturnValue runPlugin(String queryJSON, Class pluginClassName) {
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
