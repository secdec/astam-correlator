package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.framework.TestConstants.RAILSGOAT_SOURCE_LOCATION;
import static org.junit.Assert.assertTrue;

/**
 * Created by sgerick on 4/27/2015.
 */
public class RailsModelParserTest {

    //  TODO - Update these test cases
    //  Declaration of these parameters must have changed since these tests were originally written
    private static final String[][] RAILSGOAT_MODELS = new String [][]{
    //  {"model", "param1", "param2", "param3"},
        //{"analytics", "ip_address", "referrer", "user_agent"},
        //{"benefits", "backup"},
        //{"key_management", "iv", "user_id"},
        {"message", "creator_id", "message", "receiver_id"},
        //{"paid_time_off", "pto_earned", "pto_taken", "sick_days_earned", "sick_days_taken"},
        {"pay", "bank_account_num", "bank_routing_num", "percent_of_deposit"},
        //{"performance", "comments", "date_submitted", "reviewer", "score"},
        //{"retirement", "employee_contrib", "employer_contrib", "total"},
        {"schedule", "date_begin", "date_end", "event_desc", "event_name", "event_type"},
        {"user", "email", "password" },
        //{"work_info", "DoB", "SSN", "bonuses", "income", "years_worked"}
    };


    @Test
    public void testRailsGoatModelParser() {
        File f = new File(RAILSGOAT_SOURCE_LOCATION);
        assert(f.exists());
        assert(f.isDirectory());

        System.err.println("parsing "+f.getAbsolutePath() );
        Map<String, Map<String, ParameterDataType>> modelMap = RailsModelParser.parse(f);
        System.err.println( "\n" + "Parse done." + "\n");
        compareModels(RAILSGOAT_MODELS, modelMap);

    }

    private void compareModels(String[][] testModels, Map<String, Map<String, ParameterDataType>> modelMap) {
        for (String[] testModel : testModels) {
            String testModelName = testModel[0];
            assertTrue(testModelName + " not found in returned modelMap.",
                    modelMap.containsKey(testModelName));

            Map<String, ParameterDataType> modelParams = modelMap.get(testModelName);
            List<String> testParams = new ArrayList<String>(testModel.length - 1);
            testParams.addAll(Arrays.asList(testModel).subList(1, testModel.length));

            assertTrue("Non-equal number of params in model " + testModelName
                            + ". Expected: " + testParams.size()
                            + ", Returned: " + modelParams.size(),
                    modelParams.size() == testParams.size());

            if (!modelParams.keySet().containsAll(testParams)) {
                for (String param : testParams) {
                    assertTrue(param + " not found as param in " + testModelName,
                            modelParams.containsKey(param));
                }
            }


        }

    }


}

