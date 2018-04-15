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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.Environment;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author neop
 */
public class WorldFileList {

    static final int FILE_VER_MAJOR = 2;
    static final int FILE_VER_MINOR = 0;

    static final int MAX_HISTORY_ENTRIES = 15;

    private static final String HISTORY_FILENAME = "history";
    @Deprecated
    private static final String AVAILABLE_WORLDS_FILENAME = "worlds";


    // world file history <file, world name>
    private static final LinkedList<WorldFileEntry> worldFileHistory = new LinkedList<>();

    /**
     * Get File describing path and name of history file
     * @return file object
     */
    private static File getHistoryFile(){
        return new File(Environment.getUserDataDir() + File.separator + HISTORY_FILENAME);
    }

    /**
     * Get File describing path and name of available worlds file
     * @return file object
     */
    @Deprecated
    private static File getAvailableWorldsFile(){
        return new File(Environment.getWorldsDir() + File.separator + AVAILABLE_WORLDS_FILENAME);
    }

    /**
     * Add entry. Removes previous occurencies of the same file.
     * @param entry entry to add
     */
    public static void push(WorldFileEntry entry){
        if(entry != null){
            // remove equal entries
            LinkedList<WorldFileEntry> toBeRemoved = new LinkedList<>();

            for(WorldFileEntry it: worldFileHistory){
                if(it.getFile().equals(entry.getFile())){
                    toBeRemoved.push(it);
                }
            }

            for(WorldFileEntry it: toBeRemoved){
                worldFileHistory.remove(it);
            }

            // insert entry
            worldFileHistory.push(entry);
        }
    }

    /**
     * Get entry by file
     * @param file file
     * @return entry or null
     */
    public static WorldFileEntry get(File file){
        WorldFileEntry ret = null;
        // find entry
        for(WorldFileEntry entry: worldFileHistory){
            if(entry.getFile().equals(file)){
                ret = entry;
                break;
            }
        }
        return ret;
    }

    /**
     * Get limited list of entries
     * @return
     */
    public static LinkedList<WorldFileEntry> getEntries(){
        LinkedList<WorldFileEntry> ret = new LinkedList<>(worldFileHistory);

        while(ret.size() > MAX_HISTORY_ENTRIES){
            ret.removeLast();
        }

        return ret;
    }

    /**
     * Removes entries that do not exist in file system
     */
    private static void cleanList(){
        LinkedList<WorldFileEntry> toBeRemoved = new LinkedList<>();

        for(WorldFileEntry entry: worldFileHistory){
            if(!entry.getFile().exists()){
                toBeRemoved.add(entry);
            }
        }

        for(WorldFileEntry it: toBeRemoved){
            worldFileHistory.remove(it);
        }
    }

    // ------------------- list file handling ----------------------------------

    /**
     * Read history
     */
    public static void read(){
        final File historyFile = getHistoryFile();
        final File availableWorldsFile = getAvailableWorldsFile();

        boolean success = false;

        worldFileHistory.clear();

        // read history
        try {
            if(historyFile.exists() && historyFile.canRead()){
                readListJSON(historyFile);
                success = true;
            }
        } catch (Exception ex){
            Logger.getLogger(WorldFileList.class.getName()).log(Level.SEVERE, null, ex);
        }

        // fallback: legacy available worlds file
        if(!success){
            if(availableWorldsFile.exists() && availableWorldsFile.canRead()){
                readWorldList(availableWorldsFile);
            } else {
                System.err.println("No world history file or not readable");
            }
        }

        // remove nonexistant entries
        cleanList();
    }

    /**
     * Write history
     */
    public static void write(){
        final File historyFile = getHistoryFile();

        // remove nonexistant entries
        cleanList();

        // write history
        if(!historyFile.exists() || historyFile.canWrite()){
            writeListJSON(historyFile);
        } else {
            System.err.println("Could not write world history file "
                    + historyFile.getAbsolutePath());
        }
    }

    /**
     * Read JSON-formatted history file
     * @param file
     */
    private static void readListJSON(File file){
        JSONObject jRoot = null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            jRoot = new JSONObject(new String(bytes));
        } catch (IOException ex) {
            Logger.getLogger(WorldFileList.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(jRoot != null){
            jRoot.has("ver");
            String fileVersion = jRoot.getString("ver");

            Integer verMajor = 0, verMinor = 0;

            String[] split = fileVersion.split("\\.");
            if(split.length >= 2){
                verMajor = Integer.decode(split[0]);
                verMinor = Integer.decode(split[1]);
            }

            if(verMajor == FILE_VER_MAJOR && verMinor >= 0){
                if(jRoot.has("history")){
                    JSONArray jHistory = jRoot.getJSONArray("history");

                    for(int i = jHistory.length()-1; i >= 0; --i){
                        JSONObject jElement = jHistory.getJSONObject(i);

                        if(jElement.has("file") && jElement.has("name")){
                            File entryFile = new File(jElement.getString("file"));
                            String entryName = jElement.getString("name");

                            push(new WorldFileEntry(entryName, entryFile));
                        }
                    }
                }
            }
        }
    }

    /**
     * Write JSON-formatted history file
     * @param file
     */
    private static void writeListJSON(File file){
        if(!file.exists() || file.canWrite()){
            BufferedWriter writer = null;

            // create parent directories
            File parentDir = file.getParentFile();
            if(!parentDir.exists()){
                parentDir.mkdirs();
            }

            try {
                writer = new BufferedWriter(new FileWriter(file));
            } catch (IOException ex) {
                Logger.getLogger(WorldFileList.class.getName()).log(Level.WARNING, null, ex);
            }

            if(writer != null){
                JSONObject jRoot = new JSONObject();

                jRoot.put("ver", "" + FILE_VER_MAJOR + "." + FILE_VER_MINOR);

                JSONArray jHistory = new JSONArray();
                jRoot.put("history", jHistory);

                for(WorldFileEntry entry: getEntries()){
                    JSONObject jElement = new JSONObject();
                    jHistory.put(jElement);

                    jElement.put("file", entry.getFile().getAbsoluteFile());
                    jElement.put("name", entry.getWorldName());
                }

                jRoot.write(writer, 4, 0);

                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(WorldFileList.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            System.err.println("Could not write file " + file.getAbsolutePath());
        }
    }

    /**
     * Read available worlds file (legacy)
     */
    @Deprecated
    private static void readWorldList(File historyFile){
        try {
            // read from available worlds file
            BufferedReader reader = new BufferedReader(new FileReader(historyFile));

            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.startsWith("f ")){ // world file entry
                    String fileName = line.substring(2).trim();

                    WorldFileDefault worldFile = new WorldFileDefault(fileName);
                    if(worldFile.canRead()){ // is world file
                        String name;
                        try {
                            // get world name
                            name = worldFile.readWorldName();
                        } catch (Exception ex) {
                            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                            // use file name if world name not found
                            name = fileName.substring(fileName.lastIndexOf('/') + 1);
                        }
                        // add found world to list
                        push(new WorldFileEntry(name, new File(fileName)));
                    }
                }
            }
        } catch (FileNotFoundException ex) {} catch (IOException ex) {
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class WorldFileEntry {

        String worldName;
        File file;

        public WorldFileEntry(String worldName, File file) {
            this.worldName = worldName;
            this.file = file;
        }

        public String getWorldName() {
            return worldName;
        }

        public void setWorldName(String worldName) {
            this.worldName = worldName;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

    }
}
