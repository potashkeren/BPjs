package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import org.mozilla.javascript.*;

import java.io.Serializable;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BThreadJsProxy;
import java.util.Optional;

/**
 * The state of a BThread at {@code bsync}.
 *
 * @author orelmosheweinstock
 * @author Michael
 */
public class BThreadSyncSnapshot implements Serializable {

    /** Name of the BThread described */
    private String name;

    /**
     * The Javascript function that will be called when {@code this} BThread runs.
     */
    private Function entryPoint;

    /**
     * BThreads may specify a function that runs when they are removed because
     * of a {@code breakUpon} statement.
     */
    private Function interruptHandler = null;
    
    /** Proxy to {@code this}, used from the Javascript code.*/
    private final BThreadJsProxy proxy = new BThreadJsProxy(this);

    /** Scope for the Javascript code execution. */
    private Scriptable scope;

    /** Continuation of the code. */
    private Object continuation;
    
    /** BSync statement of the BThread at the time of the snapshot. */
    private BSyncStatement bSyncStatement;
    
    public BThreadSyncSnapshot(String aName, Function anEntryPoint) {
        name = aName;
        entryPoint = anEntryPoint;
    }

    /**
     * Convenience constructor with default parameters.
     */
    public BThreadSyncSnapshot() {
        this(BThreadSyncSnapshot.class.getName(), null);
    }
    
    /**
     * Creates the next snapshot of the BThread in a given run. 
     * @param aContinuation The BThread's continuation for the next sync.
     * @param aStatement The BThread's statement for the next sync.
     * @return a copy of {@code this} with updated continuation and statement.
     */
    public BThreadSyncSnapshot copyWith( Object aContinuation, BSyncStatement aStatement ) {
        BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, entryPoint);
        retVal.continuation = aContinuation;
        retVal.setInterruptHandler(interruptHandler);
        retVal.setupScope(scope.getParentScope());

        retVal.bSyncStatement = aStatement;
        aStatement.setBthread(retVal);
        
        return retVal;
    }
    
    void setupScope(Scriptable programScope) {
        scope = (Scriptable) Context.javaToJS(proxy, programScope);
        scope.delete("equals");
        scope.setParentScope(programScope);
    
        Scriptable curScope = entryPoint.getParentScope();
        if ( curScope.getParentScope() == null ) {
            entryPoint.setParentScope(scope);
            scope.setParentScope(curScope);
        } else {
            while ( curScope.getParentScope().getParentScope() != null) {
                curScope = curScope.getParentScope();
            }
            scope.setParentScope(curScope.getParentScope());
            curScope.setParentScope(scope);            
        }
    }

    public BSyncStatement getBSyncStatement() {
        return bSyncStatement;
    }

    public void setBSyncStatement(BSyncStatement stmt) {
        bSyncStatement = stmt;
        if ( bSyncStatement.getBthread() != this ) {
            bSyncStatement.setBthread(this);
        }
    }

    public Object getContinuation() {
        return continuation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[BThreadSyncSnapshot: " + name + "]";
    }

    public Optional<Function> getInterrupt() {
        return Optional.ofNullable(interruptHandler);
    }
    
    public void setInterruptHandler(Function anInterruptHandler) {
        interruptHandler = anInterruptHandler;
    }

    public Scriptable getScope() {
        return scope;
    }

    public Function getEntryPoint() {
        return entryPoint;
    }
    
}
