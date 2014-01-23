/** 
 * This is an extremely basic plugin interface.
 * See the com.github.seqware.queryengine.plugins.* packages for
 * examples of more complex plugins, in particular MapReduce plugins 
 * built using HBase.
 * 
 * For this evaluation project I want to use something very simple.  We're going
 * to have a single plugin interface that contains two sets of methods. The first
 * will be a mapreduce style to process features (variants).  The map method
 * will be called for each position in the genome, a collection of FeatureSets
 * and associated features will then be passed in.  The plugin author implementing
 * the map method will then write a key and value to an object.  That object will
 * be sorted and grouped by keys before being passed to the reduce method.  The
 * reduce method will then be called for each distinct key created in the map output
 * and the reduce method will write its result back to a hash.
 * 
 * There will be a similar pair of map and reduce methods for readsets.  If the
 * particular plugin does not process read or feature data please 
 * 
 * Plugin authors in this test should *not* write large amounts of data to the 
 * output of either the map or reduce phase since each backend will need to implement
 * this and, for those backends that don't have native MapReduce support, it will
 * be difficult to make this output/input coupling robust and able to handle
 * more than what easily fits in memory.
 * 
 * LEFT OFF HERE: need to bring in FeatureSet, Feature, ReadSet, and Read into this project
 * 
 */

package io.seqware.queryengine.sandbox.testing.plugins;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author boconnor
 */
public interface PluginInterface {
    
    /**
     * Allows you to say this plugin will process reads, if false then
     * the calling storage backend can save time and not process reads.
     * @return 
     */
    public boolean canProcessReads();
    
    /**
     * Allows you to say this plugin will process variants, if false then
     * the calling storage backend can save time and not process variants.
     * @return 
     */
    public boolean canProcessVariants();
    
    /**
     * This method gets called for each position in the genome.
     * The underlying storage system will have to figure out how to make the 
     * features map below which gives the plugin author a list of all the features 
     * that overlap the current position of the genome, grouped by featureSets.
     * 
     * @param position
     * @param features
     * @param output 
     */
    public void map(long position, Map<FeatureSet, Collection<Feature>> features, Map<String, String> output);

    /**
     * Used to process the results from the FeatureSet map.
     * 
     * @param key
     * @param values
     * @param output 
     */
    public void featureReduce(String key, Iterable<String> values, Map<String, String> output);
    
    public void map(Map<ReadSet, Collection<Reads>> reads, long position, Map<String, String> output);

    public void readReduce(String key, Iterable<String> values, Map<String, String> output);
    
}
