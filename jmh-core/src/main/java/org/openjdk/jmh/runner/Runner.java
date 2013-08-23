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
package org.openjdk.jmh.runner;

import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.logic.results.internal.RunResult;
import org.openjdk.jmh.output.OutputFormatFactory;
import org.openjdk.jmh.output.format.OutputFormat;
import org.openjdk.jmh.output.format.internal.BinaryOutputFormatReader;
import org.openjdk.jmh.runner.options.HarnessOptions;
import org.openjdk.jmh.runner.parameters.Defaults;
import org.openjdk.jmh.util.AnnotationUtils;
import org.openjdk.jmh.util.InputStreamDrainer;
import org.openjdk.jmh.util.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runner frontend class. Responsible for running micro benchmarks in this JVM.
 *
 * @author staffan.friberg@oracle.com,
 * @author anders.astrand@oracle.com
 * @author sergey.kuksenko@oracle.com
 */
public class Runner extends BaseRunner {

    /** Class holding all our runtime options/arguments */
    private final HarnessOptions options;

    private final MicroBenchmarkList list;

    public Runner(HarnessOptions options) {
        super(options, createOutputFormat(options));
        this.list = MicroBenchmarkList.defaultList();
        this.options = options;
    }

    /** Setup helper method, creates OutputFormat according to argv options. */
    private static OutputFormat createOutputFormat(HarnessOptions options) {
        PrintStream out;
        // setup OutputFormat singleton
        if (options.getOutput() == null) {
            out = System.out;
        } else {
            try {
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(options.getOutput()))));
                System.setOut(out); // override to print everything to file
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException(ex);
            }
        }

        return OutputFormatFactory.createFormatInstance(out, options.getOutputFormat(), options.isVerbose());
    }

    /** Main entry point */
    public void run() throws RunnerException {
        Set<BenchmarkRecord> benchmarks = list.find(out, options.getRegexps(), options.getExcludes());

        if (benchmarks.isEmpty()) {
            out.println("No matching benchmarks. Miss-spelled regexp? Use -v for verbose output.");
            out.flush();
            out.close();
            return;
        }

        if (options.shouldList() || options.isVerbose()) {
            out.println("Benchmarks: ");

            // list microbenchmarks if -l and/or -v
            for (BenchmarkRecord benchmark : benchmarks) {
                out.println(benchmark.getUsername());
            }
        }

        // override the benchmark types;
        // this may yield new benchmark records
        if (options.getBenchModes() != null) {
            List<BenchmarkRecord> newBenchmarks = new ArrayList<BenchmarkRecord>();
            for (BenchmarkRecord br : benchmarks) {
                for (Mode m : options.getBenchModes()) {
                    newBenchmarks.add(br.cloneWith(m));
                }

            }

            benchmarks.clear();
            benchmarks.addAll(newBenchmarks);
        }

        // clone with all the modes
        List<BenchmarkRecord> newBenchmarks = new ArrayList<BenchmarkRecord>();
        for (BenchmarkRecord br : benchmarks) {
            if (br.getMode() == Mode.All) {
                for (Mode mode : Mode.values()) {
                    if (mode == Mode.All) continue;
                    newBenchmarks.add(br.cloneWith(mode));
                }
            } else {
                newBenchmarks.add(br);
            }
        }

        benchmarks.clear();
        benchmarks.addAll(newBenchmarks);

        // exit if list only, else run benchmarks
        if (!options.shouldList()) {
            if ((!options.getWarmupMicros().isEmpty()) ||
                    (options.getWarmupMode() == HarnessOptions.WarmupMode.BEFOREANY)) {
                runBulkWarmupBenchmarks(benchmarks);
            } else {
                runBenchmarks(benchmarks);
            }
        }

        out.flush();
        out.close();
    }

    /**
     * Run specified warmup microbenchmarks prior to running any requested mircobenchmarks.
     * TODO: Currently valid only for non-external JVM runs
     */
    private void runBulkWarmupBenchmarks(Set<BenchmarkRecord> benchmarks) {
        out.startRun();

        // list of micros executed before iteration
        Set<BenchmarkRecord> warmupMicros = new TreeSet<BenchmarkRecord>();

        List<String> warmupMicrosRegexp = options.getWarmupMicros();
        if (warmupMicrosRegexp != null && !warmupMicrosRegexp.isEmpty()) {
            warmupMicros.addAll(list.find(out, warmupMicrosRegexp, Collections.<String>emptyList()));
        }
        if (options.getWarmupMode() == HarnessOptions.WarmupMode.BEFOREANY) {
            warmupMicros.addAll(benchmarks);
        }

        if (!warmupMicros.isEmpty()) {
            // run warmup iterations of the requested benchmarks before running
            // any measured iterations of any of the requested benchmarks. This
            // has the effect of getting all the classes loaded getting the JITed
            // code the the final state, possibly invalidating optimizations that
            // might not be invalided until later and quite possibly invalidated
            // during measurement iteration causing a performance shift or simply
            // increased variance.
            // currently valid only for non-external JVM runs

            for (BenchmarkRecord benchmark : warmupMicros) {
                runBenchmark(benchmark, true, false);
            }
        }
        // run microbenchmarks
        //
        for (BenchmarkRecord benchmark : benchmarks) {
            runBenchmark(benchmark, false, true);
        }
        out.endRun();
    }

    private int decideForks(int optionForks, int benchForks) {
        if (optionForks == -1) {
            if (benchForks == -1) {
                return Defaults.DEFAULT_FORK_TIMES;
            } else {
                return benchForks;
            }
        } else {
            return optionForks;
        }
    }

    private int decideWarmupForks(int optionWarmupForks, Fork forkAnnotation) {
        if (optionWarmupForks == -1) {
            return (forkAnnotation != null) ? forkAnnotation.warmups() : 0;
        } else {
            return optionWarmupForks;
        }
    }

    private void runBenchmarks(Set<BenchmarkRecord> benchmarks) {
        Set<BenchmarkRecord> embedded = new TreeSet<BenchmarkRecord>();
        Set<BenchmarkRecord> forked = new TreeSet<BenchmarkRecord>();

        out.startRun();
        for (BenchmarkRecord benchmark : benchmarks) {
            int f = decideForks(options.getForkCount(), benchForks(benchmark));
            if (f > 0) {
                forked.add(benchmark);
            } else {
                embedded.add(benchmark);
            }
        }

        for (BenchmarkRecord benchmark : embedded) {
            runBenchmark(benchmark, true, true);
        }

        runSeparate(forked);
        out.endRun();
    }

    private void runSeparate(Set<BenchmarkRecord> benchmarksToFork) {
        BinaryOutputFormatReader reader = null;
        try {
            reader = new BinaryOutputFormatReader(out);
            for (BenchmarkRecord benchmark : benchmarksToFork) {
                runSeparateMicroBenchmark(reader, benchmark, reader.getHost(), reader.getPort());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                reader.terminate();
            }
        }
    }

    /**
     * Tests if the benchmark has the fork annotation
     */
    private int benchForks(BenchmarkRecord benchmark) {
        Method m = MicroBenchmarkHandlers.findBenchmarkMethod(benchmark);
        Fork fork = m.getAnnotation(Fork.class);
        return (fork != null) ? fork.value() : -1;
    }

    /**
     * Run the micro benchmark in a separate JVM process
     *
     * @param benchmark micro to run
     * @param host host VM host
     * @param port host VM port
     */
    private void runSeparateMicroBenchmark(BinaryOutputFormatReader reader, BenchmarkRecord benchmark, String host, int port) {

        // Running microbenchmark in separate JVM requires to read some options from annotations.

        final Method benchmarkMethod = MicroBenchmarkHandlers.findBenchmarkMethod(benchmark);
        Fork forkAnnotation = benchmarkMethod.getAnnotation(Fork.class);

        String annJvmArgs = null;
        if (forkAnnotation != null && AnnotationUtils.isSet(forkAnnotation.jvmArgs())) {
            annJvmArgs = forkAnnotation.jvmArgs().trim();
        }

        String annJvmArgsAppend = null;
        if (forkAnnotation != null && AnnotationUtils.isSet(forkAnnotation.jvmArgsAppend())) {
            annJvmArgsAppend = forkAnnotation.jvmArgsAppend().trim();
        }

        String annJvmArgsPrepend = null;
        if (forkAnnotation != null && AnnotationUtils.isSet(forkAnnotation.jvmArgsPrepend())) {
            annJvmArgsPrepend = forkAnnotation.jvmArgsPrepend().trim();
        }

        String[] commandString = options.getSeparateExecutionCommand(benchmark, annJvmArgs, annJvmArgsPrepend, annJvmArgsAppend, host, port);

        int forkCount = decideForks(options.getForkCount(), benchForks(benchmark));
        int warmupForkCount = decideWarmupForks(options.getWarmupForkCount(), forkAnnotation);
        if (warmupForkCount > 0) {
            String[] warmupForkCheat = Utils.concat(commandString, new String[]{"-wi", "1", "-i", "0"});
            out.verbosePrintln("Warmup forking " + warmupForkCount + " times using command: " + Arrays.toString(warmupForkCheat));
            for (int i = 0; i < warmupForkCount; i++) {
                doFork(reader, warmupForkCheat);
            }
        }

        // TODO: should we report fork number somehow?
        out.verbosePrintln("Forking " + forkCount + " times using command: " + Arrays.toString(commandString));
        for (int i = 0; i < forkCount; i++) {
            doFork(reader, commandString);
        }
    }

    private void doFork(BinaryOutputFormatReader reader, String[] commandString) {
        try {
            Process p = Runtime.getRuntime().exec(commandString);

            // drain streams, else we might lock up
            InputStreamDrainer errDrainer = new InputStreamDrainer(p.getErrorStream(), System.err);
            InputStreamDrainer outDrainer = new InputStreamDrainer(p.getInputStream(), System.out);

            errDrainer.start();
            outDrainer.start();

            int ecode = p.waitFor();

            errDrainer.join();
            outDrainer.join();

            // need to wait for all pending messages to be processed
            // before starting the next benchmark
            reader.waitFinish();

            if (ecode != 0) {
                out.println("WARNING: Forked process returned code: " + ecode);
                if (options.shouldFailOnError()) {
                    throw new IllegalStateException("WARNING: Forked process returned code: " + ecode);
                }
            }

        } catch (IOException ex) {
            out.exception(ex);
        } catch (InterruptedException ex) {
            out.exception(ex);
        }
    }

}
