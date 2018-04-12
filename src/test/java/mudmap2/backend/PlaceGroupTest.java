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
public class PlaceGroupTest {

    public PlaceGroupTest() {
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
    public void testPlaceGroup(){
        System.out.println("PlaceGroup");

        PlaceGroup instance1 = new PlaceGroup("MyGroup");
        assertEquals("MyGroup", instance1.getName());
        assertEquals(Color.BLACK, instance1.getColor());

        PlaceGroup instance2 = new PlaceGroup("Another group", Color.ORANGE);
        assertEquals("Another group", instance2.getName());
        assertEquals(Color.ORANGE, instance2.getColor());

        PlaceGroup instance3 = new PlaceGroup(null);
        assertNull(instance3.getName());

        PlaceGroup instance4 = new PlaceGroup(null, null);
        assertNull(instance4.getName());
        assertNull(instance4.getColor());
    }

    /**
     * Test of getName method, of class PlaceGroup.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        // test with both contructors
        String expResult = "myGroup";
        PlaceGroup instance = new PlaceGroup(expResult, Color.CYAN);
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class PlaceGroup.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");

        // test with both contructors
        PlaceGroup instance = new PlaceGroup("myGroup", Color.CYAN);

        String name = "Another area";
        instance.setName(name);
        String result = instance.getName();
        assertEquals(name, result);
    }

    /**
     * Test of getColor method, of class PlaceGroup.
     */
    @Test
    public void testGetColor() {
        System.out.println("getColor");

        Color expResult = Color.CYAN;
        PlaceGroup instance = new PlaceGroup("myGroup", expResult);
        Color result = instance.getColor();
        assertEquals(expResult, result);
    }

    /**
     * Test of setColor method, of class PlaceGroup.
     */
    @Test
    public void testSetColor() {
        System.out.println("setColor");

        PlaceGroup instance = new PlaceGroup("myGroup", Color.GREEN);

        Color color = Color.RED;
        instance.setColor(color);
        Color result = instance.getColor();
        assertEquals(color, result);

        color = null;
        instance.setColor(color);
        result = instance.getColor();
        assertEquals(color, result);
    }

    /**
     * Test of toString method, of class PlaceGroup.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        String name = "myGroup";
        PlaceGroup instance = new PlaceGroup(name, Color.yellow);
        String result = instance.toString();
        assertEquals(name, result);
    }

    /**
     * Test of compareTo method, of class PlaceGroup.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");

        String name1 = "myGroup";
        String name2 = "myGroup";
        String name3 = "Another area";
        PlaceGroup instance = new PlaceGroup(name1, Color.yellow);
        PlaceGroup pg1 = new PlaceGroup(name2, Color.BLACK);
        PlaceGroup pg2 = new PlaceGroup(name3, Color.BLACK);

        int expResult = name1.compareTo(name2);
        int result = instance.compareTo(pg1);
        assertEquals(expResult, result);

        expResult = name1.compareTo(name3);
        result = instance.compareTo(pg2);
        assertEquals(expResult, result);
    }

}