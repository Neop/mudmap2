/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2015  Neop (email: mneop@web.de)
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

/*  File description
 *
 *  Reads and writes world files (mudmap 1+ file type)
 */
package mudmap2.backend.WorldFileReader.current;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mudmap2.utils.Pair;
import mudmap2.backend.Area;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.WorldFileReader.Exception.WorldFileException;
import mudmap2.backend.WorldManager;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldFileType;

/**
 * Reads and writes world files (mudmap 1+ file type)
 * @author Neop
 */
@Deprecated
public class WorldFileMM1 extends WorldFile {
    /*
     * Note: the version number should only vary if the
     * world file structure changed. Before mudmap 1.4.60 the
     * file version was always equal to the program version
     */
    public static final int reader_major = 1;
    public static final int reader_minor = 7;
    private boolean version_mismatch, version_mismatch_confirmed;

    RiskLevel risk_level_default;

    public static boolean compatibility_mudmap_1;

    int file_major, file_minor;

    // temporary data for layer quadtree optimization
    HashMap<Integer, Pair<Integer, Integer>> layer_center;

    int cur_place_id;
    String cur_place_name;
    Place cur_place;

    Area curArea;
    HashMap<Integer, Area> areas = new HashMap<>();
    ArrayList<Pair<Place, Integer>> children = new ArrayList<>();
    ArrayList<WorldFileMM1.PathTmp> tmp_paths = new ArrayList<>();
    ArrayList<WorldFileMM1.PathTmp> tmp_paths_deprecated = new ArrayList<>();

    // error counter for deprecated path specification format
    // increments if there are more than one path between two places
    // (there is no way to reconstruct which exits were connected)
    int path_connection_error_dep_double;

    // -----------------------

    public WorldFileMM1(String filename) {
        super(filename);
        compatibility_mudmap_1 = true;
    }

    public boolean getCompatibilityMudmap1(){
        return compatibility_mudmap_1;
    }

    public void setCompatibilityMudmap1(boolean c){
        compatibility_mudmap_1 = c;
    }

