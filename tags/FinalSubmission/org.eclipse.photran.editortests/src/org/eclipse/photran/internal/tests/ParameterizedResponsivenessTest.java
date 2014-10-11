/*******************************************************************************
 * Copyright (c) 2012 TODO COMPANY NAME and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    andrew (TODO COMPANY NAME) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;

/**
 * Run editor responsiveness tests on files of all sizes in the range specified 
 * by the constants and using both types of model builder.
 * 
 * @author Andrew Kryczka
 */
public class ParameterizedResponsivenessTest extends TestCase {
    
    private static final int NUM_TRIALS = 5;
    
    private static final int MIN_NUM_LINES = 10000;
    private static final int MAX_NUM_LINES = 90000;
    private static final int INTVL_NUM_LINES = 10000;
    
    private static final String OUTPUT_FILE = "results.dat";
    private static final String RESULTS_DIR = "graph";
    
    private BufferedWriter bufFileWriter;
    
    /**
     * Setup configures file to which results (average response times) will be printed.
     * 
     * @throws IOException if results file couldn't be created
     */
    @Override
    public void setUp() throws IOException {
        File f = new File(RESULTS_DIR, OUTPUT_FILE);
        bufFileWriter = new BufferedWriter(new FileWriter(f, false));
        bufFileWriter.write("Lines\tDefault time\tConcurrent time\n");
    }
    
    /**
     * Test all file sizes using both model builders and write average response times to a file.
     * 
     * @throws Exception if a test fails
     */
    public void testAll() throws Exception {
        
        for (int fileSize = MIN_NUM_LINES; fileSize <= MAX_NUM_LINES; fileSize += INTVL_NUM_LINES) {
            
            long totalDefaultTime = 0;
            long totalConcurrentTime = 0;
            
            AbstractEditorTest test = generateTest(fileSize, false);
            test.setUp();
            
            for (int trial = 0; trial < NUM_TRIALS; ++trial) {
                
                System.out.println("Testing file with " + fileSize + " lines.");
                
                totalDefaultTime += test.doTestDefault();
                totalConcurrentTime += test.doTestConcurrent();
            }
            
            // Report file size in terms of thousands of lines
            bufFileWriter.append("" + (fileSize/1000) + "\t" + (totalDefaultTime/NUM_TRIALS) + "\t" + (totalConcurrentTime/NUM_TRIALS) + "\n");
            bufFileWriter.flush();
        }
    }
    
    
    /**
     * Helper method to generate an AbstractEditorTest test on a file with
     * the specified number of lines.
     *  
     * @param numLines number of lines in test file
     * @param isConcurrent whether concurrent or default model builder should be used
     * @return the object to run the tests
     */
    private AbstractEditorTest generateTest(int numLines, boolean isConcurrent) {

        final String filename = "testfile-" + numLines + "-lines.f90";
        
        final StringBuilder fileContents = new StringBuilder();
        fileContents.append("\nprogram p\n");
        for (int i = 0; i < numLines-2; i++)
            fileContents.append("    print *, 'Hello World'\n");
        fileContents.append("end");
        
        AbstractEditorTest res = new AbstractEditorTest() {
            
            @SuppressWarnings("restriction")
            @Override
            protected IFile getTestFile() throws Exception
            {
                return importFile(filename, fileContents.toString());
            }
        };
        return res;
    }
}