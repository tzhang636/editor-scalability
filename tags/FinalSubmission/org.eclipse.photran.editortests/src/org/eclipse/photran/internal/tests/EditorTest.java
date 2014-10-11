/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Maurice Lam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.ui.IEditorPart;

/**
 * 
 * @author Maurice Lam
 */
@SuppressWarnings("restriction")
public class EditorTest extends AbstractEditorTest
{
    
    @Override
    protected long doTestModel(boolean isConcurrent) throws CoreException, InterruptedException
    {
        long time = super.doTestModel(isConcurrent);
        assertTrue(hasTestModule(tu));
        return time;
    }
    
    /**
     * Test with the default, blocking FortranModelBuilder
     */
    public void testDefault() throws CoreException, InterruptedException
    {
        doTestDefault();
    }
    
    /**
     * Test with the concurrent model builder
     */
    public void testConcurrent() throws CoreException, InterruptedException
    {
        doTestConcurrent();
    }

    // Test that it parses correctly even if the type interrupts in the middle of parsing
    public void testInterrupt() throws CModelException
    {
        FortranPreferences.PREFERRED_MODEL_BUILDER.setValue("concurrentModelBuilder");
        type("!testing interrupt...\n");
        firePostSelectionChanged();
        
        quietSleep(10);
        
        type("!!INTERRUPT~\n");
        
        tu = (ITranslationUnit)CDTUITools
            .getEditorInputCElement(((IEditorPart)fEditor).getEditorInput());
        

        initTime(0);
        // poll for parsing completion
        for (int i = 0; i < 6000; i++)
        {
            if (tu.isStructureKnown())
            {
                assertTrue(hasTestModule(tu));
                time("Parse completion time", 0);
                return;
            }
            quietSleep(10);
        }
        fail("Timeout: took too long to parse. ");
        
    }

    // *********************** Helpers ************************//
    
    // copied from CDT BasicCEditorTest

    @Override
    protected IFile getTestFile() throws Exception
    {
        IFile file = importFile("workspace/thirtythousand.f90",
            readTestFile(Activator.getDefault(), "testfiles", "thirtythousand.f90"));
        return file;
    }

}
