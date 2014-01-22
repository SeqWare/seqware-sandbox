package io.seqware.queryengine.sandbox.testing;

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
public class TestBackends implements BackendTestInterface
{
    public static void main( String[] args ) throws IOException
    {
        ReturnValue returned = new ReturnValue();
        TestBackends testb = new TestBackends();
        BufferedReader in;
        String line = new String();
        String temp = new String();
        
        //Store the pathfield into dummy backend, a global var : Global.HBaseBackend
        testb.loadFeatureSet("/Users/bso/gene5.vcf");        

        //Convert Dummy JSONQuery input text file to single string
		try { 
			in = new BufferedReader(new FileReader("/Users/bso/queryJSON.txt"));
	        while ((line = in.readLine()) != null){
	        	temp = temp.concat(line);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Obtain matched features
		try {
			returned = testb.getFeatures(temp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		String filePath = new String("/Users/bso/output2.txt");
		
		//Initialize query stores to dump queries from input JSON
		HashMap<String, String> fmapQuery = new HashMap<String,String>();
		HashMap<String, String> fsmapQuery = new HashMap<String,String>();
		HashMap<String, String> regmapQuery = new HashMap<String,String>();

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
						fmapQuery.put(InKey.toString(), 
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
								
								regmapQuery.put(chromosomeID.toString(), 
												".");
								
							} else if (region.contains(":") == true){
								
								//i.e. selects "22" from "chr22:1-99999"
								String chromosomeID = region.substring(
										region.indexOf("r")+1,
										region.indexOf(":"));
								
								String range = region.substring(
										region.indexOf(":")+1,
										region.length());
								
								regmapQuery.put(chromosomeID.toString(), 
												range.toString());
							}
						}
					}
			}
		}
		
		//Initialize comparison process
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
		    int Querysize; //Points needed for a variant Attribute (totaled through all features, and one from region) to be VALID
		    int fieldCounter;
		    int upperBound; //Region query
		    int lowerBound;
		    int variantChr;
		    List<Map> sorted = new ArrayList<Map>();
		    String chrPairsTempasString = new String();
		    String chrID = new String();
		    String noMatch = new String();
		    String temp= new String();

		    //Determine if user has input a chromosome query
		    if (regmapQuery.size() >= 1){
		    	Querysize = fmapQuery.size() +1;
		    } else{
		    	Querysize = fmapQuery.size();
		    }
		    
		    /**BEGIN LOOPING OF EVERY VARIANT LINE TO MATCH FOR QUERY RESULTS
		     * 
		     */
			while (vcfIterator.hasNext()){ //loop through each VARIANT
			    Iterator regmapIterator = regmapQuery
		    							.entrySet()
		    							.iterator();
			    
				Iterator fmapIterator = fmapQuery
										.entrySet()
										.iterator(); //iterate over fmapQuery
				
				VariantContext variantContext = vcfIterator.next();
				fieldCounter = 0; //Reset the field counter
				
				while(regmapIterator.hasNext()){//loop through each query in chromosomes
					Map.Entry chrPairs = (Map.Entry)regmapIterator.next();
					
					//GATHER FIRST POINT FROM MATCHING CHROMOSOME NUMBER AND RANGE IN QUERY
					if (chrPairs.getKey().equals(variantContext.getChr())){ //check if query in chromosomes matches current chrID in variant
						chrPairsTempasString = chrPairs
								.getValue()
								.toString();
						
						if (chrPairsTempasString.equals(".") == true){ //Checks if the current variant contains ALL POS
							fieldCounter++;
							chrID = chrPairs
									.getKey()
									.toString();
							
						} else if (chrPairsTempasString.equals(".") == false) {
								
								lowerBound = Integer.parseInt(chrPairsTempasString.substring(
										0,chrPairsTempasString.indexOf("-")));
								
								upperBound = Integer.parseInt(chrPairsTempasString.substring(
										chrPairsTempasString.indexOf("-")+1, 
										chrPairsTempasString.length()));
								
								variantChr = variantContext.getStart();

								if (variantChr >= lowerBound && variantChr <= upperBound){ //checks if current variant POS is within specified range
									fieldCounter++;
									chrID = chrPairs
											.getKey()
											.toString();
								}
						}
					}
					
					//GATHER THE REST OF THE POINTS FROM MATCHING ALL THE FEATURES IN QUERY
				    while (fmapIterator.hasNext()) { //loop through each query in features
				        Map.Entry pairs = (Map.Entry)fmapIterator.next();
				        
				        String variantAttribute = variantContext
				        		.getAttributeAsString(
				        				pairs.getKey().toString(), 
				        				noMatch);
				        
				        String queryAttribute = pairs
				        		.getValue()
				        		.toString();
				        
				        if (variantAttribute.equals(pairs.getValue().toString())){				        	
				        	fieldCounter++; //Accumulate points
				        }
				    }
				    
				    /**
				    FINAL CHECK, ONLY VARIANTS THAT HAVE MATCHED ALL THE SEARCH CRITERA WILL RUN BELOW
				    **/
				    
				    if (fieldCounter == Querysize){
				    	Iterator attributeIter = variantContext
				    							.getAttributes()
				    							.entrySet()
				    							.iterator();
				    	
				    	String attributeSorted = new String();
				    	String attributeSortedHolder = new String();
				    	
				    	//Resort the info field from a map format to match VCF format
				    	while(attributeIter.hasNext()){ 
				    		Map.Entry pair = (Map.Entry)attributeIter.next();
				    		
				    		attributeSortedHolder = pair.getKey().toString() + "=" + 
				    								pair.getValue().toString() + ";";
				    		
				    		attributeSorted = attributeSorted + attributeSortedHolder;
				    	}
				    	
				    	//Write variant to file
				    	writer.println("chr" + chrID + "\t" +
				    					variantContext.getEnd() + "\t" +
				    					variantContext.getReference() + "\t" +
				    					variantContext.getAltAlleleWithHighestAlleleCount() + "\t" +
				    					attributeSorted.toString().substring(0, attributeSorted.length()-1)); //Remove last semicolon in attributeSorted
				    }
				}
			}
			writer.close();
		}	
		finished.storeKv("queryResultFile", filePath);
		return finished;
	}
	
	private FeatureReader<VariantContext> getFeatureReader(File sVcfFile, 
															final VCFCodec vcfCodec, 
															final boolean requireIndex) {
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
