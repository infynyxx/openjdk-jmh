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

import org.openjdk.jmh.link.BinaryLinkClient;
import org.openjdk.jmh.output.OutputFormatFactory;
import org.openjdk.jmh.runner.options.Options;

/**
 * Runner frontend class. Responsible for running micro benchmarks in forked JVM.
 *
 * @author sergey.kuksenko@oracle.com
 */
public class ForkedRunner extends BaseRunner {

    public ForkedRunner(Options options, BinaryLinkClient link) {
        super(options, OutputFormatFactory.createBinaryHook(link));
    }

    public void run(BenchmarkRecord benchmark) {
        // expect the tuple from the parent process
        if (options.isVerbose()) {
            out.println("Benchmarks: ");
            out.println(benchmark.getUsername());
        }
        out.startRun();
        runBenchmark(benchmark, true, true);
        out.endRun();
        out.flush();
        out.close();
    }

}
