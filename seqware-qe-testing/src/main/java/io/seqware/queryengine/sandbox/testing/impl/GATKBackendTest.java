package io.seqware.queryengine.sandbox.testing.impl;

import io.seqware.queryengine.sandbox.testing.utils.*;
import io.seqware.queryengine.sandbox.testing.BackendTestInterface;
import io.seqware.queryengine.sandbox.testing.ReturnValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.sting.gatk.walkers.variantutils.SelectHeaders;
import org.broadinstitute.variant.variantcontext.Allele;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.variantcontext.writer.VariantContextWriter;
import org.broadinstitute.variant.variantcontext.writer.VariantContextWriterFactory;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.broadinstitute.variant.vcf.VCFIDHeaderLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

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
    public static void main( String[] args ) throws IOException, JSONException
    {
        ReturnValue returned = new ReturnValue();
        GATKBackendTest testb = new GATKBackendTest();
        BufferedReader in;
        String line = new String();
        String temp = new String();
        
        //Point to local VCF file to be read
        testb.loadFeatureSet("/Users/bso/gene6.vcf");   

 
        //Point to local JSON text file to be read
        in = new BufferedReader(new FileReader("/Users/bso/queryJSON3.txt"));
        while ((line = in.readLine()) != null){
        	temp = temp.concat(line);
        }
		
        //Point to Output file to be written to
        Global.outputFilePath = "/Users/bso/output2.txt";
        
		//Obtain matched features
        returned = testb.getFeatures(temp);
    }

	@Override
	public ReturnValue getIntrocutionDocs() {
		return null;
	}

	@Override
	public ReturnValue setupBackend(HashMap<String, String> settings) {
		return null;
	}

	@Override
	public ReturnValue loadFeatureSet(String filePath) {
		ReturnValue state = new ReturnValue();
		if (FilenameUtils.getExtension(filePath).equals("vcf")){ //Check if file ext is VCF.
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
		
		//Initialize query stores to dump queries from input JSON
		HashMap<String, String> FEATURE_MAP_QUERY = new HashMap<String,String>();
		HashMap<String, String> fsmapQuery = new HashMap<String,String>();
		HashMap<String, String> REGION_MAP_QUERY = new HashMap<String,String>();

		//READ THE JSON INPUT FILE
		/**	"OutKey":
		{
			"InKey": "jsonObInner.get(InKey)"
		}*/
		while (OutterKeys.hasNext()){
			String OutKey = OutterKeys.next();
			
			if (jsonObOuter.get(OutKey) instanceof JSONObject){
				JSONObject jsonObInner = jsonObOuter.getJSONObject(OutKey);
				Iterator<String> InnerKeys = jsonObInner.keys();
				
				while (InnerKeys.hasNext()){
					String InKey = InnerKeys.next();
					
					if (OutKey.equals("feature_sets")){
						fsmapQuery.put(InKey.toString(), 
										jsonObInner.getString(InKey));
					}
					
					if (OutKey.equals("features")){
						FEATURE_MAP_QUERY.put(InKey.toString(), 
										jsonObInner.getString(InKey));
					}
				}
			} else if (jsonObOuter.get(OutKey) instanceof JSONArray){
				JSONArray jsonArInner = jsonObOuter.getJSONArray(OutKey);
					if(OutKey.equals("regions")){
						regionArray = jsonObOuter.getJSONArray(OutKey);
						
						for (int i=0; i< regionArray.length(); i++){
							String region = regionArray
											.get(i)
											.toString();
							
							if (region.contains(":") == false){
								
								//i.e. selects "22" from "chr22"
								String chromosomeID = region.substring(
										region.indexOf("r")+1,
										region.length());
								
								REGION_MAP_QUERY.put(chromosomeID.toString(), 
												".");
								
							} else if (region.contains(":") == true){
								
								//i.e. selects "22" from "chr22:1-99999"
								String chromosomeID = region.substring(
										region.indexOf("r")+1,
										region.indexOf(":"));
								
								String range = region.substring(
										region.indexOf(":")+1,
										region.length());
								
								REGION_MAP_QUERY.put(chromosomeID.toString(), 
												range.toString());
							}
						}
					}
			}
		}
		
		/**INITIALIZE READING OF VCF INPUT
		 * 
		 */
		Iterator<VariantContext> vcfIterator;
		{
			//Initialize reader and writer
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
		    String VARIANT_CHROM_PAIR = new String();
		    String CHROM_ID = new String();
		    String VARIANT_ATTRIBUTE_NOT_MATCHED = new String();
		    String temp= new String();

		    //Determine if user has input a chromosome query
		    if (REGION_MAP_QUERY.size() >= 1){
		    	FIELD_SIZE = FEATURE_MAP_QUERY.size() +1;
		    } else{
		    	FIELD_SIZE = FEATURE_MAP_QUERY.size();
		    }
		    
		    for (VCFIDHeaderLine HeaderLineListElement : header.getIDHeaderLines()){
		    	writer.println(HeaderLineListElement);
		    }
		    
		    /**BEGIN LOOPING OF EVERY VARIANT LINE TO MATCH FOR QUERY RESULTS
		     * 
		     */
			while (vcfIterator.hasNext()){ //loop through each VARIANT
			    Iterator REGION_MAP_ITERATOR = REGION_MAP_QUERY
			    		.entrySet()
			    		.iterator();
			    
				Iterator FEATURE_MAP_ITERATOR = FEATURE_MAP_QUERY
						.entrySet()
						.iterator(); 
				
				VariantContext VARIANT_CONTEXT = vcfIterator.next();
				FIELD_COUNTER = 0; //Reset the field counter

				while(REGION_MAP_ITERATOR.hasNext()){//loop through each query in chromosomes
					Map.Entry CHROM_PAIR = (Map.Entry)REGION_MAP_ITERATOR.next();
					
					//GATHER FIRST POINT FROM MATCHING CHROMOSOME NUMBER AND RANGE IN QUERY
					if (CHROM_PAIR.getKey().equals(VARIANT_CONTEXT.getChr())){ //check if query in chromosomes matches current CHROM_ID in variant
						VARIANT_CHROM_PAIR = CHROM_PAIR
								.getValue()
								.toString();
						
						if (VARIANT_CHROM_PAIR.equals(".") == true){ //Checks if the current variant contains ALL POS
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

								if (VARIANT_CHROM_ID >= CHROM_QUERY_LOWER_BOUND && VARIANT_CHROM_ID <= CHROM_QUERY_UPPER_BOUND){ //checks if current variant POS is within specified range
									FIELD_COUNTER++;
									CHROM_ID = CHROM_PAIR
											.getKey()
											.toString();
								}
						}
					}
					
					//GATHER THE REST OF THE POINTS FROM MATCHING ALL THE FEATURES IN QUERY
				    while (FEATURE_MAP_ITERATOR.hasNext()) { //loop through each query in features
				        Map.Entry pairs = (Map.Entry)FEATURE_MAP_ITERATOR.next();
				        
				        String VARIANT_ATTRIBUTE = VARIANT_CONTEXT
				        		.getAttributeAsString(
				        				pairs.getKey().toString(), 
				        				VARIANT_ATTRIBUTE_NOT_MATCHED);
				        
				        String QUERY_ATTRIBUTE = pairs
				        		.getValue()
				        		.toString();
				        
				        if (VARIANT_ATTRIBUTE.equals(QUERY_ATTRIBUTE)){				        	
				        	FIELD_COUNTER++; //Accumulate points
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
				    	
				    	//Write variant to file
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
	public ReturnValue teardownBackend(HashMap<String, String> settings) {
		return null;
	}
}
