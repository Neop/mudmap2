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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author neop
 */
public class PairTest {

    public PairTest() {
    }

    @Test
    public void testMethod() {
        Integer i1 = 123;
        Integer i2 = 345;
        Pair<Integer, Integer> instance = new Pair<>(i1, i2);

        assertEquals(i1, instance.first);
        assertEquals(i2, instance.second);
    }

}
