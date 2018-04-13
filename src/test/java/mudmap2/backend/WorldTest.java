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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
import mudmap2.backend.sssp.BreadthSearch;
import org.json.JSONObject;
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
public class WorldTest {

    public WorldTest() {
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

    /**
     * Test of constructors
     */
    @Test
    public void testWorld(){
        System.out.println("World");

        World instance1 = new World();
        assertNotNull(instance1.getHome());
        assertNotNull(instance1.getLayers());
        assertNotNull(instance1.getName());
        assertEquals("unnamed", instance1.getName());
        assertNotNull(instance1.getPathColorStd());
        assertNotNull(instance1.getPathColorNstd());
        assertNotNull(instance1.getPathColors());
        assertNotNull(instance1.getPlaceGroups());
        assertNotNull(instance1.getPreferences());
        assertNotNull(instance1.getRiskLevels());
        assertNotNull(instance1.getTileCenterColor());
        assertNull(instance1.getWorldFile());
    }

    /**
     * Test of constructors
     */
    @Test
    public void testWorld_1Arg(){
        System.out.println("World");

        String worldName = "My World";

        World instance1 = new World(worldName);
        assertNotNull(instance1.getHome());
        assertNotNull(instance1.getLayers());
        assertNotNull(instance1.getName());
        assertEquals(worldName, instance1.getName());
        assertNotNull(instance1.getPathColorStd());
        assertNotNull(instance1.getPathColorNstd());
        assertNotNull(instance1.getPathColors());
        assertNotNull(instance1.getPlaceGroups());
        assertNotNull(instance1.getPreferences());
        assertNotNull(instance1.getRiskLevels());
        assertNotNull(instance1.getTileCenterColor());
        assertNull(instance1.getWorldFile());

        World instance2 = new World(null);
        assertNotNull(instance2.getHome());
        assertNotNull(instance2.getLayers());
        assertNotNull(instance2.getName());
        assertEquals("unnamed", instance2.getName());
        assertNotNull(instance2.getPathColorStd());
        assertNotNull(instance2.getPathColorNstd());
        assertNotNull(instance2.getPathColors());
        assertNotNull(instance2.getPlaceGroups());
        assertNotNull(instance2.getPreferences());
        assertNotNull(instance2.getRiskLevels());
        assertNotNull(instance2.getTileCenterColor());
        assertNull(instance2.getWorldFile());
    }

    /**
     * Test for getWorldFile
     */
    @Test
    public void testGetWorldFile(){
        System.out.println("getWorldFile");

        World instance = new World();
        assertNull(instance.getWorldFile());

        WorldFileDefault worldFile = new WorldFileDefault("/tmp/mudmap_junit");
        instance.setWorldFile(worldFile);
        assertEquals(worldFile, instance.getWorldFile());
    }

    /**
     * Test for setWorldFile
     */
    @Test
    public void testSetWorldFile(){
        System.out.println("setWorldFile");

        World instance = new World();
        assertNull(instance.getWorldFile());

        WorldFileDefault worldFile = new WorldFileDefault("/tmp/mudmap_junit");
        instance.setWorldFile(worldFile);
        assertEquals(worldFile, instance.getWorldFile());

        instance.setWorldFile(null);
        assertNull(instance.getWorldFile());
    }

    /**
     * Test of getName method, of class World.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        String name = "MyWorld";
        World instance1 = new World(name);
        String result = instance1.getName();
        assertEquals(name, result);

        World instance2 = new World();
        assertEquals("unnamed", instance2.getName());
    }

    /**
     * Test of setName method, of class World.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");

        String name = "MyWorld", newname = "New Name";
        World instance = new World(name);

        instance.setName(newname);
        String result = instance.getName();
        assertEquals(newname, result);

        instance.setName(null);
        assertEquals("unnamed", instance.getName());
    }

    /**
     * Test of getHome method, of class World.
     */
    @Test
    public void testGetHome() {
        System.out.println("getHome");

        World instance = new World();

        WorldCoordinate expResult = new WorldCoordinate(0, 0, 0);
        WorldCoordinate result = instance.getHome();
        assertTrue(expResult.compareTo(result) == 0);

        int x = 5, y = 12;
        Layer l = instance.getNewLayer();
        instance.setHome(new WorldCoordinate(l.getId(), x, y));

        expResult = new WorldCoordinate(l.getId(), x, y);
        result = instance.getHome();
        assertTrue(expResult.compareTo(result) == 0);
    }

    /**
     * Test of setHome method, of class World.
     */
    @Test
    public void testSetHome() {
        System.out.println("setHome");

        World instance = new World();
        int x = 5, y = 12;
        Layer l = instance.getNewLayer();
        instance.setHome(new WorldCoordinate(l.getId(), x, y));

        WorldCoordinate expResult = new WorldCoordinate(l.getId(), x, y);
        WorldCoordinate result = instance.getHome();
        assertTrue(expResult.compareTo(result) == 0);
    }

    /**
     * Test for putPlaceholder
     */
    @Test
    public void testSetPlaceholder(){
        System.out.println("putPlaceholder");

        World instance = new World();
        Layer layer = instance.getNewLayer();

        assertNull(layer.get(5, 7));

        instance.putPlaceholder(layer.getId(), 5, 7);
        assertNotNull(layer.get(5, 7));
    }

    /**
     * Test of getLayer method, of class World.
     */
    @Test
    public void testGetLayer() {
        System.out.println("getLayer");

        World instance = new World();

        Layer result = instance.getLayer(3);
        assertNull(result);

        Layer l1 = new Layer(instance);
        Layer l2 = new Layer(instance);

        instance.addLayer(l1);
        instance.addLayer(l2);

        result = instance.getLayer(l1.getId());
        assertEquals(l1, result);
        result = instance.getLayer(l2.getId());
        assertEquals(l2, result);
    }

    /**
     * Test of addLayer method, of class World.
     */
    @Test
    public void testAddLayer() {
        System.out.println("addLayer");

        World instance = new World();
        Layer layer1 = new Layer(instance);
        Layer layer2 = new Layer(instance);

        assertNull(instance.getLayer(layer1.getId()));
        assertNull(instance.getLayer(layer2.getId()));

        instance.addLayer(layer1);
        Layer result1 = instance.getLayer(layer1.getId());
        assertEquals(layer1, result1);

        instance.addLayer(layer2);

        Layer result2 = instance.getLayer(layer1.getId());
        assertEquals(layer1, result2);

        Layer result3 = instance.getLayer(layer2.getId());
        assertEquals(layer2, result3);

        // test null
        try {
            instance.addLayer(null);
            fail();
        } catch(NullPointerException ex){
            // expected
        }
    }

    /**
     * Test of getNewLayer method, of class World.
     */
    @Test
    public void testGetNewLayer_Name() {
        System.out.println("getNewLayer");

        World instance1 = new World();
        assertTrue(instance1.getLayers().isEmpty());

        instance1.getNewLayer("MyLayer");
        assertEquals(1, instance1.getLayers().size());

        Layer[] layers1 = instance1.getLayers().toArray(new Layer[1]);
        assertEquals("MyLayer", layers1[0].getName());

        // test null
        World instance2 = new World();
        assertTrue(instance2.getLayers().isEmpty());

        instance2.getNewLayer(null);
        assertEquals(1, instance2.getLayers().size());

        Layer[] layers2 = instance2.getLayers().toArray(new Layer[1]);
        assertFalse(layers2[0].hasName());
    }

    /**
     * Test of getNewLayer method, of class World.
     */
    @Test
    public void testGetNewLayer() {
        System.out.println("getNewLayer");

        World instance = new World("MyWorld");

        Layer result = instance.getNewLayer();
        assertNotNull(result);
        // check whether the new layer is actually in the list
        assertEquals(result, instance.getLayer(result.getId()));
    }

    /**
     * Test for getNextLayerID
     */
    @Test
    public void testGetNextLayerID(){
        System.out.println("getNextLayerID");

        World instance = new World();

        assertEquals((Integer) 1, instance.getNextLayerID());
        assertEquals((Integer) 2, instance.getNextLayerID());
        assertEquals((Integer) 3, instance.getNextLayerID());
        assertEquals((Integer) 4, instance.getNextLayerID());
    }

    /**
     * Test for setNextLayerID
     */
    @Test
    public void testSetNextLayerID(){
        System.out.println("setNextLayerID");

        World instance = new World();

        assertEquals((Integer) 1, instance.getNextLayerID());
        assertEquals((Integer) 2, instance.getNextLayerID());
        assertEquals((Integer) 3, instance.getNextLayerID());

        instance.setNextLayerID(17);
        assertEquals((Integer) 17, instance.getNextLayerID());
        assertEquals((Integer) 18, instance.getNextLayerID());
        assertEquals((Integer) 19, instance.getNextLayerID());
    }

    /**
     * Test of getLayers method, of class World.
     */
    @Test
    public void testGetLayers() {
        System.out.println("getLayers");

        World instance = new World("MyWorld");

        Collection<Layer> layers = instance.getLayers();
        assertNotNull(layers);
        assertTrue(layers.isEmpty());

        Layer l1 = new Layer(instance);
        instance.addLayer(l1);
        Layer l2 = new Layer(instance);
        instance.addLayer(l2);
        Layer l3 = instance.getNewLayer();

        layers = instance.getLayers();
        assertEquals(3, layers.size());
        assertTrue(layers.contains(l1));
        assertTrue(layers.contains(l2));
        assertTrue(layers.contains(l3));
    }

    /**
     * Test of getPathColorStd method, of class World.
     */
    @Test
    public void testGetPathColorStd() {
        System.out.println("getPathColorStd");

        World instance = new World("MyWorld");
        Color expResult = instance.pathColorCardinal;
        Color result = instance.getPathColorStd();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPathColorNstd method, of class World.
     */
    @Test
    public void testGetPathColorNstd() {
        System.out.println("getPathColorNstd");

        World instance = new World("MyWorld");
        Color expResult = instance.pathColorNonCardinal;
        Color result = instance.getPathColorNstd();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPathColor method, of class World.
     */
    @Test
    public void testGetPathColor() {
        System.out.println("getPathColor");

        World instance = new World("MyWorld");

        String dir1 = "";
        Color expResult1 = instance.pathColorNonCardinal;
        Color result1 = instance.getPathColor(dir1);
        assertEquals(expResult1, result1);

        String dir2 = "n";
        Color expResult2 = instance.pathColorCardinal;
        Color result2 = instance.getPathColor(dir2);
        assertEquals(expResult2, result2);

        String dir3 = "u";
        Color expResult3 = instance.pathColorNonCardinal;
        Color result3 = instance.getPathColor(dir3);
        assertEquals(expResult3, result3);

        String dir4 = "foo";
        Color expResult4 = instance.pathColorNonCardinal;
        Color result4 = instance.getPathColor(dir4);
        assertEquals(expResult4, result4);

        // test null
        assertEquals(instance.getPathColorStd(), instance.getPathColor(null));
    }

    /**
     * Test of setPathColor method, of class World.
     */
    @Test
    public void testSetPathColor() {
        System.out.println("setPathColor");

        String dir1 = "foo";
        Color color1 = Color.GRAY;
        World instance = new World("MyWorld");
        instance.setPathColor(dir1, color1);
        Color result1 = instance.getPathColor(dir1);
        assertEquals(color1, result1);

        String dir2 = "n";
        Color color2 = Color.RED;
        instance.setPathColor(dir2, color2);
        Color result2 = instance.getPathColor(dir2);
        assertEquals(color2, result2);

        // test null
        try {
            instance.setPathColor(null, Color.BLUE);
            fail();
        } catch(Exception ex){
            // expected
        }

        try {
            instance.setPathColor("n", null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test of getPathColors method, of class World.
     */
    @Test
    public void testGetPathColors() {
        System.out.println("getPathColors");

        World instance = new World("MyWorld");
        HashMap<String, Color> result = instance.getPathColors();
        assertNotNull(result);
        assertEquals(0, result.size());

        instance.setPathColor("foo", Color.yellow);
        result = instance.getPathColors();
        assertEquals(1, result.size());
    }

    /**
     * Test of setPathColor method, of class World.
     */
    @Test
    public void testSetPathColorStd() {
        System.out.println("setPathColor");

        Color color = Color.GREEN;
        World instance = new World("MyWorld");
        instance.setPathColorStd(color);
        Color result = instance.getPathColorStd();
        assertEquals(color, result);

        try {
            instance.setPathColorStd(null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test of setPathColorNstd method, of class World.
     */
    @Test
    public void testSetPathColorNstd() {
        System.out.println("setPathColorNstd");

        Color color = Color.YELLOW;
        World instance = new World("MyWorld");
        instance.setPathColorStd(color);
        Color result = instance.getPathColorStd();
        assertEquals(color, result);

        try {
            instance.setPathColorNstd(null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test of getTileCenterColor method, of class World.
     */
    @Test
    public void testGetTileCenterColor() {
        System.out.println("getTileCenterColor");

        World instance = new World("MyWorld");
        Color expResult = instance.tileCenterColor;
        Color result = instance.getTileCenterColor();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTileCenterColor method, of class World.
     */
    @Test
    public void testSetTileCenterColor() {
        System.out.println("setTileCenterColor");

        Color color = Color.MAGENTA;
        World instance = new World("MyWorld");
        instance.setTileCenterColor(color);
        assertEquals(color, instance.tileCenterColor);

        try {
            instance.setTileCenterColor(null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test of setShowPlaceID method, of class World.
     */
    @Test
    public void testSetShowPlaceID() {
        System.out.println("setShowPlaceID");

        World instance = new World("MyWorld");

        World.ShowPlaceID expResult = World.ShowPlaceID.NONE;
        instance.setShowPlaceID(expResult);
        World.ShowPlaceID result = instance.getShowPlaceId();
        assertEquals(expResult, result);

        expResult = World.ShowPlaceID.ALL;
        instance.setShowPlaceID(expResult);
        result = instance.getShowPlaceId();
        assertEquals(expResult, result);

        expResult = World.ShowPlaceID.UNIQUE;
        instance.setShowPlaceID(expResult);
        result = instance.getShowPlaceId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShowPlaceId method, of class World.
     */
    @Test
    public void testGetShowPlaceId() {
        System.out.println("getShowPlaceId");

        World instance = new World("MyWorld");
        World.ShowPlaceID expResult = World.ShowPlaceID.UNIQUE;
        World.ShowPlaceID result = instance.getShowPlaceId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPlaceGroups method, of class World.
     */
    @Test
    public void testGetPlaceGroups() {
        System.out.println("getPlaceGroups");

        World instance = new World("MyWorld");
        ArrayList<PlaceGroup> result = instance.getPlaceGroups();
        assertNotNull(result);
        assertEquals(0, result.size());

        PlaceGroup a1 = new PlaceGroup("myGroup", Color.yellow);
        PlaceGroup a2 = new PlaceGroup("Second Area", Color.yellow);
        instance.addPlaceGroup(a1);
        instance.addPlaceGroup(a2);

        result = instance.getPlaceGroups();
        assertEquals(2, result.size());
        assertTrue(result.contains(a1));
        assertTrue(result.contains(a2));
    }

    /**
     * Test of addPlaceGroup method, of class World.
     */
    @Test
    public void testAddPlaceGroup() {
        System.out.println("addPlaceGroup");

        World instance = new World("MyWorld");

        PlaceGroup a1 = new PlaceGroup("myGroup", Color.yellow);
        PlaceGroup a2 = new PlaceGroup("Second group", Color.yellow);
        instance.addPlaceGroup(a1);
        instance.addPlaceGroup(a2);

        assertTrue(instance.getPlaceGroups().contains(a1));
        assertTrue(instance.getPlaceGroups().contains(a2));

        // test null
        try {
            instance.addPlaceGroup(null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test of removePlaceGroup method, of class World.
     */
    @Test
    public void testRemovePlaceGroup() {
        System.out.println("removePlaceGroup");

        World instance = new World("MyWorld");

        PlaceGroup a1 = new PlaceGroup("myGroup", Color.yellow);
        PlaceGroup a2 = new PlaceGroup("Second Area", Color.yellow);
        instance.addPlaceGroup(a1);
        instance.addPlaceGroup(a2);

        instance.removePlaceGroup(a1);

        assertFalse(instance.getPlaceGroups().contains(a1));
        assertTrue(instance.getPlaceGroups().contains(a2));
    }

    /**
     * Test of getRiskLevels method, of class World.
     */
    @Test
    public void testGetRiskLevels() {
        System.out.println("getRiskLevels");

        World instance = new World("MyWorld");
        Collection<RiskLevel> result = instance.getRiskLevels();
        assertEquals(5, result.size());
    }

    /**
     * Test of getRiskLevel method, of class World.
     */
    @Test
    public void testGetRiskLevel() {
        System.out.println("getRiskLevel");

        int id = 1;
        World instance = new World("MyWorld");
        RiskLevel result = instance.getRiskLevel(id);
        assertNotNull(result);

        RiskLevel rl = new RiskLevel("MyRisklevel", Color.yellow);
        instance.addRiskLevel(rl);
        result = instance.getRiskLevel(rl.getId());
        assertEquals(rl, result);
    }

    /**
     * Test of addRiskLevel method, of class World.
     */
    @Test
    public void testAddRiskLevel() {
        System.out.println("addRiskLevel");

        World instance = new World("MyWorld");

        RiskLevel riskLevel1 = new RiskLevel(null, Color.yellow);
        instance.addRiskLevel(riskLevel1);
        Collection<RiskLevel> result1 = instance.getRiskLevels();
        assertEquals(6, result1.size());
        assertTrue(result1.contains(riskLevel1));

        // same id as first risk level, id will be changed to a unique value
        RiskLevel riskLevel2 = new RiskLevel(riskLevel1.getId(), "blub", Color.yellow);
        instance.addRiskLevel(riskLevel2);
        Collection<RiskLevel> result2 = instance.getRiskLevels();
        assertEquals(7, result2.size());
        assertTrue(result2.contains(riskLevel1));
        assertTrue(result2.contains(riskLevel2));
        assertEquals(riskLevel1.getId()+1, riskLevel2.getId());

        // test null
        try {
            instance.addRiskLevel(null);
            fail();
        } catch(Exception ex){
            // expected
        }
    }

    /**
     * Test for setRiskLevel
     */
    @Test
    public void testSetRiskLevel(){
        System.out.println("setRiskLevel");

        World instance = new World();

        RiskLevel riskLevel1 = new RiskLevel(17, "desc", Color.yellow);
        RiskLevel riskLevel2 = new RiskLevel(17, "desc", Color.red);

        instance.setRiskLevel(riskLevel1);
        assertEquals(riskLevel1, instance.getRiskLevel(17));

        instance.setRiskLevel(riskLevel2);
        assertEquals(riskLevel2, instance.getRiskLevel(17));
    }

    /**
     * Test of removeRiskLevel method, of class World.
     */
    @Test
    public void testRemoveRiskLevel() {
        System.out.println("removeRiskLevel");

        RiskLevel rl = new RiskLevel(null, Color.yellow);
        World instance = new World("MyWorld");

        try {
            instance.removeRiskLevel(rl); // rl not yet added
            fail();
        } catch (Exception ex) {
            // expected
        }

        try {
            instance.addRiskLevel(rl);
            instance.removeRiskLevel(rl);
            Collection<RiskLevel> result = instance.getRiskLevels();
            assertEquals(5, result.size());
            assertFalse(result.contains(rl));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        // test null
        try {
            instance.removeRiskLevel(null); // rl not yet added
        } catch (Exception ex) {
            fail();
        }
    }

    /**
     * Test for getPreferences
     */
    @Test
    public void testGetPreferences(){
        System.out.println("getPreferences");

        World instance = new World();

        assertNotNull(instance.getPreferences());
    }

    /**
     * Test for setPreferences
     */
    @Test
    public void testSetPreferences(){
        System.out.println("setPreferences");

        World instance = new World();
        JSONObject orig = instance.getPreferences();

        instance.setPreferences(new JSONObject());
        assertNotSame(orig, instance.getPreferences());
    }


    /**
     * Test of breadthSearch method, of class World.
     */
    @Test
    public void testBreadthSearch() {
        try {
            System.out.println("breadthSearch");

            World instance = new World();
            Layer layer1 = instance.getNewLayer();
            Layer layer2 = instance.getNewLayer();

            // create places
            Place place11 = new Place("", 0, 0, layer1);
            Place place12 = new Place("", 1, 0, layer1);
            Place place13 = new Place("", 2, 1, layer1);
            Place place14 = new Place("", 5, 1, layer1);
            // inaccessible place
            Place place15 = new Place("", 5, 4, layer1);

            layer1.put(place11);
            layer1.put(place12);
            layer1.put(place13);
            layer1.put(place14);
            layer1.put(place15);

            // create connections
            place11.connectPath(new Path(place11, "n", place12, "s"));
            place12.connectPath(new Path(place12, "e", place13, "w"));
            place13.connectPath(new Path(place13, "u", place14, "d"));
            place14.connectPath(new Path(place14, "u", place11, "d"));

            // first test 11 -> 11
            Place result1 = instance.breadthSearch(place11, place11);
            assertEquals(place11, result1);

            BreadthSearch.BreadthSearchData breadthSearchData1 = result1.getBreadthSearchData();
            assertNotNull(breadthSearchData1);
            assertNull(breadthSearchData1.predecessor);

            // second test 11 -> 12
            Place result2 = instance.breadthSearch(place11, place12);
            assertEquals(place12, result2);

            BreadthSearch.BreadthSearchData breadthSearchData2 = result2.getBreadthSearchData();
            assertNotNull(breadthSearchData2);
            assertEquals(place11, breadthSearchData2.predecessor);
            assertNull(breadthSearchData2.predecessor.breadthSearchData.predecessor);

            // second test 11 -> 13
            Place result3 = instance.breadthSearch(place11, place13);
            assertEquals(place13, result3);

            BreadthSearch.BreadthSearchData breadthSearchData3 = result3.getBreadthSearchData();
            assertNotNull(breadthSearchData3);

            assertTrue(place12 == breadthSearchData3.predecessor || place14 == breadthSearchData3.predecessor);
            breadthSearchData3 = breadthSearchData3.predecessor.getBreadthSearchData();
            assertNotNull(breadthSearchData3);

            assertEquals(place11, breadthSearchData3.predecessor);
            breadthSearchData3 = breadthSearchData3.predecessor.getBreadthSearchData();
            assertNull(breadthSearchData3.predecessor);

            // third test 11 -> 14
            Place result4 = instance.breadthSearch(place11, place14);
            assertEquals(place14, result4);

            BreadthSearch.BreadthSearchData breadthSearchData4 = result4.getBreadthSearchData();
            assertNotNull(breadthSearchData4);
            assertEquals(place11, breadthSearchData4.predecessor);
            assertNull(breadthSearchData4.predecessor.breadthSearchData.predecessor);

            // fourth test 11 -> 15
            Place result5 = instance.breadthSearch(place11, place15);
            assertNull(result5);
        } catch (Layer.PlaceNotInsertedException ex) {
            fail(ex.getMessage());
        }

    }

}
