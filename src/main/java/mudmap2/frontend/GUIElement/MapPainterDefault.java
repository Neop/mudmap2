/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.frontend.GUIElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import mudmap2.utils.Pair;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import static mudmap2.frontend.WorldTab.getShowPathsCurved;

/**
 *
 * @author Neop
 */
public class MapPainterDefault implements MapPainter {

    static final float path_stroke_width = 3;

    static final float tile_selection_stroke_width = 3;
    static final java.awt.Color tile_selection_color = new java.awt.Color(255, 0, 0);

    static final float tile_risk_level_stroke_width = 2;
    static final int tile_border_width = 10;
    static final int exit_circle_radius = 5;

    static final int draw_text_threshold = 40;

    HashSet<Place> place_group;
    WorldCoordinate place_group_shift_start, place_group_shift_end;
    int place_selected_x, place_selected_y;
    boolean place_selection_enabled;

    double graphics_width, graphics_height;
    int tile_size;
    WorldCoordinate cur_pos;

    Font last_tile_font;

    public MapPainterDefault() {
        place_group = null;
        place_selected_x = place_selected_y = 0;
        place_selection_enabled = false;
        last_tile_font = null;
    }

    @Override
    public void setPlaceGroup(HashSet<Place> group, WorldCoordinate shift_start, WorldCoordinate shift_end) {
        place_group = group;
        place_group_shift_start = shift_start;
        place_group_shift_end = shift_end;
    }

    @Override
    public void setPlaceSelection(int x, int y) {
        place_selected_x = x;
        place_selected_y = y;
    }

    @Override
    public void setPlaceSelectionEnabled(boolean b) {
        place_selection_enabled = b;
    }

    /**
     * Returns true if the tile is large enough to draw text
     * @return
     */
    private boolean getTileDrawText(){
        return tile_size >= draw_text_threshold;
    }

    /**
     * Gets the current tile border area size
     * @return area border width
     */
    private int getTileBorderWidth(){
        // with interpolation for smooth transition
        return (int) Math.round(tile_border_width * Math.min(1.0, Math.max(0.5, (double) (tile_size - 20) / 80)));
    }

    /**
     * Gets the radius of the exit circles / dots
     * @return
     */
    private int getExitCircleRadius(){
        return (int) Math.round(exit_circle_radius * Math.min(1.0, Math.max(0.5, (double) (tile_size - 20) / 80)));
    }

    /**
     * Gets the stroke width of the tile selection box
     * @return
     */
    private float getTileSelectionStrokeWidth(){
        return tile_selection_stroke_width * (float) (1.0 + tile_size / 200.0);
    }

    /**
     * Gets the stroke width of the risk level border
     * @return
     */
    private float getRiskLevelStrokeWidth(){
        return tile_risk_level_stroke_width * (float) (1.0 + tile_size / 200.0);
    }

    /**
     * Gets the path stroke width
     * @return
     */
    private float getPathStrokeWidth(){
        return path_stroke_width * (float) (1.0 + tile_size / 200.0);
    }

    public Font getTileFont(){
        return last_tile_font;
    }

    /**
     * Returns true, if a place is selected by group selection
     * @param place
     * @return
     */
    private boolean placeGroupIsSelected(Place place){
        if(place != null){
            if(place_group_shift_end != null && place_group_shift_start != null
                && place_group_shift_end.getLayer() == place.getLayer().getId()){
                int x1 = (int) Math.round(place_group_shift_end.getX());
                int x2 = (int) Math.round(place_group_shift_start.getX());
                int y1 = (int) Math.round(place_group_shift_end.getY());
                int y2 = (int) Math.round(place_group_shift_start.getY());

                int x_min = Math.min(x1, x2);
                int x_max = Math.max(x1, x2);
                int y_min = Math.min(y1, y2);
                int y_max = Math.max(y1, y2);

                if(place.getX() >= x_min && place.getX() <= x_max
                    && place.getY() >= y_min && place.getY() <= y_max) return true;
            }
            if(place_group != null && place_group.contains(place)) return true;
        }
        return false;
    }

