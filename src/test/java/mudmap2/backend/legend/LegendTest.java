/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2018  Neop (email: mneop@web.de)
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
package mudmap2.backend.legend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import mudmap2.backend.Layer;
import mudmap2.backend.World;
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
public class LegendTest {

    World world;
    Layer layer;

    public LegendTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        world = new World();
        layer = world.getNewLayer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isIncludePathColors method, of class Legend.
     */
    @Test
    public void testIsIncludePathColors() {
        System.out.println("isIncludePathColors");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludePathColors());
    }

    /**
     * Test of setIncludePathColors method, of class Legend.
     */
    @Test
    public void testSetIncludePathColors() {
        System.out.println("setIncludePathColors");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludePathColors());

        instance.setIncludePathColors(true);
        assertTrue(instance.isIncludePathColors());

        instance.setIncludePathColors(false);
        assertFalse(instance.isIncludePathColors());
    }

    /**
     * Test of isIncludeRiskLevels method, of class Legend.
     */
    @Test
    public void testIsIncludeRiskLevels() {
        System.out.println("isIncludeRiskLevels");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludeRiskLevels());
    }

    /**
     * Test of setIncludeRiskLevels method, of class Legend.
     */
    @Test
    public void testSetIncludeRiskLevels() {
        System.out.println("setIncludeRiskLevels");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludeRiskLevels());

        instance.setIncludeRiskLevels(true);
        assertTrue(instance.isIncludeRiskLevels());

        instance.setIncludeRiskLevels(false);
        assertFalse(instance.isIncludeRiskLevels());
    }

    /**
     * Test of isIncludePlaceGroups method, of class Legend.
     */
    @Test
    public void testIsIncludePlaceGroups() {
        System.out.println("isIncludePlaceGroups");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludePlaceGroups());
    }

    /**
     * Test of setIncludePlaceGroups method, of class Legend.
     */
    @Test
    public void testSetIncludePlaceGroups() {
        System.out.println("setIncludePlaceGroups");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        assertFalse(instance.isIncludePlaceGroups());

        instance.setIncludePlaceGroups(true);
        assertTrue(instance.isIncludePlaceGroups());

        instance.setIncludePlaceGroups(false);
        assertFalse(instance.isIncludePlaceGroups());
    }

    /**
     * Test of getBackground method, of class Legend.
     */
    @Test
    public void testGetBackground() {
        System.out.println("getBackground");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);
        assertNotNull(instance.getBackground());
    }

    /**
     * Test of setBackgroundColor method, of class Legend.
     */
    @Test
    public void testSetBackgroundColor() {
        System.out.println("setBackgroundColor");

        Legend instance = new Legend(layer, Legend.Orientation.HORIZONTAL, 500);

        instance.setBackgroundColor(Color.ORANGE);
        assertEquals(Color.ORANGE, instance.getBackground());
    }

    /**
     * Test of generate method, of class Legend.
     */
    @Test
    public void testGenerate() {
        System.out.println("generate");

        int imageWidth = 500;
        int imageHeight = 700;

        Legend instance1 = new Legend(layer, Legend.Orientation.HORIZONTAL, imageWidth);
        try {
            BufferedImage result = instance1.generate();
            assertNotNull(result);
            assertEquals(imageWidth, result.getWidth());
        } catch(Legend.RenderException ex){
            fail(ex.getMessage());
        }

        Legend instance2 = new Legend(layer, Legend.Orientation.VERTICAL, imageHeight);
        try {
            BufferedImage result = instance2.generate();
            assertNotNull(result);
            assertEquals(imageHeight, result.getHeight());
        } catch(Legend.RenderException ex){
            fail(ex.getMessage());
        }
    }

}
