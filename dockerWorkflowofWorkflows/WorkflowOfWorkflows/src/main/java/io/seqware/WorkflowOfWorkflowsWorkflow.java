package io.seqware;

import java.util.Random;
import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowDataModel;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import org.apache.commons.lang.RandomStringUtils;

/**
 * <p>
 * For more information on developing workflows, see the documentation at <a
 * href="http://seqware.github.io/docs/6-pipeline/java-workflows/">SeqWare Java Workflows</a>.
 * </p>
 *
 * Quick reference for the order of methods called: 1. setupDirectory 2. setupFiles 3. setupWorkflow 4. setupEnvironment 5. buildWorkflow
 *
 * See the SeqWare API for <a href=
 * "http://seqware.github.io/javadoc/stable/apidocs/net/sourceforge/seqware/pipeline/workflowV2/AbstractWorkflowDataModel.html#setupDirectory%28%29"
 * >AbstractWorkflowDataModel</a> for more information.
 */
public class WorkflowOfWorkflowsWorkflow extends AbstractWorkflowDataModel {

    @Override
    public void buildWorkflow() {

        Random random = new Random();
        // randomly generate the number of chained seqware jobs between 2 and 5
        int steps = random.nextInt(4) + 2;

        try {

            Job previousJob = null;
            for (int i = 0; i < steps; i++) {
                // create a job and link it up if necessary
                Job currentJob = this.getWorkflow().createBashJob("job_" + i);
                // create input and output
                currentJob.getCommand().addArgument("mkdir -m 0777 -p workspace" + i + "\n");
                currentJob.getCommand().addArgument("mkdir -m 0777 -p workspace" + (i + 1) + "\n");
                currentJob.getCommand().addArgument("mkdir -m 0777 -p datastore" + i + "\n");
                currentJob.getCommand().addArgument("touch workspace" + i + "/content.txt \n");
                currentJob.getCommand().addArgument(
                        "docker run --rm -h master \\\n"
                                // mount input inside the nested container
                                + "-v `pwd`/workspace" + i + ":/input \\\n"
                                // mount output directory
                                + "-v `pwd`/workspace" + (i + 1) + ":/output \\\n"
                                // mount the workflow inside the inner container
                                + "-v " + getProperty("workflow_dir") + ":/workflow \\\n"
                                // mount a datastore directory so that we can look at the working directory for seqware inside the container
                                + "-v `pwd`/datastore" + i + ":/datastore \\\n"
                                // run a docker container with seqware 1.1.0-rc.1
                                + "quay.io/seqware/seqware_whitestar:quay \\\n"
                                // this is the actual command we run inside the container, which is to launch a workflow
                                + "seqware bundle launch \\\n"
                                // create some output
                                + "--dir /workflow "
                                +
                                // this is the command the workflow will run
                                "--override command=\"cat /input/content.txt > /output/content.txt && echo "
                                + RandomStringUtils.randomAlphabetic(5) + " >> /output/content.txt && "
                                // ownership inside and output containers is wonky
                                + "chmod a+wrx /output/content.txt \" --no-metadata ;\n");
                if (previousJob != null) {
                    currentJob.addParent(previousJob);
                }
                previousJob = currentJob;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read workflow location from ini", e);
        }
    }

}
