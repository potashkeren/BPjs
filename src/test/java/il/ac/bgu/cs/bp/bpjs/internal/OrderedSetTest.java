/*
 * The MIT License
 *
 * Copyright 2017 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.internal;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class OrderedSetTest {
    

    /**
     * Test of of method, of class OrderedSet.
     */
    @Test
    public void testBasics() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c", "c");
        assertEquals("a", sut.first());
        assertEquals("c", sut.last());
        assertEquals("b", sut.tailSet("b").first());
        assertEquals("c", sut.tailSet("b").last());
        
        assertEquals( new HashSet<>(Arrays.asList("a","b","c")), sut);
    }
    
    @Test
    public void testEquals() {
        OrderedSet<String> sutA = OrderedSet.of();
        OrderedSet<String> sutB = OrderedSet.of();
        
        assertEquals(sutA, sutA);
        assertNotEquals(null, sutA);
        assertNotEquals("not a set", sutA);
        
        assertEquals(sutB, sutA);
        
        sutA.add("A");
        assertNotEquals(sutA, sutB);
        assertNotEquals(sutB, sutA);
        
        sutB.add("A");
        assertEquals(sutA, sutB);
        assertEquals(sutB, sutA);
        
        // double addition should be ignored
        sutA.add("A");
        assertEquals(sutA, sutB);
        assertEquals(sutB, sutA);
        
        sutA.add("X");
        assertNotEquals(sutA, sutB);
        assertNotEquals(sutB, sutA);
    }

    
}