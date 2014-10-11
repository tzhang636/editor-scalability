package org.eclipse.photran.internal.core.model;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Alternative FortranModelBuilder that performs parsing tasks in a separate thread.
 * 
 * User can select which model builder to use at run-time via Window > Preferences > Fortran
 * 
 * TODO: determine what data accessed by parsing thread is shared and mutable; synchronize accordingly
 * 
 * @authors joe, tom, andrew
 */
public class ConcurrentFortranModelBuilder extends FortranModelBuilder {
    
    private final static ExecutorService parseExecutor = Executors.newSingleThreadExecutor();
    private static Future<Object> parseFuture = null;

    /**
     * Represent parsing tasks by implementing Callable so they can be run by an executor, throw checked
     * exceptions, and can be canceled.
     * 
     */
    private class ParseCallable implements Callable<Object>
    {
        private final boolean quickParseMode;
        
        public ParseCallable(boolean qpm) {
            this.quickParseMode = qpm;
        }
        
        @Override
        public Object call() throws Exception
        {
            ConcurrentFortranModelBuilder.super.parse(quickParseMode);
            return null;
        }
    }

    /**
     * Delegates parsing tasks to the executor.
     * 
     * No more than one task can be queued simultaneously since we cancel
     * previously scheduled tasks before scheduling a new one
     * 
     * @throws Exception TODO: why?
     */
    @Override
    public void parse(boolean quickParseMode) throws Exception
    {
        if (parseFuture != null) {
            if (!parseFuture.isDone()) parseFuture.cancel(true);
        }
        Callable<Object> myParseCallable = new ParseCallable(quickParseMode);
        parseFuture = parseExecutor.submit(myParseCallable);
    }
    
    /**
     * Concurrent model builder has interruptible parsing, so return whether the thread has been interrupted.
     */
    @SuppressWarnings("restriction")
    @Override
    protected boolean isInterrupted(FortranElement elem) throws Exception {
        if (Thread.currentThread().isInterrupted())
        {
            translationUnit.removeChild(elem);
            setIsStructureKnown(false);
            interrupted = true;
            return true;
        }
        return false;
    }
}