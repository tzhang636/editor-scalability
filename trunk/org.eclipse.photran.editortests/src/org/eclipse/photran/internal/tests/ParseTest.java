package org.eclipse.photran.internal.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.model.ConcurrentFortranModelBuilder;
import org.eclipse.photran.internal.core.model.FortranElement.Module;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;
import org.eclipse.photran.internal.core.model.FortranModelBuilder.OnParseCompleteListener;
import org.eclipse.photran.internal.core.model.IFortranModelBuilder;

@SuppressWarnings("restriction")
public class ParseTest extends BaseTestFramework implements OnParseCompleteListener
{

    private TranslationUnit tu;

    private volatile boolean parseComplete;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.photran.internal.tests.BaseTestFramework#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.tu = getTranslationUnit();
        parseComplete = false;
    }

    private TranslationUnit getTranslationUnit() throws Exception
    {
        return new TranslationUnit(BaseTestFramework.cproject, get10kTestFile(),
            FortranCorePlugin.FORTRAN_CONTENT_TYPE);
    }

    private FortranModelBuilder getBuilder(TranslationUnit tu)
    {
        FortranModelBuilder builder = new ConcurrentFortranModelBuilder();
        builder.setTranslationUnit(tu);
        builder.setOnParseCompleteListener(this);
        return builder;
    }

    public void testParseCompletes() throws Exception
    {
        long nanoTime = System.nanoTime();

        FortranModelBuilder builder = getBuilder(tu);
        builder.parse(false);

        long blockingTime = System.nanoTime() - nanoTime;
        System.out.println("Blocking time: " + (blockingTime / 1e6) + "ms");

        // poll for parsing completion
        // the magic number is here just to avoid infinite loop
        for (int i = 0; i < 6000; i++)
        {
            if (parseComplete)
            {
                // parsing successful
                long completionTime = System.nanoTime() - nanoTime;
                System.out.println("Completion time (±10ms): " + (completionTime / 1e6) + "ms");

                assertTrue(hasTestModule(tu));
                return;
            }
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                System.out.println("interrupted");
            }
        }
        fail("Timeout: took too long to parse. ");
    }

    public void testNonBlocking() throws Exception
    {
        FortranModelBuilder builder = getBuilder(tu);
        builder.parse(false);

        // Note that in strict theoretical sense, the code below is not guaranteed to run before the
        // parsing completes. But it's good enough since main threads are most likely to continue
        // running before the child thread(s). And the parsing takes a relatively long time.
        assertFalse(parseComplete);
    }

    // look for the test module in the parsed translation unit
    private boolean hasTestModule(ITranslationUnit tu) throws CModelException
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

    private IFile get10kTestFile() throws Exception
    {
        IFile file = importFile("workspace/tenthousand.f90",
            readTestFile(Activator.getDefault(), "testfiles", "tenthousand.f90"));
        return file;
    }

    protected String readTestFile(Plugin activator, String srcDir, String filename)
        throws IOException, URISyntaxException
    {
        URL resource = activator.getBundle().getResource(srcDir + "/" + filename);
        System.out.println(activator.getBundle().toString());
        assertNotNull(resource);
        return readStream(resource.openStream());
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

    @Override
    public void onParseComplete(IFortranModelBuilder builder, boolean success)
    {
        assertTrue("Parse failed", success);
        parseComplete = true;
    }

}