    @Override
    public World readFile() throws Exception {
        World world = new World();
        world.setWorldFile(this);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            layer_center = new HashMap<>();

            String line;
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();

                    if(!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")){
                        if(line.startsWith("ver "))         readFileVersion(line);
                        else if(line.startsWith("wname "))  world.setName(line.substring(6).trim());
                        else if(line.startsWith("wcol "))   readPathColorCardinal(line, world);
                        else if(line.startsWith("wcnd "))   readPathColorNonCardinal(line, world);
                        else if(line.startsWith("pcol "))   readPathColorUser(line, world);
                        else if(line.startsWith("tccol "))  readTileCenterColor(line, world);
                        else if(line.startsWith("home "))   readHome(line, world);
                        else if(line.startsWith("dlc "))    readRiskLevel(line, world);
                        else if(line.startsWith("show_place_id ")) readShowPlaceID(line, world);

                        else if(line.startsWith("a "))      readArea(line, world);
                        else if(line.startsWith("acol "))   readAreaColor(line, world);

                        // layer center (for quadtree optimization)
                        else if(line.startsWith("lc "))     readLayerCenter(line);

                        else if(line.startsWith("p "))      readPlaceName(line);
                        else if(line.startsWith("ppos "))   readPlacePosition(line, world);
                        else if(line.startsWith("par "))    readPlaceArea(line, world);
                        else if(line.startsWith("pb "))     readPlaceFlag(line);
                        else if(line.startsWith("pw "))     readPlacePathPw(line); // deprecated
                        else if(line.startsWith("pp "))     readPlacePath(line);
                        else if(line.startsWith("pchi "))   readPlaceChild(line);
                        else if(line.startsWith("pdl "))    readPlaceRiskLevel(line, world);
                        else if(line.startsWith("prl "))    readPlaceRecommendedLevel(line);
                        else if(line.startsWith("pcom "))   readPlaceComment(line);

                        else if(!line.startsWith("mver"))   readUnrecognized(line);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                int ret = JOptionPane.showConfirmDialog(null,
                        "Error while parsing worlds file, places might be missing. Continue? \n" + ex.getMessage(),
                        "Loading world", JOptionPane.YES_NO_OPTION);
                if(ret == JOptionPane.NO_OPTION) throw new Exception("Could not read world file");
            }

            // connect children and parent places
            for(Pair<Place, Integer> p: children){
                p.first.connectChild(world.getPlace(p.second));
            }

            // new path specification format is introduced in file version 1.5.0 (after 1.4.45)
            if(file_major == 1 && file_minor >= 5){ // connect paths
                for(WorldFileMM1.PathTmp path: tmp_paths){
                    path.place_a.connectPath(new Path(path.place_a, path.exits[0], world.getPlace(path.place_b), path.exits[1]));
                }
            } else { // connect deprecated paths (for compatibility to mudmap 1)
                int error_not_paired_cnt = 0;
                String error_places = "";
                for(WorldFileMM1.PathTmp path: tmp_paths_deprecated){
                    if(path.exits[1] == null) error_not_paired_cnt++;
                    path.place_a.connectPath(new Path(path.place_a, path.exits[0], world.getPlace(path.place_b), (path.exits[1] != null) ? path.exits[1] : "unknown"));
                }
                // error messages
                if(path_connection_error_dep_double > 0){
                    System.out.println("Warning: " + path_connection_error_dep_double + " paths might not be properly reconstructed (exit mispairings might occur at places with more than two connections to each other)");
                    JOptionPane.showMessageDialog(null, path_connection_error_dep_double + " paths might not be properly reconstructed from a MUD Map v1 world file.\nExit mispairings might occur at places that are directly connected via more than one path.", "World reconstruction warning", JOptionPane.WARNING_MESSAGE);
                }
                if(error_not_paired_cnt > 0){
                    System.out.println("Warning: " + error_not_paired_cnt + " paths could not be properly reconstructed (an exit is unknown for each error place pair)");
                    JOptionPane.showMessageDialog(null, error_not_paired_cnt + " paths could not be properly reconstructed.\nThis means that one exit of each faulty path is unknown.", "World reconstruction warning", JOptionPane.WARNING_MESSAGE);
                }
            }

        } catch (FileNotFoundException ex) {
            System.out.println("Could not open world file \"" + filename + "\", file not found");
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
            JOptionPane.showMessageDialog(null,
                    "Could not open world file \"" + filename + "\": file not found",
                    "Loading world", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not open world file \"" + filename + "\", please contact the developer: " + ex.toString(),
                    "Loading world", JOptionPane.ERROR_MESSAGE);
            throw ex;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not open world file \"" + filename + "\", please contact the developer: " + ex.toString(),
                    "Loading world", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }

