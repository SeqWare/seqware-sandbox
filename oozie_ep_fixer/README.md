## Oozie Execution Path Fixer Utility

This is a partial workaround for the issue described in SEQWARE-1903 or more importantly OOZIE-1879.
If you're lucky, this will set the execution path correctly and allow the workflow to continue. 

In short, when seqware workflow-run retry fails, give this a try. 

### Running

Run the utility with an Oozie job id and a properties file containing JDBC parameters for the Oozie database. 

        java -jar target/oozie-epfixer-1.0-jar-with-dependencies.jar -j <oozie id> -p test.properties

For example: 

	java -jar target/oozie-epfixer-1.0-jar-with-dependencies.jar -j 0000019-140603165511591-oozie-oozi-W -p test.properties

An example of the properties file is provided as sample.properties
