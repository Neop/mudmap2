/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package mudmap2.backend.prquadtree;

import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author neop
 */
public class QuadtreeTest {

    public QuadtreeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private Object createAndInsertTestObject(Integer x, Integer y, Quadtree quadtree) throws Exception{
        Object object = new Object();
        quadtree.insert(object, x, y);
        return object;
    }

    /**
     * Test of get method, of class Quadtree.
     */
    @Test
    public void testGet() {
        System.out.println("get");

        Quadtree instance = new Quadtree();

        int x1 = 0;
        int y1 = 0;
        Object result1 = null;
        try {
            assertNull(instance.get(x1, y1));
            result1 = createAndInsertTestObject(x1, y1, instance);
            Object actualResult = instance.get(x1, y1);
            assertEquals(result1, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x2 = 1;
        int y2 = 1;
        Object result2 = null;
        try {
            assertNull(instance.get(x2, y2));
            result2 = createAndInsertTestObject(x2, y2, instance);
            Object actualResult = instance.get(x2, y2);
            assertEquals(result2, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x3 = -1;
        int y3 = 1;
        Object result3 = null;
        try {
            assertNull(instance.get(x3, y3));
            result3 = createAndInsertTestObject(x3, y3, instance);
            Object actualResult = instance.get(x3, y3);
            assertEquals(result3, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x4 = 1;
        int y4 = -1;
        Object result4 = null;
        try {
            assertNull(instance.get(x4, y4));
            result4 = createAndInsertTestObject(x4, y4, instance);
            Object actualResult = instance.get(x4, y4);
            assertEquals(result4, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x5 = -1;
        int y5 = -1;
        Object result5 = null;
        try {
            assertNull(instance.get(x5, y5));
            result5 = createAndInsertTestObject(x5, y5, instance);
            Object actualResult = instance.get(x5, y5);
            assertEquals(result5, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x6 = 2;
        int y6 = 2;
        Object result6 = null;
        try {
            assertNull(instance.get(x6, y6));
            result6 = createAndInsertTestObject(x6, y6, instance);
            Object actualResult = instance.get(x6, y6);
            assertEquals(result6, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x7 = -2;
        int y7 = 2;
        Object result7 = null;
        try {
            assertNull(instance.get(x7, y7));
            result7 = createAndInsertTestObject(x7, y7, instance);
            Object actualResult = instance.get(x7, y7);
            assertEquals(result7, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x8 = 2;
        int y8 = -2;
        Object result8 = null;
        try {
            assertNull(instance.get(x8, y8));
            result8 = createAndInsertTestObject(x8, y8, instance);
            Object actualResult = instance.get(x8, y8);
            assertEquals(result8, actualResult);
        } catch (Exception ex) {
            fail();
        }

        int x9 = -2;
        int y9 = -2;
        Object result9 = null;
        try {
            assertNull(instance.get(x9, y9));
            result9 = createAndInsertTestObject(x9, y9, instance);
            Object actualResult = instance.get(x9, y9);
            assertEquals(result9, actualResult);
        } catch (Exception ex) {
            fail();
        }

        if(result1 != null) assertEquals(result1, instance.get(x1, y1));
        if(result2 != null) assertEquals(result2, instance.get(x2, y2));
        if(result3 != null) assertEquals(result3, instance.get(x3, y3));
        if(result4 != null) assertEquals(result4, instance.get(x4, y4));
        if(result5 != null) assertEquals(result5, instance.get(x5, y5));
        if(result6 != null) assertEquals(result6, instance.get(x6, y6));
        if(result7 != null) assertEquals(result7, instance.get(x7, y7));
        if(result8 != null) assertEquals(result8, instance.get(x8, y8));
        if(result9 != null) assertEquals(result9, instance.get(x9, y9));
    }

    /**
     * Test of exist method, of class Quadtree.
     */
    @Test
    public void testExist() {
        System.out.println("exist");

        Quadtree instance = new Quadtree();

        int x1 = 0;
        int y1 = 0;
        Object result1 = null;
        try {
            assertFalse(instance.exist(x1, y1));
            result1 = createAndInsertTestObject(x1, y1, instance);
            Boolean result = instance.exist(x1, y1);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x2 = 1;
        int y2 = 1;
        Object result2 = null;
        try {
            assertFalse(instance.exist(x2, y2));
            result2 = createAndInsertTestObject(x2, y2, instance);
            Boolean result = instance.exist(x2, y2);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x3 = -1;
        int y3 = 1;
        Object result3 = null;
        try {
            assertFalse(instance.exist(x3, y3));
            result3 = createAndInsertTestObject(x3, y3, instance);
            Boolean result = instance.exist(x3, y3);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x4 = 1;
        int y4 = -1;
        Object result4 = null;
        try {
            assertFalse(instance.exist(x4, y4));
            result4 = createAndInsertTestObject(x4, y4, instance);
            Boolean result = instance.exist(x4, y4);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x5 = -1;
        int y5 = -1;
        Object result5 = null;
        try {
            assertFalse(instance.exist(x5, y5));
            result5 = createAndInsertTestObject(x5, y5, instance);
            Boolean result = instance.exist(x5, y5);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x6 = 2;
        int y6 = 2;
        Object result6 = null;
        try {
            assertFalse(instance.exist(x6, y6));
            result6 = createAndInsertTestObject(x6, y6, instance);
            Boolean result = instance.exist(x6, y6);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x7 = -2;
        int y7 = 2;
        Object result7 = null;
        try {
            assertFalse(instance.exist(x7, y7));
            result7 = createAndInsertTestObject(x7, y7, instance);
            Boolean result = instance.exist(x7, y7);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x8 = 2;
        int y8 = -2;
        Object result8 = null;
        try {
            assertFalse(instance.exist(x8, y8));
            result8 = createAndInsertTestObject(x8, y8, instance);
            Boolean result = instance.exist(x8, y8);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        int x9 = -2;
        int y9 = -2;
        Object result9 = null;
        try {
            assertFalse(instance.exist(x9, y9));
            result9 = createAndInsertTestObject(x9, y9, instance);
            Boolean result = instance.exist(x9, y9);
            assertTrue(result);
        } catch (Exception ex) {
            fail();
        }

        if(result1 != null) assertTrue(instance.exist(x1, y1));
        if(result2 != null) assertTrue(instance.exist(x2, y2));
        if(result3 != null) assertTrue(instance.exist(x3, y3));
        if(result4 != null) assertTrue(instance.exist(x4, y4));
        if(result5 != null) assertTrue(instance.exist(x5, y5));
        if(result6 != null) assertTrue(instance.exist(x6, y6));
        if(result7 != null) assertTrue(instance.exist(x7, y7));
        if(result8 != null) assertTrue(instance.exist(x8, y8));
        if(result9 != null) assertTrue(instance.exist(x9, y9));
    }

    /**
     * Test of isEmpty method, of class Quadtree.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");

        Quadtree instance = new Quadtree();
        assertTrue(instance.isEmpty());

        try {
            instance.insert(new Object(), 4, 5);
            assertFalse(instance.isEmpty());
            instance.remove(4, 5);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail();
        }

        try {
            instance.insert(new Object(), 4, -5);
            assertFalse(instance.isEmpty());
            instance.remove(4, -5);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail();
        }

        try {
            instance.insert(new Object(), -4, 5);
            assertFalse(instance.isEmpty());
            instance.remove(-4, 5);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail();
        }

        try {
            instance.insert(new Object(), -4, -5);
            assertFalse(instance.isEmpty());
            instance.remove(-4, -5);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail();
        }

        try {
            instance.insert(new Object(), 0, 0);
            assertFalse(instance.isEmpty());
            instance.remove(0, 0);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail();
        }
    }

    /**
     * Test of insert method, of class Quadtree.
     */
    @Test
    public void testInsert() {
        System.out.println("insert");
        //testGet(); // also tests insert

        Quadtree instance = new Quadtree();
        try {
            instance.insert(new Object(), 1, 1);
        } catch (Exception ex) {
            fail();
        }
        try {
            instance.insert(new Object(), 1, 1);
            fail();
        } catch (Exception ex) {}
    }

    /**
     * Test of remove method, of class Quadtree.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        //testIsEmpty(); // also tests remove
    }

    /**
     * Test of move method, of class Quadtree.
     */
    @Test
    public void testMove() {
        System.out.println("move");

        Quadtree instance = new Quadtree();

        int x_bef = 0;
        int y_bef = 0;
        int x_aft = 1;
        int y_aft = 1;

        try {
            instance.move(x_bef, y_bef, x_aft, y_aft);
            fail();
        } catch (Exception ex) {}

        try {
            Object object = new Object();
            instance.insert(object, x_bef, y_bef);
            instance.move(x_bef, y_bef, x_aft, y_aft);
            assertEquals(object, instance.get(x_aft, y_aft));
        } catch (Exception ex) {
            fail();
        }

        try {
            x_bef = -1;
            y_bef = -5;
            Object object = new Object();
            instance.insert(object, x_bef, y_bef);
            instance.move(x_bef, y_bef, x_aft, y_aft);
            fail();
        } catch (Exception ex) {}
    }

    /**
     * Test of values method, of class Quadtree.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        Quadtree instance = new Quadtree();
        HashSet result = instance.values();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        Object object1 = new Object();
        try {
            instance.insert(object1, 1, 2);
        } catch (Exception ex) {
            fail();
        }
        result = instance.values();
        assertEquals(1, result.size());
        assertTrue(result.contains(object1));

        Object object2 = new Object();
        try {
            instance.insert(object2, 2, 2);
        } catch (Exception ex) {
            fail();
        }
        result = instance.values();
        assertEquals(2, result.size());
        assertTrue(result.contains(object1));
        assertTrue(result.contains(object2));
    }

}
