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
public class RiskLevelTest {

    public RiskLevelTest() {
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
    public void testRiskLevel_2Args(){
        System.out.println("RiskLevel");

        String desc = "description";

        RiskLevel instance1 = new RiskLevel(desc, Color.yellow);
        assertEquals(desc, instance1.getDescription());
        assertEquals(Color.yellow, instance1.getColor());

        // test description null
        RiskLevel instance2 = new RiskLevel(null, Color.yellow);
        assertEquals(instance1.getId()+1, instance2.getId());
        assertNull(instance2.getDescription());
        assertEquals(Color.yellow, instance2.getColor());

        // test color null
        RiskLevel instance3 = new RiskLevel(desc, null);
        assertEquals(instance2.getId()+1, instance3.getId());
        assertEquals(desc, instance3.getDescription());
        assertNull(instance3.getColor());
    }

    /**
     * Test of constructor
     */
    @Test
    public void testRiskLevel_3Args(){
        System.out.println("RiskLevel");

        String desc = "description";

        RiskLevel instance1 = new RiskLevel(5, desc, Color.yellow);
        assertEquals(5, instance1.getId());
        assertEquals(desc, instance1.getDescription());
        assertEquals(Color.yellow, instance1.getColor());

        // test description null
        RiskLevel instance2 = new RiskLevel(45, null, Color.yellow);
        assertEquals(45, instance2.getId());
        assertNull(instance2.getDescription());
        assertEquals(Color.yellow, instance2.getColor());

        // test color null
        RiskLevel instance3 = new RiskLevel(23, desc, null);
        assertEquals(23, instance3.getId());
        assertEquals(desc, instance1.getDescription());
        assertNull(instance3.getColor());
    }

    /**
     * Test of getId method, of class RiskLevel.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");

        int expResult = 0;
        RiskLevel instance1 = new RiskLevel(expResult, "description", Color.yellow);
        int result1 = instance1.getId();
        assertEquals(expResult, result1);

        int expResult2 = 4;
        RiskLevel instance2 = new RiskLevel(expResult2, "description", Color.yellow);
        int result2 = instance2.getId();
        assertEquals(expResult2, result2);

        int expResult3 = -2;
        RiskLevel instance3 = new RiskLevel(expResult3, "description", Color.yellow);
        int result3 = instance3.getId();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getColor method, of class RiskLevel.
     */
    @Test
    public void testGetColor() {
        System.out.println("getColor");

        Color expResult = Color.PINK;
        RiskLevel instance = new RiskLevel("description", expResult);
        Color result = instance.getColor();
        assertEquals(expResult, result);

        // test null
        RiskLevel instance2 = new RiskLevel("description", null);
        assertNull(instance2.getColor());
    }

    /**
     * Test of setColor method, of class RiskLevel.
     */
    @Test
    public void testSetColor() {
        System.out.println("setColor");

        Color c = Color.GRAY;
        RiskLevel instance = new RiskLevel("description", c);

        instance.setColor(c);
        Color result = instance.getColor();
        assertEquals(c, result);

        // test set null
        instance.setColor(null);
        assertNull(instance.getColor());
    }

    /**
     * Test of getDescription method, of class RiskLevel.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");

        String expResult = "description";
        RiskLevel instance1 = new RiskLevel(expResult, Color.yellow);
        String result1 = instance1.getDescription();
        assertEquals(expResult, result1);

        // test null
        RiskLevel instance2 = new RiskLevel(null, Color.yellow);
        assertNull(instance2.getDescription());
    }

    /**
     * Test of setDescription method, of class RiskLevel.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");

        String desc = "new Text";
        RiskLevel instance = new RiskLevel("description", Color.yellow);

        instance.setDescription(desc);
        String result = instance.getDescription();
        assertEquals(desc, result);

        // test set null
        instance.setDescription(null);
        assertNull(instance.getDescription());
    }

    /**
     * Test of toString method, of class RiskLevel.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        String expResult = "description";
        RiskLevel instance1 = new RiskLevel(expResult, Color.yellow);
        String result1 = instance1.toString();
        assertEquals(expResult, result1);

        // test null
        RiskLevel instance2 = new RiskLevel(null, Color.yellow);
        String result2 = instance2.toString();
        assertEquals("", result2);
    }

}
