import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabase;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabaseFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CLI {

    public static void LOG(String msg) {
        System.out.println(msg);
    }

    private static FrameworkType parseFrameworkString(String framework) {
        Object[] possibleValues = FrameworkType.class.getEnumConstants();
        FrameworkType result = null;
        for (Object value : possibleValues) {
            FrameworkType type = (FrameworkType)value;
            if (type.getDisplayName().equalsIgnoreCase(framework) || type.toString().equalsIgnoreCase(framework)) {
                result = type;
            }
        }

        if (result == FrameworkType.NONE) {
            LOG("WARN: 'NONE' is an invalid framework type, using 'DETECT' instead");
            result = FrameworkType.DETECT;
        }

        if (result == null) {
            LOG("WARN: Couldn't parse framework type '" + framework + "', using 'DETECT' instead");
            result = FrameworkType.DETECT;
        }

        return result;
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            LOG("Parameters:");
            LOG("* -verbose");
            LOG("* /path/to/source/code");
            LOG("* FRAMEWORK@/path/to/source/code");
            LOG("\tSupported values:");
            Object[] possibleFrameworkTypes = FrameworkType.class.getEnumConstants();
            for (Object type : possibleFrameworkTypes) {
                FrameworkType frameworkType = (FrameworkType)type;
                if (frameworkType != FrameworkType.NONE) {
                    LOG("\t - " + frameworkType.toString());
                }
            }
            LOG("\n");
            LOG("Examples:");
            LOG("");
            LOG("-- Run HAM on '/etc/project dir/' with JSP parsing; run HAM on '/etc/other-dir/' with framework auto-detection");
            LOG("> java -jar threadfix-ham-cli.java \"JSP@/etc/project dir\" /etc/other-dir/ ");
            LOG("");
            LOG("-- Run HAM on 'path/to/project' with verbose output enabled");
            LOG("> java -jar threadfix-ham-cli.java -verbose \"path/to/project\"");
            return;
        }

        Config config = new Config();

        List<HamTask> tasks = new ArrayList<HamTask>();
        for (String arg : args) {

            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                if(arg.equalsIgnoreCase("verbose")) {
                    config.verbose = true;
                    LOG("Using verbose output");
                } else {
                    LOG("WARN: Unknown parameter '" + arg + "'");
                }
                continue;
            }

            String frameworkName = null;
            if (arg.contains("@")) {
                String[] parts = arg.split("@");
                frameworkName = parts[0];
                arg = parts[1];
            }
            File file = new File(arg);
            if (!file.exists() || !file.isDirectory()) {
                LOG("ERROR: Path is not a folder or does not exist: " + arg);
            } else {
                HamTask newTask = new HamTask();
                newTask.targetDirectory = file;
                if (frameworkName != null) {
                    newTask.frameworkType = parseFrameworkString(frameworkName);
                }

                tasks.add(newTask);
            }
        }

        LOG("Running HAM on " + tasks.size() + " directories:");
        for (HamTask task : tasks) {
            LOG("- " + task.targetDirectory.getAbsolutePath() + "(" + task.frameworkType + ")");
        }

        for (HamTask task : tasks) {
            try {
                LOG("Starting HAM for " + task.targetDirectory.getAbsolutePath());
                EndpointDatabase endpointDatabase;
                if (task.frameworkType == FrameworkType.DETECT) {
                    endpointDatabase = EndpointDatabaseFactory.getDatabase(task.targetDirectory);
                } else {
                    endpointDatabase = EndpointDatabaseFactory.getDatabase(task.targetDirectory, task.frameworkType);
                }

                if (endpointDatabase == null) {
                    LOG("WARN: Could not make EndpointDatabase for " + task.targetDirectory.getAbsolutePath() + ", skipping");
                    continue;
                }
                List<Endpoint> resultEndpoints = endpointDatabase.generateEndpoints();
                LOG("Found " + resultEndpoints.size() + " endpoints");

                if (config.verbose) {
                    LOG("Endpoints:");
                    for (Endpoint endpoint : resultEndpoints) {
                        LOG("- " + endpoint.toString());
                    }
                }

                List<RouteParameter> parameters = new ArrayList<RouteParameter>();
                for (Endpoint endpoint : resultEndpoints) {
                    parameters.addAll(endpoint.getParameters().values());
                }

                LOG("Found " + parameters.size() + " parameters");

                int numOptional = 0;
                int numHaveDataType = 0;
                int numHaveParamType = 0;
                int numHaveAcceptedValues = 0;
                for (RouteParameter param : parameters) {
                    if (param.isOptional()) {
                        ++numOptional;
                    }
                    if (param.getDataType() != null) {
                        ++numHaveDataType;
                    }
                    if (param.getParamType() != RouteParameterType.UNKNOWN) {
                        ++numHaveParamType;
                    }
                    if (param.getAcceptedValues() != null && param.getAcceptedValues().size() > 0) {
                        ++numHaveAcceptedValues;
                    }
                }
                LOG("- " + numOptional + "/" + parameters.size() + " parameters are optional");
                LOG("- " + (parameters.size() - numHaveDataType) + "/" + parameters.size() + " are missing their data type");
                LOG("- " + (parameters.size() - numHaveParamType) + "/" + parameters.size() + " are missing their parameter type");
                LOG("- " + numHaveAcceptedValues + "/" + parameters.size() + " have a list of accepted values");

            } catch (Exception e) {
                LOG("ERROR: Exception occurred while parsing endpoints for " + task.targetDirectory.getAbsolutePath());
                e.printStackTrace();
            } finally {
                LOG("Finished HAM for " + task.targetDirectory.getAbsolutePath());
            }
        }
    }
}
