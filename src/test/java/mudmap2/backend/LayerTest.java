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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.prquadtree.Quadtree;
import mudmap2.utils.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
     * Test for Layer's constructors
     */
    @Test
    public void testLayer(){
        System.out.println("getlayer");

        // test Layer(World), check world-unique id generation
        World world1 = new World();
        World world2 = new World();

        Layer layerW1_1 = new Layer(world1);
        Layer layerW2_1 = new Layer(world2);
        Layer layerW1_2 = new Layer(world1);

        assertEquals(1, (int) layerW1_1.getId());
        assertEquals(2, (int) layerW1_2.getId());
        assertEquals(1, (int) layerW2_1.getId());

        // test Layer(int, World), check world-unique id generation
        Layer layerW1_3 = new Layer(6, world1);
        Layer layerW1_4 = new Layer(7, world2);
        Layer layerW2_3 = new Layer(6, world1);

        assertEquals(6, (int) layerW1_3.getId());
        assertEquals(7, (int) layerW1_4.getId());
        assertEquals(6, (int) layerW2_3.getId());

        // test properties
        assertEquals(world1, layerW1_1.getWorld());
        assertEquals(0, layerW1_1.getPlaces().size());
        assertEquals(0, layerW1_1.getXMax());
        assertEquals(0, layerW1_1.getXMin());
        assertEquals(0, layerW1_1.getYMax());
        assertEquals(0, layerW1_1.getYMin());

        try {
            Layer layer = new Layer(null);
            fail();
        } catch(NullPointerException ex){
            // expected
        }

        try {
            Layer layer = new Layer(10, null);
            fail();
        } catch(NullPointerException ex){
            // expected
        }
    }

    /**
     * Test for getName
     */
    @Test
    public void testGetSetName(){
        System.out.println("getName");

        Layer instance1 = new Layer(world);

        // default name
        assertNotNull(instance1.getName());
        assertEquals("Map " + instance1.getId(), instance1.getName());

        System.out.println("setName");
        // explicit name
        instance1.setName("Layer name");
        assertEquals("Layer name", instance1.getName());

        // reset name
        instance1.setName(null);
        assertEquals("Map " + instance1.getId(), instance1.getName());
    }

    /**
     * Test for hasName
     */
    @Test
    public void testHasName(){
        System.out.println("hasName");

        Layer instance1 = new Layer(world);

        // default name
        assertNotNull(instance1.getName());
        assertFalse(instance1.hasName());

        // explicit name
        instance1.setName("Layer name");
        assertTrue(instance1.hasName());

        // reset name
        instance1.setName(null);
        assertFalse(instance1.hasName());
    }

    /**
     * Test of setQuadtree method, of class Layer.
     */
    @Test
    public void testSetQuadtree() {
        System.out.println("setQuadtree");

        try {
            // use reflection to access private field
            Field fieldElements = Layer.class.getDeclaredField("elements");
            fieldElements.setAccessible(true);

            int centerX = -4;
            int centerY = 5;
            Layer instance = new Layer(world);

            // remember original quadtree
            Quadtree quadtreeOrig = (Quadtree) fieldElements.get(instance);
            assertNotNull(quadtreeOrig);

            instance.setQuadtree(centerX, centerY);
            Quadtree quadtreeNew = (Quadtree) fieldElements.get(instance);
            assertNotNull(quadtreeNew);
            assertNotSame(quadtreeOrig, quadtreeNew);

            instance.setQuadtree(centerX, centerY);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            fail(ex.getMessage());
        }
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
            fail(ex.getMessage());
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
            fail(ex.getMessage());
        }
    }

    /**
     * Test getExactCenter
     */
    @Test
    public void testGetExactCenter(){
        System.out.println("getExactCenter");

        Layer instance = new Layer(world);

        // no places in layer
        Pair<Double, Double> center1 = instance.getExactCenter();
        assertNotNull(center1);
        assertEquals((Double) 0.0, center1.first);
        assertEquals((Double) 0.0, center1.second);

        // two places in layer
        int x1 = 3, x2 = -6;
        int y1 = 5, y2 = -1;

        try {
            instance.put(new Place("MyPlace", x1, y1, instance));
            instance.put(new Place("MyPlace", x2, y2, instance));

            Double expResultX = -1.5;
            Double expResultY = 2.0;

            Pair<Double, Double> center2 = instance.getExactCenter();
            assertNotNull(center2);

            assertEquals(expResultX, center2.first);
            assertEquals(expResultY, center2.second);

        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getXMin method, of class Layer.
     */
    @Test
    public void testGetXMin() {
        System.out.println("getXMin");

        Layer instance = new Layer(world);

        // no place in layer
        assertEquals(0, instance.getXMin());

        // two places in layer
        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = -6;
            int result = instance.getXMin();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getXMax method, of class Layer.
     */
    @Test
    public void testGetXMax() {
        System.out.println("getXMax");

        Layer instance = new Layer(world);

        // no place in layer
        assertEquals(0, instance.getXMax());

        // two places in layer
        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = 3;
            int result = instance.getXMax();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getYMin method, of class Layer.
     */
    @Test
    public void testGetYMin() {
        System.out.println("getYMin");

        Layer instance = new Layer(world);

        // no place in layer
        assertEquals(0, instance.getYMin());

        // two places in layer
        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = -8;
            int result = instance.getYMin();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getYMax method, of class Layer.
     */
    @Test
    public void testGetYMax() {
        System.out.println("getYMax");

        Layer instance = new Layer(world);

        // no place in layer
        assertEquals(0, instance.getYMax());

        // two places in layer
        try {
            instance.put(new Place("MyPlace", 3, 5, instance));
            instance.put(new Place("MyPlace", -6, -8, instance));

            int expResult = 5;
            int result = instance.getYMax();
            assertEquals(expResult, result);
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of putPlace method, of class Layer.
     */
    @Test
    public void testPut_3args(){
        System.out.println("put");

        Layer instance = new Layer(world);
        Layer other = world.getNewLayer();

        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        // try to put a place to an occupied position
        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element, x, y);
            fail();
        } catch (Exception ex) {
            // expected
        }
        
        // move place to a different layer
        try {
            // put on other layer
            int x1 = 3;
            int y1 = 7;
            Place element = new Place("MyPlace", x1, y1, instance);
            other.put(element, x1, y1);
            assertEquals(element, other.get(x1, y1));
            
            // move to instance layer
            int x2 = 5;
            int y2 = -4;
            instance.put(element, x2, y2);
            assertNull(other.get(x1, y1));
            assertEquals(element, instance.get(x2, y2));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of putPlace method, of class Layer.
     * @throws java.lang.Exception
     */
    @Test
    public void testPut_LayerElement() throws Exception {
        System.out.println("put");

        Layer instance = new Layer(world);
        Layer other = world.getNewLayer();

        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        // try to put a place to an occupied position
        try {
            int x = 0;
            int y = 0;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            fail();
        } catch (Layer.PlaceNotInsertedException ex) {
            // expected
        }

        // move place to a different layer
        try {
            // put on other layer
            int x = 3;
            int y = 7;
            Place element = new Place("MyPlace", x, y, instance);
            other.put(element, x, y);
            assertEquals(element, other.get(x, y));
            
            // move to instance layer
            instance.put(element);
            assertNull(other.get(x, y));
            assertEquals(element, instance.get(x, y));
        } catch (Exception ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
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
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = -5;
            int y = -2;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        try {
            int x = 3;
            int y = 9;
            Place element = new Place("MyPlace", x, y, instance);
            instance.put(element);
            assertEquals(element, instance.get(x, y));
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        // get unoccupied position
        int x = 100;
        int y = 0;
        assertNull(instance.get(x, y));
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

            // check for center place (el1)
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

            // check for el2 (some neighbors are not available)
            distance = 1;
            result = instance.getNeighbors(x+1, y+1, distance);
            assertEquals(1, result.size());
            assertTrue(result.contains(el1));

            distance = 2;
            result = instance.getNeighbors(x+1, y+1, distance);
            assertEquals(2, result.size());
            assertTrue(result.contains(el1));
            assertTrue(result.contains(el3));

            distance = 3;
            result = instance.getNeighbors(x+1, y+1, distance);
            assertEquals(4, result.size());
            assertTrue(result.contains(el1));
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

        int expResult2 = 100;
        Layer instance2 = new Layer(expResult2, world);
        int result2 = instance2.getId();
        assertEquals(expResult2, result2);
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
            assertTrue(instance.exist(0, 0));

            instance.remove(element);
            assertFalse(instance.exist(0, 0));
        } catch (RuntimeException | Layer.PlaceNotInsertedException ex) {
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
        // empty layer
        assertTrue(instance.isEmpty());

        try {
            Place element = new Place("MyPlace", 0, 0, instance);
            instance.put(element);
            // layer has one place
            assertFalse(instance.isEmpty());

            instance.remove(element);
            // empty layer
            assertTrue(instance.isEmpty());
        } catch (RuntimeException | Layer.PlaceNotInsertedException ex) {
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
     *
     */
    @Test
    public void testIsPlaceNameUnique(){
        System.out.println("isPlaceNameUnique");

        Layer instance = world.getNewLayer();

        // place not inserted yet: name is unique
        assertTrue(instance.isPlaceNameUnique("PlaceName"));

        try {
            Place place1 = new Place("PlaceName", 0, 0, instance);
            instance.put(place1);
            // place inserted, name is unique
            assertTrue(instance.isPlaceNameUnique("PlaceName"));

            Place place2 = new Place("PlaceName", 1, 0, instance);
            instance.put(place2);
            // place name inserted twice, name is unique
            assertFalse(instance.isPlaceNameUnique("PlaceName"));

            Place place3 = new Place("PlaceName", 2, 0, instance);
            instance.put(place3);
            // place name inserted thrice, name is unique
            assertFalse(instance.isPlaceNameUnique("PlaceName"));

            instance.remove(place3);
            instance.remove(place2);
            assertTrue(instance.isPlaceNameUnique("PlaceName"));
        } catch (Layer.PlaceNotInsertedException | RuntimeException ex) {
            Logger.getLogger(LayerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
