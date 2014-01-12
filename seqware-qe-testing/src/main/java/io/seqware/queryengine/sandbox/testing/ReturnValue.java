/**
 * This is just a super simple return object that can be used in our testing
 * to send back information about runtime, error states, etc to the calling
 * test program.
 * 
 * Some notes:
 * 
 * <ul>
 * <li>The state will represent different states in our test system</li>
 * <li>The kv HashMap will contain various key-values the test object wants to pass back to the reporting tool</li>
 * </ul>
 * 
 * TODO:
 * 
 * <ul>
 *   <li>we will want to define what goes into the key-value hashmap</li>
 *   <li>we will want to add more states</li>
 * </ul>
 */

package io.seqware.queryengine.sandbox.testing;

import java.util.HashMap;

/**
 *
 * @author boconnor
 */
public class ReturnValue {
    
    // constants
    // everything is OK!
    public static final int SUCCESS = 0;
    // unspecified error
    public static final int ERROR = 1;
    // this method is currently not implemented but we plan on it
    public static final int NOT_IMPLEMENTED = 2;
    // this particular method cannot be implemented given the limtations of this backend
    public static final int NOT_SUPPORTED = 3;
    // the backend requirement (like HBase daemon) is not setup properly so the test are not possible
    public static final int BACKEND_NOT_SETUP = 4;
    // the backend setup for this implementation did not work
    public static final int BACKEND_SETUP_FAILURE = 5;
    // the backend doesn't support this file import type
    public static final int BACKEND_FILE_IMPORT_NOT_SUPPORTED = 6;
    
    // obj vars
    private int state = SUCCESS;
    private HashMap<String, String> kv = new HashMap<String, String>();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public HashMap<String, String> getKv() {
        return kv;
    }
    
}
