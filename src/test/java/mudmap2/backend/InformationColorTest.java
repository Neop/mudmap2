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
public class InformationColorTest {

    public InformationColorTest() {
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
     * Test of constructor
     */
    @Test
    public void testInformationColor_2Args(){
        System.out.println("InformationColor");

        String desc = "description";

        InformationColor instance1 = new InformationColor(desc, Color.yellow);
        assertEquals(desc, instance1.getDescription());
        assertEquals(Color.yellow, instance1.getColor());

        // test description null
        InformationColor instance2 = new InformationColor(null, Color.yellow);
        assertEquals(instance1.getId()+1, instance2.getId());
        assertNull(instance2.getDescription());
        assertEquals(Color.yellow, instance2.getColor());

        // test color null
        InformationColor instance3 = new InformationColor(desc, null);
        assertEquals(instance2.getId()+1, instance3.getId());
        assertEquals(desc, instance3.getDescription());
        assertNull(instance3.getColor());
    }

    /**
     * Test of constructor
     */
    @Test
    public void testInformationColor_3Args(){
        System.out.println("InformationColor");

        String desc = "description";

        InformationColor instance1 = new InformationColor(5, desc, Color.yellow);
        assertEquals(5, instance1.getId());
        assertEquals(desc, instance1.getDescription());
        assertEquals(Color.yellow, instance1.getColor());

        // test description null
        InformationColor instance2 = new InformationColor(45, null, Color.yellow);
        assertEquals(45, instance2.getId());
        assertNull(instance2.getDescription());
        assertEquals(Color.yellow, instance2.getColor());

        // test color null
        InformationColor instance3 = new InformationColor(23, desc, null);
        assertEquals(23, instance3.getId());
        assertEquals(desc, instance1.getDescription());
        assertNull(instance3.getColor());
    }

    /**
     * Test of getId method, of class InformationColor.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");

        int expResult = 0;
        InformationColor instance1 = new InformationColor(expResult, "description", Color.yellow);
        int result1 = instance1.getId();
        assertEquals(expResult, result1);

        int expResult2 = 4;
        InformationColor instance2 = new InformationColor(expResult2, "description", Color.yellow);
        int result2 = instance2.getId();
        assertEquals(expResult2, result2);

        int expResult3 = -2;
        InformationColor instance3 = new InformationColor(expResult3, "description", Color.yellow);
        int result3 = instance3.getId();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getColor method, of class InformationColor.
     */
    @Test
    public void testGetColor() {
        System.out.println("getColor");

        Color expResult = Color.PINK;
        InformationColor instance = new InformationColor("description", expResult);
        Color result = instance.getColor();
        assertEquals(expResult, result);

        // test null
        InformationColor instance2 = new InformationColor("description", null);
        assertNull(instance2.getColor());
    }

    /**
     * Test of setColor method, of class InformationColor.
     */
    @Test
    public void testSetColor() {
        System.out.println("setColor");

        Color c = Color.GRAY;
        InformationColor instance = new InformationColor("description", c);

        instance.setColor(c);
        Color result = instance.getColor();
        assertEquals(c, result);

        // test set null
        instance.setColor(null);
        assertNull(instance.getColor());
    }

    /**
     * Test of getDescription method, of class InformationColor.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");

        String expResult = "description";
        InformationColor instance1 = new InformationColor(expResult, Color.yellow);
        String result1 = instance1.getDescription();
        assertEquals(expResult, result1);

        // test null
        InformationColor instance2 = new InformationColor(null, Color.yellow);
        assertNull(instance2.getDescription());
    }

    /**
     * Test of setDescription method, of class InformationColor.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");

        String desc = "new Text";
        InformationColor instance = new InformationColor("description", Color.yellow);

        instance.setDescription(desc);
        String result = instance.getDescription();
        assertEquals(desc, result);

        // test set null
        instance.setDescription(null);
        assertNull(instance.getDescription());
    }

    /**
     * Test of toString method, of class InformationColor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        String expResult = "description";
        InformationColor instance1 = new InformationColor(expResult, Color.yellow);
        String result1 = instance1.toString();
        assertEquals(expResult, result1);

        // test null
        InformationColor instance2 = new InformationColor(null, Color.yellow);
        String result2 = instance2.toString();
        assertEquals("", result2);
    }

}
