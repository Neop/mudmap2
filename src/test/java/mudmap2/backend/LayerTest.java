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
package mudmap2.backend;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class LayerTest {

    static World world;

    public LayerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        world = new World("Unittest");
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

    /**
     * Test of setQuadtree method, of class Layer.
     */
    @Test
    @Ignore
    public void testSetQuadtree() {
        System.out.println("setQuadtree");

        int center_x = -4;
        int center_y = 5;
        Layer instance = new Layer(world);
        instance.setQuadtree(center_x, center_y);

        // how to check this?
    }

    /**
     * Test of getCenterX method, of class Layer.
     */
    @Test
    public void testGetCenterX() {
        System.out.println("getCenterX");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = (3-6)/2;
            int result = instance.getCenterX();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getCenterY method, of class Layer.
     */
    @Test
    public void testGetCenterY() {
        System.out.println("getCenterY");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = (5-8)/2;
            int result = instance.getCenterY();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getXMin method, of class Layer.
     */
    @Test
    public void testGetXMin() {
        System.out.println("getXMin");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = -6;
            int result = instance.getXMin();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getXMax method, of class Layer.
     */
    @Test
    public void testGetXMax() {
        System.out.println("getXMax");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = 3;
            int result = instance.getXMax();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getYMin method, of class Layer.
     */
    @Test
    public void testGetYMin() {
        System.out.println("getYMin");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = -8;
            int result = instance.getYMin();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getYMax method, of class Layer.
     */
    @Test
    public void testGetYMax() {
        System.out.println("getYMax");

        Layer instance = new Layer(world);

        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = 5;
            int result = instance.getYMax();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of putPlace method, of class Layer.
     */
    @Test
    public void testPut_3args(){
        System.out.println("put");

        Layer instance = new Layer(world);

        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of putPlace method, of class Layer.
     */
    @Test
    public void testPut_LayerElement() throws Exception {
        System.out.println("put");

        Layer instance = new Layer(world);

        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getPlace method, of class Layer.
     */
    @Test
    public void testGet() {
        System.out.println("get");

        Layer instance = new Layer(world);

        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getNeighbors method, of class Layer.
     */
    @Test
    public void testGetNeighbors() {
        System.out.println("getNeighbors");

        int x = 0;
        int y = 0;
        int distance = 2;
        Layer instance = new Layer(world);

        LinkedList<Place> result = instance.getNeighbors(x, y, distance);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        Place el1 = new Place("Place1", x, y, instance);
        Place el2 = new Place("Place1", x+1, y+1, instance);
        Place el3 = new Place("Place1", x-1, y-1, instance);
        Place el4 = new Place("Place1", x-2, y-2, instance);
        Place el5 = new Place("Place1", x+2, y-2, instance);
        try {
            instance.put(el1);
            instance.put(el2);
            instance.put(el3);
            instance.put(el4);
            instance.put(el5);

            distance = 0;
            result = instance.getNeighbors(x, y, distance);
            assertTrue(result.isEmpty());

            distance = 1;
            result = instance.getNeighbors(x, y, distance);
            assertEquals(2, result.size());
            assertTrue(result.contains(el2));
            assertTrue(result.contains(el3));

            distance = 2;
            result = instance.getNeighbors(x, y, distance);
            assertEquals(4, result.size());
            assertTrue(result.contains(el2));
            assertTrue(result.contains(el3));
            assertTrue(result.contains(el4));
            assertTrue(result.contains(el5));
        } catch (Layer.PlaceNotInsertedException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getId method, of class Layer.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");

        int expResult = 0;
        Layer instance = new Layer(expResult, world);
        int result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWorld method, of class Layer.
     */
    @Test
    public void testGetWorld() {
        System.out.println("getWorld");

        Layer instance = new Layer(world);
        World result = instance.getWorld();
        assertEquals(world, result);
    }

    /**
     * Test of toString method, of class Layer.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        Layer instance = new Layer(world);
        String expResult = instance.getName();
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of removePlace method, of class Layer.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");

        Layer instance = new Layer(world);

        try {
            Place element = new Place("MyPlace", 0, 0, instance);
            instance.put(element);
            assertEquals(element, instance.get(0, 0));

            instance.remove(element);
        assertFalse(instance.exist(0, 0));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of exist method, of class Layer.
     */
    @Test
    public void testExist() {
        System.out.println("exist");

        Layer instance = new Layer(world);

        int x = 0;
        int y = 0;
        assertFalse(instance.exist(x, y));
        try {
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertTrue(instance.exist(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        x = 4;
        y = 5;
        assertFalse(instance.exist(x, y));
        try {
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertTrue(instance.exist(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        x = -2;
        y = 6;
        assertFalse(instance.exist(x, y));
        try {
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertTrue(instance.exist(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        x = 3;
        y = -5;
        assertFalse(instance.exist(x, y));
        try {
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertTrue(instance.exist(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        x = -8;
        y = -6;
        assertFalse(instance.exist(x, y));
        try {
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertTrue(instance.exist(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of isEmpty method, of class Layer.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");

        Layer instance = new Layer(world);
        assertTrue(instance.isEmpty());

        try {
            Place element = new Place("MyPlace", 0, 0, instance);
            instance.put(element);
            assertFalse(instance.isEmpty());

            instance.remove(element);
            assertTrue(instance.isEmpty());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }

    /**
     * Test of getPlaces method, of class Layer.
     */
    @Test
    public void testGetPlaces() {
        System.out.println("getPlaces");

        Layer instance = new Layer(world);

        HashSet<Place> result = instance.getPlaces();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        Place el1 = new Place("Place1", 0, 0, instance);
        Place el2 = new Place("Place2", -1, 2, instance);
        try {
            instance.put(el1);
            instance.put(el2);

            result = instance.getPlaces();
            assertEquals(2, result.size());
            assertTrue(result.contains(el1));
            assertTrue(result.contains(el2));
        } catch (Layer.PlaceNotInsertedException ex) {
            fail(ex.getMessage());
        }


    }

}
