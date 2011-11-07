/*
 * The MIT License
 *
 * Copyright (C) 2011 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins_ci.plugins.run_condition.example;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.FormValidation;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.text.DateFormat;
import java.util.Calendar;

public class ExampleCondition extends RunCondition {

    final int buildNumberMultiple;
    final boolean onlyInOfficeHours;

    @DataBoundConstructor
    public ExampleCondition(final int buildNumberMultiple, final boolean onlyInOfficeHours) {
        this.buildNumberMultiple = buildNumberMultiple;
        this.onlyInOfficeHours = onlyInOfficeHours;
    }

    public int getBuildNumberMultiple() {
        return buildNumberMultiple;
    }

    public boolean isOnlyInOfficeHours() {
        return onlyInOfficeHours;
    }

    @Override
    public boolean runPrebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return true;
    }

    @Override
    public boolean runPerform(final AbstractBuild<?, ?> build, final BuildListener listener) {
        if ((build.getNumber() % buildNumberMultiple) != 0) return false;
        if (onlyInOfficeHours && !isInOfficeHoursNow(listener)) return false;
        return true;
    }

    private boolean isInOfficeHoursNow(final BuildListener listener) {
        final Calendar now = Calendar.getInstance();
        final Calendar startTime = (Calendar) now.clone();
        final Calendar endTime = (Calendar) now.clone();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 59);
        startTime.set(Calendar.SECOND, 59);
        endTime.set(Calendar.HOUR_OF_DAY, 17);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.SECOND, 0);
        final DateFormat df = DateFormat.getDateTimeInstance();
        listener.getLogger().println(Messages.isOfficeHours_console(format(df, startTime), format(df, now), format(df, endTime)));
        return startTime.before(now) && now.before(endTime);
    }

    private String format(final DateFormat dateFormat, final Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }

    @Extension
    public static class ExampleConditionDescriptor extends RunConditionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.exampleCondition_displayName();
        }

        public FormValidation doCheckBuildNumberMultiple(@QueryParameter final String value) {
            return FormValidation.validatePositiveInteger(value);
        }

    }

}
