/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.runner.options;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.profile.ProfilerType;
import org.openjdk.jmh.runner.parameters.TimeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OptionsBuilder implements Options, ChainedOptionsBuilder {

    private boolean finalized;

    private void checkFinalized() {
        if (finalized) {
            throw new IllegalStateException("The builder is already finalized");
        }
    }

    @Override
    public Options build() {
        finalized = true;
        return this;
    }

    // ---------------------------------------------------------------------------

    private final List<String> regexps = new ArrayList<String>();

    @Override
    public ChainedOptionsBuilder include(String regexp) {
        checkFinalized();
        regexps.add(regexp);
        return this;
    }

    @Override
    public List<String> getRegexps() {
        return regexps;
    }

    // ---------------------------------------------------------------------------

    private final List<String> excludes = new ArrayList<String>();

    @Override
    public ChainedOptionsBuilder exclude(String regexp) {
        excludes.add(regexp);
        return this;
    }

    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    // ---------------------------------------------------------------------------

    private OutputFormatType ofType = OutputFormatType.defaultType();

    @Override
    public ChainedOptionsBuilder outputFormat(OutputFormatType type) {
        ofType = type;
        return this;
    }

    @Override
    public OutputFormatType getOutputFormat() {
        return ofType;
    }

    // ---------------------------------------------------------------------------

    private String output;

    @Override
    public ChainedOptionsBuilder output(String filename) {
        this.output = filename;
        return this;
    }

    @Override
    public String getOutput() {
        return output;
    }

    // ---------------------------------------------------------------------------

    private boolean shouldDoGC;

    @Override
    public ChainedOptionsBuilder shouldDoGC(boolean value) {
        shouldDoGC = value;
        return this;
    }

    @Override
    public boolean shouldDoGC() {
        return shouldDoGC;
    }

    // ---------------------------------------------------------------------------

    private EnumSet<ProfilerType> profilers = EnumSet.noneOf(ProfilerType.class);

    @Override
    public ChainedOptionsBuilder addProfiler(ProfilerType prof) {
        this.profilers.add(prof);
        return this;
    }

    @Override
    public Set<ProfilerType> getProfilers() {
        return profilers;
    }

    // ---------------------------------------------------------------------------

    private boolean isVerbose;

    @Override
    public ChainedOptionsBuilder verbose(boolean value) {
        isVerbose = value;
        return this;
    }

    @Override
    public boolean isVerbose() {
        return isVerbose;
    }

    // ---------------------------------------------------------------------------

    private boolean shouldFailOnError;

    @Override
    public ChainedOptionsBuilder failOnError(boolean value) {
        shouldFailOnError = value;
        return this;
    }

    @Override
    public boolean shouldFailOnError() {
        return shouldFailOnError;
    }

    // ---------------------------------------------------------------------------

    private boolean shouldOutputDetails;

    @Override
    public ChainedOptionsBuilder outputDetails(boolean value) {
        shouldOutputDetails = value;
        return this;
    }

    @Override
    public boolean shouldOutputDetailedResults() {
        return shouldOutputDetails;
    }

    // ---------------------------------------------------------------------------

    private int threads = -1;

    @Override
    public ChainedOptionsBuilder threads(int count) {
        this.threads = count;
        return this;
    }

    @Override
    public int getThreads() {
        return threads;
    }

    // ---------------------------------------------------------------------------

    private Boolean syncIterations;

    @Override
    public ChainedOptionsBuilder syncIterations(boolean value) {
        this.syncIterations = value;
        return this;
    }

    @Override
    public Boolean getSynchIterations() {
        return syncIterations;
    }

    // ---------------------------------------------------------------------------

    private int warmupIterations = -1;

    @Override
    public ChainedOptionsBuilder warmupIterations(int value) {
        this.warmupIterations = value;
        return this;
    }

    @Override
    public int getWarmupIterations() {
        return warmupIterations;
    }

    // ---------------------------------------------------------------------------

    private TimeValue warmupTime;

    @Override
    public ChainedOptionsBuilder warmupTime(TimeValue value) {
        this.warmupTime = value;
        return this;
    }

    @Override
    public TimeValue getWarmupTime() {
        return warmupTime;
    }

    // ---------------------------------------------------------------------------

    private WarmupMode warmupMode = WarmupMode.defaultMode();

    @Override
    public ChainedOptionsBuilder warmupMode(WarmupMode mode) {
        this.warmupMode = mode;
        return this;
    }

    @Override
    public WarmupMode getWarmupMode() {
        return warmupMode;
    }

    // ---------------------------------------------------------------------------

    private final List<String> warmupMicros = new ArrayList<String>();

    @Override
    public ChainedOptionsBuilder includeWarmup(String regexp) {
        warmupMicros.add(regexp);
        return this;
    }

    @Override
    public List<String> getWarmupMicros() {
        return warmupMicros;
    }

    // ---------------------------------------------------------------------------

    private int iterations = -1;

    @Override
    public ChainedOptionsBuilder measurementIterations(int count) {
        this.iterations = count;
        return this;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    // ---------------------------------------------------------------------------

    private TimeValue measurementTime;

    @Override
    public ChainedOptionsBuilder measurementTime(TimeValue value) {
        this.measurementTime = value;
        return this;
    }

    @Override
    public TimeValue getRuntime() {
        return measurementTime;
    }

    // ---------------------------------------------------------------------------

    private EnumSet<Mode> benchModes;

    @Override
    public ChainedOptionsBuilder mode(Mode mode) {
        if (benchModes == null) {
            benchModes = EnumSet.noneOf(Mode.class);
        }
        benchModes.add(mode);
        return this;
    }

    @Override
    public Collection<Mode> getBenchModes() {
        return benchModes;
    }

    // ---------------------------------------------------------------------------

    private TimeUnit timeUnit;

    @Override
    public ChainedOptionsBuilder timeUnit(TimeUnit tu) {
        this.timeUnit = tu;
        return this;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    // ---------------------------------------------------------------------------

    private int forks = -1;

    @Override
    public ChainedOptionsBuilder forks(int value) {
        this.forks = value;
        return this;
    }

    @Override
    public int getForkCount() {
        return forks;
    }

    // ---------------------------------------------------------------------------

    private int warmupForks = -1;

    @Override
    public ChainedOptionsBuilder warmupForks(int value) {
        this.warmupForks = value;
        return this;
    }

    @Override
    public int getWarmupForkCount() {
        return warmupForks;
    }

    // ---------------------------------------------------------------------------

    private String jvmClassPath;

    @Override
    public ChainedOptionsBuilder jvmClasspath(String value) {
        this.jvmClassPath = value;
        return this;
    }

    @Override
    public String getJvmClassPath() {
        return jvmClassPath;
    }

    // ---------------------------------------------------------------------------

    private String jvmBinary;

    @Override
    public ChainedOptionsBuilder jvm(String path) {
        this.jvmBinary = path;
        return this;
    }

    @Override
    public String getJvm() {
        return jvmBinary;
    }

    // ---------------------------------------------------------------------------

    private String jvmArgs;

    @Override
    public ChainedOptionsBuilder jvmArgs(String value) {
        this.jvmArgs = value;
        return this;
    }

    @Override
    public String getJvmArgs() {
        return jvmArgs;
    }

}
