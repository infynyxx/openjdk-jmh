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
package org.openjdk.jmh.output.format;

import org.openjdk.jmh.logic.results.IterationData;
import org.openjdk.jmh.logic.results.internal.RunResult;
import org.openjdk.jmh.profile.ProfilerResult;
import org.openjdk.jmh.runner.BenchmarkRecord;
import org.openjdk.jmh.runner.parameters.BenchmarkParams;
import org.openjdk.jmh.runner.parameters.IterationParams;

import java.io.IOException;
import java.util.Collection;

/**
 * Internal interface for OutputFormat.
 * <p/>
 * TODO: This interface might need touchups for formats that require symmetric headers or recursion (XML etc).
 *
 * @author anders.astrand@oracle.com
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface OutputFormat {

    /**
     * Format for iteration start.
     *
     * @param benchmark benchmark name
     * @param params
     * @param iteration iteration-number
     */
    public void iteration(BenchmarkRecord benchmark, IterationParams params, int iteration, IterationType type);

    /**
     * Format for end-of-iteration.
     *
     * @param name      name of benchmark
     * @param params
     * @param iteration iteration-number
     * @param data    result of iteration
     * @param profiles  profiler results
     */
    public void iterationResult(BenchmarkRecord name, IterationParams params, int iteration, IterationType type, IterationData data, Collection<ProfilerResult> profiles);

    /**
     * Format for start-of-benchmark output.
     *
     * @param verbose Should we output verbose info?
     */
    public void startBenchmark(BenchmarkRecord name, BenchmarkParams mbParams, boolean verbose);

    /**
     * Format for end-of-benchmark.
     *
     * @param name       benchmark name
     * @param result statistics of the run
     */
    public void endBenchmark(BenchmarkRecord name, RunResult result);

    /**
     * Format for start-of-benchmark output.
     */
    public void startRun();

    /**
     * Format for end-of-benchmark.
     */
    public void endRun();

    /**
     * Format for detailed results output.
     *
     * @param name      benchmark name
     * @param params
     * @param iteration iteration number
     * @param data   AggregatedResults with detailed run results
     */
    public void detailedResults(BenchmarkRecord name, IterationParams params, int iteration, IterationData data);

    /* ------------- SPECIAL TRACING METHODS -------------------- */

    void exception(Throwable ex);

    /* ------------- RAW OUTPUT METHODS ------------------- */

    void println(String s);

    void flush();

    void close();

    void verbosePrintln(String s);

    void write(int b);

    void write(byte[] b) throws IOException;

}
