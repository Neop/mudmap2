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
package mudmap2.frontend.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.legend.Legend;
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.GUIElement.WorldPanel.WorldPanel;
import mudmap2.frontend.WorldTab;
import mudmap2.utils.Pair;

/**
 * Image export dialog
 * @author neop
 */
public class ExportImageDialog extends ActionDialog {

    final WorldTab worldTab;

    // gui components
    // general settings
    JRadioButton rbCurrentView, rbCurrentMap, rbAllMaps, rbSelection;
    JFileChooser fileChooser;
    JSpinner spTileSize;
    JLabel lImageSize;
    // background
    JRadioButton rbBackgroundTransparent, rbBackgroundColor;
    ColorChooserButton ccbBackgroundColor;
    JCheckBox cbBackgroundGrid;
    // legend
    JCheckBox cbLegendPathColors, cbLegendPlaceGroups, cbLegendRiskLevels;
    JRadioButton rbLegendPosTop, rbLegendPosBottom;
    JRadioButton rbLegendPosLeft, rbLegendPosRight;
    ColorChooserButton ccbLegendBackground;

    // state
    WorldCoordinate centerPosition = null;

    int imageWidth, imageHeight;

    public ExportImageDialog(JFrame parent, WorldTab tab){
        super(parent, "Export map to image", true);
        worldTab = tab;
    }

