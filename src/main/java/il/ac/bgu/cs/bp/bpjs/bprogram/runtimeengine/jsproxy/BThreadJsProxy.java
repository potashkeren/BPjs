/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.Events;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 * Serves as {@code this} (of sorts) for Javascript BThread code. Public methods
 * in this class are directly callable form BThread code, no qualification needed.
 * 
 * For example, the following code invokes {@link #bsync(org.mozilla.javascript.NativeObject) }:
 * 
 * <pre><code>
 * bp.registerBThread( function(){
 *   bsync(...);
 * });
 * </code></pre>
 * @author michael
 */
public class BThreadJsProxy implements java.io.Serializable {
    
    private final BThreadSyncSnapshot bthread;

    public BThreadJsProxy(BThreadSyncSnapshot bthread) {
        this.bthread = bthread;
    }
    
    public void bsync( NativeObject jsRWB ) {
        Map<String, Object> jRWB = (Map)Context.jsToJava(jsRWB, Map.class);
        
        BSyncStatement stmt = BSyncStatement.make();
        Object req = jRWB.get("request");
        if ( req != null ) {
            if ( req instanceof BEvent ) {
                stmt = stmt.request((BEvent)req);
            } else if ( req instanceof NativeArray ) {
                NativeArray arr = (NativeArray) req;
                stmt = stmt.request(
                        Arrays.asList( arr.getIndexIds() ).stream()
                              .map( i -> (BEvent)arr.get(i) )
                              .collect( toSet() ));
            } 
        }
        
        stmt = stmt.waitFor( convertToEventSet(jRWB.get("waitFor")) )
                     .block( convertToEventSet(jRWB.get("block")) )
                 .interrupt( convertToEventSet(jRWB.get("interrupt")) );
        
        captureBThreadState(stmt);
        
    }

    private EventSet convertToEventSet( Object jsObject ) {
        if ( jsObject == null ) return Events.emptySet;
        
        // This covers event sets AND events.
        if ( jsObject instanceof EventSet ) {
            return (EventSet)jsObject;
        
        } else if ( jsObject instanceof NativeArray ) {
            NativeArray arr = (NativeArray) jsObject;
            if ( Stream.of(arr.getIds()).anyMatch( id -> arr.get(id)==null) ) {
                throw new RuntimeException("EventSet Array contains null sets.");
            }
            return ComposableEventSet.anyOf(
              Arrays.asList(arr.getIndexIds()).stream()
                    .map( i ->(EventSet)arr.get(i) )
                    .collect( toSet() ) );
        } else {
            final String errorMessage = "Cannot convert " + jsObject + " of class " + jsObject.getClass() + " to an event set";
            Logger.getLogger(BThreadSyncSnapshot.class.getName()).log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException( errorMessage);
        }
    }
    
    public void setInterruptHandler( Object aPossibleHandler ) {
        bthread.setInterruptHandler(
                (aPossibleHandler instanceof Function) ? (Function) aPossibleHandler: null );
    }
    
    private void captureBThreadState(BSyncStatement stmt) throws ContinuationPending {
        bthread.setBSyncStatement(stmt);
        stmt.setBthread(bthread);
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        capturedContinuation.setApplicationState(stmt);
        throw capturedContinuation;
    }

}