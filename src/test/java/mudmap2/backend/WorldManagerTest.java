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
package mudmap2.backend;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
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
public class WorldManagerTest {

    File file;

    public WorldManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            // create temporary world file
            file = File.createTempFile("junit_mudmap_world", "");
            file.delete();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        file.delete();
    }

    /**
     * Test of register method, of class WorldManager.
     */
    @Test
    public void testRegister() {
        System.out.println("register");
        try {
            // access internal list
            Field fieldLoadedWorlds = WorldManager.class.getDeclaredField("loadedWorlds");
            fieldLoadedWorlds.setAccessible(true);

            HashSet<World> loadedWorlds = (HashSet<World>) fieldLoadedWorlds.get(null);

            World world = new World("MyWorld");
            world.setWorldFile(new WorldFileDefault(file.getAbsolutePath()));
            world.getWorldFile().writeFile(world);

            assertFalse(loadedWorlds.contains(world));

            WorldManager.register(world);
            assertTrue(loadedWorlds.contains(world));
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of close method, of class WorldManager.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        try {
            // access internal list
            Field fieldLoadedWorlds = WorldManager.class.getDeclaredField("loadedWorlds");
            fieldLoadedWorlds.setAccessible(true);

            HashSet<World> loadedWorlds = (HashSet<World>) fieldLoadedWorlds.get(null);

            World world = new World("MyWorld");
            world.setWorldFile(new WorldFileDefault(file.getAbsolutePath()));
            world.getWorldFile().writeFile(world);

            WorldManager.register(world);
            assertTrue(loadedWorlds.contains(world));

            WorldManager.close(world);
            assertFalse(loadedWorlds.contains(world));
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getWorld method, of class WorldManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetWorld() throws Exception {
        System.out.println("getWorld");

        // create temporary world
        World world = new World("MyWorld");
        world.setWorldFile(new WorldFileDefault(file.getAbsolutePath()));
        world.getWorldFile().writeFile(world);

        WorldManager.register(world);
        // expecting registered world
        assertSame(world, WorldManager.getWorld(world.getWorldFile().getFilename()));

        WorldManager.close(world);
        // expecting new world
        assertNotSame(world, WorldManager.getWorld(world.getWorldFile().getFilename()));
    }

    /**
     * Test of getNewWorld method, of class WorldManager.
     */
    @Test
    public void testGetNewWorld() {
        System.out.println("getNewWorld");

        final String name = "MyWorld";
        World result = WorldManager.getNewWorld(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

}
