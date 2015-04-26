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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mudmap2.Pair;
import mudmap2.Paths;
import mudmap2.backend.Area;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.WorldManager;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldReadException;

/**
 * Reads and writes world files (mudmap 1+ file type)
 * @author Neop
 */
public class WorldFileMM1 implements WorldFile {
    /* 
     * Note: the version number should only vary if the
     * world file structure changed. Before mudmap 1.4.60 the
     * file version was always equal to the program version
     */
    public static final int reader_major = 1;
    public static final int reader_minor = 7;
    private boolean version_mismatch, version_mismatch_confirmed;
    
    RiskLevel risk_level_default;
    
    private boolean compatibility_mudmap_1;
    
    World world;

    int file_major, file_minor;
    int cur_area;
    
    // temporary data for layer quadtree optimization
    HashMap<Integer, Pair<Integer, Integer>> layer_center;
    
    int cur_place_id;
    String cur_place_name;
    Place cur_place;
    
    ArrayList<Pair<Place, Integer>> children = new ArrayList<Pair<Place, Integer>>();
    ArrayList<WorldFileMM1.PathTmp> tmp_paths = new ArrayList<WorldFileMM1.PathTmp>();
    ArrayList<WorldFileMM1.PathTmp> tmp_paths_deprecated = new ArrayList<WorldFileMM1.PathTmp>();
    
    // error counter for deprecated path specification format
    // increments if there are more than one path between two places
    // (there is no way to reconstruct which exits were connected)
    int path_connection_error_dep_double;
            
    // -----------------------
    
    public WorldFileMM1(World _world) {
        world = _world;
        compatibility_mudmap_1 = true;
    }

    public boolean get_compatibility_mudmap1(){
        return compatibility_mudmap_1;
    }
    
    public void set_compatibility_mudmap1(boolean c){
        compatibility_mudmap_1 = c;
    }
    
    @Override
    public void readFile(String file) throws WorldReadException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            file_major = file_minor = 0;
            version_mismatch = version_mismatch_confirmed = false;
            
            cur_area = -1;

            layer_center = new HashMap<Integer, Pair<Integer, Integer>>();
            
            // temporary data for creating a place
            cur_place_id = -1;
            cur_place_name = "";
            cur_place = new Place(-1, "", 0, 0, new Layer(-1, world));
            
            risk_level_default = world.get_risk_level(0);
            
            children = new ArrayList<Pair<Place, Integer>>();
            tmp_paths = new ArrayList<WorldFileMM1.PathTmp>();
            tmp_paths_deprecated = new ArrayList<WorldFileMM1.PathTmp>();
            
            path_connection_error_dep_double = 0;
            
