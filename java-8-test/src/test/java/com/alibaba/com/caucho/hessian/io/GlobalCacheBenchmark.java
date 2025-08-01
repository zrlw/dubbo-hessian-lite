/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.base.SerializeTestBase;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup
@Fork(1)
public class GlobalCacheBenchmark extends SerializeTestBase {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GlobalCacheBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class GlobalCacheModeBenchmark extends GlobalCacheBenchmark {
        @Param({"true", "false"})
        public String globalCacheMode;

        private final List<String> chunkList = new ArrayList<>();

        private Random random;

        @Setup(Level.Iteration)
        public void setUp() {
            System.setProperty("com.caucho.hessian.io.globalCacheMode", globalCacheMode);

            int startCodePoint = 0x0000;
            int endCodePoint = 0x10FFFF;
            int chunkSize = 1000;
            for (int i = startCodePoint; i <= endCodePoint; i += chunkSize) {
                StringBuilder chunkBuilder = new StringBuilder();
                int chunkEnd = Math.min(i + chunkSize - 1, endCodePoint);

                for (int codePoint = i; codePoint <= chunkEnd; codePoint++) {
                    if (Character.isValidCodePoint(codePoint)) {
                        chunkBuilder.append(Character.toChars(codePoint));
                    }
                }
                chunkList.add(chunkBuilder.toString());
            }

            random = new Random();
        }

        @Benchmark
        public void testUnicode() throws Exception {
            int testIdx = random.nextInt(chunkList.size());
            baseHessian2Serialize(chunkList.get(testIdx));
        }
    }

}
