/**
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
package org.openjdk.jmh.samples;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

public class JMHSample_02_BenchmarkModes {

    /*
     * JMH generates lots of synthetic code for the microbenchmarks for
     * you during the compilation. JMH can measure the methods in lots
     * of modes, and it will generate all the needed code at once.
     * Users may select the default benchmark mode with the special
     * annotation, or select/override the mode via the command line.
     *
     * With this scenario, we start to measure something useful. Note
     * that we can conveniently have the exception at the benchmark method,
     * in order to reduce some of the clutter.
     *
     * P.S. It is helping at times to look into the generated code trying
     * to diagnose  the performance issue. You might see you don't measuring
     * it right! The generated code for this particular sample is somewhere at
     *  target/generated-sources/annotations/.../JMHSample_02_BenchmarkModes.java
     */

    /*
     * This benchmark type measures the raw throughput.
     * We are using the special annotation to select the units to measure in,
     * although you can use the default.
     */

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureThroughput() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * This benchmark type measures the average execution time.
     * Some might say it is the reciprocal throughput, and it really is.
     * There are workloads where measuring times is more convenient though.
     */

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAvgTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * This benchmark type samples the execution time.
     * With this benchmark, we are gathering the execution timings on their own,
     * which allows us to infer the distributions, percentiles, etc.
     *
     * At this point, JMH only calculates percentile estimates.
     *
     * JMH also tries to auto-adjust sampling frequency: if the method
     * is long enough, you will end up capturing all the samples.
     */
    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureSamples() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * This benchmark type measures the single method invocation time.
     * This mode is useful to do cold startup tests, when you specifically
     * do not want to call the benchmark method continuously.
     */

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureSingleShot() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * We can also ask for multiple benchmark modes at once. All the tests
     * above can be replaced with just a single test like this:
     */

    @GenerateMicroBenchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAll() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * HOW TO RUN THIS TEST:
     *
     * You can run this test with:
     *    $ mvn clean install
     *    $ java -jar target/microbenchmarks.jar ".*JMHSample_02.*" -i 5 -r 1s
     *    (we requested 5 iterations, 1 sec each)
     *
     * You are expected to see the different run modes for the same benchmark.
     * Note the units are different, scores are consistent with each other.
     */

}