        return world;
    }

    private void readFileVersion(String line) throws Exception{
        // removePlace tag and split version
        String[] tmp = safeSplit(line.substring(4), ".", 2);

        file_major = Integer.parseInt(tmp[0]);
        file_minor = Integer.parseInt(tmp[1]);

        version_mismatch = (file_major > reader_major || (file_major == reader_major && (file_minor > reader_minor)));
        if(file_major > reader_major){
            int ret = JOptionPane.showConfirmDialog(null,
                    "World file version is greater than the reader version. Please update MUD Map. Continuing might cause data loss.",
                    "Loading world", JOptionPane.OK_CANCEL_OPTION);
            if(ret == JOptionPane.CANCEL_OPTION) throw new WorldFileException(filename, "Could not read world file", null);
        }
    }

    private void readPathColorCardinal(String line, World world){
        String[] tmp = safeSplit(line.substring(5), " ", 3);
        Color color = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
        if(world.getPathColor() == world.getPathColorNstd()) world.setPathColorNstd(new Color(color.getRGB()));
        world.setPathColor(color);
    }

    private void readPathColorNonCardinal(String line, World world){
        String[] tmp = safeSplit(line.substring(5), " ", 3);
        world.setPathColorNstd(new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2])));
    }

    private void readTileCenterColor(String line, World world){
        String[] tmp = safeSplit(line.substring(6), " ", 3);
        Color color = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
        world.setTileCenterColor(color);
    }

    private void readPathColorUser(String line, World world){
        int i = 0, r = 0, g = 0, b;
        Color col = null;
        String[] data = line.substring(5).split("[\\s;]");
        for(String str: data){
            switch(++i){
                case 1:
                    r = Integer.parseInt(str);
                    break;
                case 2:
                    g = Integer.parseInt(str);
                    break;
                case 3:
                    b = Integer.parseInt(str);
                    col = new Color(r, g, b);
                    break;
                default:
                    // assign color to path tag
                    world.getPathColors().put(str.replaceAll("\\\\_", " "), col);
            }
        }
    }

    private void readHome(String line, World world){
        String[] tmp = safeSplit(line.substring(5), " ", 3);
        world.setHome(new WorldCoordinate(Integer.parseInt(tmp[0]), Double.parseDouble(tmp[1]), Double.parseDouble(tmp[2])));
    }

    private void readRiskLevel(String line, World world){
        String[] tmp = safeSplit(line, " ", 5);
        int rlid = Integer.parseInt(tmp[1]);
        String description = configGetText(5, line);

        // only create a new risk level if it doesn't exist yet
        if(world.getRiskLevel(rlid) == null)
            world.addRiskLevel(new RiskLevel(rlid, description, new Color(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]))));
        else {
            world.getRiskLevel(rlid).setDescription(description);
            world.getRiskLevel(rlid).setColor(new Color(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4])));
        }
    }

    private void readShowPlaceID(String line, World world){
        world.setShowPlaceID(World.ShowPlaceID.valueOf(line.split("\\s")[1]));
    }

    private void readArea(String line, World world){
        Integer curAreaID = Integer.parseInt(line.split("\\s")[1]);
        curArea = new Area(configGetText(2, line));
        world.addArea(curArea);
        areas.put(curAreaID, curArea);
    }

    private void readAreaColor(String line, World world){
        if(null != curArea){
            String[] tmp = line.split("\\s");
            Color color = new Color(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]));
            curArea.setColor(color);
        }
    }

    // layer center (for quadtree optimization)
    private void readLayerCenter(String line){
        String[] tmp = line.split("\\s");
        layer_center.put(Integer.parseInt(tmp[1]), new Pair<>(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3])));
    }

    private void readPlaceName(String line){
        cur_place_id = Integer.parseInt(line.split("\\s")[1]);
        cur_place_name = configGetText(2, line);
    }

    private void readPlacePosition(String line, World world) throws Exception{
        String[] tmp = safeSplit(line, " ", 4);
        int layer = Integer.parseInt(tmp[1]);

        // create the layer, if it doesn't exist
        Layer l = world.getLayer(layer);
        if(l == null) world.addLayer(l = new Layer(layer, world));
        if(l.isEmpty() && layer_center.containsKey(layer)){
            Pair<Integer, Integer> p = layer_center.get(layer);
            l.setQuadtree(p.first, p.second);
        }

        if(cur_place_id != -1){
            // create place and add it to the layer and places list
            cur_place = new Place(cur_place_id, cur_place_name, Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), world.getLayer(Integer.parseInt(tmp[1])));
            cur_place.setRiskLevel(risk_level_default);
            try {
                world.putPlace(cur_place);
            } catch (Exception ex) {
                Logger.getLogger(WorldFileMM1.class.getName()).log(Level.SEVERE, null, ex);
                int ret = JOptionPane.showConfirmDialog(null, "Could not add place " + cur_place.getName() + " to world. Continue?", "Loading world", JOptionPane.YES_NO_OPTION);
                if(ret == JOptionPane.NO_OPTION) throw new Exception("Could not read world file");
            }
        }
    }

    private void readPlaceArea(String line, World world){
        Integer areaID = Integer.parseInt(line.substring(3).trim());
        cur_place.setArea(areas.get(areaID));
    }

    private void readPlaceFlag(String line){
        cur_place.setFlag(line.substring(3).trim(), true);
    }

    // deprecated
    private void readPlacePathPw(String line){
        /* This tag describes 3 of 4 pieces of information
        * needed for a path connection. Both places have
        * corresponding tags, but a path might not be reconstructable
        * if there are more than on connections between two places
        */
        String[] tmp = safeSplit(line, " ", 3);
        int other_place_id = Integer.parseInt(tmp[1]);

        boolean found_path = false;

        for(WorldFileMM1.PathTmp path: tmp_paths_deprecated){
            // find place - place pair and connect it
            if(path.place_b == cur_place_id && path.place_a.getId() == other_place_id){
                if(path.exits[1] == null){
                    path.exits[1] = tmp[2];
                    found_path = true;
                    break;
                } else path_connection_error_dep_double++;
            }
        }
        // if there is no pair create a new entry
        if(!found_path) tmp_paths_deprecated.add(new WorldFileMM1.PathTmp(cur_place, other_place_id, tmp[2], null));
    }

    private void readPlacePath(String line){
        String tmp[] = safeSplit(line.substring(3).trim(), "$", 3);

        tmp_paths.add(new WorldFileMM1.PathTmp(cur_place, Integer.parseInt(tmp[0]), tmp[1], tmp[2]));
    }

    private void readPlaceChild(String line){
        String[] tmp = safeSplit(line, " ", 2);
        children.add(new Pair<>(cur_place, Integer.parseInt(tmp[1])));
    }

    private void readPlaceRiskLevel(String line, World world){
        int rlid = Integer.parseInt(line.substring(3).trim());
        RiskLevel rl = world.getRiskLevel(rlid);
        if(rl != null) cur_place.setRiskLevel(rl);
        else System.out.println("Couldn't load risk level " + rlid + " for " + cur_place_name);
    }

    private void readPlaceRecommendedLevel(String line){
        String[] tmp = line.split(" ");
        cur_place.setRecLevelMin(Integer.parseInt(tmp[1]));
        cur_place.setRecLevelMax(Integer.parseInt(tmp[2]));
    }

    private void readPlaceComment(String line){
        cur_place.addComment(line.substring(4).trim());
    }

    private void readUnrecognized(String line) throws Exception{
        System.out.println("Unrecognized line in world data: " + line);
        if(version_mismatch && !version_mismatch_confirmed){
            int ret = JOptionPane.showConfirmDialog(null, "Couldn't parse a line in world file while the world file version (" + file_major + "." + file_minor + ") is greater than the file reader version (" + reader_major + "." + reader_minor + "). Please update MUD Map.\nIf you'd like to continue using this version of MUD Map, you can click on 'yes'. This might cause data-loss!", "Loading world", JOptionPane.YES_NO_OPTION);
            version_mismatch_confirmed = ret == JOptionPane.YES_OPTION;
            if(!version_mismatch_confirmed) throw new Exception("World file version is greater than file reader version. Please update mudmap or consult the developer.");
        }
    }

    /**
     * Returns the text data of a configuration file entry
     *
     * Some config file lines consist of space-separated data followed by a text
     * This method returns the text part.
     *
     * @param non_text number of data entries to be removed
     * @param line raw config line
     * @return text part
     */
    private String configGetText(int non_text, String line){
        while(non_text-- != 0){
            int index = line.indexOf(" ");
            // if " " not found -> entry not found in line
            if(index < 0) return "";
            line = line.substring(index).trim();
        }
        return line;
    }

    /**
     * Create a backup copy of the world file
     */
    @Override
    public void backup() {
        try {
            File fileold = new File(filename);
            File filenew = new File(filename + ".bak");

            if(fileold.canRead()){
                if(filenew.exists()) filenew.delete();
                Files.copy(fileold.toPath(), filenew.toPath());
            }

        } catch (IOException ex) {
            Logger.getLogger(WorldFileMM1.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Could not create world backup file", "World backup", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Splits a string in exactly num substrings separated by separator
     * Additional substrings will be empty if less than num-1 separators
     * are found
     *
     * @param line
     * @param separator
     * @param num
     * @return
     */
    private String[] safeSplit(String line, String separator, Integer num) {
        String tmp[] = new String[num];
        int index[] = new int[num];

        int i = 0;
        for(; i < num; ++i){
            if(i == 0){ // first
                index[0] = line.indexOf(separator); // find next separator
                tmp[0] = line.substring(0, (index[0]!=-1?index[0]:0));
            } else {
                index[i] = line.indexOf(separator, index[i-1] + 1);
                if(index[i] == -1) // reached end of string
                    tmp[i] = line.substring(index[i-1] + 1);
                else tmp[i] = line.substring(index[i-1] + 1, index[i]);
            }
            // break if no more separators are found
            if(index[i] == -1) break;
        }
        // fill remaining elements with empty strings
        for(++i;i < num; ++i) tmp[i] = new String();

        return tmp;
    }

    @Override
    public String readWorldName() throws Exception {
        String line, worldname = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            Pair<Integer, Integer> fileVersion = new Pair<>(0, 0);
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    if(line.startsWith("wname")){
                        worldname = line.substring(6);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldFileDefault.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldFileDefault.class.getName()).log(Level.SEVERE, null, ex);
        }

        return worldname;
    }

    @Override
    public Boolean canRead() {
        Boolean ret = false;

        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            Boolean worldNameFound = false;
            Boolean fileVersionFound = false;

            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.startsWith("wname ")) worldNameFound = true;
                else if(line.startsWith("ver ")) fileVersionFound = true;
                if(worldNameFound && fileVersionFound) break;
            }

            ret = worldNameFound && fileVersionFound;
        } catch (IOException | UnsupportedOperationException ex) {
            return false;
        }

        return ret;
    }

    @Override
    public WorldFileType getWorldFileType() {
        return WorldFileType.MUDMAP1;
    }

    // Path creation helper class
    private class PathTmp {
        public Place place_a;
        public int place_b;
        public String[] exits;

        PathTmp(Place _place_a, int _place_b, String exit_a, String exit_b){
            place_a = _place_a;
            place_b = _place_b;

            exits = new String[2];
            exits[0] = exit_a;
            exits[1] = exit_b;
        }
    }

    // ----------------------

    @Override
    public void writeFile(World world) {
        // create IDs for areas
        HashMap<Area, Integer> areaIDs = new HashMap<>();
        Integer cnt = 0;
        for(Area a: world.getAreas()){
            Boolean inUse = false;
            // removePlace unused
            for(Place place: world.getPlaces()){
                if(place.getArea() == a){
                    inUse = true;
                    break;
                }
            }
            if(inUse) areaIDs.put(a, ++cnt);
        }

        try {
            // open file
            File file = new File(filename);
            file.getParentFile().mkdirs();
            PrintWriter outstream = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            outstream.println("# MUD Map 2 world file");
            outstream.println("# compatibility for MUD Map 1 " + (compatibility_mudmap_1 ? "enabled" : "disabled"));

            outstream.println("ver " + reader_major + "." + reader_minor);
            outstream.println("mver " + WorldFileMM1.class.getPackage().getImplementationVersion());
            outstream.println("wname " + world.getName());
            outstream.println("wcol " + world.getPathColor().getRed() + " " + world.getPathColor().getGreen() + " " + world.getPathColor().getBlue());
            outstream.println("wcnd " + world.getPathColorNstd().getRed() + " " + world.getPathColorNstd().getGreen() + " " + world.getPathColorNstd().getBlue());
            outstream.println("tccol " + world.getTileCenterColor().getRed() + " " + world.getTileCenterColor().getGreen() + " " + world.getTileCenterColor().getBlue());

            HashMap<Color, String> pcol = new HashMap<>();
            for(Map.Entry<String, Color> entry: world.getPathColors().entrySet()){
                if(pcol.containsKey(entry.getValue())){ // if value is already in pcol
                    pcol.put(entry.getValue(), pcol.get(entry.getValue()) + ";" + entry.getKey().replaceAll(" ", "\\_"));
                } else {
                    pcol.put(entry.getValue(), entry.getKey().replaceAll(" ", "\\_"));
                }
            }
            for(Map.Entry<Color, String> entry: pcol.entrySet()){
                outstream.println("pcol " + entry.getKey().getRed() + " " + entry.getKey().getGreen() + " " + entry.getKey().getBlue() + " " + entry.getValue());
            }

            outstream.println("home " + world.getHome());
            outstream.println("show_place_id " + world.getShowPlaceId());

            // risk levels
            for(RiskLevel rl: world.getRiskLevels())
                outstream.println("dlc " + rl.getId() + " " + rl.getColor().getRed() + " " + rl.getColor().getGreen() + " " + rl.getColor().getBlue() + " " + rl.getDescription());

            // areas
            for(Map.Entry<Area, Integer> area: areaIDs.entrySet()){
                if(area.getValue() != null){
                    outstream.println("a " + area.getValue() + " " + area.getKey().getName());
                    outstream.println("acol " + area.getKey().getColor().getRed()
                            + " " + area.getKey().getColor().getGreen()
                            + " " + area.getKey().getColor().getBlue());
                }
            }

            // layers (for quadtree optimization)
            for(Layer l: world.getLayers()){
                outstream.println("lc " + l.getId() + " " + l.getCenterX() + " " + l.getCenterY());
            }

            outstream.flush();

            // places
            for(Place p: world.getPlaces()){
                outstream.println("p " + p.getId() + " " + p.getName());
                outstream.println("ppos " + p.getLayer().getId() + " " + p.getX() + " " + p.getY());
                if(p.getArea() != null) outstream.println("par " + areaIDs.get(p.getArea()));

                // paths
                for(Path path: p.getPaths()){
                    Place other_place = path.getOtherPlace(p);

                    if(compatibility_mudmap_1) // deprecated path format
                        outstream.println("pw " + other_place.getId() + " " + path.getExit(p));

                    // new path format
                    if(path.getPlaces()[0] == p) // only one of both places should describe the path
                        outstream.println("pp " + other_place.getId() + "$" + path.getExit(p) + "$" + path.getExit(other_place));
                }

                // risk level and recommended level
                if(p.getRiskLevel() != null) outstream.println("pdl " + p.getRiskLevel().getId());
                if(p.getRecLevelMin() != -1 || p.getRecLevelMax() != -1) outstream.println("prl " + p.getRecLevelMin() + " " + p.getRecLevelMax());

                // children
                for(Place child: p.getChildren()) outstream.println("pchi " + child.getId());
                // comments
                for(String comment: p.getComments()) outstream.println("pcom " + comment);
                // flags
                for(Map.Entry<String, Boolean> flag: p.getFlags().entrySet()){
                    if(flag.getValue()) outstream.println("pb " + flag.getKey());
                }

                outstream.flush();
            }
        } catch (IOException ex) {
            System.out.printf("Couldn't write world file " + mudmap2.Paths.getConfigFile());
            JOptionPane.showMessageDialog(null, "Could not write world file!", "Saving world", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(World.class.getName()).log(Level.WARNING, null, ex);
        }
    }

}
