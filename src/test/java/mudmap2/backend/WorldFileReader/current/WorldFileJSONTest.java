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
package mudmap2.backend.WorldFileReader.current;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileReader.WorldFileType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author neop
 */
public class WorldFileJSONTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public WorldFileJSONTest() {
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
     * Test of readFile method, of class WorldFileJSON.
     */
    @Test
    public void testReadFile() throws Exception {
        System.out.println("readFile");
        WorldFileJSON instance = null;
        World expResult = null;
        World result = instance.readFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeFile method, of class WorldFileJSON.
     */
    @Test
    public void testWriteFile() throws Exception {
        System.out.println("writeFile");
        World world = null;
        WorldFileJSON instance = null;
        instance.writeFile(world);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of backup method, of class WorldFileJSON.
     */
    @Test
    public void testBackup() throws Exception {
        System.out.println("backup");
        WorldFileJSON instance = null;
        instance.backup();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readWorldName method, of class WorldFileJSON.
     */
    @Test
    public void testReadWorldName() throws Exception {
        System.out.println("readWorldName");

        String worldName = "foobar";
        World world = new World(worldName);
        String wfjFile = folder.getRoot() + "/wfj";

        try {
            WorldFileJSON wfj = new WorldFileJSON(wfjFile);
            wfj.writeFile(world);
        } catch (IOException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Could not create files for test");
        }

        WorldFileJSON instance = new WorldFileJSON(wfjFile);
        String result = instance.readWorldName();
        assertEquals(worldName, result);
    }

    /**
     * Test of canRead method, of class WorldFileJSON.
     */
    @Test
    public void testCanRead() {
        System.out.println("canRead");

        World world = new World("foobar");

        String wfjFile = folder.getRoot() + "/wfj";
        String wfmFile = folder.getRoot() + "/wfm";

        try {
            WorldFileJSON wfj = new WorldFileJSON(wfjFile);
            wfj.writeFile(world);
        } catch (IOException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Could not create files for test");
        }

        WorldFileMM1 wfm = new WorldFileMM1(wfmFile);
        wfm.writeFile(world);

        WorldFileJSON instancewfj = new WorldFileJSON(wfjFile);
        Boolean result = instancewfj.canRead();
        assertTrue(result);

        WorldFileJSON instancewfm = new WorldFileJSON(wfmFile);
        result = instancewfm.canRead();
        assertFalse(result);
    }

    /**
     * Test of getWorldFileType method, of class WorldFileJSON.
     */
    @Test
    public void testGetWorldFileType() {
        System.out.println("getWorldFileType");

        WorldFileJSON instance = new WorldFileJSON(null);
        WorldFileType result = instance.getWorldFileType();
        assertEquals(WorldFileType.JSON, result);
    }

    /**
     * Test of colToHex and hexToCol methods
     */
    @Test
    public void testColorConversion() {
        WorldFileJSON instance = new WorldFileJSON(null);
        try {
            Method colToHex = WorldFileJSON.class.getDeclaredMethod("colToHex", Color.class);
            Method hexToCol = WorldFileJSON.class.getDeclaredMethod("hexToCol", String.class);
            colToHex.setAccessible(true);
            hexToCol.setAccessible(true);

            Color colOrig = new Color(255, 128, 64);
            String hex = (String) colToHex.invoke(instance, colOrig);
            Color col = (Color) hexToCol.invoke(instance, hex);

            assertEquals("#ff8040", hex);
            assertEquals(colOrig.getRGB(), col.getRGB());

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
