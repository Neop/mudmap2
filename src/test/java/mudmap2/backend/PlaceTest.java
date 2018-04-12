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

import java.awt.Color;
import java.util.HashSet;
import java.util.TreeMap;
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
public class PlaceTest {

    static World world = null;
    static Layer layer;

    public PlaceTest() {
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
        layer = new Layer(world);
        world.addLayer(layer);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of constructor
     */
    @Test
    public void testPlace(){
        System.out.println("Place");

        Place instance1 = new Place("MyPlace", 123, 456, layer);
        assertEquals("MyPlace", instance1.getName());
        assertEquals(123,       instance1.getX());
        assertEquals(456,       instance1.getY());
        assertEquals(layer,     instance1.getLayer());

        Place instance2 = new Place("MyPlace", -123, 456, layer);
        assertEquals("MyPlace", instance2.getName());
        assertEquals(-123,      instance2.getX());
        assertEquals(456,       instance2.getY());
        assertEquals(layer,         instance2.getLayer());

        Place instance3 = new Place("MyPlace", 123, -456, layer);
        assertEquals("MyPlace", instance3.getName());
        assertEquals(123,      instance3.getX());
        assertEquals(-456,       instance3.getY());
        assertEquals(layer,     instance3.getLayer());

        // test name = null
        Place instance4 = new Place(null, 123, 456, layer);
        assertNull(instance4.getName());
        assertEquals(123,      instance4.getX());
        assertEquals(456,       instance4.getY());
        assertEquals(layer,     instance4.getLayer());

        // test layer = null
        Place instance5 = new Place("MyPlace", 123, 456, null);
        assertEquals("MyPlace", instance5.getName());
        assertEquals(123,      instance5.getX());
        assertEquals(456,       instance5.getY());
        assertNull(instance5.getLayer());
    }

    /**
     * Test of getId method, of class Place.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");

        int id = 0;
        Place instance = new Place(id, "name", 0, 0, layer);
        int result = instance.getId();
        assertEquals(id, result);

        id = 1;
        instance = new Place(id, "name", 1, 0, layer);
        result = instance.getId();
        assertEquals(id, result);

        id = -1;
        instance = new Place(id, "name", 2, 0, layer);
        result = instance.getId();
        assertEquals(id, result);
    }

    /**
     * Test of getName method, of class Place.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        String expResult1 = "MyPlace";
        Place instance1 = new Place(expResult1, 0, 0, layer);
        String result = instance1.getName();
        assertEquals(expResult1, result);

        // test null
        Place instance2 = new Place(null, 0, 0, layer);
        assertNull(instance2.getName());
    }

    /**
     * Test of setName method, of class Place.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");

        String name = "FooBar";
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.setName(name);
        String result = instance.getName();
        assertEquals(name, result);

        // set empty name
        name = "";
        instance = new Place("MyPlace", 0, 0, layer);
        instance.setName(name);
        result = instance.getName();
        assertEquals(name, result);

        // set null
    }

    /**
     * Test of getCoordinate method, of class Place.
     */
    @Test
    public void testGetCoordinate() {
        System.out.println("getCoordinate");

        int x = 0, y = 0;
        Place instance = new Place("MyPlace", x, y, layer);
        WorldCoordinate expResult = new WorldCoordinate(layer.getId(), x, y);
        WorldCoordinate result = instance.getCoordinate();
        assertEquals(0, expResult.compareTo(result));

        x = 1; y = 1;
        instance = new Place("MyPlace", x, y, layer);
        expResult = new WorldCoordinate(layer.getId(), x, y);
        result = instance.getCoordinate();
        assertEquals(0, expResult.compareTo(result));

        x = -1; y = 1;
        instance = new Place("MyPlace", x, y, layer);
        expResult = new WorldCoordinate(layer.getId(), x, y);
        result = instance.getCoordinate();
        assertEquals(0, expResult.compareTo(result));

        x = 1; y = -1;
        instance = new Place("MyPlace", x, y, layer);
        expResult = new WorldCoordinate(layer.getId(), x, y);
        result = instance.getCoordinate();
        assertEquals(0, expResult.compareTo(result));

        x = -1; y = -1;
        instance = new Place("MyPlace", x, y, layer);
        expResult = new WorldCoordinate(layer.getId(), x, y);
        result = instance.getCoordinate();
        assertEquals(0, expResult.compareTo(result));
    }

    /**
     * Test of getPlaceGroup method, of class Place.
     */
    @Test
    public void testGetPlaceGroup() {
        System.out.println("getPlaceGroup");

        Place instance = new Place("MyPlace", 0, 0, layer);
        assertNull(instance.getPlaceGroup());

        PlaceGroup placeGroup = new PlaceGroup("myGroup", Color.red);
        instance.setPlaceGroup(placeGroup);
        assertEquals(placeGroup, instance.getPlaceGroup());
    }

    /**
     * Test of setPlaceGroup method, of class Place.
     */
    @Test
    public void testSetPlaceGroup() {
        System.out.println("setPlaceGroup");

        PlaceGroup placeGroup = new PlaceGroup("myGroup", Color.red);
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.setPlaceGroup(placeGroup);
        PlaceGroup result = instance.getPlaceGroup();
        assertEquals(placeGroup, result);

        // test null
        instance.setPlaceGroup(null);
        result = instance.getPlaceGroup();
        assertEquals(null, result);
    }

    /**
     * Test of getRecLevelMin method, of class Place.
     */
    @Test
    public void testGetRecLevelMin() {
        System.out.println("getRecLevelMin");

        Place instance = new Place("MyPlace", 0, 0, layer);
        // test default value
        assertEquals(-1, instance.getRecLevelMin());

        int expResult1 = 5;
        instance.setRecLevelMin(expResult1);
        int result1 = instance.getRecLevelMin();
        assertEquals(expResult1, result1);

        int expResult2 = -3;
        instance.setRecLevelMin(expResult2);
        int result2 = instance.getRecLevelMin();
        assertEquals(expResult2, result2);

        int expResult3 = 0;
        instance.setRecLevelMin(expResult3);
        int result3 = instance.getRecLevelMin();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of setRecLevelMin method, of class Place.
     */
    @Test
    public void testSetRecLevelMin() {
        System.out.println("setRecLevelMin");

        int recLevelMin1 = 0;
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.setRecLevelMin(recLevelMin1);
        int result1 = instance.getRecLevelMin();
        assertEquals(recLevelMin1, result1);

        int recLevelMin2 = 10;
        instance.setRecLevelMin(recLevelMin2);
        int result2 = instance.getRecLevelMin();
        assertEquals(recLevelMin2, result2);

        int recLevelMin3 = -2;
        instance.setRecLevelMin(recLevelMin3);
        int result3 = instance.getRecLevelMin();
        assertEquals(recLevelMin3, result3);

        int recLevelMin4 = 0;
        instance.setRecLevelMin(recLevelMin4);
        int result4 = instance.getRecLevelMin();
        assertEquals(recLevelMin4, result4);
    }

    /**
     * Test of getRecLevelMax method, of class Place.
     */
    @Test
    public void testGetRecLevelMax() {
        System.out.println("getRecLevelMax");

        Place instance = new Place("MyPlace", 0, 0, layer);
        int expResult = -1; // default value
        int result = instance.getRecLevelMax();
        assertEquals(expResult, result);

        int expResult1 = 5;
        instance.setRecLevelMax(expResult1);
        int result1 = instance.getRecLevelMax();
        assertEquals(expResult1, result1);

        int expResult2 = -3;
        instance.setRecLevelMax(expResult2);
        int result2 = instance.getRecLevelMax();
        assertEquals(expResult2, result2);

        int expResult3 = 0;
        instance.setRecLevelMax(expResult3);
        int result3 = instance.getRecLevelMax();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of setRecLevelMax method, of class Place.
     */
    @Test
    public void testSetRecLevelMax() {
        System.out.println("setRecLevelMax");

        Place instance = new Place("MyPlace", 0, 0, layer);

        int recLevelMax1 = 0;
        instance.setRecLevelMax(recLevelMax1);
        int result1 = instance.getRecLevelMax();
        assertEquals(recLevelMax1, result1);

        int recLevelMax2 = 4;
        instance.setRecLevelMax(recLevelMax2);
        int result2 = instance.getRecLevelMax();
        assertEquals(recLevelMax2, result2);

        int recLevelMax3 = -1;
        instance.setRecLevelMax(recLevelMax3);
        int result3 = instance.getRecLevelMax();
        assertEquals(recLevelMax3, result3);
    }

    /**
     * Test of getRiskLevel method, of class Place.
     */
    @Test
    public void testGetRiskLevel() {
        System.out.println("getRiskLevel");

        Place instance = new Place("MyPlace", 0, 0, layer);
        // default value
        assertNull(instance.getRiskLevel());

        RiskLevel expResult = new RiskLevel("description", Color.yellow);
        instance.setRiskLevel(expResult);
        RiskLevel result = instance.getRiskLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRiskLevel method, of class Place.
     */
    @Test
    public void testSetRiskLevel() {
        System.out.println("setRiskLevel");

        Place instance = new Place("MyPlace", 0, 0, layer);

        RiskLevel risk_level = new RiskLevel("description", Color.yellow);
        instance.setRiskLevel(risk_level);
        RiskLevel result = instance.getRiskLevel();
        assertEquals(risk_level, result);

        // test null
        instance.setRiskLevel(null);
        result = instance.getRiskLevel();
        assertEquals(null, result);
    }

    /**
     * Test of addComment method, of class Place.
     */
    @Test
    public void testAddComment() {
        System.out.println("addComment");

        String comment1 = "My Comment";
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.addComment(comment1);
        assertEquals(1, instance.getComments().size());
        assertTrue(instance.getComments().contains(comment1));

        String comment2 = "Another comment";
        instance.addComment(comment2);
        assertEquals(2, instance.getComments().size());
        assertTrue(instance.getComments().contains(comment1));
        assertTrue(instance.getComments().contains(comment2));
    }

    /**
     * Test of deleteComments method, of class Place.
     */
    @Test
    public void testDeleteComments() {
        System.out.println("deleteComments");

        Place instance = new Place("MyPlace", 0, 0, layer);

        try {
            instance.deleteComments();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        String comment1 = "My Comment";
        String comment2 = "Another comment";
        instance.addComment(comment1);
        instance.addComment(comment2);
        assertEquals(2, instance.getComments().size());

        instance.deleteComments();
        assertEquals(0, instance.getComments().size());
    }

    /**
     * Test of getComments method, of class Place.
     */
    @Test
    public void testGetComments() {
        System.out.println("getComments");

        Place instance = new Place("MyPlace", 0, 0, layer);
        assertNotNull(instance.getComments());
        assertEquals(0, instance.getComments().size());

        String comment1 = "My Comment";
        instance.addComment(comment1);
        assertEquals(1, instance.getComments().size());
        assertTrue(instance.getComments().contains(comment1));

        String comment2 = "Another comment";
        instance.addComment(comment2);
        assertEquals(2, instance.getComments().size());
        assertTrue(instance.getComments().contains(comment1));
        assertTrue(instance.getComments().contains(comment2));
    }

    /**
     * Test of getCommentsString method, of class Place.
     */
    @Test
    public void testGetCommentsString() {
        System.out.println("getCommentsString");

        Place instance = new Place("MyPlace", 0, 0, layer);

        assertNotNull(instance.getCommentsString());
        assertEquals("", instance.getCommentsString());

        String comment1 = "My Comment";
        String comment2 = "Another comment";
        instance.addComment(comment1);
        instance.addComment(comment2);
        assertEquals(2, instance.getComments().size());

        String expResult = comment1 + "\n" + comment2;
        String result = instance.getCommentsString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getExit method, of class Place.
     */
    @Test
    public void testGetExit() {
        System.out.println("getExit");

        Place instance = new Place("MyPlace", 0, 0, layer);
        Place p2 = new Place("Other Place", 1, 0, layer);

        assertNull(instance.getExit("n"));

        String dir1 = "n";
        String dir2 = "s";
        Path p = new Path(p2, dir1, instance, dir2);
        instance.connectPath(p);
        Path result = instance.getExit(dir2);
        assertEquals(p, result);
    }

    /**
     * Test of getPaths method, of class Place.
     */
    @Test
    public void testGetPaths_Place() {
        System.out.println("getPaths");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place place1 = new Place("Another place", 1, 2, layer);

        HashSet<Path> result1 = instance.getPaths(place1);
        assertNotNull(result1);
        assertTrue(result1.isEmpty());

        Path p1 = new Path(instance, "n", place1, "s");
        Path p2 = new Path(place1, "n", instance, "s");
        instance.connectPath(p1);
        instance.connectPath(p2);

        HashSet<Path> result2 = instance.getPaths(place1);
        assertEquals(2, result2.size());
        assertTrue(result2.contains(p1));
        assertTrue(result2.contains(p2));

        HashSet<Path> result3 = instance.getPaths(null);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
    }

    /**
     * Test of getPaths method, of class Place.
     */
    @Test
    public void testGetPaths_0args() {
        System.out.println("getPaths");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place place = new Place("Another place", 1, 2, layer);

        HashSet<Path> result = instance.getPaths();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        Path p1 = new Path(instance, "n", place, "s");
        Path p2 = new Path(place, "n", instance, "s");
        instance.connectPath(p1);
        instance.connectPath(p2);

        result = instance.getPaths();
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    /**
     * Test of removePath method, of class Place.
     */
    @Test
    public void testRemovePath() {
        System.out.println("removePath");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place place = new Place("Another place", 1, 2, layer);

        // path does not exist in place
        try {
            instance.removePath(new Path(instance, "n", place, "s"));
        } catch(Exception e){
            // don't throw if path has already been removed
            fail();
        }

        // test null
        try {
            instance.removePath(null);
            fail();
        } catch(Exception e){
            // expected
        }

        Path p1 = new Path(instance, "n", place, "s");
        Path p2 = new Path(place, "n", instance, "s");
        instance.connectPath(p1);
        instance.connectPath(p2);

        HashSet<Path> result = instance.getPaths(place);
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));

        instance.removePath(p1);
        result = instance.getPaths(place);
        assertEquals(1, result.size());
        assertFalse(result.contains(p1));
        assertTrue(result.contains(p2));

        instance.removePath(p2);
        result = instance.getPaths(place);
        assertEquals(0, result.size());
        assertFalse(result.contains(p2));
    }

    /**
     * Test of connectPath method, of class Place.
     */
    @Test
    public void testConnectPath() {
        System.out.println("connectPath");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place place1 = new Place("Another place", 1, 2, layer);
        Place place2 = new Place("Another place", 1, 2, layer);

        assertTrue(instance.getPaths().isEmpty());

        Path p1 = new Path(instance, "n", place1, "s");
        instance.connectPath(p1);
        assertEquals(1, instance.getPaths().size());
        assertTrue(instance.getPaths().contains(p1));

        Path p2 = new Path(place1, "n", instance, "s");
        instance.connectPath(p2);
        assertEquals(2, instance.getPaths().size());
        assertTrue(instance.getPaths().contains(p1));
        assertTrue(instance.getPaths().contains(p2));

        // test null
        try {
            instance.connectPath(null);
            fail();
        } catch(RuntimeException e){
            // expected
        }
        assertEquals(2, instance.getPaths().size());

        // test other place is null
        Path p3 = new Path(null, "n", instance, "s");
        try {
            instance.connectPath(p3);
            fail();
        } catch(RuntimeException e){
            // expected
        }

        // test instance not in path
        Path p4 = new Path(place1, "n", place2, "s");
        try {
            instance.connectPath(p4);
            fail();
        } catch(RuntimeException e){
            // expected
        }

        // test instance to instance, different exits
        Path p5 = new Path(instance, "n", instance, "s");
        try {
            instance.connectPath(p5);
        } catch(RuntimeException e){
            fail();
        }

        // test instance to instance, same exit
        Path p6 = new Path(instance, "n", instance, "n");
        try {
            instance.connectPath(p6);
            fail();
        } catch(RuntimeException e){
            // expected
        }
    }

    /**
     * Test of getPathTo method, of class Place.
     */
    @Test
    public void testGetPathTo() {
        System.out.println("getPathTo");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place place = new Place("Another place", 1, 2, layer);

        Path p1 = new Path(instance, "n", place, "s");
        Path p2 = new Path(place, "e", instance, "w");
        instance.connectPath(p1);
        instance.connectPath(p2);

        Path result = instance.getPathTo("ne");
        assertNull(result);

        result = instance.getPathTo("n");
        assertNotNull(result);
        assertEquals(p1, result);

        result = instance.getPathTo("w");
        assertNotNull(result);
        assertEquals(p2, result);
    }

    /**
     * Test of getFlag method, of class Place.
     */
    @Test
    public void testGetFlag() {
        System.out.println("getFlag");

        Place instance = new Place("MyPlace", 0, 0, layer);

        String key = "abc";
        boolean state = false;
        instance.setFlag(key, state);
        boolean result = instance.getFlag(key);
        assertEquals(state, result);

        // test null
        assertFalse(instance.getFlag(null));
    }

    /**
     * Test of setFlag method, of class Place.
     */
    @Test
    public void testSetFlag() {
        System.out.println("setFlag");

        Place instance = new Place("MyPlace", 0, 0, layer);

        String key = "abc";
        boolean state1 = false;
        instance.setFlag(key, state1);
        boolean result1 = instance.getFlag(key);
        assertEquals(state1, result1);

        // change previously set value
        boolean state2 = true;
        instance.setFlag(key, state2);
        boolean result2 = instance.getFlag(key);
        assertEquals(state2, result2);

        // test null
        boolean state3 = true;
        instance.setFlag(null, state3);
    }

    /**
     * Test of getFlags method, of class Place.
     */
    @Test
    public void testGetFlags() {
        System.out.println("getFlags");

        Place instance = new Place("MyPlace", 0, 0, layer);
        TreeMap<String, Boolean> expResult = new TreeMap<>();
        instance.setFlag("a", true);
        expResult.put("a", true);
        instance.setFlag("b", false);
        expResult.put("b", false);
        instance.setFlag("foobar", true);
        expResult.put("foobar", true);
        TreeMap<String, Boolean> result = instance.getFlags();
        assertEquals(expResult, result);
    }

    /**
     * Test of connectChild method, of class Place.
     */
    @Test
    public void testConnectChild() {
        System.out.println("connectChild");

        Layer layer2 = new Layer(world);
        Place p1 = new Place("Child", 0, 0, layer2);
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.connectChild(p1);
        // test child
        assertEquals(1, p1.getParents().size());
        assertTrue(p1.getParents().contains(instance));
        // test parent
        assertEquals(1, instance.getChildren().size());
        assertTrue(instance.getChildren().contains(p1));

        // test with second place
        Layer layer3 = new Layer(world);
        Place p2 = new Place("Child", 0, 0, layer3);
        instance.connectChild(p2);
        // test child
        assertEquals(1, p2.getParents().size());
        assertTrue(p2.getParents().contains(instance));
        // test parent
        assertEquals(2, instance.getChildren().size());
        assertTrue(instance.getChildren().contains(p1));
        assertTrue(instance.getChildren().contains(p2));

        // test null
        try {
            instance.connectChild(null);
            fail();
        } catch(NullPointerException ex){
            // expected
        }
    }

    /**
     * Test of removeChild method, of class Place.
     */
    @Test
    public void testRemoveChild() {
        System.out.println("removeChild");

        Layer layer2 = new Layer(world);
        Layer layer3 = new Layer(world);
        Place p1 = new Place("Child", 0, 0, layer2);
        Place p2 = new Place("Child", 0, 0, layer3);
        Place instance = new Place("MyPlace", 0, 0, layer);
        instance.connectChild(p1);
        instance.connectChild(p2);
        // cross connection to to check that only one place is removed
        p1.connectChild(p2);

        // pre conditions
        assertEquals(2, instance.getChildren().size());
        assertEquals(1, p1.getParents().size());
        assertEquals(2, p2.getParents().size());

        // removePlace child p1 from instance
        instance.removeChild(p1);
        assertEquals(1, instance.getChildren().size());
        assertEquals(0, p1.getParents().size());
        assertEquals(2, p2.getParents().size());

        // removePlace child p2 from instance
        instance.removeChild(p2);
        assertEquals(0, instance.getChildren().size());
        assertEquals(0, p1.getParents().size());
        assertEquals(1, p2.getParents().size());

        // test null
    }

    /**
     * Test of getChildren method, of class Place.
     */
    @Test
    public void testGetChildren() {
        System.out.println("getChildren");

        Layer layer2 = new Layer(world);
        Layer layer3 = new Layer(world);
        Place p1 = new Place("Child", 0, 0, layer2);
        Place p2 = new Place("Child", 0, 0, layer3);
        Place instance = new Place("MyPlace", 0, 0, layer);

        assertNotNull(instance.getChildren());
        assertEquals(0, instance.getChildren().size());

        instance.connectChild(p1);
        instance.connectChild(p2);

        assertEquals(2, instance.getChildren().size());
        assertTrue(instance.getChildren().contains(p1));
        assertTrue(instance.getChildren().contains(p2));
    }

    /**
     * Test of getParents method, of class Place.
     */
    @Test
    public void testGetParents() {
        System.out.println("getParents");

        Layer layer2 = new Layer(world);
        Layer layer3 = new Layer(world);
        Place p1 = new Place("Child", 0, 0, layer2);
        Place p2 = new Place("Child", 0, 0, layer3);
        Place instance = new Place("MyPlace", 0, 0, layer);

        assertNotNull(instance.getParents());
        assertEquals(0, instance.getParents().size());

        p1.connectChild(instance);
        assertEquals(1, instance.getParents().size());
        assertTrue(instance.getParents().contains(p1));

        p2.connectChild(instance);
        assertEquals(2, instance.getParents().size());
        assertTrue(instance.getParents().contains(p1));
        assertTrue(instance.getParents().contains(p2));
    }

    /**
     * Test of toString method, of class Place.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        Place instance = new Place("MyPlace", 0, 0, layer);
        String expResult = instance.getName() + " (ID: " + instance.getId() + ")";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of compareTo method, of class Place.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place p1 = new Place("MyPlace", 0, 0, layer);
        Place p2 = new Place("Another Place", -1, 2, layer);

        try {
            instance.compareTo(null);
            fail();
        } catch (Exception e) {}

        int expResult = instance.getName().compareTo(p1.getName());
        int result = instance.compareTo(p1);
        assertEquals(expResult, result);

        expResult = instance.getName().compareTo(p2.getName());
        result = instance.compareTo(p2);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeConnections method, of class Place.
     */
    @Test
    public void testRemoveConnections() {
        System.out.println("removeConnections");

        Place instance = new Place("MyPlace", 1, 1, layer);
        Place p1 = new Place("MyPlace", 0, 0, layer);
        Place p2 = new Place("Another Place", -1, 2, layer);
        Place p3 = new Place("Another Place", 1, 2, layer);

        p2.connectChild(instance);
        instance.connectChild(p3);
        instance.connectPath(new Path(instance, "n", p2, "s"));

        assertEquals(1, instance.getChildren().size());
        assertEquals(1, instance.getParents().size());
        assertEquals(1, instance.getPaths().size());

        instance.removeConnections();

        assertEquals(0, instance.getChildren().size());
        assertEquals(0, instance.getParents().size());
        assertEquals(0, instance.getPaths().size());
    }

    /**
     * Test of matchKeywords method, of class Place.
     */
    @Test
    public void testMatchKeywords() {
        System.out.println("matchKeywords");

        Place instance = new Place("Sea near a forest", 1, 2, layer);
        instance.addComment("This is a small place");
        instance.addComment("Small house next the trees");

        String[] keywords = null;
        boolean result = instance.matchKeywords(keywords);
        assertFalse(result);

        keywords = new String[1];
        keywords[0] = "Forest";
        result = instance.matchKeywords(keywords);
        assertTrue(result);

        keywords = new String[1];
        keywords[0] = "car";
        result = instance.matchKeywords(keywords);
        assertFalse(result);

        keywords = new String[2];
        keywords[0] = "Forest";
        keywords[1] = "car";
        result = instance.matchKeywords(keywords);
        assertFalse(result);

        keywords = new String[1];
        keywords[0] = "small";
        result = instance.matchKeywords(keywords);
        assertTrue(result);
    }

    /**
     * Test of duplicate method, of class Place.
     */
    @Test
    public void testDuplicate() {
        System.out.println("duplicate");

        Place instance = new Place("MyPlace", 1, 2, layer);
        Place result = instance.duplicate();

        assertEquals(instance.getName(), result.getName());
        assertEquals(instance.getRiskLevel(), result.getRiskLevel());
        assertEquals(instance.getRecLevelMin(), result.getRecLevelMin());
        assertEquals(instance.getRecLevelMax(), result.getRecLevelMax());
        assertEquals(instance.getPlaceGroup(), result.getPlaceGroup());
        assertEquals(instance.getCommentsString(), result.getCommentsString());
        assertEquals(instance.getFlags().entrySet().toString(), result.getFlags().entrySet().toString());
    }

    /**
     * Test of breadthSearchReset method, of class Place.
     */
    @Test
    public void testBreadthSearchReset() {
        System.out.println("breadthSearchReset");

        Place instance = new Place("MyPlace", 1, 2, layer);
        Place place1 = new Place("MyPlace", 1, 3, layer);
        Place place2 = new Place("MyPlace", 1, 4, layer);

        instance.connectPath(new Path(instance, "n", place1, "s"));
        place1.connectPath(new Path(place1, "n", place2, "s"));

        world.breadthSearch(instance, place2);

        assertTrue(instance.getBreadthSearchData().marked);

        instance.breadthSearchReset();
        assertFalse(instance.getBreadthSearchData().marked);
    }

    /**
     * Test of getBreadthSearchData method, of class Place.
     */
    @Test
    public void testGetBreadthSearchData() {
        System.out.println("getBreadthSearchData");

        Place instance = new Place("MyPlace", 1, 2, layer);
        Place place1 = new Place("MyPlace", 1, 3, layer);
        Place place2 = new Place("MyPlace", 1, 4, layer);

        instance.connectPath(new Path(instance, "n", place1, "s"));
        place1.connectPath(new Path(place1, "n", place2, "s"));

        assertFalse(instance.getBreadthSearchData().marked);

        world.breadthSearch(instance, place2);

        assertTrue(instance.getBreadthSearchData().marked);
        assertNull(instance.getBreadthSearchData().predecessor);
        assertNotNull(place2.getBreadthSearchData().predecessor);
    }
}
