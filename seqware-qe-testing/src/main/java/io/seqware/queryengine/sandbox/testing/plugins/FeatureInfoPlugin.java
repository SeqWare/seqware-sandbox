package io.seqware.queryengine.sandbox.testing.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.plugins.Feature;
import io.seqware.queryengine.sandbox.testing.plugins.FeatureSet;
import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;
import io.seqware.queryengine.sandbox.testing.model.VCFReader;

public class FeatureInfoPlugin implements FeaturePluginInterface{
	
	//These will take the featuresInfoTree of VCF data from different files and perform mapreduce
	@Override
	public void map(long position,
			Map<FeatureSet, Collection<Feature>> features,
			Map<String, String> output) {

	}
	
	@Override
	public void reduce(String key, Iterable<String> values,
			Map<String, String> output) {
	}


	
}
