package io.seqware.oozie.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;

/**
 * This is a temporary workaround for the issue described in SEQWARE-1903.
 * 
 * @author dyuen
 */
public class Fixer {

    public static final String PropertiesFlag = "p";
    public static final String JobFlag = "j";

    public static boolean performFix(Configuration config, String jobID) throws SQLException, ClassNotFoundException {
        String dbUser = config.getString("OOZIE_DB_USER");
        String dbPass = config.getString("OOZIE_DB_PASS");
        String dbServer = config.getString("OOZIE_DB_SERVER");
        String dbDB = config.getString("OOZIE_DB");

        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://" + dbServer + "/" + dbDB;
        Connection connection = DriverManager.getConnection(url, dbUser, dbPass);
        // determine the id and name of the relevant join
        QueryRunner queryRunner = new QueryRunner();
        String query = "select id, name, execution_path from wf_actions where wf_id = '" + jobID
                + "' and type = ':JOIN:' order by start_time DESC LIMIT 1;";
        Logger.getLogger("io.seqware.oozie.util.Fixer").info("Query: " + query);
        Object[] queryResults = queryRunner.query(connection, query, new ArrayHandler());
        String actionID = (String) queryResults[0];
        String name = (String) queryResults[1];
        String executionPathOld = (String) queryResults[2];
        Logger.getLogger("io.seqware.oozie.util.Fixer").debug("Identified relevant join as " + actionID + " with name " + name);
        // get correct execution path
        query = "select execution_path from wf_actions where wf_id = '" + jobID + "' AND transition = '" + name
                + "' order by end_time ASC LIMIT 1;";
        Logger.getLogger("io.seqware.oozie.util.Fixer").info("Query: " + query);
        String executionPathNew = queryRunner.query(connection, query, new ScalarHandler<String>());
        if (executionPathOld.equals(executionPathNew)) {
            Logger.getLogger("io.seqware.oozie.util.Fixer").fatal("Execution path not changed, correct execution path is already in place");
            return true;
        } else {
            query = "update wf_actions set execution_path = '" + executionPathNew + "' where id = '" + actionID + "';";
            Logger.getLogger("io.seqware.oozie.util.Fixer").info("Query: " + query);
            int update = queryRunner.update(connection, query);
            if (update == 1) {
                Logger.getLogger("io.seqware.oozie.util.Fixer").info(
                        "Execution_path changed from " + executionPathOld + " to " + executionPathNew);
            } else {
                Logger.getLogger("io.seqware.oozie.util.Fixer").fatal("Execution path not changed, error updating database");
            }
        }
        return true;
    }

    /**
     * 
     * @param args
     * @throws java.lang.ClassNotFoundException
     */
    public static void main(final String[] args) throws ParseException, ConfigurationException {
        // create Options object
        Options options = new Options();
        options.addOption(PropertiesFlag, "properties", true, "filename of properties file containing jdbc parameters");
        options.addOption(JobFlag, "job", true, "oozie id of the job that requires a fix");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(PropertiesFlag) && cmd.hasOption(JobFlag)) {
            Configuration config = new PropertiesConfiguration(cmd.getOptionValue(PropertiesFlag));
            String jobID = cmd.getOptionValue(JobFlag);
            try {
                if (!Fixer.performFix(config, jobID)) {
                    throw new RuntimeException("Failed to run Oozie fixer");
                }
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException("Failed to run Oozie fixer", ex);
            }
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar oozie-epfixer-1.0.jar", options);
        }
    }

}