    /**
     * Creates the UI
     */
    @Override
    void create() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);

        panel.add(createScopePanel());
        panel.add(createTileSizePanel());
        panel.add(new JSeparator());
        panel.add(createBackgroundPanel());
        panel.add(new JSeparator());
        panel.add(createLegendPanel());
        panel.add(new JSeparator());
        panel.add(createFileChooserPanel());

        updateImageSize();
        updateDialogComponents();

        pack();
        setResizable(false);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2,
                getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    /**
     * creates UI panel for the export scope (what to export)
     * @return panel
     */
    JPanel createScopePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        rbCurrentView = new JRadioButton("current view");
        rbCurrentMap = new JRadioButton("current map");
        rbAllMaps = new JRadioButton("all maps");
        rbSelection = new JRadioButton("selected places");

        ButtonGroup bgScope = new ButtonGroup();
        bgScope.add(rbCurrentView);
        bgScope.add(rbCurrentMap);
        bgScope.add(rbAllMaps);
        bgScope.add(rbSelection);

        rbCurrentView.addActionListener(new UpdateImageSizeListener());
        rbCurrentMap.addActionListener(new UpdateImageSizeListener());
        rbAllMaps.addActionListener(new UpdateImageSizeListener());
        rbSelection.addActionListener(new UpdateImageSizeListener());

        rbCurrentView.setSelected(true);

        JLabel label = new JLabel("Export: ");
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        panel.add(label);

        panel.add(rbCurrentView);
        panel.add(rbCurrentMap);
        panel.add(rbAllMaps);
        panel.add(rbSelection);

        if(worldTab.getWorldPanel().placeGroupGetSelection().isEmpty()){
            rbSelection.setEnabled(false);
        }

        // TODO: read setting from file

        return panel;
    }

    /**
     * Creates UI panel for changing the tile size
     * @return panel
     */
    JPanel createTileSizePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(new JLabel("Tile size: "));

        spTileSize = new JSpinner(new SpinnerNumberModel(
                (int) worldTab.getWorldPanel().getTileSize(),
                WorldPanel.TILE_SIZE_MIN, WorldPanel.TILE_SIZE_MAX, 1));
        panel.add(spTileSize);

        spTileSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateImageSize();
            }
        });

        panel.add(lImageSize = new JLabel());

        return panel;
    }

    /**
     * Creats backround (color, grid) panel
     * @return panel
     */
    JPanel createBackgroundPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.gridx = constraints.gridy = 1;
        JLabel label = new JLabel("Background:");
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        panel.add(label, constraints);

        rbBackgroundTransparent = new JRadioButton("transparent");
        rbBackgroundColor = new JRadioButton("color");

        ButtonGroup bgBackground = new ButtonGroup();
        bgBackground.add(rbBackgroundTransparent);
        bgBackground.add(rbBackgroundColor);

        rbBackgroundTransparent.setSelected(true);

        constraints.gridx++;
        panel.add(rbBackgroundTransparent, constraints);
        constraints.gridx++;
        panel.add(rbBackgroundColor, constraints);

        constraints.gridx++;
        constraints.weightx = 4;
        ccbBackgroundColor = new ColorChooserButton(panel, Color.white);
        panel.add(ccbBackgroundColor, constraints);

        constraints.gridy++;
        constraints.gridx = 1;
        constraints.weightx = 0;

        cbBackgroundGrid = new JCheckBox("Draw grid");
        panel.add(cbBackgroundGrid, constraints);

        // TODO: read colors and settings

        return panel;
    }

    /**
     * Creates legend panel
     * @return panel
     */
    JPanel createLegendPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel pRow1 = new JPanel();
        pRow1.setLayout(new GridBagLayout());
        panel.add(pRow1);

        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.fill = GridBagConstraints.HORIZONTAL;
        constraints1.insets = new Insets(2, 2, 2, 2);
        constraints1.weightx = 1;

        constraints1.gridx = constraints1.gridy = 1;
        JLabel label = new JLabel("Legend:");
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        pRow1.add(label, constraints1);

        cbLegendPathColors = new JCheckBox("Path colors");
        cbLegendPlaceGroups = new JCheckBox("Place groups");
        cbLegendRiskLevels = new JCheckBox("Risk levels");

        constraints1.gridx++;
        pRow1.add(cbLegendPathColors, constraints1);
        constraints1.gridx++;
        pRow1.add(cbLegendPlaceGroups, constraints1);
        constraints1.gridx++;
        pRow1.add(cbLegendRiskLevels, constraints1);

        cbLegendPathColors.addActionListener(new UpdateDialogListener());
        cbLegendPlaceGroups.addActionListener(new UpdateDialogListener());
        cbLegendRiskLevels.addActionListener(new UpdateDialogListener());

        constraints1.gridy++;
        constraints1.gridx = 1;
        pRow1.add(new JLabel("Position:"), constraints1);

        rbLegendPosBottom = new JRadioButton("Bottom");
        rbLegendPosLeft = new JRadioButton("Left");
        rbLegendPosRight = new JRadioButton("Right");
        rbLegendPosTop = new JRadioButton("Top");

        ButtonGroup bgLegendPos = new ButtonGroup();
        bgLegendPos.add(rbLegendPosBottom);
        bgLegendPos.add(rbLegendPosLeft);
        bgLegendPos.add(rbLegendPosRight);
        bgLegendPos.add(rbLegendPosTop);

        rbLegendPosBottom.setSelected(true);

        constraints1.gridx++;
        pRow1.add(rbLegendPosTop, constraints1);
        constraints1.gridx++;
        pRow1.add(rbLegendPosBottom, constraints1);
        constraints1.gridx++;
        pRow1.add(rbLegendPosLeft, constraints1);
        constraints1.gridx++;
        pRow1.add(rbLegendPosRight, constraints1);


        JPanel pRow2 = new JPanel();
        pRow2.setLayout(new GridBagLayout());
        panel.add(pRow2);

        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.fill = GridBagConstraints.HORIZONTAL;
        constraints2.insets = new Insets(2, 2, 2, 2);

        constraints2.gridx = constraints1.gridy = 1;
        pRow2.add(new JLabel("Background color:"), constraints2);

        constraints2.gridx++;
        constraints2.weightx = 2;
        ccbLegendBackground = new ColorChooserButton(pRow2, Color.LIGHT_GRAY);
        pRow2.add(ccbLegendBackground, constraints2);

        // TODO: read color and settings

        return panel;
    }

    /**
     * Creates file chooser
     * @return file chooser
     */
    JFileChooser createFileChooserPanel(){
        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        FileFilter pngFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return  f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG images";
            }
        };

        fileChooser.addChoosableFileFilter(pngFileFilter);
        fileChooser.setFileFilter(pngFileFilter);

        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equalsIgnoreCase("ApproveSelection")){
                    save();
                } else {
                    dispose();
                }
            }
        });

        return fileChooser;
    }

    /**
     * Updates the displayed image size
     */
    void updateImageSize(){
        if(centerPosition == null){
            centerPosition = new WorldCoordinate(worldTab.getWorldPanel().getPosition());
        }

        Pair<Integer, Integer> imageSize = new Pair<>(0, 0);

        if(rbCurrentView.isSelected()){
            imageSize.first = worldTab.getWorldPanel().getWidth();
            imageSize.second = worldTab.getWorldPanel().getHeight();
        } else if(rbCurrentMap.isSelected()){
            Layer layer = worldTab.getWorld().getLayer(centerPosition.getLayer());
            imageSize = getMapSize(layer);
        } else if(rbAllMaps.isSelected()){
            for(Layer layer: worldTab.getWorld().getLayers()){
                // get largest image by area
                Pair<Integer, Integer> imageSize2 = getMapSize(layer);
                if(imageSize.first * imageSize.second < imageSize2.first * imageSize2.second){
                    imageSize = imageSize2;
                }
            }
        } else if(rbSelection.isSelected()){
            HashSet<Place> places = worldTab.getWorldPanel().placeGroupGetSelection();
            imageSize = getSelectionSize(places);
        }

        imageWidth = imageSize.first;
        imageHeight = imageSize.second;

        lImageSize.setText("Image size: " + imageWidth + "x" + imageHeight + "px");
    }

    void updateDialogComponents(){
        boolean legendEnabled = cbLegendPathColors.isSelected() ||
                cbLegendPlaceGroups.isSelected() ||
                cbLegendRiskLevels.isSelected();

        rbLegendPosBottom.setEnabled(legendEnabled);
        rbLegendPosLeft.setEnabled(legendEnabled);
        rbLegendPosRight.setEnabled(legendEnabled);
        rbLegendPosTop.setEnabled(legendEnabled);
    }

    /**
     * Calculates the size of a map
     * @param layer map
     * @return width and height
     */
    Pair<Integer, Integer> getMapSize(Layer layer){
        int mapXMax = layer.getXMax();
        int mapXMin = layer.getXMin();
        int mapYMax = layer.getYMax();
        int mapYMin = layer.getYMin();

        int tileSize = (int) spTileSize.getValue();

        int width = mapXMax - mapXMin + 1;
        int height = mapYMax - mapYMin + 1;

        return new Pair<>(width * tileSize, height * tileSize);
    }

    /**
     * Calculates the size needed to draw the selected places
     * @param places selected places
     * @return width and height
     */
    Pair<Integer, Integer> getSelectionSize(HashSet<Place> places){
        Pair<Integer, Integer> imageSize = new Pair<>(0, 0);
        if(!places.isEmpty()){
            // get bounds of selected places
            int mapXMax, mapXMin, mapYMax, mapYMin;

            Place place1 = places.iterator().next();
            mapXMax = mapXMin = place1.getX();
            mapYMax = mapYMin = place1.getY();

            for(Place place: places){
                mapXMax = Math.max(mapXMax, place.getX());
                mapXMin = Math.min(mapXMin, place.getX());
                mapYMax = Math.max(mapYMax, place.getY());
                mapYMin = Math.min(mapYMin, place.getY());
            }

            // calculate image size from bounds
            int tileSize = (int) spTileSize.getValue();
            imageSize.first = (mapXMax - mapXMin + 1) * tileSize;
            imageSize.second = (mapYMax - mapYMin + 1) * tileSize;
        }
        return imageSize;
    }

    /**
     * Calculates the selection center
     * @param places selected places
     * @return center coordinates
     */
    Pair<Double, Double> getSelectionCenter(HashSet<Place> places){
        Pair<Double, Double> selectionCenter = new Pair<>(0.0, 0.0);
        if(!places.isEmpty()){
            // get bounds of selected places
            int mapXMax, mapXMin, mapYMax, mapYMin;

            Place place1 = places.iterator().next();
            mapXMax = mapXMin = place1.getX();
            mapYMax = mapYMin = place1.getY();

            for(Place place: places){
                mapXMax = Math.max(mapXMax, place.getX());
                mapXMin = Math.min(mapXMin, place.getX());
                mapYMax = Math.max(mapYMax, place.getY());
                mapYMin = Math.min(mapYMin, place.getY());
            }

            // calculate selection center from bounds
            selectionCenter.first = 0.5 * (double) (mapXMax - mapXMin + 1) + (double) mapXMin;
            selectionCenter.second = 0.5 * (double) (mapYMax - mapYMin + 1) + (double) mapYMin - 1;
        }
        return selectionCenter;
    }

    /**
     * Exports image(s)
     */
    void save(){
        try {
            File file = fileChooser.getSelectedFile();
            if(!file.getName().toLowerCase().endsWith(".png"))
                file = new File(file.getAbsolutePath() + ".png");

            if(!rbAllMaps.isSelected()){ // draw current layer
                file.createNewFile();
                drawMap(file, centerPosition);
            } else { // draw each layer
                int ret = JOptionPane.showConfirmDialog(getParent(),
                        "" + worldTab.getWorld().getLayers().size()
                        + " layers will be exported.", "Export layer to image",
                        JOptionPane.OK_CANCEL_OPTION);

                if(ret == JOptionPane.OK_OPTION){
                    String filename = file.getAbsolutePath();
                    filename = filename.substring(0, filename.lastIndexOf('.'));

                    int num = 0;
                    for(Layer layer: worldTab.getWorld().getLayers()){
                        worldTab.showMessage("Exporting layer " + ++num + " of "
                                + worldTab.getWorld().getLayers().size());

                        centerPosition.setLayer(layer.getId());

                        file = new File(filename + layer.getId() + "_" + layer.getName() + ".png");
                        file.createNewFile();

                        drawMap(file, centerPosition);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ExportImageDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(getParent(),
                    "Could not export image " + ex.getLocalizedMessage(),
                    "Export layer to image", JOptionPane.ERROR_MESSAGE);
        }

        dispose();
    }

    /**
     * Generates image (map and legend)
     * @param file file to write
     * @param center layer and center information
     * @throws IOException
     */
    void drawMap(File file, WorldCoordinate center) throws IOException {
        if(file.canWrite()){
            // render images
            BufferedImage imageMap = drawLayer(center);
            BufferedImage imageLegend = null;

            if(imageMap != null){
                int imageOutWidth = imageMap.getWidth();
                int imageOutHeight = imageMap.getHeight();

                final boolean drawLegendPathColors = cbLegendPathColors.isSelected();
                final boolean drawLegendPlaceGroups = cbLegendPlaceGroups.isSelected();
                final boolean drawLegendRiskLevels = cbLegendRiskLevels.isSelected();
                final boolean legendEnabled = drawLegendPathColors
                        || drawLegendPlaceGroups
                        || drawLegendRiskLevels;

                if(legendEnabled){
                    imageLegend = drawLegend(
                            worldTab.getWorld().getLayer(center.getLayer()),
                            imageMap.getWidth(), imageMap.getHeight());

                    if(rbLegendPosTop.isSelected() || rbLegendPosBottom.isSelected()){
                        imageOutHeight += imageLegend.getHeight();
                    } else if(rbLegendPosLeft.isSelected() || rbLegendPosRight.isSelected()){
                        imageOutWidth += imageLegend.getWidth();
                    }
                }

                // create output image
                BufferedImage imageOut;
                Graphics graphics;
                if(rbBackgroundTransparent.isSelected()){
                    imageOut = new BufferedImage(imageOutWidth, imageOutHeight, BufferedImage.TYPE_INT_ARGB);
                    graphics = imageOut.getGraphics();
                    graphics.setClip(0, 0, imageOutWidth, imageOutHeight);

                    ((Graphics2D) graphics).setBackground(new Color(255, 255, 255, 0));
                    graphics.clearRect(0, 0, imageOutWidth, imageOutHeight);
                } else {
                    imageOut = new BufferedImage(imageOutWidth, imageOutHeight, BufferedImage.TYPE_INT_RGB);
                    graphics = imageOut.getGraphics();
                    graphics.setClip(0, 0, imageOutWidth, imageOutHeight);

                    ((Graphics2D) graphics).setBackground(ccbBackgroundColor.getColor());
                    graphics.clearRect(0, 0, imageOutWidth, imageOutHeight);
                }

                // combine images
                int mapX = 0, mapY = 0;
                int legendX = 0, legendY = 0;

                if(legendEnabled){
                    if(rbLegendPosBottom.isSelected()){
                        legendY = imageMap.getHeight();
                    } else if(rbLegendPosTop.isSelected()){
                        mapY = imageLegend.getHeight();
                    } else if(rbLegendPosLeft.isSelected()){
                        mapX = imageLegend.getWidth();
                    } else if(rbLegendPosRight.isSelected()){
                        legendX = imageMap.getWidth();
                    }
                    graphics.drawImage(imageMap, mapX, mapY, null);
                    graphics.drawImage(imageLegend, legendX, legendY, null);
                } else {
                    graphics.drawImage(imageMap, 0, 0, null);
                }

                ImageIO.write(imageOut, "PNG", file);
                worldTab.showMessage("Image " + file.getName() + " exported");
            } else {
                JOptionPane.showMessageDialog(getParent(),
                        "Could draw map " + file.getPath(),
                        "Export map to image", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(getParent(),
                    "Could not write to file " + file.getPath(),
                    "Export map to image", JOptionPane.ERROR_MESSAGE);
            worldTab.showMessage("Could not export image");
        }
    }

    /**
     * Draws map
     * @param center layer and center information
     * @return image
     */
    BufferedImage drawLayer(WorldCoordinate center){
        BufferedImage image = null;
        Integer tileSize = (Integer) spTileSize.getValue();
        int width, height;

        // get image size and map center
        if(rbSelection.isSelected()){
            HashSet<Place> places = worldTab.getWorldPanel().placeGroupGetSelection();
            Pair<Integer, Integer> selectionSize = getSelectionSize(places);
            width = selectionSize.first;
            height = selectionSize.second;

            Pair<Double, Double> selectionCenter = getSelectionCenter(places);
            center = new WorldCoordinate(center.getLayer(), selectionCenter.first, selectionCenter.second);
        } else if(rbCurrentView.isSelected()){
            Dimension size = worldTab.getWorldPanel().getSize();
            width = size.width;
            height = size.height;
        } else { // whole map
            Pair<Integer, Integer> mapSize = getMapSize(worldTab.getWorld().getLayer(center.getLayer()));
            width = mapSize.first;
            height = mapSize.second;

            Layer layer = worldTab.getWorld().getLayer(center.getLayer());
            Pair<Double, Double> exactCenter = layer.getExactCenter();
            center = new WorldCoordinate(layer.getId(), exactCenter.first, exactCenter.second);
        }

        if(width != 0 && height != 0){
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        if(image != null){
            Graphics graphics = image.createGraphics();
            graphics.setClip(0, 0, width, height);

            ((Graphics2D) graphics).setBackground(new Color(255, 255, 255, 0));
            graphics.clearRect(0, 0, width, height);

            graphics.setFont(worldTab.getFont());

            MapPainterDefault mappainter = new MapPainterDefault();
            mappainter.setGridEnabled(cbBackgroundGrid.isSelected());
            mappainter.paint(graphics, tileSize, width, height,
                    worldTab.getWorld().getLayer(center.getLayer()),
                    center);
        }
        return image;
    }

    /**
     * Draws legend
     * @param layer layer
     * @param mapGraphicsWidth
     * @param mapGraphicsHeight
     * @return image
     */
    BufferedImage drawLegend(Layer layer, int mapGraphicsWidth, int mapGraphicsHeight){
        BufferedImage image = null;

        final boolean drawLegendPathColors = cbLegendPathColors.isSelected();
        final boolean drawLegendPlaceGroups = cbLegendPlaceGroups.isSelected();
        final boolean drawLegendRiskLevels = cbLegendRiskLevels.isSelected();

        // TODO: make configurable: Orientation, entries
        Legend.Orientation orientation = Legend.Orientation.HORIZONTAL;
        int size = mapGraphicsWidth;
        if(rbLegendPosLeft.isSelected() || rbLegendPosRight.isSelected()){
            orientation = Legend.Orientation.VERTICAL;
            size = mapGraphicsHeight;
        }

        Legend legend = new Legend(layer, orientation, size);
        legend.setBackgroundColor(ccbLegendBackground.getColor());
        legend.setIncludePathColors(drawLegendPathColors);
        legend.setIncludePlaceGroups(drawLegendPlaceGroups);
        legend.setIncludeRiskLevels(drawLegendRiskLevels);

        try {
            image = legend.generate();
        } catch (Legend.RenderException ex) {
            Logger.getLogger(ExportImageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }

    /**
     * Listener for image/tile size changes
     */
    class UpdateImageSizeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            updateImageSize();
        }

    }

    /**
     * Listener for dialog button changes
     */
    class UpdateDialogListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            updateDialogComponents();
        }

    }

}
