package io.seqware;

import java.util.Map;
import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowDataModel;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
/**
 * <p>For more information on developing workflows, see the documentation at
 * <a href="http://seqware.github.io/docs/6-pipeline/java-workflows/">SeqWare Java Workflows</a>.</p>
 * 
 * Quick reference for the order of methods called:
 * 1. setupDirectory
 * 2. setupFiles
 * 3. setupWorkflow
 * 4. setupEnvironment
 * 5. buildWorkflow
 * 
 * See the SeqWare API for 
 * <a href="http://seqware.github.io/javadoc/stable/apidocs/net/sourceforge/seqware/pipeline/workflowV2/AbstractWorkflowDataModel.html#setupDirectory%28%29">AbstractWorkflowDataModel</a> 
 * for more information.
 */
public class GenericWorkflow extends AbstractWorkflowDataModel {

    private String command;
    private String memory = null; 
    private String javaMemory = null;

    private void init() {
	try {
	    String commandParam = "command";
	    String memoryParam = "memory";
	    String javaMemoryParam = "java_memory";
	    //optional properties
	    command = getProperty(commandParam);
	    if (hasPropertyAndNotNull(memoryParam)) {
		memory = getProperty(memoryParam);
	    }
	    if (hasPropertyAndNotNull(javaMemoryParam)) {
		javaMemory = getProperty(javaMemoryParam);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Could not read command from ini", e);
	}
    }

    @Override
    public void setupDirectory() {
	//since setupDirectory is the first method run, we use it to initialize variables too.
	init();
    }
 
    @Override
    public void buildWorkflow() {
        // a simple bash job to call mkdir
	// note that this job uses the system's mkdir (which depends on the system being *nix)
	// this also translates into a 3000 h_vmem limit when using sge 
        Job mkdirJob = this.getWorkflow().createBashJob("command");
        mkdirJob.getCommand().addArgument(command);      
	if (memory != null){
	    mkdirJob.setMaxMemory(memory);
	}
	if (javaMemory != null){
	    mkdirJob.getCommand().setMaxMemory(javaMemory);
	}
    }
}
