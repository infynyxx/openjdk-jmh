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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.options.handlers.ForkOptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that handles all the options and arguments specific to the harness JVM.
 *
 * @author anders.astrand@oracle.com
 * @author sergey.kuksenko@oracle.com
 */
public class HarnessOptions extends BaseOptions {


    // test selection options
    @Argument(metaVar = "REGEXP", usage = "Microbenchmarks to run. Regexp filtering out classes or methods which are MicroBenchmarks.")
    protected List<String> regexps = new ArrayList<String>();

    // micro options
    // the following list of options are not forwarding into forked JVM.
    // "forwarding option should be declared in BaseOptions class" (c) capt. O.

    @Option(name = "-f", aliases = {"--fork"}, metaVar = "{ INT }", usage = "Start each benchmark in new JVM, forking from the same JDK unless --jvm is set. Optional parameter specifies number of times harness should fork. Zero forks means \"no fork\", also \"false\" is accepted", handler = ForkOptionHandler.class)
    protected int fork = -1;

    @Option(name = "-wf", aliases = {"--warmupfork"}, metaVar = "{ INT }", usage = "Number of warmup fork executions. (warmup fork execution results are ignored).")
    protected int warmupFork = -1;

    @Option(name = "-o", aliases = {"--output"}, metaVar = "FILE", usage = "Redirect output to FILE")
    protected String output = null;

    @Option(name = "-of", aliases = {"--outputformat"}, metaVar = "FORMAT", usage = "Format to use for output, use --listformats to list available formats")
    protected OutputFormatType outputFormat = OutputFormatType.TextReport;

    @Option(name = "--jvm", metaVar = "JVM", usage = "Custom JVM to use with fork.")
    protected String jvm = null;

    @Option(name = "--jvmargs", metaVar = "JVMARGS", usage = "Custom JVM arguments for --jvm, default is to use parent process's arguments")
    protected String jvmArgs = null;

    @Option(name = "--jvmclasspath", metaVar = "CLASSPATH", usage = "Custom classpath for --jvm, default is to use parent process's classpath")
    protected String jvmClassPath = null;

    @Option(name = "-e", aliases = {"--exclude"}, multiValued = true, metaVar = "REGEXP", usage = "Microbenchmarks to exclude. Regexp filtering out classes or methods which are MicroBenchmarks.")
    protected List<String> excludes = new ArrayList<String>();

    @Option(name = "-wm", aliases = {"--warmupmode"}, usage = "Warmup mode for warming up selected micro benchmarks. Warmup modes are BeforeAny (measurements) or BeforeEach (measurement) (original mode)")
    protected WarmupMode warmupMode = WarmupMode.defaultMode();

    @Option(name = "-wmb", aliases = {"--warmupmicrobenchmarks"}, multiValued = true, metaVar = "REGEXP", usage = "Microbenchmarks to run for warmup before running any other benchmarks. These micros may be different from the target micros to warm up the harness or other parts of the JVM prior to running the target micro benchmarks. Regexp filtering out classes or methods which are MicroBenchmarks.")
    protected List<String> warmupMicros = new ArrayList<String>();

    // show something options
    @Option(name = "-l", aliases = {"--list"}, usage = "List available microbenchmarks and exit. Filter using available regexps.")
    protected boolean list = false;

    @Option(name = "--listformats", usage = "List available output formats")
    protected boolean listFormats = false;

    @Option(name = "-h", aliases = {"--help"}, usage = "Display help")
    protected boolean help = false;

    @Option(name = "--listprofilers", usage = "List available profilers")
    protected boolean listProfilers = false;

    /**
     * Kawaguchi's parser
     */
    private CmdLineParser parser;

    public static HarnessOptions newInstance() {
        HarnessOptions opts = new HarnessOptions();
        opts.parser = new CmdLineParser(opts);
        return opts;
    }

    protected HarnessOptions() {
    }

    /**
     * Print Usage
     *
     * @param message Message to print at the top
     */
    public void printUsage(String message) {
        System.err.println(message);
        System.err.println("Usage: [options] [benchmark regexp]*");
        parser.printUsage(System.err);
    }

    /**
     * parse arguments and set fields in the Options instance
     *
     * @throws CmdLineException
     */
    public void parseArguments(String[] argv) throws CmdLineException {
        parser.parseArgument(argv);
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public WarmupMode getWarmupMode() {
        return warmupMode;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public List<String> getRegexps() {
        if (!regexps.isEmpty()) {
            return regexps;
        } else {
            // assume user requested all benchmarks
            return Collections.singletonList(".*");
        }
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public List<String> getWarmupMicros() {
        if (warmupMicros == null) {
            return Collections.emptyList();
        } else {
            return warmupMicros;
        }
    }

    /**
     * Getter
     *
     * @return the value
     */
    public boolean shouldList() {
        return list;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public String getJvm() {
        return jvm;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public String getJvmArgs() {
        return jvmArgs;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public String getJvmClassPath() {
        return jvmClassPath;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public int getForkCount() {
        return fork;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public int getWarmupForkCount() {
        return warmupFork;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public String getOutput() {
        return output;
    }

    /**
     * Getter
     *
     * @return the value
     */
    @Override
    public OutputFormatType getOutputFormat() {
        return outputFormat;
    }

    /**
     * Getter
     *
     * @return the value
     */
    public boolean shouldListFormats() {
        return listFormats;
    }

    /**
     * Getter
     *
     * @return the value
     */
    public boolean shouldHelp() {
        return help;
    }


    /**
     * Getter
     *
     * @return the value
     */
    public boolean shouldListProfilers() {
        return listProfilers;
    }

    @Override
    public String getHostName() {
        throw new UnsupportedOperationException("Asking for forked VM options");
    }

    @Override
    public int getHostPort() {
        throw new UnsupportedOperationException("Asking for forked VM options");
    }

    @Override
    public String getBenchmark() {
        throw new UnsupportedOperationException("Asking for forked VM options");
    }

}