    /**
     * Calculates the offset of the exit visualization (dot/circle) to the
     * upper left corner of a tile
     * @param dir eit direction
     * @param x_offset reference to the x offset
     * @param y_offset reference to the y offset
     * @return false if the dot/circle doesn't have to be drawn
     */
    private Pair<Integer, Integer> getExitOffset(String dir){
        Pair<Integer, Integer> ret = new Pair<>(0, 0);
        int border_width = getTileBorderWidth();
        switch (dir) {
            case "n":
                // north
                ret.first = tile_size / 2;
                ret.second = border_width;
                break;
            case "e":
                // east
                ret.first = tile_size - border_width;
                ret.second = tile_size / 2;
                break;
            case "s":
                // south
                ret.first = tile_size / 2;
                ret.second = tile_size - border_width;
                break;
            case "w":
                // west
                ret.first = border_width;
                ret.second = tile_size / 2;
                break;
            case "ne":
                // north-east
                ret.first = tile_size - border_width;
                ret.second = border_width;
                break;
            case "se":
                // south-east
                ret.first = ret.second = tile_size - border_width;
                break;
            case "nw":
                // north-west
                ret.first = ret.second = border_width;
                break;
            case "sw":
                // south-west
                ret.first = border_width;
                ret.second = tile_size - border_width;
                break;
            default:
                ret.first = ret.second = tile_size / 2;
                break;
        }
        return ret;
    }

    /**
    * Gets the normal vector of an exit
    * @param dir exit direction
    * @return normal vector
    */
    private Pair<Double, Double> getExitNormal(String dir){
        Pair<Double, Double> ret = new Pair<>(0.0, 0.0);
        switch (dir) {
            case "n":
                ret.first = 0.0;
                ret.second = 1.0;
                break;
            case "e":
                ret.first = 1.0;
                ret.second = 0.0;
                break;
            case "s":
                ret.first = 0.0;
                ret.second = -1.0;
                break;
            case "w":
                ret.first = -1.0;
                ret.second = 0.0;
                break;
            case "ne":
                ret.first = 1.0;
                ret.second = 1.0;
                break;
            case "se":
                ret.first = 1.0;
                ret.second = -1.0;
                break;
            case "nw":
                ret.first = -1.0;
                ret.second = 1.0;
                break;
            case "sw":
                ret.first = -1.0;
                ret.second = -1.0;
                break;
        }
        // normalize it
        if(ret.first != 0.0 && ret.second != 0.0){
            double length = Math.sqrt(ret.first * ret.first + ret.second * ret.second);
            ret.first /= length;
            ret.second /= length;
        }
        return ret;
    }

