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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.Area;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileReader.WorldFileType;
import org.apache.commons.io.FileUtils;
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
public class WorldFileMM1Test {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public WorldFileMM1Test() {
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
     * Test of getCompatibilityMudmap1 and setCompatibilityMudmap1 methods, of class WorldFileMM1.
     */
    @Test
    public void testGetCompatibilityMudmap1() {
        System.out.println("getCompatibilityMudmap1");

        String wfmm1File = folder.getRoot() + "/wfmm1";
        WorldFileMM1 instance = new WorldFileMM1(wfmm1File);

        instance.setCompatibilityMudmap1(true);
        assertTrue(instance.getCompatibilityMudmap1());

        instance.setCompatibilityMudmap1(false);
        assertFalse(instance.getCompatibilityMudmap1());
    }

    /**
     * Test of readFile and writeFile methods, of class WorldFileMM1.
     */
    @Test
    public void testReadWriteFile() throws Exception {
        System.out.println("readFile / writeFile");

        String worldName = "FooBar";
        World world = new World(worldName);

        Layer layer1 = world.getNewLayer();
        Layer layer2 = world.getNewLayer();

        String placeNames[] = {"Foo", "Foo Bar", "Baz"};
        Integer placeX[] = {0, 1, 6};
        Integer placeY[] = {0, 0, 4};
        Place pl0 = new Place(placeNames[0], placeX[0], placeY[0], layer1);
        Place pl1 = new Place(placeNames[1], placeX[1], placeY[1], layer1);
        Place pl2 = new Place(placeNames[2], placeX[2], placeY[2], layer2);
        world.put(pl0);
        world.put(pl1);
        world.put(pl2);

        Path path0 = new Path(pl0, "n", pl1, "s");
        Path path1 = new Path(pl1, "e", pl0, "w");
        pl0.connectPath(path0);
        pl1.connectPath(path1);

        pl0.connectChild(pl2);

        String areaName0 = "myArea";
        String areaName1 = "my second area";
        Color areaCol0 = Color.orange;
        Color areaCol1 = Color.blue;
        Area area0 = new Area(areaName0, areaCol0);
        Area area1 = new Area(areaName1, areaCol1);
        pl0.setArea(area0);
        pl1.setArea(area1);

        String flag0 = "a";
        String flag1 = "cd e";
        String flag2 = "fgh";
        pl0.setFlag(flag0, true);
        pl0.setFlag(flag1, true);
        pl0.setFlag(flag2, false);

        String comment0 = "This is a test comment";
        String comment1 = "and a second comment line";
        String comment2 = "and another one";
        pl0.addComment(comment0);
        pl0.addComment(comment1);
        pl0.addComment(comment2);

        String wfmm1File = folder.getRoot() + "/wfmm1";
        WorldFileMM1 instanceWriter = new WorldFileMM1(wfmm1File);
        instanceWriter.writeFile(world);

        WorldFileMM1 instanceReader = new WorldFileMM1(wfmm1File);
        World result = instanceReader.readFile();

        assertEquals(worldName, result.getName());
        assertEquals(3, result.getPlaces().size());
        assertEquals(2, result.getLayers().size());

        Place pl0r = result.getPlace(pl0.getId());
        Place pl1r = result.getPlace(pl1.getId());
        Place pl2r = result.getPlace(pl2.getId());
        assertNotNull(pl0r);
        assertNotNull(pl1r);
        assertNotNull(pl2r);
        assertEquals(pl0.getName(), pl0r.getName());
        assertEquals(pl1.getName(), pl1r.getName());
        assertEquals(pl2.getName(), pl2r.getName());
        assertEquals(pl0.getX(), pl0r.getX());
        assertEquals(pl0.getY(), pl0r.getY());
        assertEquals(pl1.getX(), pl1r.getX());
        assertEquals(pl1.getY(), pl1r.getY());
        assertEquals(pl2.getX(), pl2r.getX());
        assertEquals(pl2.getY(), pl2r.getY());
        assertEquals(pl0r.getLayer(), pl1r.getLayer());
        assertNotSame(pl0r.getLayer().getId(), pl2r.getLayer().getId());

        assertEquals(2, pl0r.getPaths().size());
        assertEquals(2, pl1r.getPaths().size());
        assertEquals(0, pl2r.getPaths().size());
        Path path0r = pl0r.getPaths().toArray(new Path[2])[0];
        Path path1r = pl0r.getPaths().toArray(new Path[2])[1];
        Path path2r = pl1r.getPaths().toArray(new Path[2])[0];
        Path path3r = pl1r.getPaths().toArray(new Path[2])[1];
        assertTrue(path0r != path1r);
        assertTrue(path0r == path2r || path0r == path3r);
        assertTrue(path1r == path2r || path1r == path3r);
        assertEquals(pl0r.getPathTo("n"), pl1r.getPathTo("s"));
        assertEquals(pl0r.getPathTo("w"), pl1r.getPathTo("e"));

        assertEquals(1, pl0r.getChildren().size());
        assertEquals(0, pl1r.getChildren().size());
        assertEquals(0, pl2r.getChildren().size());
        assertEquals(0, pl0r.getParents().size());
        assertEquals(0, pl1r.getParents().size());
        assertEquals(1, pl2r.getParents().size());
        assertEquals(pl0r.getChildren().toArray(new Place[1])[0], pl2r);
        assertEquals(pl2r.getParents().toArray(new Place[1])[0], pl0r);

        assertNotNull(pl0r.getArea());
        assertNotNull(pl1r.getArea());
        assertNull(pl2r.getArea());
        assertEquals(pl0.getArea().getName(), pl0r.getArea().getName());
        assertEquals(pl1.getArea().getName(), pl1r.getArea().getName());

        assertEquals(2, pl0r.getFlags().size());
        assertEquals(0, pl1r.getFlags().size());
        assertEquals(0, pl2r.getFlags().size());
        assertTrue(pl0r.getFlag(flag0));
        assertTrue(pl0r.getFlag(flag1));
        assertFalse(pl0r.getFlag(flag2));

        assertEquals(3, pl0r.getComments().size());
        assertEquals(0, pl1r.getComments().size());
        assertEquals(0, pl2r.getComments().size());
        assertTrue(pl0r.getComments().contains(comment0));
        assertTrue(pl0r.getComments().contains(comment1));
        assertTrue(pl0r.getComments().contains(comment2));
    }

