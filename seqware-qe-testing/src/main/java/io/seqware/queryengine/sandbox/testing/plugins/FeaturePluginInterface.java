/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.plugins;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author dyuen
 */
public interface FeaturePluginInterface extends PluginInterface {

    /**
     * This method gets called for each position in the genome.
     * The underlying storage system will have to figure out how to make the
     * features map below which gives the plug-in author a list of all the features
     * that overlap the current position of the genome, grouped by featureSets.
     *
     * @param position
     * @param features
     * @param output
     */
    public void map(long position, Feature inputFeatures, Map<FeatureSet, Collection<Feature>> features, Map<String, String> output);

    /**
     * Used to process the results from the FeatureSet map.
     *
     * @param key
     * @param values
     * @param output
     */
    public void reduce(String key, Iterable<String> values, Map<String, String> output);
    
}
