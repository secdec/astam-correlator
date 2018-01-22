////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.webapp.validator;

import com.denimgroup.threadfix.data.entities.DayInWeek;
import com.denimgroup.threadfix.data.entities.ScheduledFrequencyType;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;
import com.denimgroup.threadfix.data.entities.ScheduledPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by jrios on 6/27/2017.
 */
@Component
public class GitPollMonitorValidator implements Validator {

    /**
     * Can this {@link Validator} {@link #validate(Object, Errors) validate}
     * instances of the supplied {@code clazz}?
     * <p>This method is <i>typically</i> implemented like so:
     * <pre class="code">return Foo.class.isAssignableFrom(clazz);</pre>
     * (Where {@code Foo} is the class (or superclass) of the actual
     * object instance that is to be {@link #validate(Object, Errors) validated}.)
     *
     * @param clazz the {@link Class} that this {@link Validator} is
     *              being asked if it can {@link #validate(Object, Errors) validate}
     * @return {@code true} if this {@link Validator} can indeed
     * {@link #validate(Object, Errors) validate} instances of the
     * supplied {@code clazz}
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return ScheduledGitPoll.class.equals(clazz);
    }


    @Autowired
    private Validator validator;

    /**
     * Validate the supplied {@code target} object, which must be
     * of a {@link Class} for which the {@link #supports(Class)} method
     * typically has (or would) return {@code true}.
     * <p>The supplied {@link Errors errors} instance can be used to report
     * any resulting validation errors.
     *
     * @param target the object that is to be validated (can be {@code null})
     * @param errors contextual state about the validation process (never {@code null})
     * @see {ValidationUtils}
     */
    @Override
    public void validate(Object target, Errors errors) {
        ScheduledGitPoll gitPoll = (ScheduledGitPoll)target;
        validator.validate(gitPoll, errors);

        //Validate all field values
        if(gitPoll.isEnabled()) {

            if (gitPoll.getFrequency() == null || gitPoll.getFrequency().length() == 0) {
                errors.rejectValue("frequency", "errors.required");
            }

            else {
                ScheduledFrequencyType frequencyType = ScheduledFrequencyType.getFrequency(gitPoll.getFrequency());
                if (frequencyType == null) {
                    errors.rejectValue("frequency", "errors.invalid.monitor.frequency");
                }
                else{

                    //regardless always validate minutes
                    if(gitPoll.getMinute() < 0 || gitPoll.getMinute() >  59)
                        errors.rejectValue("minute", "errors.invalid.monitor.minute");


                    //lets validate values for the recurring schedules
                    if(frequencyType == ScheduledFrequencyType.RECURRING){
                        if(gitPoll.getHour() < 0 || gitPoll.getHour() > 23)
                            errors.rejectValue("hour", "errors.invalid.monitor.hour");

                    }else{

                        //checks when not recurring
                        if(gitPoll.getHour() < 1 || gitPoll.getHour() > 12)
                            errors.rejectValue("hour", "errors.invalid.monitor.hour");



                        if (gitPoll.getPeriod() != null || gitPoll.getPeriod().length() > 0) {
                            if (ScheduledPeriodType.getPeriod(gitPoll.getPeriod()) == null) {
                                errors.rejectValue("period", "errors.invalid.monitor.period");
                            }
                        }

                        //checks when scheduled weekly
                        if(frequencyType == ScheduledFrequencyType.WEEKLY){
                            if (gitPoll.getDay() != null && gitPoll.getDay().length() > 0) {
                                if(DayInWeek.getDay(gitPoll.getDay()) == null)
                                    errors.rejectValue("day", "errors.invalid.monitor.day");
                            }
                        }
                    }
                }

            }
        }
    }

}
