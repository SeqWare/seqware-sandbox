package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.Global;
import io.seqware.queryengine.sandbox.testing.utils.JSONQueryParser;
import io.seqware.queryengine.sandbox.testing.utils.ReadSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.UUID;

import javax.swing.text.html.HTMLDocument;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;

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

public class GATKPicardBackendTest implements BackendTestInterface {
  
  public Map<String,String> fileMap = 
		  new HashMap<String,String>();
  public static HashMap<String, String> READ_SETS = new HashMap<String, String>();
  public static HTMLDocument htmlReport = new HTMLDocument(); // The HTML Report to be written 
  static String FILTER_SORTED;
  static Set<String> QUERY_KEYS;
  private long StartTime;
  private long EndTime;
  
  @Override
  public String getName(){
    return "GATKPicardBackendTest";
  }
  
  /** getIntroductionDocs()
   *  Creates an HTMLDocument object to use as a log
   */
  @Override
  public ReturnValue getIntroductionDocs() {
	ReturnValue rv = new ReturnValue();
	String introHTML =
			(""
	      + "   <head>"
	      + "     <title>SeqWare Query Engine: GATK_Picard_BackendTest</title>"
	      + "     <style type=\"text/css\">"
	      + "       body { background-color: #EEEEEE; }"
	      + "       h3  { color: red; }"
	      + "     </style>"
	      + "     </head>" 
	      + "     <h1>SeqWare Query Engine: GATK_Picard_BackendTest</h1>"
	      + "     <p>This backend is created through the use of two API's to load and query VCF and BAM files respectively. A JSON String query is used to query the files. </p>"
	      + "     <ul>"
	      + "       <li><b>GATK API</b>: VCF Querying</li>"
	      + "       <li><b>Picard/SAMTools API</b>: BAM Querying</li>"
	      + "     </ul>"
	      + "     <hr>"
	      + "     <h3><center>GATK API</center></h3>"
	      + "     <hr>"
	      + "     <h4><b>Performance</b></h4>"
	      + "       <div>"
	      + "	      <p>"
	      + "           The VCF files have loaded in " +  " milliseconds on average. It has so far been tested with VCF files with sizes up to 2 gigs. It takes approdimately a minute to apply a query of 4 features to a file 1.5 GB in size."
	      + "         </p>"
	      + "       </div>"
	      + "     <h4>GATK documentation and usage</h4>"
	      + "       <div>"
	      + "         <p>"
	      + "           There is a lack of documentation on GATK tools, as it seems to be geared towards end users and not developers. It was difficult at first to figure out how the data flowed between the ndecessary functions"
	      + "           to read a VCF file and extract the required data. This was purely a fault of the lack of documentation, as the organization of the functions seems to be make alot of sense after having gone through the initial"
	      + "           learning curve."
	      + "         </p>"
	      + "       </div>"
	      + "	  <hr>"
	      + "     <h3><center>Picard/SAMTools API</center><h3>"
	      + "     <hr>"
	      + "     <h4><b>Performance</b></h4>"
	      + "     <h4>Picard/SAMTools documentation and usage</h4>"
	      );
	rv.getKv().put(BackendTestInterface.DOCS, introHTML);
	return rv;
  }
  
  //This is used to decompress the GZ file to get the vcf
  public String gzDecompressor(String filePathGZ) throws IOException{
	  File inputGZFile = 
			  new File(filePathGZ);
	  String filename = inputGZFile
				.getName()
				.substring(0, inputGZFile.getName().indexOf("."));
	  byte[] buf = 
			  new byte[1024];
      int len;
	  String outFilename = "src/main/resources/" + filename + ".vcf";
	  FileInputStream instream = 
			  new FileInputStream(filePathGZ);
      GZIPInputStream ginstream = 
    		  new GZIPInputStream(instream);
      FileOutputStream outstream = 
    		  new FileOutputStream(outFilename);
      System.out.println("Decompressing... " + filePathGZ);
      while ((len = ginstream.read(buf)) > 0) 
     {
       outstream.write(buf, 0, len);
     }
      outstream.close();
      ginstream.close();
      
	  return outFilename;
  }
  
