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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.Environment;
import org.json.JSONArray;
import org.json.JSONObject;
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
public class WorldFileListTest {

    static File file1, file2, file3;

    public WorldFileListTest() {
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
            file1 = File.createTempFile("junit_mudmap_world", "");
            file2 = File.createTempFile("junit_mudmap_world", "");
            file3 = File.createTempFile("junit_mudmap_world", "");
        } catch (IOException ex) {
            Logger.getLogger(WorldFileListTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @After
    public void tearDown() {
        file1.delete();
        file2.delete();
        file3.delete();
    }

    /**
     * Test of push method, of class WorldFileList.
     */
    @Test
    public void testPush() {
        System.out.println("push");

        try {
            WorldFileList.WorldFileEntry entry1 = new WorldFileList.WorldFileEntry("World1", file1);
            WorldFileList.push(entry1);
            assertTrue(WorldFileList.getEntries().contains(entry1));

            WorldFileList.WorldFileEntry entry2 = new WorldFileList.WorldFileEntry("World2", file2);
            WorldFileList.push(entry2);
            assertTrue(WorldFileList.getEntries().contains(entry2));

            WorldFileList.WorldFileEntry entry3 = new WorldFileList.WorldFileEntry("World3", file3);
            WorldFileList.push(entry3);
            assertTrue(WorldFileList.getEntries().contains(entry3));

            // same file as entry2
            WorldFileList.WorldFileEntry entry4 = new WorldFileList.WorldFileEntry("World4", file2);
            WorldFileList.push(entry4);
            assertTrue(WorldFileList.getEntries().contains(entry4));
            assertFalse(WorldFileList.getEntries().contains(entry2));

            // test null
            int sizeBefore = WorldFileList.getEntries().size();
            WorldFileList.push(null);
            assertEquals(sizeBefore, WorldFileList.getEntries().size());
        } catch (SecurityException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of get method, of class WorldFileList.
     */
    @Test
    public void testGet() {
        System.out.println("get");

        WorldFileList.WorldFileEntry entry1 = new WorldFileList.WorldFileEntry("World1", file1);
        WorldFileList.WorldFileEntry entry2 = new WorldFileList.WorldFileEntry("World1", file2);
        WorldFileList.WorldFileEntry entry3 = new WorldFileList.WorldFileEntry("World1", file3);

        WorldFileList.push(entry1);
        WorldFileList.push(entry2);
        WorldFileList.push(entry3);

        WorldFileList.WorldFileEntry result1 = WorldFileList.get(file1);
        assertSame(entry1, result1);

        WorldFileList.WorldFileEntry result2 = WorldFileList.get(file2);
        assertSame(entry2, result2);

        WorldFileList.WorldFileEntry result3 = WorldFileList.get(file3);
        assertSame(entry3, result3);
    }

    /**
     * Test of getEntries method, of class WorldFileList.
     */
    @Test
    public void testGetEntries() {
        System.out.println("getEntries");

        WorldFileList.WorldFileEntry entry1 = new WorldFileList.WorldFileEntry("World1", file1);
        WorldFileList.WorldFileEntry entry2 = new WorldFileList.WorldFileEntry("World1", file2);
        WorldFileList.WorldFileEntry entry3 = new WorldFileList.WorldFileEntry("World1", file3);

        WorldFileList.push(entry1);
        WorldFileList.push(entry2);
        WorldFileList.push(entry3);

        LinkedList<WorldFileList.WorldFileEntry> result = WorldFileList.getEntries();
        assertNotNull(result);
        assertTrue(result.contains(entry1));
        assertTrue(result.contains(entry2));
        assertTrue(result.contains(entry3));
    }

    /**
     * Test of read method, of class WorldFileList.
     */
    @Test
    public void testRead() {
        System.out.println("read");
        try {
            File tempPath = File.createTempFile("junit_mudmap_filelist", "");
            tempPath.delete();
            tempPath.mkdirs();

            Environment.setUserDataDir(tempPath.getAbsolutePath());

            final String world1Name = "World1";
            final String world2Name = "My World";
            final String testString = "{\"ver\":\"2.0\",\"history\":[{\"file\":\"" +
                    file1.getAbsolutePath() + "\",\"name\":\"" + world1Name +
                    "\"},{\"file\":\"" + file2.getAbsolutePath() + "\",\"name\":\"" +
                    world2Name + "\"}]}";

            try {
                // write test file
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath.getAbsoluteFile() + "/history"));
                writer.write(testString);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                fail(ex.getMessage());
            }

            WorldFileList.read();
            LinkedList<WorldFileList.WorldFileEntry> entries = WorldFileList.getEntries();
            assertEquals(2, entries.size());

            File[] files = tempPath.listFiles();
            if(files != null){
                for(File f: files){
                    f.delete();
                }
            }
            tempPath.delete();
        } catch (IOException ex) {
            Logger.getLogger(WorldFileListTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of write method, of class WorldFileList.
     */
    @Test
    public void testWrite() {
        System.out.println("write");
        try {
            File tempPath = File.createTempFile("junit_mudmap_filelist", "");
            tempPath.delete();
            tempPath.mkdirs();

            Environment.setUserDataDir(tempPath.getAbsolutePath());
            WorldFileList.read(); // expected to fail, clears list

            WorldFileList.WorldFileEntry entry1 = new WorldFileList.WorldFileEntry("World1", file1);
            WorldFileList.WorldFileEntry entry2 = new WorldFileList.WorldFileEntry("World2", file2);
            WorldFileList.WorldFileEntry entry3 = new WorldFileList.WorldFileEntry("World3", file3);

            WorldFileList.push(entry1);
            WorldFileList.push(entry2);
            WorldFileList.push(entry3);

            WorldFileList.write();

            // check result

            File file = new File(tempPath.getAbsoluteFile() + File.separator + "history");
            JSONObject jRoot = null;
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                jRoot = new JSONObject(new String(bytes));
            } catch (IOException ex) {
                Logger.getLogger(WorldFileListTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(jRoot != null){
                assertTrue(jRoot.has("ver"));
                assertTrue(jRoot.has("history"));

                JSONArray jHistory = jRoot.getJSONArray("history");
                assertNotNull(jHistory);
                assertTrue(jHistory.length() >= 3);

                for(int i = 0; i < jHistory.length(); ++i){
                    JSONObject jElement = jHistory.getJSONObject(i);
                    assertNotNull(jElement);

                    assertTrue(jElement.has("file"));
                    assertTrue(jElement.has("name"));

                    final String elName = jElement.getString("name");
                    final String elFile = jElement.getString("file");
                    assertNotNull(elName);
                    assertNotNull(elFile);

                    if(elName.equals(entry1.getWorldName())){
                        assertEquals(elFile, entry1.getFile().getAbsolutePath());
                    } else if(elName.equals(entry2.getWorldName())){
                        assertEquals(elFile, entry2.getFile().getAbsolutePath());
                    } else if(elName.equals(entry3.getWorldName())){
                        assertEquals(elFile, entry3.getFile().getAbsolutePath());
                    } else {
                        fail("Read invalid history entry " + elName);
                    }
                }
            }

            File[] files = tempPath.listFiles();
            if(files != null){
                for(File f: files){
                    f.delete();
                }
            }
            tempPath.delete();
        } catch (IOException ex) {
            Logger.getLogger(WorldFileListTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
