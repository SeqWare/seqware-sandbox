package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;
import io.seqware.queryengine.sandbox.testing.utils.Global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
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

import org.apache.commons.io.FilenameUtils;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.broadinstitute.variant.vcf.VCFIDHeaderLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Splitter;

public class GATK_Picard_BackendTest implements BackendTestInterface {
  
 
  public static SAMFileReader samReader; // Used to read the SAM/BAM file.
  public static HTMLDocument htmlReport; // The HTML Report to be written 
  static String FILTER_SORTED;
  static Set<String> QUERY_KEYS;
  
  @Override
  public String getName(){
    return "GATK and Picard BackendTest";
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
    htmlReport = (HTMLDocument) p.getDocument();    
    ReturnValue r = new ReturnValue();
    r.setState(ReturnValue.SUCCESS);
    return r;
  }
  
  @Override
  public ReturnValue loadFeatureSet(String filePath) { 
	  ReturnValue rv = new ReturnValue();
		
		//Check if file ext is VCF.
		if (FilenameUtils.getExtension(filePath).equals("vcf")){
			String featureSetId = BackendTestInterface.FEATURE_SET_ID;
			File vcfFile = 
					new File(filePath);
			String VALUE = filePath;
//			Global.HBaseStorage.put(KEY, VALUE);
		    long elapsedTime = System.nanoTime();
			elapsedTime = (System.nanoTime() - elapsedTime) / 1000000;
			
			
			
	        try {
				htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<div><h3>loadFeatureSet</h3><p>Loaded file in time: "+ elapsedTime + " milliseconds</p></div>");
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//State of SUCCESS
			rv.setState(ReturnValue.SUCCESS);
			rv.getKv().put(featureSetId, VALUE);
			return rv;
		} else {
			
			//State of NOT_SUPPORTED
			rv.setState(ReturnValue.BACKEND_FILE_IMPORT_NOT_SUPPORTED);
			return rv;
		} 
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
         
  private FeatureReader<VariantContext> getFeatureReader(File sVcfFile, final VCFCodec vcfCodec, final boolean requireIndex) {
	  return AbstractFeatureReader.getFeatureReader(sVcfFile.getAbsolutePath(), vcfCodec, requireIndex);
  }	
  
  @Override
  public ReturnValue getFeatures(String queryJSON) throws JSONException, IOException { 
		
		//Read the input JSON file to seperate ArrayLists for parsing
		ReturnValue rv = new ReturnValue();
		JSONObject jsonObOuter = new JSONObject(queryJSON);
		JSONArray regionArray;
		Iterator<String> OutterKeys = jsonObOuter.keys();
		
		//Points to input VCF file to process
		//Points to output filepath
		
		File sortedVcfFile = new File(Global.HBaseStorage.get("gene").toString());
		String filePath = Global.outputFilePath;
		
		txtJSONParser JParse = new txtJSONParser(queryJSON);
		
		//Initialize query stores to dump queries from input JSON
		HashMap<String, String> featureMapQuery = JParse.getfeatureMapQuery();
		HashMap<String, String> featureSetMapQuery = JParse.getfeatureSetMapQuery();
		HashMap<String, String> regionMapQuery = JParse.getregionMapQuery();
		
		QUERY_KEYS = featureMapQuery.keySet();
		
		/**INITIALIZE READING OF VCF INPUT**/ 
		Iterator<VariantContext> vcfIterator;
		{
		    PrintWriter writer = 
		    		new PrintWriter(filePath, "UTF-8");
		    final VCFCodec vcfCodec = 
		    		new VCFCodec(); //declare codec
		    final boolean requireIndex = 
		    		false; //index not required
		    BufferedReader vcfReader = 
		    		new BufferedReader(new FileReader(sortedVcfFile));
		    FeatureReader<VariantContext> reader = 
		    		getFeatureReader(sortedVcfFile, vcfCodec, requireIndex);
		    vcfIterator = 
		    		reader.iterator();
		    VCFHeader header = 
		    		(VCFHeader) reader.getHeader();
		    
		    //Initialize variables for query checking
		    int fieldSize; //Points needed for a variant Attribute (totaled through all features, and one from region) to be VALID
		    int fieldCounter;
		    int miniFieldCounter;
		    int chromQueryUpperBound; //Region query
		    int chromQueryLowerBound;
		    int variantChromID;
		    List<Map> sorted = new ArrayList<Map>();
		    String chromID = new String();
		    String queryAttribute;
		    String variantChromPair;
		    String variantAttribute;
		    
		    //Used to help sort the Filter column during reading the VCF
		    Set<String> filterSet;
		    List<String> filterSetQuery;
		    
		    //Used to help sort the Filter column during reading the VCF
		    Set<String> infoKeySet;
		    Set<String> infoKeySetQuery;
		    Map<String, String> infoMapQuery;

		    //Determine if user has input a chromosome query
		    if (regionMapQuery.containsKey(".") == false){ // if query does not contain ALL chromosome 
		    	fieldSize = featureMapQuery.size() +1;
		    } else{
		    	fieldSize = featureMapQuery.size();
		    }
		    
		    /**BEGIN LOOPING OF EVERY HEADER LINE TO MATCH FOR FEATURE_SET QUERY RESULTS**/
		    for (VCFIDHeaderLine headerLine : header.getIDHeaderLines()){
		    	Iterator featureSetMapIter = featureSetMapQuery
		    			.entrySet()
		    			.iterator();
		    	while (featureSetMapIter.hasNext()){
		    		Map.Entry pairs = (Map.Entry)featureSetMapIter.next();
		    		String headerFeatureSetValue = pairs
		    				.getValue()
		    				.toString();
		    		if (headerFeatureSetValue.equals(headerLine.getID())){
		    			writer.println(headerLine);
		    		}
		    	}
		    }
		    
		    /**BEGIN LOOPING OF EVERY VARIANT LINE TO MATCH FOR chromID, (RANGE), FEATURE RESULTS**/
			while (vcfIterator.hasNext()){
				FILTER_SORTED = "";
				//Reset the field counter on next line
				fieldCounter = 0; 
				miniFieldCounter = 0;
				
				VariantContext variantContext = vcfIterator.next();
				
			    Iterator regionMapIter = regionMapQuery
			    		.entrySet()
			    		.iterator();
				Iterator featureMapIter = featureMapQuery
						.entrySet()
						.iterator();

				//Loop through each query in Chromosomes in VCF
				while(regionMapIter.hasNext()){
					
					Map.Entry chromPair = (Map.Entry)regionMapIter.next();
					//GATHER FIRST POINT FROM MATCHING CHROMOSOME NUMBER AND RANGE IN QUERY
					if (chromPair.getKey().equals(variantContext.getChr())){ //check if query in chromosomes matches current chromID in variant
						variantChromPair = chromPair
								.getValue()
								.toString();
						
						//Checks if the current variant contains ALL POS
						if (variantChromPair.equals(".") == true){ 
							fieldCounter++;
							chromID = chromPair
									.getKey()
									.toString();
							
						} else if (variantChromPair.equals(".") == false) {
								
								chromQueryLowerBound = Integer.parseInt(variantChromPair.substring(
										0,variantChromPair.indexOf("-")));
								
								chromQueryUpperBound = Integer.parseInt(variantChromPair.substring(
										variantChromPair.indexOf("-")+1, 
										variantChromPair.length()));
								
								variantChromID = variantContext.getStart();

								//checks if current variant POS is within specified range
								if (variantChromID >= chromQueryLowerBound && variantChromID <= chromQueryUpperBound){ 
									fieldCounter++;
									chromID = chromPair
											.getKey()
											.toString();
								}
						}
					} else if (chromPair.getKey().toString().equals(".")){
						chromID = variantContext.getChr().toString();
					}
					
					/**GATHER THE REST OF THE POINTS FROM MATCHING ALL THE FEATURES IN QUERY**/
					//loop through each query in features
				    while (featureMapIter.hasNext()) { 
				        Map.Entry pairs = (Map.Entry)featureMapIter.next();
				        
				        String colname = pairs
				        		.getKey()
				        		.toString();
				        
			        	queryAttribute = pairs
			        			.getValue()
			        			.toString();
			        	
				        if(colname.equals("INFO")){
						    String variantAttributeNotMatched = new String();
						    
						    infoMapQuery = Splitter.on(",").withKeyValueSeparator("=").split(
						    		queryAttribute);
						    
						    infoKeySet = variantContext
						    		.getAttributes()
						    		.keySet();

						    infoKeySetQuery = infoMapQuery.keySet();
						    
						    Map<String,Object> infoMap = variantContext.getAttributes();
						    
						    for (String variantKey : infoMap.keySet()){
						    	for (String queryKey : infoKeySetQuery){
						    		if (infoMap.get(variantKey).toString().equals(infoMapQuery.get(queryKey))){
						    			miniFieldCounter++;
						    		}
						    	}
						    }
						    
					        if (infoKeySet.containsAll(infoKeySetQuery) &&
					        		miniFieldCounter == infoMapQuery.size()){	
					        	//Accumulate points
					        	fieldCounter++; 
					        }
					        
					      //add point if QUAL matches
				        } else if (colname.equals("QUAL")){
				        	variantAttribute = String.valueOf(variantContext
				        			.getPhredScaledQual()); 
				        	
				        	variantAttribute = variantAttribute //Truncate String converted double.
				        			.substring(0, variantAttribute.indexOf("."));

					        if (variantAttribute.equals(queryAttribute)){	
					        	
					        	//Accumulate points
					        	fieldCounter++; 
					        }
					        
					        //add point if ID matches
				        } else if (colname.equals("ID")){
				        	variantAttribute = variantContext
				        			.getID().toString();
				        	
					        if (variantAttribute.equals(queryAttribute)){	
					        	
					        	//Accumulate points
					        	fieldCounter++;
					        }
				        } else if (colname.equals("FILTER")){
				        	filterSet = variantContext.getFilters();
				        	if ((filterSet.size() == 0) //check for no filters applied
				        			&& (queryAttribute.toLowerCase().equals("false"))){
				        		fieldCounter++;
				        		FILTER_SORTED = "PASS";
				        	} else if (filterSet.size() != 0){ //filters were applied, add them
				        		
				        		filterSetQuery = Splitter.onPattern(",").splitToList(queryAttribute);
				        		if (filterSetQuery.containsAll(filterSet) 
				        				&& filterSet.containsAll(filterSetQuery)){
				        			fieldCounter++;
				        			
				        		}
				        	}
				        } else if (colname.equals("ALT")){
				        	variantAttribute = variantContext.getAltAlleleWithHighestAlleleCount().toString();
				        	
				        	if (variantAttribute.equals(queryAttribute)){
				        		fieldCounter++;
				        	}
				        }
				    }
				    
				    /**
				    FINAL CHECK, ONLY VARIANTS THAT HAVE MATCHED ALL THE SEARCH CRITERA WILL RUN BELOW
				    **/
				    
				    if (fieldCounter == fieldSize){
				    	Iterator attributeMapIter = variantContext
				    			.getAttributes()
				    			.entrySet()
				    			.iterator();
				    	
				    	Iterator filterIter = variantContext
				    			.getFilters()
				    			.iterator();
				    	
				    	String attributeSorted = new String();
				    	String attributeSortedHolder = new String();
				    	
				    	String filterSortedHolder = new String();
				    	Set<String> filterSortedSet;
				    	
				    	/**Resort the info field from a map format to match VCF format**/
				    	while(attributeMapIter.hasNext()){ 
				    		Map.Entry pair = (Map.Entry)attributeMapIter.next();
				    		
				    		attributeSortedHolder = pair.getKey().toString() + "=" + 
				    								 pair.getValue().toString() + ";";
				    		
				    		attributeSorted = attributeSorted + attributeSortedHolder;
				    	}
				    	
				    	/**Resort the filter set from a set format to match VCF format**/
				    	//This runs if there is FILTER in the JSON query
				    	if (QUERY_KEYS.contains("FILTER")){ 
					    	while(filterIter.hasNext()){
					    		filterSortedHolder = filterIter.next().toString() + ";";
					    		
					    		FILTER_SORTED = FILTER_SORTED + filterSortedHolder;
					    	}
					    	
					    	FILTER_SORTED = FILTER_SORTED.substring(0, FILTER_SORTED.length()-1);
				    	
				    	//This runs if there is no FILTER in the JSON query
				    	} else if (!QUERY_KEYS.contains("FILTER")){ 
				    		filterSortedSet = variantContext.getFilters();
				    		
				    		//If there is no filter applied, assume that the feature is a PASS
				    		if (filterSortedSet.size() == 0){ 
				    			FILTER_SORTED = "PASS";
				    			
			    			//If there are filter(s) applied, add them to the TSV file output
				    		} else if (filterSortedSet.size() != 0){  
						    	while(filterIter.hasNext()){ 
						    		filterSortedHolder = filterIter.next().toString() + ";";
						    		FILTER_SORTED = FILTER_SORTED + filterSortedHolder;
						    	}
						    	//Remove the last semicolon
						    	FILTER_SORTED = FILTER_SORTED.toString().substring(0, FILTER_SORTED.length()-1); 
				    		}
				    	}
				    	
				    	//Prepare score to be written to TSV file
			        	String PhredScore = String.valueOf(variantContext
			        			.getPhredScaledQual()); 
			        	
			        	PhredScore = PhredScore //Truncate String converted double.
			        			.substring(0, PhredScore.indexOf("."));
			        	
				    	//Write variant to a TSV file
				    	writer.println("chr" + chromID + "\t" +
				    					variantContext.getEnd() + "\t" +
				    					variantContext.getID() + "\t" +
				    					variantContext.getReference() + "\t" +
				    					variantContext.getAltAlleleWithHighestAlleleCount() + "\t" +
				    					PhredScore + "\t" +
				    					FILTER_SORTED + "\t" +
				    					attributeSorted.toString().substring(0, attributeSorted.length()-1)); //Remove last semicolon in attributeSorted
				    }
				}

			}
			writer.close();
		}	
		long elapsedTime = System.nanoTime();

		try {
			htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>Finished in " + elapsedTime + " milliseconds.</p></div>");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		rv.getKv().put(BackendTestInterface.QUERY_RESULT_FILE, filePath);
		return rv; 
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
//    rt.getKv().put(BackendTestInterface.DOCS, )
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