    /**
     * fits the string to max_width in px, cuts it at whitespaces, if possible
     * @param str string to be fitted
     * @param fm fontmetrics
     * @param max_length maximum length of the string in pixel
     * @param max_lines maximum number of lines
     * @return a list of strings
     */
    private LinkedList<String> fitLineLength(String str, FontMetrics fm, int max_length, int max_lines){
        LinkedList<String> ret;
        if(fm.stringWidth(str) <= max_length){ // string isn't too long, return it
            ret = new LinkedList<>();
            ret.add(str);
        } else { // string is too long
            // roughly fit the string
            int strlen = Math.min(str.length(), max_length / fm.charWidth('.'));

            // find last ' ' before max_length, if there is no ' ' cut the
            // string at max_length
            while(fm.stringWidth(str.substring(0, strlen)) > max_length){
                int whitespace = str.substring(0, strlen).lastIndexOf(' ');
                // if there is still a whitespace: cut the string
                if(whitespace != -1) strlen = whitespace;
                else {
                    // if there is no whitespace fit the string length to the line pixel width
                    int lenpx = fm.stringWidth(str.substring(0, (int) Math.ceil(strlen / 1.5)));
                    while(lenpx > max_length){
                        strlen = (int) Math.ceil(strlen / 1.5);
                        lenpx = fm.stringWidth(str.substring(0, strlen));
                        //if(lenpx < max_length) strlen *= 1.5;
                    }
                    break;
                }
            }

            // cut the next part and return it, abbreviate the string if the max line number is reached
            if(max_lines > 0){
                ret = fitLineLength(str.substring(strlen).trim(), fm, max_length, max_lines - 1);
                ret.addFirst(str.substring(0, strlen));
            } else {
                ret = new LinkedList<>();
                if(strlen > 3) ret.add(str.substring(0, strlen - 3) + "...");
                else ret.add("...");
            }
        }
        return ret;
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param place_x a world (place) coordinate (x axis)
     * @return a screen coordinate x
     */
    private int getScreenPosX(int place_x){
        double screen_center_x = (graphics_width / tile_size) / 2; // note: wdtwd2
        int place_x_offset = (int) (Math.round(cur_pos.getX()) - Math.round(screen_center_x));
        return (int)((place_x - place_x_offset + remint(screen_center_x) - remint(cur_pos.getX())) * tile_size);
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param place_y a world (place) coordinate (y axis)
     * @return a screen coordinate y
     */
    private int getScreenPosY(int place_y){
        double screen_center_y = (graphics_height / tile_size) / 2;
        int place_y_offset = (int) (Math.round(cur_pos.getY()) - Math.round(screen_center_y));
        return (int)((-place_y + place_y_offset - remint(screen_center_y) + remint(cur_pos.getY())) * tile_size + graphics_height);
    }

    /**
     * Checks whether a place is currently drawn on the screen
     * @param place
     * @return
     */
    private boolean isOnScreen(Place place){
        int x = getScreenPosX(place.getX());
        if(x < 0 || x > graphics_width) return false;

        int y = getScreenPosY(place.getY());
        /*
        if(y < 0 || y > graphics_height) return false;
        else return true;*/
        return !(y < 0 || y > graphics_height);
    }

    /**
     * Remove integer part, the part after the point remains
     * @param val
     * @return
     */
    private double remint(double val){
        return val - Math.round(val);
    }

    @Override
    public void paint(Graphics g, int tile_size, double graphics_width, double graphics_height, Layer layer, WorldCoordinate cur_pos) {
        this.graphics_width = graphics_width;
        this.graphics_height = graphics_height;
        this.tile_size = tile_size;
        this.cur_pos = cur_pos;

        last_tile_font = g.getFont();

        final float selection_stroke_width = getTileSelectionStrokeWidth();
        final int tile_border_width_scaled = getTileBorderWidth();

        // max number of text lines tht fit in a tile
        FontMetrics fm = g.getFontMetrics();
        final int max_lines = (int) Math.floor((double)(tile_size - 3 * (tile_border_width_scaled + (int) Math.ceil(getRiskLevelStrokeWidth()))) / fm.getHeight());

        // screen center in world coordinates
        final double screen_center_x = (graphics_width / tile_size) / 2.0; // note: wdtwd2
        final double screen_center_y = (graphics_height / tile_size) / 2.0;

        final int place_x_offset = (int) (Math.round((float) cur_pos.getX()) - Math.round(screen_center_x));
        final int place_y_offset = (int) (Math.round((float) cur_pos.getY()) - Math.floor(screen_center_y));

        // more precalculation
        final double place_x_px_const = remint(screen_center_x) - remint(cur_pos.getX());
        final double place_y_px_const = remint(screen_center_y) + remint(cur_pos.getY());

        // prepare graphic for paths
        // Paths will be drawn on this graphic and later on copied to g
        ArrayList<Pair<Integer, Integer>> tile_positions = new ArrayList<>(); // to mask out the tile positions on graphic_path
        BufferedImage image_path = new BufferedImage((int) graphics_width, (int) graphics_height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphic_path = image_path.getGraphics();
        ((Graphics2D) graphic_path).setStroke(new BasicStroke(getPathStrokeWidth()));
        ((Graphics2D) graphic_path).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // get the locations of copied places
        HashSet<Pair<Integer, Integer>> copied_place_locations = mudmap2.CopyPaste.get_copy_place_locations();

        // clear screen
        g.clearRect(0, 0, (int) graphics_width + 1, (int) graphics_height + 1);

        // ------------------ draw the tiles / places ----------------------
        for(int tile_x = (g.getClipBounds().x / tile_size) - 1; tile_x < graphics_width / tile_size + 1; ++tile_x){
            for(int tile_y = (g.getClipBounds().y / tile_size) - 1; tile_y < graphics_height / tile_size + 1; ++tile_y){

                // place position on the map
                int place_x = tile_x + place_x_offset;
                int place_y = (int)(graphics_height / tile_size) - tile_y + place_y_offset;

                if(layer != null && layer.exist(place_x, place_y)){
                    Place cur_place = (Place) layer.get(place_x, place_y);

                    // place position in pixel on the screen
                    int place_x_px = (int) Math.round((tile_x + place_x_px_const) * tile_size);
                    int place_y_px = (int) Math.round((tile_y + place_y_px_const) * tile_size);

                    tile_positions.add(new Pair<>(place_x_px, place_y_px));

                    // number of drawn text lines
                    int line_num = 0;

                    // draw area color
                    if(cur_place.getArea() != null){
                        g.setColor(cur_place.getArea().getColor());
                        g.fillRect(place_x_px, place_y_px, tile_size, tile_size);
                    }

                    // draw tile center color
                    if(getTileDrawText()){
                        g.setColor(layer.getWorld().getTileCenterColor());
                        g.fillRect(place_x_px + tile_border_width_scaled, place_y_px + tile_border_width_scaled,
                                tile_size - 2 * tile_border_width_scaled, tile_size - 2 * tile_border_width_scaled);
                    }

                    // draw risk level border
                    if(cur_place.getRiskLevel() != null){
                        g.setColor(cur_place.getRiskLevel().getColor());
                        ((Graphics2D)g).setStroke(new BasicStroke(getRiskLevelStrokeWidth()));
                        g.drawRect(place_x_px + tile_border_width_scaled, place_y_px + tile_border_width_scaled,
                                tile_size - 2 * tile_border_width_scaled - (int) (0.5 * getRiskLevelStrokeWidth()),
                                tile_size - 2 * tile_border_width_scaled - (int) (0.5 * getRiskLevelStrokeWidth()));
                    }

                    // draw text, if tiles are large enough
                    if(getTileDrawText()){
                        g.setColor(Color.BLACK);

                        // place name
                        // gets place name if unique, else place name with ID
                        String place_name = ((layer.getWorld().isPlaceNameUnique(cur_place.getName()) && layer.getWorld().getShowPlaceId() == World.ShowPlaceID.UNIQUE) || layer.getWorld().getShowPlaceId() == World.ShowPlaceID.NONE)
                                                ? cur_place.getName() : cur_place.toString();
                        LinkedList<String> line = fitLineLength(place_name, fm, (int) (tile_size - 2 * (tile_border_width_scaled + selection_stroke_width)), max_lines);
                        for(String str: line){
                            g.drawString(str,
                                    place_x_px + tile_border_width_scaled + (int) selection_stroke_width + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                    place_y_px + tile_border_width_scaled + fm.getHeight() * (1 + line_num));
                            line_num++;
                        }

                        if(line_num < max_lines){ // it isn't unusual for some places to fill up all the lines
                            // recommended level
                            int reclvlmin = cur_place.getRecLevelMin(), reclvlmax = cur_place.getRecLevelMax();
                            if(reclvlmin > -1 || reclvlmax > -1){
                                g.drawString("lvl " + (reclvlmin > -1 ? reclvlmin : "?") + " - " + (reclvlmax > -1 ? reclvlmax : "?"),
                                        place_x_px + tile_border_width_scaled + (int) selection_stroke_width + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                        place_y_px + tile_border_width_scaled + fm.getHeight() * (1 + line_num));
                                line_num++;
                            }

                            // sub areas / children
                            if(line_num < max_lines && !cur_place.getChildren().isEmpty()){
                                int children_num = cur_place.getChildren().size();
                                String sa_str = "sa" + (children_num > 1 ? " (" + cur_place.getChildren().size() + "): " : ": ");

                                boolean first_child = true;
                                for(Place child: cur_place.getChildren()){
                                    sa_str += (first_child ? "" : ", ") + child.getName();
                                    first_child = false;
                                }
                                line = fitLineLength(sa_str, fm, (int) (tile_size - 2 * (tile_border_width_scaled + selection_stroke_width)), max_lines - line_num);
                                for(String str: line){
                                    g.drawString(str,
                                            place_x_px + tile_border_width_scaled + (int) selection_stroke_width + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                            place_y_px + tile_border_width_scaled + fm.getHeight() * (1 + line_num));
                                    line_num++;
                                }
                            }

                            // flags
                            if(line_num < max_lines){
                                String flags = "";
                                // place has comments
                                if(!cur_place.getComments().isEmpty()) flags += "C";
                                if(!cur_place.getChildren().isEmpty()) flags += "Sa";
                                if(!cur_place.getParents().isEmpty()) flags += "Pa";

                                // other flags
                                for(Map.Entry<String, Boolean> flag: cur_place.getFlags().entrySet()){
                                    if(flag.getValue()) flags += flag.getKey().toUpperCase();
                                    if(fm.stringWidth(flags) >= tile_size - 2 * tile_border_width_scaled) break;
                                }

                                // draw flags
                                g.drawString(flags,
                                        place_x_px + tile_border_width_scaled + (int) Math.ceil(2 * selection_stroke_width),
                                        place_y_px + tile_size - tile_border_width_scaled - (int) Math.ceil(2 * selection_stroke_width));
                            }
                        }
                    }

                    // mark place group selection
                    if(placeGroupIsSelected(cur_place)){
                        g.setColor(new Color(255, 255, 255, 128));
                        g.fillRect(place_x_px, place_y_px, tile_size, tile_size);
                    }

                    // draw path lines here
                    boolean exit_up = false, exit_down = false;
                    for(Path path: cur_place.getPaths()){
                        Place other_place = path.getOtherPlace(cur_place);

                        Color color_place1 = layer.getWorld().getPathColor(path.getExitDirections()[0]);
                        Color color_place2 = layer.getWorld().getPathColor(path.getExitDirections()[1]);
                        if(path.getPlaces()[0] != cur_place) {
                            Color tmp = color_place1;
                            color_place1 = color_place2;
                            color_place2 = tmp;
                        }

                        // if both places of a path are on the same layer and at least one of the two places is on the screen
                        // usually the main place (path.getPlaces()[0]) draws the path. If it isn't on screen, the other place draws it
                        if(Objects.equals(other_place.getLayer().getId(), layer.getId()) && (path.getPlaces()[0] == cur_place || !isOnScreen(other_place))){
                            Pair<Integer, Integer> exit_offset = getExitOffset(path.getExit(cur_place));
                            Pair<Integer, Integer> exit_offset_other = getExitOffset(path.getExit(other_place));

                            boolean draw_curves = getShowPathsCurved();

                            // exit positions on the map
                            double exit1x = place_x_px + exit_offset.first;
                            double exit1y = place_y_px + exit_offset.second;
                            double exit2x = place_x_px + (other_place.getX() - cur_place.getX()) * tile_size + exit_offset_other.first;
                            double exit2y = place_y_px - (other_place.getY() - cur_place.getY()) * tile_size + exit_offset_other.second;

                            if(color_place1.equals(color_place2)){ // same color
                                ((Graphics2D) graphic_path).setPaint(color_place1);
                            } else { // draw gradient
                                GradientPaint gp = new GradientPaint((float) exit1x, (float) exit1y, color_place1,
                                                                     (float) exit2x, (float) exit2y, color_place2);
                                ((Graphics2D) graphic_path).setPaint(gp);
                            }

                            if(draw_curves){
                                Pair<Double, Double> normal1 = getExitNormal(path.getExit(cur_place));
                                Pair<Double, Double> normal2 = getExitNormal(path.getExit(other_place));

                                double dx = exit2x - exit1x;
                                double dy = exit2y - exit1y;

                                if(draw_curves = Math.sqrt(dx * dx + dy * dy) >= 1.5 * tile_size){
                                    CubicCurve2D c = new CubicCurve2D.Double();
                                    c.setCurve(// point 1
                                            exit1x, exit1y,
                                            // point 2
                                            exit1x + normal1.first * tile_size, exit1y - normal1.second * tile_size,
                                            // point 3
                                            exit2x + normal2.first * tile_size, exit2y - normal2.second * tile_size,
                                            // point 4
                                            exit2x, exit2y);
                                    ((Graphics2D) graphic_path).draw(c);
                                }
                            }

                            if(!draw_curves) {
                                graphic_path.drawLine((int) exit1x, (int) exit1y, (int) exit2x, (int) exit2y);
                            }
                        }

                        // draw exit dots, if tiles are larger than 20
                        if(tile_size >= 20){
                            g.setColor(color_place1);
                            String exit = path.getExit(cur_place);
                            switch (exit) {
                                case "u":
                                    exit_up = true;
                                    break;
                                case "d":
                                    exit_down = true;
                                    break;
                                default:
                                    Pair<Integer, Integer> exit_offset = getExitOffset(exit);
                                    if(exit_offset.first != tile_size / 2 || exit_offset.second != tile_size / 2){
                                        int exit_circle_radius2 = getExitCircleRadius();
                                        g.fillOval(place_x_px + exit_offset.first - exit_circle_radius2, place_y_px + exit_offset.second - exit_circle_radius2, 2 * exit_circle_radius2, 2 * exit_circle_radius2);
                                    }   break;
                            }
                        }
                    }

                    // draw exits
                    if(tile_size >= 20){
                        // the up / down flags have to be drawn after the
                        // exits to know whether they have to be drawn
                        if((exit_up || exit_down) && getTileDrawText() && line_num <= max_lines){
                            g.setColor(Color.BLACK);
                            // have some arrows: ￪￬ ↑↓
                            String updownstr = "" + (exit_up ? "↑" : "") + (exit_down ? "↓" : "");
                            g.drawString(updownstr,
                                    place_x_px + tile_size - tile_border_width_scaled - fm.stringWidth(updownstr) - (int) Math.ceil(2 * selection_stroke_width),
                                    place_y_px + tile_size - tile_border_width_scaled - (int) Math.ceil(2 * selection_stroke_width));
                        }
                    }
                }

                if(copied_place_locations != null){
                    boolean location_found = false;
                    for(Pair<Integer, Integer> location: copied_place_locations){
                        if(location.first == place_x - place_selected_x && location.second == place_y - place_selected_y){
                            location_found = true;
                            break;
                        }
                    }

                    if(location_found){
                        int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.getX())) * tile_size); // alternative: get_screen_pos_x();
                        int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.getY())) * tile_size);

                        g.setColor(Color.BLUE);
                        ((Graphics2D)g).setStroke(new BasicStroke(selection_stroke_width));


                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width + tile_size / 4), (int) (place_y_px + selection_stroke_width));

                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px + selection_stroke_width));

                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width + tile_size  / 4), (int) (place_y_px - selection_stroke_width + tile_size));

                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px - selection_stroke_width + tile_size));
                    }
                }

                // draw cursor / place selection
                if(place_selection_enabled && place_x == place_selected_x && place_y == place_selected_y){
                    int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.getX())) * tile_size); // alternative: get_screen_pos_x();
                    int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.getY())) * tile_size);

                    g.setColor(tile_selection_color);
                    ((Graphics2D)g).setStroke(new BasicStroke((selection_stroke_width)));

                    g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                    g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width + tile_size / 4), (int) (place_y_px + selection_stroke_width));

                    g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                    g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px + selection_stroke_width));

                    g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                    g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width + tile_size  / 4), (int) (place_y_px - selection_stroke_width + tile_size));

                    g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                    g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px - selection_stroke_width + tile_size));
                }
            }
        }

        // mask out tile positions on graphic_path
        ((Graphics2D) graphic_path).setBackground(new Color(0,0,0,0));
        int clear_tile_size = tile_size - 2 * tile_border_width_scaled;
        for(Pair<Integer, Integer> p: tile_positions)
            //graphic_path.clearRect(p.first, p.second, p.first + tile_size, p.second + tile_size);
            graphic_path.clearRect(p.first + tile_border_width_scaled, p.second + tile_border_width_scaled, clear_tile_size, clear_tile_size);

        // draw graphic_path to g
        g.drawImage(image_path, 0, 0, null);
        graphic_path.dispose();
    }

}
