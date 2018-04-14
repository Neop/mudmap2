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
package mudmap2.frontend;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.World;
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
public class WorldTabTest {

    public WorldTabTest() {
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
     * Test of getWorldPanel method, of class WorldTab.
     */
    @Test
    public void testGetWorldPanel() {
        System.out.println("getWorldPanel");

        WorldTab instance = new WorldTab(null, new World(), false);
        assertNotNull(instance.getWorldPanel());

        instance = new WorldTab(null, new World(), true);
        assertNotNull(instance.getWorldPanel());

        instance = new WorldTab(null, new World(), "", false);
        assertNotNull(instance.getWorldPanel());

        instance = new WorldTab(null, new World(), "", true);
        assertNotNull(instance.getWorldPanel());

        instance = new WorldTab(instance);
        assertNotNull(instance.getWorldPanel());
    }

    /**
     * Test of getWorld method, of class WorldTab.
     */
    @Test
    public void testGetWorld() {
        System.out.println("getWorld");

        WorldTab instance = new WorldTab(null, new World(), false);
        assertNotNull(instance.getWorld());

        instance = new WorldTab(null, new World(), true);
        assertNotNull(instance.getWorld());

        instance = new WorldTab(null, new World(), "", false);
        assertNotNull(instance.getWorld());

        instance = new WorldTab(null, new World(), "", true);
        assertNotNull(instance.getWorld());

        instance = new WorldTab(instance);
        assertNotNull(instance.getWorld());
    }

    /**
     * Test of get/seFilename methods, of class WorldTab.
     */
    @Test
    public void testGetFilename() {
        System.out.println("getFilename");

        WorldTab instance = new WorldTab(null, new World(), false);

        String filename = "/tmp/foobar/baz";
        instance.setFilename(filename);

        assertEquals(filename, instance.getFilename());
    }

    /**
     * Test of save method, of class WorldTab.
     */
    @Test
    public void testSave() {
        try {
            System.out.println("save");

            File file = File.createTempFile("mudmap2_junit_WorldTabTest_testSave", "");
            file.delete();
            assertFalse(file.exists());

            WorldTab instance = new WorldTab(null, new World(), false);
            instance.setFilename(file.getAbsolutePath());
            instance.save();

            assertTrue(file.exists());
            file.delete();
            assertFalse(file.exists());
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of layerSelected method, of class WorldTab.
     */
    /*@Test
    public void testLayerSelected() {
        System.out.println("layerSelected");
        Layer layer = null;
        MouseEvent event = null;
        WorldTab instance = null;
        instance.layerSelected(layer, event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of createLayer method, of class WorldTab.
     */
    @Test
    @Ignore
    public void testCreateLayer() {
        System.out.println("createLayer");

        World world = new World();

        WorldTab instance = new WorldTab(null, world, false);

        int layerCnt = world.getLayers().size();

        instance.createLayer();

        assertEquals(layerCnt +1, world.getLayers().size());
    }

    /**
     * Test of placeSelected method, of class WorldTab.
     */
    /*@Test
    public void testPlaceSelected() {
        System.out.println("placeSelected");
        Place place = null;
        WorldTab instance = null;
        instance.placeSelected(place);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of messageReceived method, of class WorldTab.
     */
    /*@Test
    public void testMessageReceived() {
        System.out.println("messageReceived");
        String message = "";
        WorldTab instance = null;
        instance.messageReceived(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of statusUpdate method, of class WorldTab.
     */
    /*@Test
    public void testStatusUpdate() {
        System.out.println("statusUpdate");
        WorldTab instance = null;
        instance.statusUpdate();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of TileSizeChanged method, of class WorldTab.
     */
    /*@Test
    public void testTileSizeChanged() {
        System.out.println("TileSizeChanged");
        WorldTab instance = null;
        instance.TileSizeChanged();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getMeta method, of class WorldTab.
     */
    /*@Test
    public void testGetMeta() {
        System.out.println("getMeta");
        HashMap<Integer, Integer> layerTranslation = null;
        WorldTab instance = null;
        JSONObject expResult = null;
        JSONObject result = instance.getMeta(layerTranslation);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of readMeta method, of class WorldTab.
     */
    /*@Test
    public void testReadMeta() {
        System.out.println("readMeta");
        WorldTab instance = null;
        instance.readMeta();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of setMeta method, of class WorldTab.
     */
    /*@Test
    public void testSetMeta() {
        System.out.println("setMeta");
        JSONObject meta = null;
        WorldTab instance = null;
        instance.setMeta(meta);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

}
