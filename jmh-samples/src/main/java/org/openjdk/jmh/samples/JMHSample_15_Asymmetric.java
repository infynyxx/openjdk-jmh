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
package org.openjdk.jmh.samples;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Group)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_15_Asymmetric {

    /*
     * So far all the tests were symmetric: the same code was executed in all the threads.
     * At times, you need the asymmetric test. JMH provides this with the notion of @Group,
     * which can bind several methods together, and all the threads are distributed among
     * the test methods.
     *
     * Each execution group consists of two or more threads. If more threads are requested
     * to be run via the command line option, then more execution groups are created.
     * The meaning of @Threads slightly changes with this mode: now, it marks how many threads
     * are executing the method in the single execution group.
     *
     * Note that two state scopes: Scope.Benchmark and Scope.Thread are not covering all
     * the use cases here -- you either share everything in the state, or share nothing.
     * To break this, we have the middle ground Scope.Group, which marks the state to be
     * shared within the execution group, but not among the groups.
     *
     * Putting this all together, if we want to run this with 8 threads, this example means:
     *  a) make two execution groups, each having 4 threads
     *  b) each execution group has one benchmark instance to share (= share the $counter)
     *  c) each execution group has 3 thread executing inc().
     *  d) each execution group has 1 thread executing get().
     */

    private AtomicInteger counter;

    @Setup
    public void up() {
        counter = new AtomicInteger();
    }

    @GenerateMicroBenchmark
    @Group("g")
    @Threads(3)
    public int inc() {
        return counter.incrementAndGet();
    }

    @GenerateMicroBenchmark
    @Group("g")
    public int get() {
        return counter.get();
    }

    /*
     * HOW TO RUN THIS TEST:
     *
     * You can run this test with:
     *    $ mvn clean install
     *    $ java -jar target/microbenchmarks.jar ".*JMHSample_15.*" -i 5 -r 1s
     *    (we requested 5 iterations, 1 sec each)
     *
     * You will have the distinct metrics for inc() and get() from this run.
     */

}
