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
public class PathTest {

    static World world;
    Place[] places;

    public PathTest() {
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
        Layer layer = new Layer(world);
        places = new Place[3];
        places[0] = new Place("MyPlace", 1, 2, layer);
        places[1] = new Place("Other place", 3, -1, layer);
        places[2] = new Place("Third place", 5, 7, layer);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getPlaces method, of class Path.
     */
    @Test
    public void testGetPlaces() {
        System.out.println("getPlaces");

        Path instance = new Path(places[0], "n", places[1], "s");
        Place[] result = instance.getPlaces();
        Place[] expResult = {places[0], places[1]};
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of hasPlace method, of class Path.
     */
    @Test
    public void testHasPlace() {
        System.out.println("hasPlace");

        Path instance = new Path(places[0], "n", places[1], "s");
        assertTrue(instance.hasPlace(places[0]));
        assertTrue(instance.hasPlace(places[1]));
        assertFalse(instance.hasPlace(places[2]));
    }

    /**
     * Test of getExitDirections method, of class Path.
     */
    @Test
    public void testGetExitDirections() {
        System.out.println("getExitDirections");

        String dir1 = "n", dir2 = "s";
        Path instance = new Path(places[0], dir1, places[1], dir2);
        String[] expResult = {dir1, dir2};
        String[] result = instance.getExitDirections();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getExit method, of class Path.
     */
    @Test
    public void testGetExit() {
        System.out.println("getExit");

        String dir1 = "n", dir2 = "s";
        Path instance = new Path(places[0], dir1, places[1], dir2);
        String result = instance.getExit(places[0]);
        assertEquals(dir1, result);
        result = instance.getExit(places[1]);
        assertEquals(dir2, result);
    }

    /**
     * Test of getOtherPlace method, of class Path.
     */
    @Test
    public void testGetOtherPlace() {
        System.out.println("getOtherPlace");

        Path instance = new Path(places[0], "n", places[1], "s");
        Place result = instance.getOtherPlace(places[0]);
        assertEquals(places[1], result);
        result = instance.getOtherPlace(places[1]);
        assertEquals(places[0], result);
        try {
            instance.getOtherPlace(places[2]);
            fail();
        } catch(Exception ex){}
    }

    /**
     * Test of remove method, of class Path.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");

        Path instance = new Path(places[0], "n", places[1], "s");
        places[0].connectPath(instance);
        assertTrue(places[0].getPaths().contains(instance));
        assertTrue(places[1].getPaths().contains(instance));
        instance.remove();
        assertFalse(places[0].getPaths().contains(instance));
        assertFalse(places[1].getPaths().contains(instance));
    }

    /**
     * Test of getOppositeDir method, of class Path.
     */
    @Test
    public void testGetOppositeDir() {
        System.out.println("getOppositeDir");

        String dir = "n";
        String expResult = "s";
        String result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "s";
        expResult = "n";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "e";
        expResult = "w";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "w";
        expResult = "e";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "u";
        expResult = "d";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "d";
        expResult = "u";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "ne";
        expResult = "sw";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "nw";
        expResult = "se";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "se";
        expResult = "nw";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);

        dir = "sw";
        expResult = "ne";
        result = Path.getOppositeDir(dir);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDir method, of class Path.
     */
    @Test
    public void testGetDir() {
        System.out.println("getDir");

        int x = 0;
        int y = 0;
        String expResult = "";
        String result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = 0;
        y = 1;
        expResult = "n";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = 0;
        y = -1;
        expResult = "s";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = 1;
        y = 0;
        expResult = "e";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = -1;
        y = 0;
        expResult = "w";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = 1;
        y = 1;
        expResult = "ne";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = -1;
        y = 1;
        expResult = "nw";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = 1;
        y = -1;
        expResult = "se";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);

        x = -1;
        y = -1;
        expResult = "sw";
        result = Path.getDir(x, y);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDirNum method, of class Path.
     */
    @Test
    public void testGetDirNum() {
        System.out.println("getDirNum");

        String dir = ""; // invalid dir
        int expResult = -1;
        int result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "jkh"; // invalid dir
        expResult = -1;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "n";
        expResult = 8;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "ne";
        expResult = 9;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "e";
        expResult = 6;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "se";
        expResult = 3;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "s";
        expResult = 2;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "sw";
        expResult = 1;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "w";
        expResult = 4;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);

        dir = "nw";
        expResult = 7;
        result = Path.getDirNum(dir);
        assertEquals(expResult, result);
    }

}
