/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.benchmarks;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Measure time taken to lex/parse files of various lengths.
 *
 * Adapted from {@link org.eclipse.photran.internal.tests.parser.ParseHugeFile}
 * 
 * @author kryczka2
 */
@RunWith(value = Parameterized.class)
public class LexParseBenchmarks
{
    private static final int MIN_NUM_LINES = 5000;
    private static final int MAX_NUM_LINES = 50000;
    private static final int INTVL_NUM_LINES = 5000;

    private final int numLines;
    private String fileStr = null;
    
    // Generate parameters specifying number of lines for test cases
    @Parameters
    public static Collection<Object[]> data()
    {
        List<Object[]> params = new ArrayList<Object[]>();
        for (int currLines = MIN_NUM_LINES; currLines <= MAX_NUM_LINES; currLines += INTVL_NUM_LINES) {
            params.add(new Object[] { currLines });
        }
        return params;
    }
    
    public LexParseBenchmarks(int numLines) {
        this.numLines = numLines;
    }

    @Before
    public void setUp()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("program p\n");
        for (int i = 0; i < this.numLines-2; i++)
            sb.append("    print *, 3+4*5\n");
        sb.append("end");
        fileStr = sb.toString();
    }

    @Test
    public void testLex() throws Exception
    {
        long start = System.currentTimeMillis();

        IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(new StringReader(
            this.fileStr), null, null);
        while (lexer.yylex().getTerminal() != Terminal.END_OF_INPUT) 
            ;

        System.out.println("Time (ms) taken to lex file with " + this.numLines + " lines: " + 
            (System.currentTimeMillis() - start));
    }

    @Test
    public void testLexAndParse() throws Exception
    {
        long start = System.currentTimeMillis();

        IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(new StringReader(
            this.fileStr), null, null);
        new Parser().parse(lexer);

        System.out.println("Time (ms) taken to lex and parse file with " + this.numLines + " lines: " + 
            (System.currentTimeMillis() - start));
    }
}