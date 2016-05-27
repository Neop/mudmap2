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
public class LayerElementTest {

    static World world = null;
    Layer layer;

    public LayerElementTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        world = new World("Unittest");
    }

    @AfterClass
    public static void tearDownClass() {
        world = null;
    }

    @Before
    public void setUp() {
        layer = new Layer(world);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getX method, of class LayerElement.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");

        LayerElement instance = new LayerElement(0, 5, layer);
        int expResult = 0;
        int result = instance.getX();
        assertEquals(expResult, result);

        instance = new LayerElement(-2, -3, layer);
        expResult = -2;
        result = instance.getX();
        assertEquals(expResult, result);

        instance = new LayerElement(67, 0, layer);
        expResult = 67;
        result = instance.getX();
        assertEquals(expResult, result);
    }

    /**
     * Test of getY method, of class LayerElement.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");

        LayerElement instance = new LayerElement(2, 0, layer);
        int expResult = 0;
        int result = instance.getY();
        assertEquals(expResult, result);

        instance = new LayerElement(-2, -3, layer);
        expResult = -3;
        result = instance.getY();
        assertEquals(expResult, result);

        instance = new LayerElement(67, 45, layer);
        expResult = 45;
        result = instance.getY();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLayer method, of class LayerElement.
     */
    @Test
    public void testGetLayer() {
        System.out.println("getLayer");

        LayerElement instance = new LayerElement(0, 2, layer);
        Layer result = instance.getLayer();
        assertEquals(layer, result);
    }

    /**
     * Test of addLayer method, of class LayerElement.
     */
    @Test
    public void testSetLayer() {
        System.out.println("setLayer");

        Layer layer1 = new Layer(world);
        LayerElement instance = new LayerElement(0, 1, layer);
        assertEquals(layer, instance.getLayer());

        instance.setLayer(layer1);
        assertEquals(layer1, instance.getLayer());
    }

    /**
     * Test of setPosition method, of class LayerElement.
     */
    @Test
    public void testSetPosition() {
        System.out.println("setPosition");

        int x = 45;
        int y = -23;
        Layer layer1 = new Layer(world);

        LayerElement instance = new LayerElement(1, 2, layer);
        assertEquals(layer, instance.getLayer());

        instance.setPosition(x, y, layer1);
        assertEquals(layer1, instance.getLayer());
        assertEquals(x, instance.getX());
        assertEquals(y, instance.getY());
    }

}
