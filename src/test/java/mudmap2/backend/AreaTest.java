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
public class AreaTest {
    
    public AreaTest() {
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
     * Test of getId method, of class Area.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        
        int expResult = 5;
        Area instance = new Area(expResult, "My Area");
        int result = instance.getId();
        assertEquals(expResult, result);
        
        expResult = 0;
        instance = new Area(expResult, "My Area");
        result = instance.getId();
        assertEquals(expResult, result);
        
        expResult = -4;
        instance = new Area(expResult, "My Area");
        result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class Area.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        
        // test with both contructors
        String expResult = "MyArea";
        Area instance = new Area(expResult, Color.CYAN);
        String result = instance.getName();
        assertEquals(expResult, result);
        
        expResult = "Another area";
        instance = new Area(2, expResult);
        result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class Area.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        
        // test with both contructors
        Area instance = new Area("MyArea", Color.CYAN);
        
        String name = "Another area";
        instance.setName(name);
        String result = instance.getName();
        assertEquals(name, result);
    }

    /**
     * Test of getColor method, of class Area.
     */
    @Test
    public void testGetColor() {
        System.out.println("getColor");
        
        Color expResult = Color.CYAN;
        Area instance = new Area("MyArea", expResult);
        Color result = instance.getColor();
        assertEquals(expResult, result);
    }

    /**
     * Test of setColor method, of class Area.
     */
    @Test
    public void testSetColor() {
        System.out.println("setColor");
        
        Area instance = new Area("MyArea", Color.GREEN);
        
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
     * Test of toString method, of class Area.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        String name = "MyArea";
        Area instance = new Area(name, Color.yellow);
        String result = instance.toString();
        assertEquals(name, result);
    }

    /**
     * Test of compareTo method, of class Area.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        String name1 = "MyArea";
        String name2 = "MyArea";
        String name3 = "Another area";
        Area instance = new Area(name1, Color.yellow);
        Area area1 = new Area(name2, Color.BLACK);
        Area area2 = new Area(name3, Color.BLACK);
        
        int expResult = name1.compareTo(name2);
        int result = instance.compareTo(area1);
        assertEquals(expResult, result);
        
        expResult = name1.compareTo(name3);
        result = instance.compareTo(area2);
        assertEquals(expResult, result);
    }
    
}