  @Override
  public ReturnValue loadFeatureSet(String filePath) { 
	  ReturnValue rv = new ReturnValue();
	  GATKPicardBackendTest bt = new GATKPicardBackendTest();
	  String fileExtension = FilenameUtils.getExtension(filePath);
	  String gzDecompressedVCF = new String();
	  String VALUE = new String();
	  File vcfFile;
		//Check if file ext is VCF.
		if (fileExtension.equals("vcf")||
				fileExtension.equals("gz")||
				fileExtension.equals("tbi")){
			String htmlFragment = "<div><h3>loadFeatureSet</h3><p>Loaded file in time: " + " milliseconds</p></div>";
			//Decompress the gz file
			if (fileExtension.equals("gz")){
				try {
					gzDecompressedVCF = bt.gzDecompressor(filePath);
					vcfFile = new File(gzDecompressedVCF);
					VALUE = gzDecompressedVCF;
					String vcfID = vcfFile
							.getName()
							.substring(0, vcfFile.getName().indexOf("."));
					fileMap.put(vcfID, gzDecompressedVCF);
					System.out.println("Decompressed to " + gzDecompressedVCF);
				} catch (IOException e) {
					e.printStackTrace();
				}
//			} else if (fileExtension.equals("tbi")){
//				vcfFile = new File(filePath);
//				VALUE = filePath;
//				String vcfID = vcfFile
//						.getName()
//						.substring(0, vcfFile.getName().indexOf("."));
//				fileMap.put(vcfID, filePath);
			} else if (fileExtension.equals("vcf")){
				vcfFile = new File(filePath);
				VALUE = filePath;
				String vcfID = vcfFile
						.getName()
						.substring(0, vcfFile.getName().indexOf("."));
				fileMap.put(vcfID, filePath);
			} 
			
			//State of SUCCESS
			rv.setState(ReturnValue.SUCCESS);
			rv.getKv().put(BackendTestInterface.FEATURE_SET_ID, VALUE);
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
      // Generate UUID for the file and store
      UUID id = UUID.randomUUID();
      READ_SETS.put(id.toString(), filePath);
      
      rt.storeKv(BackendTestInterface.READ_SET_ID, id.toString());
      rt.setState(ReturnValue.SUCCESS);
      return rt; 
    } else if (filePath.endsWith(".bai")) {
      
      UUID id = UUID.randomUUID();
      READ_SETS.put(id.toString(), filePath);
      
      rt.storeKv(BackendTestInterface.READ_SET_ID, id.toString());
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
  //TODO: get all features that were loaded into fileMap.
  public ReturnValue getFeatures(String queryJSON) throws JSONException, IOException { 
	  System.out.println("Testing now.");
	  System.out.println(fileMap);
		//Read the input JSON file to seperate ArrayLists for parsing
	  	String queryJSONChecked;
	  	
	  	//This is to check if there is a totally blank query input (without even braces)
	   	if (queryJSON.equals("")){
	   		queryJSONChecked = "{}";
	   	} else {
	   		queryJSONChecked = queryJSON;
	   	}
	   	
		ReturnValue rv = new ReturnValue();
		JSONArray regionArray;

//		File sortedVcfFile = new File(fileMap.get("exampleVCFinput"));
		
		//Points to input VCF file to process
		//Points to output filepath
		String filePath = "src/main/resources/output/output.txt";
	    PrintWriter writer = 
	    		new PrintWriter(filePath, "UTF-8");
		
	    Iterator fileMapIter = fileMap.entrySet().iterator();
	    while (fileMapIter.hasNext()){
	    	Map.Entry vcfFilePath = (Map.Entry)fileMapIter.next();
	    	File sortedVcfFile = new File(vcfFilePath.getValue().toString());
			JSONQueryParser JParse = new JSONQueryParser(queryJSONChecked);
			int featureSetCount = 0;
			//Initialize query stores to dump queries from input JSON
			HashMap<String, String> featureMapQuery = JParse.getFeaturesQuery();
			HashMap<String, String> featureSetMapQuery = JParse.getFeatureSetQuery();
			HashMap<String, String> regionMapQuery = JParse.getRegionsQuery();
			
			QUERY_KEYS = featureMapQuery.keySet();
			
		    Iterator featureSetMapIter = featureSetMapQuery
		    		.entrySet()
		    		.iterator();
		    
		    /**CHECK IF THE CURRENT FEATURESETID IN QUERY BEING LOOPED MATCHES THE FILENAME IN CURRENT fileMap ENTRY**/
		    //try to move this inside the vcfIterator, so it will read the VCF file no matter the query
		    while (featureSetMapIter.hasNext()){
		    	Map.Entry featureSetID = (Map.Entry)featureSetMapIter.next();
	    		String featureSetValue = featureSetID
	    				.getValue()
	    				.toString();
	    		if (featureSetID.getKey().toString().equals(vcfFilePath.getKey().toString()) || featureSetID.getValue().equals(null)){
	    			featureSetCount = 1;
	    		}
		    }		
				/**INITIALIZE READING OF VCF INPUT**/ 
				Iterator<VariantContext> vcfIterator;
				{
		
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
				    String variantAttribute = new String();
				    
				    //Used to help sort the Filter column during reading the VCF
				    Set<String> filterSet;
				    List<String> filterSetQuery;
				    
				    //Used to help sort the Filter column during reading the VCF
				    Set<String> infoKeySet;
				    Set<String> infoKeySetQuery;
				    Map<String, String> infoMapQuery;
		
				    //Determine if user has input a chromosome query
				    if (regionMapQuery.containsKey(".") == false){ // if query does not contain ALL chromosome 
				    	fieldSize = featureMapQuery.size() 
				    			+1; //This is from one match from the chromoso
				    } else{
				    	fieldSize = featureMapQuery.size();
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
						        } else {continue;}
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
					
		
				}	
	    }writer.close();
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
    try {
      //First, parse the query for related fields
      JSONQueryParser jsonParser = new JSONQueryParser(queryJSON);
      HashMap<String, String> readSetQuery = jsonParser.getReadSetQuery();
      HashMap<String, String> readsQuery = jsonParser.getReadsQuery();
      HashMap<String, String> regionsQuery = jsonParser.getRegionsQuery();
      ArrayList<SAMRecord> samList = new ArrayList<SAMRecord>();
      
      ReadSearch rs = new ReadSearch(readSetQuery, readsQuery, regionsQuery);
      
      //Build the header
      SAMFileHeader resultHeader =  new SAMFileHeader();
      resultHeader.setAttribute(SAMFileHeader.VERSION_TAG, "1.0");
      
      //Iterate over all the saved read sets
      if (queryJSON.isEmpty()) {
        for (Entry<String, String> e: READ_SETS.entrySet()) {
          if (e.getValue().endsWith(".bai"))
            continue;
          
          File bamfile = new File(e.getValue());
          SAMFileReader samReader = new SAMFileReader(bamfile);
          for (SAMRecord r: samReader) {
            samList.add(r);
          }
          List<SAMReadGroupRecord> rgs = samReader.getFileHeader().getReadGroups();
          for (SAMReadGroupRecord rg: rgs ) {
            if (null == resultHeader.getReadGroup(rg.getReadGroupId())) {
              resultHeader.addReadGroup(rg);
            }
          }
          List<SAMSequenceRecord> sequences = samReader.getFileHeader().getSequenceDictionary().getSequences();
          for (SAMSequenceRecord seq: sequences) { 
            if (null == resultHeader.getSequence(seq.getSequenceName())) {
              resultHeader.addSequence(seq);
            }
          }
        }
      } else {
        for (Entry<String, String> e: READ_SETS.entrySet()) {
          if (e.getValue().endsWith(".bai"))
            continue;
          
          if (null != READ_SETS.get(e.getKey() + "index")) {
            File bamfile = new File(e.getValue());
            File baifile = new File(READ_SETS.get(e.getKey() + "index"));
            SAMFileReader samReader = new SAMFileReader(bamfile, baifile, true);
            samList.addAll(rs.bamSearch(samReader));
            List<SAMReadGroupRecord> rgs = samReader.getFileHeader().getReadGroups();
            for (SAMReadGroupRecord rg: rgs ) {
              if (null == resultHeader.getReadGroup(rg.getReadGroupId())) {
                resultHeader.addReadGroup(rg);
              }
            }
            List<SAMSequenceRecord> sequences = samReader.getFileHeader().getSequenceDictionary().getSequences();
            for (SAMSequenceRecord seq: sequences) { 
              if (null == resultHeader.getSequence(seq.getSequenceName())) {
                resultHeader.addSequence(seq);
              }
            }
          } else {
            File bamfile = new File(e.getValue());
            SAMFileReader samReader = new SAMFileReader(bamfile);
            samList.addAll(rs.bamSearch(samReader));
            resultHeader = samReader.getFileHeader();          
            List<SAMReadGroupRecord> rgs = samReader.getFileHeader().getReadGroups();
            for (SAMReadGroupRecord rg: rgs ) {
              if (null == resultHeader.getReadGroup(rg.getReadGroupId())) {
                resultHeader.addReadGroup(rg);
              }
            }
            List<SAMSequenceRecord> sequences = samReader.getFileHeader().getSequenceDictionary().getSequences();
            for (SAMSequenceRecord seq: sequences) { 
              if (null == resultHeader.getSequence(seq.getSequenceName())) {
                resultHeader.addSequence(seq);
              }
            }
          }
        }
      }
      String outputPath = "queryOutput.bam";
      File outputFile = new File(outputPath);
      SAMFileWriterFactory writerFactory = new SAMFileWriterFactory();
      SAMFileWriter bfw = writerFactory.makeBAMWriter(resultHeader, true, outputFile);
      
      for (SAMRecord r: samList){
        bfw.addAlignment(r);
      }
      bfw.close();
      rt.storeKv(BackendTestInterface.QUERY_RESULT_FILE, outputPath);
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
    ReturnValue rv = new ReturnValue(); 
    String conclusionHTML = 
      (   "");
    rv.getKv().put(BackendTestInterface.DOCS, conclusionHTML); 
    rv.setState(ReturnValue.SUCCESS); 
    return rv;
  }
  
  @Override
  public ReturnValue setupBackend(Map<String, String> settings) {
    //Either output file or db backend?
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }
  
  @Override
  public ReturnValue teardownBackend(Map<String, String> settings) {
    ReturnValue rt = new ReturnValue();
    rt.setState(ReturnValue.SUCCESS);
    return(rt);
  }
}