    /**
     * Test of backup method, of class WorldFileMM1.
     * @throws java.io.IOException
     */
    @Test
    public void testBackup() throws IOException {
        System.out.println("backup");

        try {
            String wfmm1File = folder.getRoot() + "/wfmm1";
            WorldFileMM1 instance = new WorldFileMM1(wfmm1File);
            // don't write world
            //instance.writeFile(new World("Foo Bar"));

            instance.backup();

            File f1 = new File(wfmm1File);
            File f2 = new File(wfmm1File + ".bak");

            assertTrue(FileUtils.contentEquals(f1, f2));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        try {
            String wfmm1File = folder.getRoot() + "/wfmm1";
            WorldFileMM1 instance = new WorldFileMM1(wfmm1File);
            // write world
            instance.writeFile(new World("Foo Bar"));

            instance.backup();

            File f1 = new File(wfmm1File);
            File f2 = new File(wfmm1File + ".bak");

            assertTrue(FileUtils.contentEquals(f1, f2));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of readWorldName method, of class WorldFileMM1.
     */
    @Test
    public void testReadWorldName() throws Exception {
        System.out.println("readWorldName");

        System.out.println("readWorldName");

        String worldName = "foobar";
        World world = new World(worldName);
        String wfmm1File = folder.getRoot() + "/wfmm1";

        WorldFileMM1 wfmm1 = new WorldFileMM1(wfmm1File);
        wfmm1.writeFile(world);

        WorldFileMM1 instance = new WorldFileMM1(wfmm1File);
        try {
            String result = instance.readWorldName();
            assertEquals(worldName, result);
        } catch (Exception ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of canRead method, of class WorldFileMM1.
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

        WorldFileMM1 instancewfj = new WorldFileMM1(wfjFile);
        Boolean result = instancewfj.canRead();
        assertFalse(result);

        WorldFileMM1 instancewfm = new WorldFileMM1(wfmFile);
        result = instancewfm.canRead();
        assertTrue(result);
    }

    /**
     * Test of getWorldFileType method, of class WorldFileMM1.
     */
    @Test
    public void testGetWorldFileType() {
        System.out.println("getWorldFileType");

        WorldFileMM1 instance = new WorldFileMM1(null);
        WorldFileType result = instance.getWorldFileType();
        assertEquals(WorldFileType.MUDMAP1, result);
    }

}
