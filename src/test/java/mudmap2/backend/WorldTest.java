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
     * Test of breadthSearch method, of class World.
     */
/*    @Test
    @Ignore // TODO: implement this test
    public void testBreadthSearch() {
        System.out.println("breadthSearch");
        Place start = null;
        Place end = null;
        World instance = null;
        Place expResult = null;
        Place result = instance.breadthSearch(start, end);
        //assertEquals(expResult, result);
        // TODO review the generated test code and removePlace the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of getLayer method, of class World.
     */
    @Test
    public void testGetLayer() {
        System.out.println("getLayer");

        World instance = new World("MyWorld");

        Layer result = instance.getLayer(3);
        assertNull(result);

        Layer l1 = new Layer(instance);
        Layer l2 = new Layer(instance);

        /*
        instance.addLayer(l1);
        instance.addLayer(l2);
        */

        result = instance.getLayer(l1.getId());
        assertEquals(l1, result);
        result = instance.getLayer(l2.getId());
        assertEquals(l2, result);
    }

    /**
     * Test of addLayer method, of class World.
     */
    @Test
    public void testSetLayer() {
        System.out.println("setLayer");

        World instance = new World("MyWorld");
        Layer l1 = new Layer(instance);
        Layer l2 = new Layer(instance);
        instance.addLayer(l1);
        instance.addLayer(l2);

        Layer result = instance.getLayer(l1.getId());
        assertEquals(l1, result);
        result = instance.getLayer(l2.getId());
        assertEquals(l2, result);
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
     * Test of getName method, of class World.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        String name = "MyWorld";
        World instance = new World(name);
        String result = instance.getName();
        assertEquals(name, result);
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
    }

    /**
     * Test of getHome method, of class World.
     */
    @Test
    public void testGetHome() {
        System.out.println("getHome");

        World instance = new World("MyWorld");
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

        World instance = new World("MyWorld");
        int x = 5, y = 12;
        Layer l = instance.getNewLayer();
        instance.setHome(new WorldCoordinate(l.getId(), x, y));

        WorldCoordinate expResult = new WorldCoordinate(l.getId(), x, y);
        WorldCoordinate result = instance.getHome();
        assertTrue(expResult.compareTo(result) == 0);
    }

    /**
     * Test of getPathColor method, of class World.
     */
    @Test
    public void testGetPathColor_0args() {
        System.out.println("getPathColor");

        World instance = new World("MyWorld");
        Color expResult = instance.pathColorCardinal;
        Color result = instance.getPathColor();
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
    public void testGetPathColor_String() {
        System.out.println("getPathColor");

        String dir = "";
        World instance = new World("MyWorld");
        Color expResult = instance.pathColorNonCardinal;
        Color result = instance.getPathColor(dir);
        assertEquals(expResult, result);

        dir = "n";
        expResult = instance.pathColorCardinal;
        result = instance.getPathColor(dir);
        assertEquals(expResult, result);

        dir = "u";
        expResult = instance.pathColorNonCardinal;
        result = instance.getPathColor(dir);
        assertEquals(expResult, result);

        dir = "foo";
        expResult = instance.pathColorNonCardinal;
        result = instance.getPathColor(dir);
        assertEquals(expResult, result);
    }

    /**
     * Test of setPathColor method, of class World.
     */
    @Test
    public void testSetPathColor_String_Color() {
        System.out.println("setPathColor");

        String dir = "foo";
        Color color = Color.GRAY;
        World instance = new World("MyWorld");
        instance.setPathColor(dir, color);
        Color result = instance.getPathColor(dir);
        assertEquals(color, result);

        dir = "n";
        color = Color.RED;
        instance.setPathColor(dir, color);
        result = instance.getPathColor(dir);
        assertEquals(color, result);
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
    public void testSetPathColor_Color() {
        System.out.println("setPathColor");

        Color color = Color.GREEN;
        World instance = new World("MyWorld");
        instance.setPathColor(color);
        Color result = instance.getPathColor();
        assertEquals(color, result);
    }

    /**
     * Test of setPathColorNstd method, of class World.
     */
    @Test
    public void testSetPathColorNstd() {
        System.out.println("setPathColorNstd");

        Color color = Color.YELLOW;
        World instance = new World("MyWorld");
        instance.setPathColor(color);
        Color result = instance.getPathColor();
        assertEquals(color, result);
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
    }

    /**
     * Test of removeArea method, of class World.
     */
    @Test
    public void testRemoveArea() {
        System.out.println("removeArea");

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
        Layer l2 = new Layer(instance);
        Layer l3 = instance.getNewLayer();

        layers = instance.getLayers();
        assertEquals(3, layers.size());
        assertTrue(layers.contains(l1));
        assertTrue(layers.contains(l2));
        assertTrue(layers.contains(l3));
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
     * Test of addRiskLevel method, of class World.
     */
    @Test
    public void testAddRiskLevel() {
        System.out.println("addRiskLevel");

        RiskLevel rl = new RiskLevel(null, Color.yellow);
        World instance = new World("MyWorld");
        instance.addRiskLevel(rl);

        Collection<RiskLevel> result = instance.getRiskLevels();
        assertEquals(6, result.size());
        assertTrue(result.contains(rl));
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
        } catch (Exception ex) {}

        try {
            instance.addRiskLevel(rl);
            instance.removeRiskLevel(rl);
            Collection<RiskLevel> result = instance.getRiskLevels();
            assertEquals(5, result.size());
            assertFalse(result.contains(rl));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

}
