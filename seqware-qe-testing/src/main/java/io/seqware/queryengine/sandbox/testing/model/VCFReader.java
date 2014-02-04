package io.seqware.queryengine.sandbox.testing.model;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;

//This defines a reader class to create a VariantContext 
//iterator for an individual VCF file to read the file.

public class VCFReader {
	File sortedVcfFile;
    
    public VCFReader(String filePath){
    	sortedVcfFile = new File(filePath);
    }
    
    public Iterator<VariantContext> getVCFIterator() throws IOException{
	    final VCFCodec vcfCodec = new VCFCodec(); //declare codec
	    final boolean requireIndex = false; //index not required
	    FeatureReader<VariantContext> reader = getFeatureReader(sortedVcfFile, vcfCodec, requireIndex);
	    Iterator<VariantContext> vcfIterator = reader.iterator();
	    return vcfIterator;
    }

	private FeatureReader<VariantContext> getFeatureReader(File sVcfFile, final VCFCodec vcfCodec, final boolean requireIndex) {
		return AbstractFeatureReader.getFeatureReader(sVcfFile.getAbsolutePath(), vcfCodec, requireIndex);
	}
    
}