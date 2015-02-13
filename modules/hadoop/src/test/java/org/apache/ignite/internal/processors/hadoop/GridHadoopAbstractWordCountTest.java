/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.hadoop;

import com.google.common.base.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.ignite.igfs.*;
import org.apache.ignite.internal.processors.fs.*;

import java.io.*;
import java.util.*;

/**
 * Abstract class for tests based on WordCount test job.
 */
public abstract class GridHadoopAbstractWordCountTest extends GridHadoopAbstractSelfTest {
    /** Input path. */
    protected static final String PATH_INPUT = "/input";

    /** Output path. */
    protected static final String PATH_OUTPUT = "/output";

    /** GGFS instance. */
    protected IgfsEx ggfs;

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();

        Configuration cfg = new Configuration();

        setupFileSystems(cfg);

        // Init cache by correct LocalFileSystem implementation
        FileSystem.getLocal(cfg);
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        ggfs = (IgfsEx)startGrids(gridCount()).fileSystem(ggfsName);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids(true);
    }

    /** {@inheritDoc} */
    @Override protected boolean ggfsEnabled() {
        return true;
    }

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 1;
    }

    /**
     * Generates test file.
     *
     * @param path File name.
     * @param wordCounts Words and counts.
     * @throws Exception If failed.
     */
    protected void generateTestFile(String path, Object... wordCounts) throws Exception {
        List<String> wordsArr = new ArrayList<>();

        //Generating
        for (int i = 0; i < wordCounts.length; i += 2) {
            String word = (String) wordCounts[i];
            int cnt = (Integer) wordCounts[i + 1];

            while (cnt-- > 0)
                wordsArr.add(word);
        }

        //Shuffling
        for (int i = 0; i < wordsArr.size(); i++) {
            int j = (int)(Math.random() * wordsArr.size());

            Collections.swap(wordsArr, i, j);
        }

        //Input file preparing
        PrintWriter testInputFileWriter = new PrintWriter(ggfs.create(new IgfsPath(path), true));

        int j = 0;

        while (j < wordsArr.size()) {
            int i = 5 + (int)(Math.random() * 5);

            List<String> subList = wordsArr.subList(j, Math.min(j + i, wordsArr.size()));
            j += i;

            testInputFileWriter.println(Joiner.on(' ').join(subList));
        }

        testInputFileWriter.close();
    }

    /**
     * Reads whole text file into String.
     *
     * @param fileName Name of the file to read.
     * @return Content of the file as String value.
     * @throws Exception If could not read the file.
     */
    protected String readAndSortFile(String fileName) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ggfs.open(new IgfsPath(fileName))));

        List<String> list = new ArrayList<>();

        String line;

        while ((line = reader.readLine()) != null)
            list.add(line);

        Collections.sort(list);

        return Joiner.on('\n').join(list) + "\n";
    }
}
