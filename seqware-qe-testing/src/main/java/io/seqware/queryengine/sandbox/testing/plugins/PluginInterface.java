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
 */

package io.seqware.queryengine.sandbox.testing.plugins;

/**
 *
 * @author boconnor
 */
public interface PluginInterface {
    
    
    
}
