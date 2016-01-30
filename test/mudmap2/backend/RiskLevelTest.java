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
     * Test of getId method, of class RiskLevel.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        
        int expResult = 0;
        RiskLevel instance = new RiskLevel(expResult, "description", Color.yellow);
        int result = instance.getId();
        assertEquals(expResult, result);
        
        expResult = 4;
        instance = new RiskLevel(expResult, "description", Color.yellow);
        result = instance.getId();
        assertEquals(expResult, result);
        
        expResult = -2;
        instance = new RiskLevel(expResult, "description", Color.yellow);
        result = instance.getId();
        assertEquals(expResult, result);
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
    }

    /**
     * Test of getDescription method, of class RiskLevel.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        
        String expResult = "description";
        RiskLevel instance = new RiskLevel(expResult, Color.yellow);
        String result = instance.getDescription();
        assertEquals(expResult, result);
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
    }

    /**
     * Test of toString method, of class RiskLevel.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        String expResult = "description";
        RiskLevel instance = new RiskLevel(expResult, Color.yellow);
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
