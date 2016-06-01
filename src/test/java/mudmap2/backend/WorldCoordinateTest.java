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
public class WorldCoordinateTest {

    public WorldCoordinateTest() {
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
     * Test of getLayer method, of class WorldCoordinate.
     */
    @Test
    public void testGetLayer() {
        System.out.println("getLayer");

        int layerId = 5;
        WorldCoordinate instance = new WorldCoordinate(layerId, 0, 0);
        int result = instance.getLayer();
        assertEquals(layerId, result);
    }

    /**
     * Test of setLayer method, of class WorldCoordinate.
     */
    @Test
    public void testSetLayer() {
        System.out.println("setLayer");

        int layerId = 5, layerId2 = 7;
        WorldCoordinate instance = new WorldCoordinate(layerId, 0, 0);
        instance.setLayer(layerId2);
        int result = instance.getLayer();
        assertEquals(layerId2, result);
    }

    /**
     * Test of getX method, of class WorldCoordinate.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        double result = instance.getX();
        assertEquals(x, result, 0.01);
    }

    /**
     * Test of getY method, of class WorldCoordinate.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        double result = instance.getY();
        assertEquals(y, result, 0.01);
    }

    /**
     * Test of setX method, of class WorldCoordinate.
     */
    @Test
    public void testSetX() {
        System.out.println("setX");

        int layerId = 5;
        double x = 4.3, newx = -2.4;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        instance.setX(newx);
        double result = instance.getX();
        assertEquals(newx, result, 0.01);
    }

    /**
     * Test of setY method, of class WorldCoordinate.
     */
    @Test
    public void testSetY() {
        System.out.println("setY");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2, newy = 4.7;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        instance.setY(newy);
        double result = instance.getY();
        assertEquals(newy, result, 0.01);
    }

    /**
     * Test of move method, of class WorldCoordinate.
     */
    @Test
    public void testMove() {
        System.out.println("move");

        int layerId = 5;
        double x = 4.3, dx = -2.4;
        double y = 5.2, dy = 3.5;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        instance.move(dx, dy);
        double resultx = instance.getX();
        double resulty = instance.getY();
        assertEquals(x + dx, resultx, 0.01);
        assertEquals(y + dy, resulty, 0.01);
    }

    /**
     * Test of toString method, of class WorldCoordinate.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        String expResult = layerId + " " + x + " " + y;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of clone method, of class WorldCoordinate.
     */
    @Test
    public void testClone() {
        System.out.println("clone");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);
        WorldCoordinate result = instance.clone();

        assertEquals(layerId, result.getLayer());
        assertEquals(x, result.getX(), 0.01);
        assertEquals(y, result.getY(), 0.01);
    }

    /**
     * Test of compareTo method, of class WorldCoordinate.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");

        int layerId = 5;
        double x = 4.3;
        double y = 5.2;
        WorldCoordinate instance = new WorldCoordinate(layerId, x, y);

        WorldCoordinate t1 = new WorldCoordinate(layerId, x, y);
        int result = instance.compareTo(t1);
        assertEquals(0, result);

        WorldCoordinate t2 = new WorldCoordinate(layerId, x + 1, y + 1);
        result = instance.compareTo(t2);
        assertTrue(result != 0);

        WorldCoordinate t3 = new WorldCoordinate(layerId, x - 3, y - 5);
        result = instance.compareTo(t2);
        assertTrue(result != 0);
    }

}
