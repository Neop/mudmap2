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
package mudmap2.frontend.GUIElement.WorldPanel;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
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
public class WorldPanelTest {

    public WorldPanelTest() {
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
     * Test of getMappainter method, of class WorldPanel.
     */
    @Test
    public void testGetMappainter() {
        System.out.println("getMappainter");

        WorldPanel instance = new WorldPanel(null, new World(), false);
        assertNotNull(instance.getMappainter());
    }

    /**
     * Test of isPassive method, of class WorldPanel.
     */
    @Test
    public void testIsPassive() {
        System.out.println("isPassive");

        WorldPanel instance = new WorldPanel(null, new World(), false);
        assertFalse(instance.isPassive());

        instance = new WorldPanel(null, new World(), true);
        assertTrue(instance.isPassive());
    }

    /**
     * Test of isFocusForced / setFocusForced method, of class WorldPanel.
     */
    @Test
    public void testIsFocusForced() {
        System.out.println("isFocusForced");

        WorldPanel instance = new WorldPanel(null, new World(), false);

        try {
            Field fieldIsContextMenuShown = WorldPanel.class.getDeclaredField("isContextMenuShown");
            fieldIsContextMenuShown.setAccessible(true);

            fieldIsContextMenuShown.set(instance, false);

            // default
            assertFalse(instance.isFocusForced());

            instance.setFocusForced(true);
            assertTrue(instance.isFocusForced());

            instance.setFocusForced(false);
            assertFalse(instance.isFocusForced());

            // simulate context menu
            fieldIsContextMenuShown.set(instance, true);

            instance.setFocusForced(true);
            assertFalse(instance.isFocusForced());

            instance.setFocusForced(false);
            assertFalse(instance.isFocusForced());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getWorld method, of class WorldPanel.
     */
    @Test
    public void testGetWorld() {
        System.out.println("getWorld");

        World world = new World();

        WorldPanel instance = new WorldPanel(null, world, false);
        assertEquals(world, instance.getWorld());
    }

    /**
     * Test of gotoHome method, of class WorldPanel.
     */
    @Test
    public void testGotoHome() {
        System.out.println("gotoHome");

        World world = new World();
        WorldCoordinate home = new WorldCoordinate(5, 7, 11);
        world.setHome(home);

        WorldPanel instance = new WorldPanel(null, world, false);
        instance.gotoHome();

        WorldCoordinate position = instance.getPosition();

        assertEquals(home.getLayer(), position.getLayer());
        assertEquals(home.getX(), position.getX(), 0.01);
        assertEquals(home.getY(), position.getY(), 0.01);
    }

    /**
     * Test of setHome method, of class WorldPanel.
     */
    @Test
    public void testSetHome() {
        System.out.println("setHome");

        World world = new World();
        WorldCoordinate coord = new WorldCoordinate(1, 6, 9);

        WorldPanel instance = new WorldPanel(null, world, false);
        instance.pushPosition(coord);
        coord = instance.getPosition(); // might be changed by moveScreenToCursor()
        instance.setHome();

        assertEquals(coord.getLayer(), world.getHome().getLayer());
        assertEquals(coord.getX(), world.getHome().getX(), 0.01);
        assertEquals(coord.getY(), world.getHome().getY(), 0.01);
    }

    /**
     * Test of getSelectedPlace method, of class WorldPanel.
     */
    @Test
    public void testGetSelectedPlace() {
        try {
            System.out.println("getSelectedPlace");

            World world = new World();
            Layer l = world.getNewLayer();
            Place pl1 = new Place("BLa", 4, 6, l);
            l.put(pl1);
            Place pl2 = new Place("Blub", 7, 1, l);
            l.put(pl2);

            WorldPanel instance = new WorldPanel(null, world, false);
            instance.pushPosition(new WorldCoordinate(l.getId(), 0, 0));

            instance.setCursor(2, 3);
            assertNull(instance.getSelectedPlace());

            instance.setCursor(pl1.getX(), pl1.getY());
            assertEquals(pl1, instance.getSelectedPlace());

            instance.setCursor(pl2.getX(), pl2.getY());
            assertEquals(pl2, instance.getSelectedPlace());
        } catch (Layer.PlaceNotInsertedException ex) {
            Logger.getLogger(WorldPanelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of get/setTileSize method, of class WorldPanel.
     */
    @Test
    public void testGetTileSize() {
        System.out.println("getTileSize");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        double ts = 57.5;
        instance.setTileSize(ts);
        assertEquals(ts, instance.getTileSize(), 0.1);
    }

    /**
     * Test of tileSizeIncrement method, of class WorldPanel.
     */
    @Test
    public void testTileSizeIncrement() {
        System.out.println("tileSizeIncrement");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        double ts = 57.5;
        instance.setTileSize(ts);
        instance.tileSizeIncrement();
        assertTrue(ts < instance.getTileSize());
    }

    /**
     * Test of tileSizeDecrement method, of class WorldPanel.
     */
    @Test
    public void testTileSizeDecrement() {
        System.out.println("tileSizeDecrement");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        double ts = 57.5;
        instance.setTileSize(ts);
        instance.tileSizeDecrement();
        assertTrue(ts > instance.getTileSize());
    }

    /**
     * Test of push/pop/get/restore/resetPosition method, of class WorldPanel.
     */
    @Test
    @Ignore
    public void testPushPopPosition() {
        System.out.println("pushPosition");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        WorldCoordinate home = new WorldCoordinate(1, 2, 3);
        WorldCoordinate pos1 = new WorldCoordinate(4, 6, 1);
        WorldCoordinate pos2 = new WorldCoordinate(1, 2, 6);
        WorldCoordinate pos3 = new WorldCoordinate(1, 8, 4);

        world.setHome(home);

        assertTrue(home.compareTo(instance.getPosition()) == 0);

        instance.popPosition();
        assertTrue(home.compareTo(instance.getPosition()) == 0);

        instance.pushPosition(pos1);
        WorldCoordinate push1 = instance.getPosition();
        //assertTrue(pos1.compareTo(instance.getPosition()) == 0);

        instance.pushPosition(pos2);
        WorldCoordinate push2 = instance.getPosition();
        //assertTrue(pos2.compareTo(instance.getPosition()) == 0);

        instance.pushPosition(pos3);
        WorldCoordinate push3 = instance.getPosition();
        //assertTrue(pos3.compareTo(instance.getPosition()) == 0);

        instance.popPosition();
        assertTrue(push2.compareTo(instance.getPosition()) == 0);

        instance.popPosition();
        assertTrue(push1.compareTo(instance.getPosition()) == 0);

        instance.popPosition();
        assertTrue(home.compareTo(instance.getPosition()) == 0);

        instance.popPosition();
        assertTrue(home.compareTo(instance.getPosition()) == 0);

        instance.restorePosition();
        assertTrue(push1.compareTo(instance.getPosition()) == 0);

        instance.restorePosition();
        assertTrue(push2.compareTo(instance.getPosition()) == 0);

        instance.restorePosition();
        assertTrue(push3.compareTo(instance.getPosition()) == 0);

        instance.restorePosition();
        assertTrue(push3.compareTo(instance.getPosition()) == 0);

        WorldCoordinate reset = new WorldCoordinate(12, 1, 6);
        instance.resetHistory(reset);
        assertTrue(reset.compareTo(instance.getPosition()) == 0);
    }

    /**
     * Test of getCursorX method, of class WorldPanel.
     */
    @Test
    public void testGetCursorX() {
        System.out.println("getCursorX");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        instance.setCursor(4, 6);
        int result = instance.getCursorX();
        assertEquals(4, result);
    }

    /**
     * Test of get/setCursorY method, of class WorldPanel.
     */
    @Test
    public void testGetCursorY() {
        System.out.println("getCursorY");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        instance.setCursor(4, 6);
        int result = instance.getCursorY();
        assertEquals(6, result);
    }

    /**
     * Test of is/setCursorForced method, of class WorldPanel.
     */
    @Test
    public void testIsCursorForced() {
        System.out.println("isCursorForced");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        boolean result = instance.isCursorForced();
        assertFalse(result);

        instance.setCursorForced(true);
        result = instance.isCursorForced();
        assertTrue(result);

        instance.setCursorForced(false);
        result = instance.isCursorForced();
        assertFalse(result);
    }

    /**
     * Test of is/setCursorEnabled method, of class WorldPanel.
     */
    @Test
    public void testIsCursorEnabled() {
        System.out.println("isCursorEnabled");

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        boolean result = instance.isCursorEnabled();
        assertTrue(result);

        instance.setCursorEnabled(false);
        result = instance.isCursorEnabled();
        assertFalse(result);

        instance.setCursorEnabled(true);
        result = instance.isCursorEnabled();
        assertTrue(result);
    }

    /**
     * Test of moveCursor method, of class WorldPanel.
     */
    @Test
    public void testMoveCursor() {
        System.out.println("moveCursor");

        int dx = 9;
        int dy = -4;

        World world = new World();
        WorldPanel instance = new WorldPanel(null, world, false);

        instance.setCursor(4, 6);
        instance.moveCursor(4 + dx, 6 + dy);
    }

    /**
     * Test of moveScreenToCursor method, of class WorldPanel.
     */
    /*@Test
    public void testMoveScreenToCursor() {
        System.out.println("moveScreenToCursor");
        WorldPanel instance = null;
        instance.moveScreenToCursor();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of placeGroupHasSelection method, of class WorldPanel.
     */
    /*@Test
    public void testPlaceGroupHasSelection() {
        System.out.println("placeGroupHasSelection");
        WorldPanel instance = null;
        boolean expResult = false;
        boolean result = instance.placeGroupHasSelection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of placeGroupGetSelection method, of class WorldPanel.
     */
    /*@Test
    public void testPlaceGroupGetSelection() {
        System.out.println("placeGroupGetSelection");
        WorldPanel instance = null;
        HashSet<Place> expResult = null;
        HashSet<Place> result = instance.placeGroupGetSelection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of addPlaceSelectionListener method, of class WorldPanel.
     */
    /*@Test
    public void testAddPlaceSelectionListener() {
        System.out.println("addPlaceSelectionListener");
        PlaceSelectionListener listener = null;
        WorldPanel instance = null;
        instance.addPlaceSelectionListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of removePlaceSelectionListener method, of class WorldPanel.
     */
    /*@Test
    public void testRemovePlaceSelectionListener() {
        System.out.println("removePlaceSelectionListener");
        PlaceSelectionListener listener = null;
        WorldPanel instance = null;
        instance.removePlaceSelectionListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of callPlaceSelectionListeners method, of class WorldPanel.
     */
    /*@Test
    public void testCallPlaceSelectionListeners() {
        System.out.println("callPlaceSelectionListeners");
        Place place = null;
        WorldPanel instance = null;
        instance.callPlaceSelectionListeners(place);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of callPlaceDeselectionListeners method, of class WorldPanel.
     */
    /*@Test
    public void testCallPlaceDeselectionListeners() {
        System.out.println("callPlaceDeselectionListeners");
        Place place = null;
        WorldPanel instance = null;
        instance.callPlaceDeselectionListeners(place);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of addCursorListener method, of class WorldPanel.
     */
    /*@Test
    public void testAddCursorListener() {
        System.out.println("addCursorListener");
        MapCursorListener listener = null;
        WorldPanel instance = null;
        instance.addCursorListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of removeCursorListener method, of class WorldPanel.
     */
    /*@Test
    public void testRemoveCursorListener() {
        System.out.println("removeCursorListener");
        MapCursorListener listener = null;
        WorldPanel instance = null;
        instance.removeCursorListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of addTileSiteListener method, of class WorldPanel.
     */
    /*@Test
    public void testAddTileSiteListener() {
        System.out.println("addTileSiteListener");
        TileSizeListener listener = null;
        WorldPanel instance = null;
        instance.addTileSiteListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of removeTileSizeListener method, of class WorldPanel.
     */
    /*@Test
    public void testRemoveTileSizeListener() {
        System.out.println("removeTileSizeListener");
        TileSizeListener listener = null;
        WorldPanel instance = null;
        instance.removeTileSizeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of addStatusListener method, of class WorldPanel.
     */
    /*@Test
    public void testAddStatusListener() {
        System.out.println("addStatusListener");
        StatusListener listener = null;
        WorldPanel instance = null;
        instance.addStatusListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of callTileSizeListeners method, of class WorldPanel.
     */
    /*@Test
    public void testCallTileSizeListeners() {
        System.out.println("callTileSizeListeners");
        WorldPanel instance = null;
        instance.callTileSizeListeners();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of removeStatusListener method, of class WorldPanel.
     */
    /*@Test
    public void testRemoveStatusListener() {
        System.out.println("removeStatusListener");
        StatusListener listener = null;
        WorldPanel instance = null;
        instance.removeStatusListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of callStatusUpdateListeners method, of class WorldPanel.
     */
    /*@Test
    public void testCallStatusUpdateListeners() {
        System.out.println("callStatusUpdateListeners");
        WorldPanel instance = null;
        instance.callStatusUpdateListeners();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    /**
     * Test of callMessageListeners method, of class WorldPanel.
     */
    /*@Test
    public void testCallMessageListeners() {
        System.out.println("callMessageListeners");
        String message = "";
        WorldPanel instance = null;
        instance.callMessageListeners(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
}