            try {    
                String line;
                while((line = reader.readLine()) != null){
                    line = line.trim();

                    if(!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")){
                        if(line.startsWith("ver "))         read_file_version(line);
                        else if(line.startsWith("wname "))  world.set_name(line.substring(6).trim());
                        else if(line.startsWith("wcol "))   read_path_color_cardinal(line);
                        else if(line.startsWith("wcnd "))   read_path_color_non_cardinal(line);
                        else if(line.startsWith("pcol "))   read_path_color_user(line);
                        else if(line.startsWith("tccol "))  read_tile_center_color(line);
                        else if(line.startsWith("home "))   read_home(line);
                        else if(line.startsWith("dlc "))    read_risk_level(line);
                        else if(line.startsWith("show_place_id ")) read_show_place_id(line);

                        else if(line.startsWith("a "))      read_area(line);
                        else if(line.startsWith("acol "))   read_area_color(line);

                        // layer center (for quadtree optimization)
                        else if(line.startsWith("lc "))     read_layer_center(line);

                        else if(line.startsWith("p "))      read_place_name(line);
                        else if(line.startsWith("ppos "))   read_place_position(line);    
                        else if(line.startsWith("par "))    read_place_area(line);
                        else if(line.startsWith("pb "))     read_place_flag(line);
                        else if(line.startsWith("pw "))     read_place_path_pw(line); // deprecated
                        else if(line.startsWith("pp "))     read_place_path(line);
                        else if(line.startsWith("pchi "))   read_place_child(line);
                        else if(line.startsWith("pdl "))    read_place_risk_level(line);
                        else if(line.startsWith("prl "))    read_place_recommended_level(line);
                        else if(line.startsWith("pcom "))   read_place_comment(line);

                        else if(!line.startsWith("mver"))   read_unrecognized(line);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // connect children and parent places
            for(Pair<Place, Integer> p: children){
                p.first.connect_child(world.get_place(p.second));
            }
            
            // new path specification format is introduced in file version 1.5.0 (after 1.4.45)
            if(file_major == 1 && file_minor >= 5){ // connect paths
                for(WorldFileMM1.PathTmp path: tmp_paths){
                    path.place_a.connect_path(new Path(path.place_a, path.exits[0], world.get_place(path.place_b), path.exits[1]));
                }
            } else { // connect deprecated paths (for compatibility to mudmap 1)
                int error_not_paired_cnt = 0;
                String error_places = "";
                for(WorldFileMM1.PathTmp path: tmp_paths_deprecated){
                    if(path.exits[1] == null) error_not_paired_cnt++;
                    path.place_a.connect_path(new Path(path.place_a, path.exits[0], world.get_place(path.place_b), (path.exits[1] != null) ? path.exits[1] : "unknown"));
                }
                // error messages
                if(path_connection_error_dep_double > 0) 
                    System.out.println("Warning: " + path_connection_error_dep_double + " paths might not be properly reconstructed (exit mispairings might occur at places with more than two connections to each other)");
                if(error_not_paired_cnt > 0)
                    System.out.println("Warning: " + error_not_paired_cnt + " paths could not be properly reconstructed (an exit is unknown for each error place pair)");
                
                // show error message dialog
                if(path_connection_error_dep_double > 0)
                    JOptionPane.showMessageDialog(null, path_connection_error_dep_double + " paths might not be properly reconstructed from a MUD Map v1 world file.\nExit mispairings might occur at places that are directly connected via more than one path.", "World reconstruction warning", JOptionPane.WARNING_MESSAGE);
                if(error_not_paired_cnt > 0)
                    JOptionPane.showMessageDialog(null, error_not_paired_cnt + " paths could not be properly reconstructed.\nThis means that one exit of each faulty path is unknown.", "World reconstruction warning", JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open world file \"" + Paths.get_available_worlds_file() + "\", file not found");
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
    private void read_file_version(String line){
        // remove tag and split version                      
        String[] tmp = line.substring(4).split("\\.");
    
        file_major = Integer.parseInt(tmp[0]);
        file_minor = Integer.parseInt(tmp[1]);

        version_mismatch = (file_major > reader_major || (file_major == reader_major && (file_minor > reader_minor)));
    }
    
    private void read_path_color_cardinal(String line){
        String[] tmp = line.substring(5).split("\\s");
        Color color = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
        if(world.get_path_color() == world.get_path_color_nstd()) world.set_path_color_nstd(new Color(color.getRGB()));
        world.set_path_color(color);
    }

    private void read_path_color_non_cardinal(String line){
        String[] tmp = line.substring(5).split("\\s");
        world.set_path_color_nstd(new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2])));
    }

    private void read_tile_center_color(String line){
        String[] tmp = line.substring(6).split("\\s");
        Color color = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
        world.set_tile_center_color(color);
    }
    
    private void read_path_color_user(String line){
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
                    world.get_path_colors().put(str.replaceAll("\\\\_", " "), col);
            }
        }
    }
    
    private void read_home(String line){
        String[] tmp = line.substring(5).split("\\s");
        world.set_home(new WorldCoordinate(Integer.parseInt(tmp[0]), Double.parseDouble(tmp[1]), Double.parseDouble(tmp[2])));
    }
    
    private void read_risk_level(String line){
        String[] tmp = line.split("\\s");
        int rlid = Integer.parseInt(tmp[1]);
        String description = config_get_text(5, line);

        // only create a new risk level if it doesn't exist yet
        if(world.get_risk_level(rlid) == null)
            world.add_risk_level(new RiskLevel(rlid, description, new Color(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]))));
        else {
            world.get_risk_level(rlid).set_description(description);
            world.get_risk_level(rlid).set_color(new Color(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4])));
        }
    }
    
    private void read_show_place_id(String line){
        world.set_show_place_id(World.ShowPlaceID_t.valueOf(line.split("\\s")[1]));
    }
    
    private void read_area(String line){
        cur_area = Integer.parseInt(line.split("\\s")[1]);
        world.add_area(new Area(cur_area, config_get_text(2, line)));
    }
    
    private void read_area_color(String line){
        String[] tmp = line.split("\\s");
        Color color = new Color(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]));
        world.get_area(cur_area).set_color(color);
    }
    
    // layer center (for quadtree optimization)
    private void read_layer_center(String line){
        String[] tmp = line.split("\\s");
        layer_center.put(Integer.parseInt(tmp[1]), new Pair<Integer, Integer>(Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3])));
    }
    
    private void read_place_name(String line){
        cur_place_id = Integer.parseInt(line.split("\\s")[1]);
        cur_place_name = config_get_text(2, line);
    }
    
    private void read_place_position(String line){
        String tmp[] = line.split("\\s");
        int layer = Integer.parseInt(tmp[1]);

        // create the layer, if it doesn't exist
        Layer l = world.get_layer(layer);
        if(l == null) world.set_layer(l = new Layer(layer, world));
        if(l.isEmpty() && layer_center.containsKey(layer)){
            Pair<Integer, Integer> p = layer_center.get(layer);
            l.set_quadtree(p.first, p.second);
        }

        if(cur_place_id != -1){
            // create place and add it to the layer and places list
            cur_place = new Place(cur_place_id, cur_place_name, Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), world.get_layer(Integer.parseInt(tmp[1])));
            cur_place.set_risk_level(risk_level_default);
            try {
                world.put(cur_place);
            } catch (Exception ex) {
                Logger.getLogger(WorldFileMM1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void read_place_area(String line){
        cur_place.set_area(world.get_area(Integer.parseInt(line.substring(3).trim())));
    }
    
    private void read_place_flag(String line){
        cur_place.set_flag(line.substring(3).trim(), true);
    }
    
    // deprecated
    private void read_place_path_pw(String line){
        /* This tag describes 3 of 4 pieces of information
        * needed for a path connection. Both places have 
        * corresponding tags, but a path might not be reconstructable
        * if there are more than on connections between two places
        */
        String[] tmp = line.split("\\s");
        int other_place_id = Integer.parseInt(tmp[1]);

        boolean found_path = false;

        for(WorldFileMM1.PathTmp path: tmp_paths_deprecated){
            // find place - place pair and connect it
            if(path.place_b == cur_place_id && path.place_a.get_id() == other_place_id){
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
    
    private void read_place_path(String line){
        String[] tmp = line.substring(3).split("[\\$;]");
        tmp_paths.add(new WorldFileMM1.PathTmp(cur_place, Integer.parseInt(tmp[0]), tmp[1], tmp[2]));
    }
    
    private void read_place_child(String line){
        String[] tmp = line.split(" ");
        children.add(new Pair<Place, Integer>(cur_place, Integer.parseInt(tmp[1])));
    }
    
    private void read_place_risk_level(String line){
        int rlid = Integer.parseInt(line.substring(3).trim());
        RiskLevel rl = world.get_risk_level(rlid);
        if(rl != null) cur_place.set_risk_level(rl);
        else System.out.println("Couldn't load risk level " + rlid + " for " + cur_place_name);
    }
    
    private void read_place_recommended_level(String line){
        String[] tmp = line.split(" ");
        cur_place.set_rec_lvl_min(Integer.parseInt(tmp[1]));
        cur_place.set_rec_lvl_max(Integer.parseInt(tmp[2]));
    }
    
    private void read_place_comment(String line){
        cur_place.add_comment(line.substring(4).trim());
    }
    
    private void read_unrecognized(String line) throws WorldReadException{
        System.out.println("Unrecognized line in world data: " + line);
        if(version_mismatch && !version_mismatch_confirmed){
            int ret = JOptionPane.showConfirmDialog(null, "Couldn't parse a line in world file while the world file version (" + file_major + "." + file_minor + ") is greater than the file reader version (" + reader_major + "." + reader_minor + "). Please update MUD Map.\nIf you'd like to continue using this version of MUD Map, you can click on 'yes'. This might cause data-loss!", "Loading world", JOptionPane.YES_NO_OPTION);
            version_mismatch_confirmed = ret == JOptionPane.YES_OPTION;
            if(!version_mismatch_confirmed) throw new WorldReadException("World file version is greater than file reader version. Please update mudmap or consult the developer.");
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
    private String config_get_text(int non_text, String line){
        while(non_text-- != 0){
            int index = line.indexOf(" ");
            // if " " not found -> entry not found in line
            if(index < 0) return "";
            line = line.substring(index).trim();
        }
        return line;
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
    public void writeFile(String file) {
        try {
            // open file
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(file)));

            outstream.println("# MUD Map 2 world file");
            outstream.println("# compatibility for MUD Map 1 " + (compatibility_mudmap_1 ? "enabled" : "disabled"));
            
            outstream.println("ver " + reader_major + "." + reader_minor);
            outstream.println("mver " + mudmap2.Mudmap2.get_version_major() + "." + mudmap2.Mudmap2.get_version_minor() + "." + mudmap2.Mudmap2.get_version_build());
            outstream.println("wname " + world.get_name());
            outstream.println("wcol " + world.get_path_color().getRed() + " " + world.get_path_color().getGreen() + " " + world.get_path_color().getBlue());
            outstream.println("wcnd " + world.get_path_color_nstd().getRed() + " " + world.get_path_color_nstd().getGreen() + " " + world.get_path_color_nstd().getBlue());
            outstream.println("tccol " + world.get_tile_center_color().getRed() + " " + world.get_tile_center_color().getGreen() + " " + world.get_tile_center_color().getBlue());
            
            HashMap<Color, String> pcol = new HashMap<Color, String>();
            for(Map.Entry<String, Color> entry: world.get_path_colors().entrySet()){
                if(pcol.containsKey(entry.getValue())){ // if value is already in pcol
                    if(pcol.isEmpty())
                        pcol.put(entry.getValue(), entry.getKey().replaceAll(" ", "\\_"));
                    else
                        pcol.put(entry.getValue(), pcol.get(entry.getValue()) + ";" + entry.getKey().replaceAll(" ", "\\_"));
                } else {
                    pcol.put(entry.getValue(), entry.getKey().replaceAll(" ", "\\_"));
                }
            }
            for(Map.Entry<Color, String> entry: pcol.entrySet()){
                outstream.println("pcol " + entry.getKey().getRed() + " " + entry.getKey().getGreen() + " " + entry.getKey().getBlue() + " " + entry.getValue());
            }
            
            outstream.println("home " + world.get_home());
            outstream.println("show_place_id " + world.get_show_place_id());
            
            // risk levels
            for(RiskLevel rl: world.get_risk_levels())
                outstream.println("dlc " + rl.get_id() + " " + rl.get_color().getRed() + " " + rl.get_color().getGreen() + " " + rl.get_color().getBlue() + " " + rl.get_description());
            
            // areas
            for(Area a: world.get_areas()){
                boolean is_in_use = false;
                for(Place place: world.get_places()) if(place.get_area() == a){
                    is_in_use = true;
                    break;
                }
                if(is_in_use){
                    outstream.println("a " + a.get_id() + " " + a.get_name());
                    outstream.println("acol " + a.get_color().getRed() + " " + a.get_color().getGreen() + " " + a.get_color().getBlue());
                }
            }
            // layers (for quadtree optimization)
            for(Layer l: world.get_layers()){
                outstream.println("lc " + l.get_id() + " " + l.get_center_x() + " " + l.get_center_y());
            }
            
            // places
            for(Place p: world.get_places()){
                outstream.println("p " + p.get_id() + " " + p.get_name());
                outstream.println("ppos " + p.get_layer() + " " + p.get_x() + " " + p.get_y());
                if(p.get_area() != null) outstream.println("par " + p.get_area().get_id());
                
                // paths
                for(Path path: p.get_paths()){
                    Place other_place = path.get_other_place(p);
                    
                    if(compatibility_mudmap_1) // deprecated path format
                        outstream.println("pw " + other_place.get_id() + " " + path.get_exit(p));
                    
                    // new path format
                    if(path.get_places()[0] == p) // only one of both places should describe the path
                        outstream.println("pp " + other_place.get_id() + "$" + path.get_exit(p) + "$" + path.get_exit(other_place));
                }
                
                // risk level and recommended level
                outstream.println("pdl " + p.get_risk_level().get_id());
                if(p.get_rec_lvl_min() != -1 || p.get_rec_lvl_max() != -1) outstream.println("prl " + p.get_rec_lvl_min() + " " + p.get_rec_lvl_max());
                
                // children
                for(Place child: p.get_children()) outstream.println("pchi " + child.get_id());
                // comments
                for(String comment: p.get_comments()) outstream.println("pcom " + comment);
                // flags
                for(Map.Entry<String, Boolean> flag: p.get_flags().entrySet()){
                    if(flag.getValue()) outstream.println("pb " + flag.getKey());
                }
            }
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write world file " + mudmap2.Paths.get_config_file());
            JOptionPane.showMessageDialog(null, "Couldn't write world file!", "Saving world", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(World.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
}
