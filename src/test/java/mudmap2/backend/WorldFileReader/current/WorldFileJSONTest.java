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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.PlaceGroup;
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
     * Test of readFile and writeFile methods, of class WorldFileJSON.
     * @throws java.lang.Exception
     */
    @Test
    public void testReadWriteFile() throws Exception {
        System.out.println("readFile / writeFile");

        String worldName = "FooBar";
        World world = new World(worldName);

        Layer layer1 = world.getNewLayer();
        Layer layer2 = world.getNewLayer();
        layer1.setName("MyLayer");

        String placeNames[] = {"Foo", "Foo Bar", "Baz"};
        Integer placeX[] = {0, 1, 6};
        Integer placeY[] = {0, 0, 4};
        Place pl0 = new Place(placeNames[0], placeX[0], placeY[0], layer1);
        Place pl1 = new Place(placeNames[1], placeX[1], placeY[1], layer1);
        Place pl2 = new Place(placeNames[2], placeX[2], placeY[2], layer2);
        layer1.put(pl0);
        layer1.put(pl1);
        layer2.put(pl2);

        Path path0 = new Path(pl0, "n", pl1, "s");
        Path path1 = new Path(pl1, "e", pl0, "w");
        pl0.connectPath(path0);
        pl1.connectPath(path1);

        pl0.connectChild(pl2);

        String pgName0 = "myArea";
        String pgName1 = "my second area";
        Color pgCol0 = Color.orange;
        Color pgCol1 = Color.blue;
        PlaceGroup pg0 = new PlaceGroup(pgName0, pgCol0);
        PlaceGroup pg1 = new PlaceGroup(pgName1, pgCol1);
        pl0.setPlaceGroup(pg0);
        pl1.setPlaceGroup(pg1);

        String flag0 = "a";
        String flag1 = "cd e";
        String flag2 = "fgh";
        pl0.setFlag(flag0, true);
        pl0.setFlag(flag1, true);
        pl0.setFlag(flag2, false);

        String comment0 = "This is a test comment";
        pl0.setComments(comment0);

        String wfjFile = folder.getRoot() + "/wfj";
        WorldFileJSON instanceWriter = new WorldFileJSON(wfjFile);
        instanceWriter.writeFile(world);

        WorldFileJSON instanceReader = new WorldFileJSON(wfjFile);
        World result = instanceReader.readFile();

        assertEquals(worldName, result.getName());
        assertEquals(2, result.getLayers().size());

        int placeNum = 0;
        Layer layer1new = null;
        Layer layer2new = null;

        for(Layer layer: result.getLayers()){
            placeNum += layer.getPlaces().size();

            // find corresponding layers
            if(layer.getPlaces().size() == 1) layer2new = layer;
            else if(layer.getPlaces().size() == 2) layer1new = layer;
        }
        assertEquals(3, placeNum);
        assertNotNull(layer1new);
        assertNotNull(layer2new);

        Place pl0r = layer1new.get(placeX[0], placeY[0]);
        Place pl1r = layer1new.get(placeX[1], placeY[1]);
        Place pl2r = layer2new.get(placeX[2], placeY[2]);
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

        assertEquals(pl0.getLayer().getName(), pl0r.getLayer().getName());
        assertTrue(pl0r.getLayer().hasName());
        assertEquals(pl1.getLayer().getName(), pl1r.getLayer().getName());
        assertFalse(pl2r.getLayer().hasName());

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

        HashSet<Path> pl0rn = pl0r.getPathsTo("n");
        HashSet<Path> pl0rw = pl0r.getPathsTo("w");
        HashSet<Path> pl1rs = pl1r.getPathsTo("s");
        HashSet<Path> pl1re = pl1r.getPathsTo("e");
        assertEquals(1, pl0rn.size());
        assertEquals(1, pl0rw.size());
        assertEquals(1, pl1rs.size());
        assertEquals(1, pl1re.size());
        assertEquals(pl0rn.toArray(new Path[1])[0], pl1rs.toArray(new Path[1])[0]);
        assertEquals(pl0rw.toArray(new Path[1])[0], pl1re.toArray(new Path[1])[0]);
        //assertEquals(pl0r.getPathTo("n"), pl1r.getPathTo("s"));
        //assertEquals(pl0r.getPathTo("w"), pl1r.getPathTo("e"));

        assertEquals(1, pl0r.getChildren().size());
        assertEquals(0, pl1r.getChildren().size());
        assertEquals(0, pl2r.getChildren().size());
        assertEquals(0, pl0r.getParents().size());
        assertEquals(0, pl1r.getParents().size());
        assertEquals(1, pl2r.getParents().size());
        assertEquals(pl0r.getChildren().toArray(new Place[1])[0], pl2r);
        assertEquals(pl2r.getParents().toArray(new Place[1])[0], pl0r);

        assertNotNull(pl0r.getPlaceGroup());
        assertNotNull(pl1r.getPlaceGroup());
        assertNull(pl2r.getPlaceGroup());
        assertEquals(pl0.getPlaceGroup().getName(), pl0r.getPlaceGroup().getName());
        assertEquals(pl1.getPlaceGroup().getName(), pl1r.getPlaceGroup().getName());

        assertEquals(2, pl0r.getFlags().size());
        assertEquals(0, pl1r.getFlags().size());
        assertEquals(0, pl2r.getFlags().size());
        assertTrue(pl0r.getFlag(flag0));
        assertTrue(pl0r.getFlag(flag1));
        assertFalse(pl0r.getFlag(flag2));

        assertEquals(comment0, pl0r.getComments());
        assertTrue(pl1r.getComments().isEmpty());
        assertTrue(pl2r.getComments().isEmpty());

        // TODO: test labels
    }

    /**
     * Test of backup method, of class WorldFileJSON.
     * @throws java.io.IOException
     */
    @Test
    public void testBackup() throws IOException {
        System.out.println("backup");

        try {
            String wfjFile = folder.getRoot() + "/wfj";
            WorldFileJSON instance = new WorldFileJSON(wfjFile);
            // don't write world
            //instance.writeFile(new World("Foo Bar"));

            instance.backup();

            File f1 = new File(wfjFile);
            File f2 = new File(wfjFile + ".bak");

            assertTrue(FileUtils.contentEquals(f1, f2));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        try {
            String wfjFile = folder.getRoot() + "/wfj";
            WorldFileJSON instance = new WorldFileJSON(wfjFile);
            // write world
            instance.writeFile(new World("Foo Bar"));

            instance.backup();

            File f1 = new File(wfjFile);
            File f2 = new File(wfjFile + ".bak");

            assertTrue(FileUtils.contentEquals(f1, f2));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of readWorldName method, of class WorldFileJSON.
     */
    @Test
    public void testReadWorldName() {
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
        try {
            String result = instance.readWorldName();
            assertEquals(worldName, result);
        } catch (Exception ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of canRead method, of class WorldFileJSON.
     */
    @Test
    public void testCanRead() {
        System.out.println("canRead");

        World world = new World("foobar");

        String wfjFile = folder.getRoot() + "/wfj";

        try {
            WorldFileJSON wfj = new WorldFileJSON(wfjFile);
            wfj.writeFile(world);
        } catch (IOException ex) {
            Logger.getLogger(WorldFileJSONTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Could not create files for test");
        }

        WorldFileJSON instancewfj = new WorldFileJSON(wfjFile);
        Boolean result = instancewfj.canRead();
        assertTrue(result);
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
            fail();
        }
    }

}
