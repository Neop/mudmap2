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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
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
public class LegendEntrySeparatorTest {

    final int imageWidth = 150;
    final int imageHeight = 50;

    Graphics g;

    public LegendEntrySeparatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        g = image.getGraphics();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getHeight method, of class LegendEntrySeparator.
     */
    @Test
    public void testGetHeight() {
        System.out.println("getHeight");

        final int imageWidth = 150;
        final int imageHeight = 50;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        LegendEntrySeparator instance = new LegendEntrySeparator();

        int expResult = LegendEntrySeparator.HEIGHT_REQUEST;
        int result = instance.getHeight(g);
        assertEquals(expResult, result);
    }

    /**
     * Test of getWidth method, of class LegendEntrySeparator.
     */
    @Test
    public void testGetWidth() {
        System.out.println("getWidth");

        final int imageWidth = 150;
        final int imageHeight = 50;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        LegendEntrySeparator instance = new LegendEntrySeparator();

        int expResult = LegendEntrySeparator.WIDTH_REQUEST;
        int result = instance.getWidth(g);
        assertEquals(expResult, result);
    }

    /**
     * Test of renderGraphic method, of class LegendEntrySeparator.
     */
    @Test
    public void testRenderGraphic() {
        System.out.println("renderGraphic");

        final int imageWidth = 150;
        final int imageHeight = 50;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        LegendEntrySeparator instance = new LegendEntrySeparator();

        int sx = 0;
        int sy = 0;
        int width = instance.getWidth(g);
        int height = instance.getHeight(g);

        assertTrue(imageWidth >= width);
        assertTrue(imageHeight >= height);

        try {
            instance.renderGraphic(g, sx, sy, width, height);
            // no way to check but expect no exceptions
        } catch (Exception ex){
            fail(ex.getMessage());
        }
    }

}
