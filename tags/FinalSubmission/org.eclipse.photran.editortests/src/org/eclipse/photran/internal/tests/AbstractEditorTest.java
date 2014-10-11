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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.photran.internal.core.model.FortranElement.Module;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.photran.internal.ui.editor.FortranSourceViewer;
import org.eclipse.photran.internal.ui.editor.FortranStmtPartitionScanner;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Provides test methods for measuring the blocking time upon adding new characters into a file
 * provided by the subclass. Supports both default and concurrent model builders.
 * 
 * @author Maurice Lam
 * @author of the bad parts - Andrew Kryczka
 */
@SuppressWarnings("restriction")
public abstract class AbstractEditorTest extends BaseTestFramework
{
    protected FortranEditor fEditor;

    protected ITranslationUnit tu;

    private StyledText textWidget;
    
    private static boolean isSetup = false;

    /**
     * Setup test fixture---if not the first test, then close currently open files and
     * open the test file in a new editor
     */
    @Override
    protected void setUp() throws Exception
    {
        if (!isSetup) {
            super.setUp();
            FortranStmtPartitionScanner.setDebug(true);
            isSetup = true;
        }
        
        getActivePage().closeAllEditors(false);
        IEditorPart ideEditor = IDE.openEditor(getActivePage(), getTestFile());
        assertTrue(ideEditor instanceof FortranEditor);
        fEditor = (FortranEditor)ideEditor;
        textWidget = fEditor.getSourceViewerx().getTextWidget();
        tu = (ITranslationUnit)CDTUITools.getEditorInputCElement(((IEditorPart)fEditor)
            .getEditorInput());
    }

    /**
     * Subclasses override this method to provide the file on which the measurements will be run
     * 
     * @return Fortran source file to use in tests
     * @throws Exception if the file creation failed
     */
    abstract protected IFile getTestFile() throws Exception;

    /**
     * Test to measure, print, and return typing + blocking time using default (i.e., sequential) model builder
     */
    public long doTestDefault() throws CoreException, InterruptedException
    {
        FortranPreferences.PREFERRED_MODEL_BUILDER.setValue("defaultModelBilder");
        System.out.println("----------------- default test --------------");
        return doTestModel(false);
    }

    /**
     * Test to measure, print, and return typing + blocking time using concurrent model builder
     */
    public long doTestConcurrent() throws CoreException, InterruptedException
    {
        FortranPreferences.PREFERRED_MODEL_BUILDER.setValue("concurrentModelBuilder");
        System.out.println("----------------- Concurrent test --------------");
        return doTestModel(true);
    }

    /**
     * Helper method to actually run the measurements
     */
    protected long doTestModel(boolean isConcurrent) throws CoreException, InterruptedException
    {
        initTime(0);
        type("! some prefixing comment\n");

        time("Type time", 0);
        
        firePostSelectionChanged();

        long res = time("Typing + Block time", 0);

        if (isConcurrent)
        {
            assertFalse(tu.isStructureKnown());

            // poll for parsing completion
            // the magic number is here just to avoid infinite loop
            for (int i = 0; i < 6000; i++)
            {
                if (tu.isStructureKnown())
                {
                    time("Parse completion time", 0);
                    return res;
                }
                quietSleep(10);
            }
            fail("Timeout: took too long to parse. ");

        }
        else
        {
            assertTrue(tu.isStructureKnown());
        }
        
        return res;
    }

    // *********************** Helpers ************************//

    // look for the test module in the parsed translation unit
    protected boolean hasTestModule(ITranslationUnit tu) throws CModelException
    {
        ICElement[] children = tu.getChildren();
        for (ICElement elem : children)
        {
            if (elem instanceof Module)
            {
                if (elem.getElementName().equals("Test_Module")) { return true; }
            }
        }
        return false;
    }

    /**
     * Helper function to perform a sleep without throwing exception
     * @param millis
     */
    protected void quietSleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupted");
        }
    }

    /**
     * Helper function to call firePostSelectionChanged on the source viewer. This is what actually
     * invokes the parse on the main thread.
     * 
     * Normally it's called after 500 ms after typing stops, but calling it directly will circumvent
     * that and invoke parsing when it's called.
     */
    protected void firePostSelectionChanged()
    {
        FortranSourceViewer sourceViewer = (FortranSourceViewer)fEditor.getSourceViewerx();
        Accessor sourceViewerAccessor = new Accessor(sourceViewer, TextViewer.class);
        sourceViewerAccessor.invoke("firePostSelectionChanged", 
            new Class< ? >[] { int.class, int.class },
            new Object[] { 0, 0 });
    }
    
    // helpers for timing counting the time

    private Map<Integer, Long> timers = new HashMap<Integer, Long>();

    protected void initTime(int reference)
    {
        timers.put(reference, System.currentTimeMillis());
    }

    protected long time(String msg, int reference) {
        long resMs = System.currentTimeMillis() - timers.get(reference);
        System.out.println(msg + ": " + resMs + "ms");
        return resMs;
    }

    // copied from CDT BasicCEditorTest

    public static IWorkbenchPage getActivePage()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        return window != null ? window.getActivePage() : null;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    // copied from photran WorkspaceTestCase
    protected String readTestFile(Plugin activator, String srcDir, String filename)
        throws IOException, URISyntaxException
    {
        URL resource = activator.getBundle().getResource(srcDir + "/" + filename);
        System.out.println(activator.getBundle().toString());
        assertNotNull(resource);
        return readStream(resource.openStream());
    }

    protected void writeTestFile(Plugin activator, String srcDir, String fileName, String contents)
    {
        IFile file = project.getProject().getFile(srcDir + "/" + fileName);
        InputStream source = new ByteArrayInputStream(contents.getBytes());
        try
        {
            file.setContents(source, false, false, null);
        }
        catch (CoreException e)
        {
            System.out.println(e.getMessage());
            fail("Failed to write file");
        }
    }

    private String readStream(InputStream inputStream) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        for (int ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
        }
        in.close();
        return sb.toString();
    }

    // type methods copied from CDT BasicCEditorTest

    /**
     * Type characters into the styled text.
     * 
     * @param characters the characters to type
     */
    protected void type(CharSequence characters)
    {
        for (int i = 0; i < characters.length(); i++)
            type(characters.charAt(i), 0, 0);
    }

    /**
     * Type a character into the styled text.
     * 
     * @param character the character to type
     */
    protected void type(char character)
    {
        type(character, 0, 0);
    }

    /**
     * Type a character into the styled text.
     * 
     * @param character the character to type
     * @param keyCode the key code
     * @param stateMask the state mask
     */
    protected void type(char character, int keyCode, int stateMask)
    {
        Event event = new Event();
        event.character = character;
        event.keyCode = keyCode;
        event.stateMask = stateMask;
        Accessor accessor = new Accessor(textWidget);
        accessor.invoke("handleKeyDown", new Object[] { event });
    }
}
