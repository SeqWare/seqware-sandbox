/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.plugins;

import io.seqware.queryengine.sandbox.testing.utils.JSONQueryParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.json.JSONException;

import com.google.common.base.Splitter;

/**
 *
 * @author boconnor
 */

public class Feature{
	ArrayList<String> Feature = new ArrayList<String>();
	static String FILTER_SORTED;
	static Set<String> QUERY_KEYS;
	
	public Feature(){
		
	}
	
	public Feature(String line){
		String currentToken;
		StringTokenizer st = new StringTokenizer(line,"\t");
		while (st.hasMoreTokens()){
			currentToken = st.nextToken();
			Feature.add(currentToken);
		}
	}
	
	public String getChr(){
		return Feature.get(0);
	}
	
	public String getPos(){
		return Feature.get(1);
	}
	
	public String getID(){
		return Feature.get(2);
	}
	
	public String getRef(){
		return Feature.get(3);
	}
	
	public String getAlt(){
		return Feature.get(4);
	}
	
	public String getQual(){
		return Feature.get(5);
	}
	
	public String getFilter(){
		return Feature.get(6);
	}
	
	public Map<String,String> getInfo(){
		Map<String,String> infoMap = new HashMap<String,String>();
		infoMap = splitToMap(Feature.get(7));
		return infoMap;
	}
	
	//Apply Filter, write to new temporary files
	public void readVCFinfo(String inputFile, String queryJSON, String outputFile, String filename) throws JSONException, IOException {
				
		//Points to input VCF file to process
		//Points to output filepath
		File sortedVcfFile = new File(inputFile);

		File filePath = new File(outputFile+"/"+filename+".txt");
		
		JSONQueryParser JParse = new JSONQueryParser(queryJSON);
		
		//Initialize query stores to dump queries from input JSON
		HashMap<String, String> featureMapQuery = JParse.getFeaturesQuery();
		HashMap<String, String> regionMapQuery = JParse.getRegionsQuery();
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
		    
		    Set<String> filterSet;
		    List<String> filterSetQuery;
		    
		    Set<String> infoKeySet;
		    Set<String> infoKeySetQuery;
		    
		    Map<String, String> infoMapQuery;
		   
		    //Determine if user has input a chromosome query
		    if (regionMapQuery.containsKey(".") == false){ // if query does not contain ALL chromosome 
		    	fieldSize = featureMapQuery.size() +1;
		    } else{
		    	fieldSize = featureMapQuery.size();
		    }
		    
		    /**BEGIN LOOPING OF EVERY VARIANT LINE TO MATCH FOR CHROM_ID, (RANGE), FEATURE RESULTS**/
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
				    while (featureMapIter.hasNext()) { //loop through each query in features
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
	}
	
	private FeatureReader<VariantContext> getFeatureReader(File sVcfFile, final VCFCodec vcfCodec, final boolean requireIndex) {
		return AbstractFeatureReader.getFeatureReader(sVcfFile.getAbsolutePath(), vcfCodec, requireIndex);
	}
	
	private Map<String,String> splitToMap(String in){
		return Splitter.onPattern(";").withKeyValueSeparator("=").split(in);
	}
	
}
