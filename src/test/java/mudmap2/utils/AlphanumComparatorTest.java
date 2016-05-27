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
package mudmap2.utils;

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
public class AlphanumComparatorTest {

    public AlphanumComparatorTest() {
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
     * Test of compare method, of class AlphanumComparator.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");

        String s1 = "abc";
        String s2 = "abc";
        AlphanumComparator instance = new AlphanumComparator();
        int result = instance.compare(s1, s2);
        assertEquals(0, result);

        s1 = "abc";
        s2 = "bbc";
        instance = new AlphanumComparator();
        result = instance.compare(s1, s2);
        assertTrue(result < 0);

        s1 = "bbc";
        s2 = "abc";
        instance = new AlphanumComparator();
        result = instance.compare(s1, s2);
        assertTrue(result > 0);

        s1 = "abc1";
        s2 = "abc10";
        instance = new AlphanumComparator();
        result = instance.compare(s1, s2);
        assertTrue(result < 0);

        s1 = "abc10";
        s2 = "abc1";
        instance = new AlphanumComparator();
        result = instance.compare(s1, s2);
        assertTrue(result > 0);
    }

}
