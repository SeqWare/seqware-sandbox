package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.*;
import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.broadinstitute.variant.vcf.VCFIDHeaderLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;

/**
 * TestBackends
 * 
 * This tool is designed to iterate over the available backends and call them
 * one-by-one, producing data structures that can be turned into reports.
 *
 */
public class GATKBackendTest implements BackendTestInterface
{
    
	@Override
	public ReturnValue getIntroductionDocs() {
		return null;
	}

	@Override
	public ReturnValue setupBackend(Map<String, String> settings) {
		return null;
	}

	@Override
	public ReturnValue loadFeatureSet(String filePath) {
		ReturnValue state = new ReturnValue();
		
		//Check if file ext is VCF.
		if (FilenameUtils.getExtension(filePath).equals("vcf")){ 
			String KEY = "gene";
			String VALUE = filePath;
			Global.HBaseStorage.put(KEY, VALUE);
			
			//State of SUCCESS
			state.setState(0);
			return state;
		} else {
			
			//State of NOT_SUPPORTED
			state.setState(3);
			return state;
		}
	}

	@Override
	public ReturnValue loadReadSet(String filePath) {
		return null;
	}

	@Override
	public ReturnValue getFeatures(String queryJSON) throws JSONException, IOException {
		
		//Read the input JSON file to seperate ArrayLists for parsing
		ReturnValue finished = new ReturnValue();
		JSONObject jsonObOuter = new JSONObject(queryJSON);
		JSONArray regionArray;
		Iterator<String> OutterKeys = jsonObOuter.keys();
		
		//Points to input VCF file to process
		//Points to output filepath
		File sortedVcfFile = new File(Global.HBaseStorage.get("gene").toString());
		String filePath = Global.outputFilePath;
		
		txtJSONParser JParse = new txtJSONParser(queryJSON);
		
		//Initialize query stores to dump queries from input JSON
		HashMap<String, String> FEATURE_MAP_QUERY = JParse.getFEATURE_MAP_QUERY();
		HashMap<String, String> FEATURE_SET_MAP_QUERY = JParse.getFEATURE_SET_MAP_QUERY();
		HashMap<String, String> REGION_MAP_QUERY = JParse.getREGION_MAP_QUERY();
		
		/**INITIALIZE READING OF VCF INPUT
		 * 
		 */
		Iterator<VariantContext> vcfIterator;
		{
		    PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		    final VCFCodec vcfCodec = new VCFCodec(); //declare codec
		    final boolean requireIndex = false; //index not required
		    BufferedReader vcfReader = new BufferedReader(new FileReader(sortedVcfFile));
		    FeatureReader<VariantContext> reader = getFeatureReader(sortedVcfFile, vcfCodec, requireIndex);
		    vcfIterator = reader.iterator();
		    VCFHeader header = (VCFHeader) reader.getHeader();
		    
		    //Initialize variables for query checking
		    int FIELD_SIZE; //Points needed for a variant Attribute (totaled through all features, and one from region) to be VALID
		    int FIELD_COUNTER;
		    int CHROM_QUERY_UPPER_BOUND; //Region query
		    int CHROM_QUERY_LOWER_BOUND;
		    int VARIANT_CHROM_ID;
		    List<Map> sorted = new ArrayList<Map>();
		    String CHROM_ID = new String();
		    String QUERY_ATTRIBUTE;
		    String VARIANT_CHROM_PAIR;
		    String VARIANT_ATTRIBUTE;
		    String temp= new String();

		    //Determine if user has input a chromosome query
		    if (REGION_MAP_QUERY.containsKey(".") == false){ // if query does not contain ALL chromosome 
		    	FIELD_SIZE = FEATURE_MAP_QUERY.size() +1;
		    } else{
		    	FIELD_SIZE = FEATURE_MAP_QUERY.size();
		    }
		    
		    /**BEGIN LOOPING OF EVERY HEADER LINE TO MATCH FOR FEATURE_SET QUERY RESULTS
		     * 
		     */
		    for (VCFIDHeaderLine CURRENT_HEADER_LINE : header.getIDHeaderLines()){
		    	Iterator FEATURE_SET_MAP_ITERATOR = FEATURE_SET_MAP_QUERY
		    			.entrySet()
		    			.iterator();
		    	while (FEATURE_SET_MAP_ITERATOR.hasNext()){
		    		Map.Entry pairs = (Map.Entry)FEATURE_SET_MAP_ITERATOR.next();
		    		String HEADER_FEATURE_SET_VALUE = pairs
		    				.getValue()
		    				.toString();
		    		if (HEADER_FEATURE_SET_VALUE.equals(CURRENT_HEADER_LINE.getID())){
		    			writer.println(CURRENT_HEADER_LINE);
		    		}
		    	}
		    }
		    
		    /**BEGIN LOOPING OF EVERY VARIANT LINE TO MATCH FOR CHROM_ID, (RANGE), FEATURE RESULTS
		     * 
		     */
			while (vcfIterator.hasNext()){
				
				//Reset the field counter on next line
				FIELD_COUNTER = 0; 
				VariantContext VARIANT_CONTEXT = vcfIterator.next();
				
			    Iterator REGION_MAP_ITERATOR = REGION_MAP_QUERY
			    		.entrySet()
			    		.iterator();
				Iterator FEATURE_MAP_ITERATOR = FEATURE_MAP_QUERY
						.entrySet()
						.iterator();

				//Loop through each query in Chromosomes in VCF
				while(REGION_MAP_ITERATOR.hasNext()){
					Map.Entry CHROM_PAIR = (Map.Entry)REGION_MAP_ITERATOR.next();
					
					//GATHER FIRST POINT FROM MATCHING CHROMOSOME NUMBER AND RANGE IN QUERY
					if (CHROM_PAIR.getKey().equals(VARIANT_CONTEXT.getChr())){ //check if query in chromosomes matches current CHROM_ID in variant
						VARIANT_CHROM_PAIR = CHROM_PAIR
								.getValue()
								.toString();
						
						//Checks if the current variant contains ALL POS
						if (VARIANT_CHROM_PAIR.equals(".") == true){ 
							FIELD_COUNTER++;
							CHROM_ID = CHROM_PAIR
									.getKey()
									.toString();
							
						} else if (VARIANT_CHROM_PAIR.equals(".") == false) {
								
								CHROM_QUERY_LOWER_BOUND = Integer.parseInt(VARIANT_CHROM_PAIR.substring(
										0,VARIANT_CHROM_PAIR.indexOf("-")));
								
								CHROM_QUERY_UPPER_BOUND = Integer.parseInt(VARIANT_CHROM_PAIR.substring(
										VARIANT_CHROM_PAIR.indexOf("-")+1, 
										VARIANT_CHROM_PAIR.length()));
								
								VARIANT_CHROM_ID = VARIANT_CONTEXT.getStart();

								//checks if current variant POS is within specified range
								if (VARIANT_CHROM_ID >= CHROM_QUERY_LOWER_BOUND && VARIANT_CHROM_ID <= CHROM_QUERY_UPPER_BOUND){ 
									FIELD_COUNTER++;
									CHROM_ID = CHROM_PAIR
											.getKey()
											.toString();
								}
						}
					} else if (CHROM_PAIR.getKey().toString().equals(".")){
						CHROM_ID = VARIANT_CONTEXT.getChr().toString();
						System.out.println(CHROM_ID);
					}
					
					//GATHER THE REST OF THE POINTS FROM MATCHING ALL THE FEATURES IN QUERY
				    while (FEATURE_MAP_ITERATOR.hasNext()) { //loop through each query in features
				        Map.Entry pairs = (Map.Entry)FEATURE_MAP_ITERATOR.next();
					    String VARIANT_ATTRIBUTE_NOT_MATCHED = new String();
					    
				        VARIANT_ATTRIBUTE = VARIANT_CONTEXT
				        		.getAttributeAsString(
				        				pairs.getKey().toString(), 
				        				VARIANT_ATTRIBUTE_NOT_MATCHED);
				        
				        QUERY_ATTRIBUTE = pairs
				        		.getValue()
				        		.toString();
				        
				        if (VARIANT_ATTRIBUTE.equals(QUERY_ATTRIBUTE)){	
				        	
				        	//Accumulate points
				        	FIELD_COUNTER++; 
				        }
				    }
				    
				    /**
				    FINAL CHECK, ONLY VARIANTS THAT HAVE MATCHED ALL THE SEARCH CRITERA WILL RUN BELOW
				    **/
				    
				    if (FIELD_COUNTER == FIELD_SIZE){
				    	Iterator ATTRIBUTE_MAP_ITERATOR = VARIANT_CONTEXT
				    			.getAttributes()
				    			.entrySet()
				    			.iterator();
				    	
				    	String ATTRIBUTE_SORTED = new String();
				    	String ATTRIBUTE_SORTEDHolder = new String();
				    	
				    	//Resort the info field from a map format to match VCF format
				    	while(ATTRIBUTE_MAP_ITERATOR.hasNext()){ 
				    		Map.Entry pair = (Map.Entry)ATTRIBUTE_MAP_ITERATOR.next();
				    		
				    		ATTRIBUTE_SORTEDHolder = pair.getKey().toString() + "=" + 
				    								 pair.getValue().toString() + ";";
				    		
				    		ATTRIBUTE_SORTED = ATTRIBUTE_SORTED + ATTRIBUTE_SORTEDHolder;
				    	}
				    	
				    	//Write variant to a TSV file
				    	writer.println("chr" + CHROM_ID + "\t" +
				    					VARIANT_CONTEXT.getEnd() + "\t" +
				    					VARIANT_CONTEXT.getReference() + "\t" +
				    					VARIANT_CONTEXT.getAltAlleleWithHighestAlleleCount() + "\t" +
				    					ATTRIBUTE_SORTED.toString().substring(0, ATTRIBUTE_SORTED.length()-1)); //Remove last semicolon in ATTRIBUTE_SORTED
				    }
				}
			}
			writer.close();
		}	
		finished.storeKv("queryResultFile", filePath);
		return finished;
	}
	
	private FeatureReader<VariantContext> getFeatureReader(File sVcfFile, final VCFCodec vcfCodec, final boolean requireIndex) {
		return AbstractFeatureReader.getFeatureReader(sVcfFile.getAbsolutePath(), vcfCodec, requireIndex);
	}

	@Override
	public ReturnValue getReads(String queryJSON) {
		return null;
	}

	@Override
	public ReturnValue runPlugin(String queryJSON, String pluginClassName) {
		return null;
	}

	@Override
	public ReturnValue getConclusionDocs() {
		return null;
	}

	@Override
	public ReturnValue teardownBackend(Map<String, String> settings) {
		return null;
	}

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
