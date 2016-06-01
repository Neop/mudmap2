/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.frontend.dialog;

import java.awt.Color;
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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.WorldCoordinate;
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.GUIElement.WorldPanel.WorldPanel;
import mudmap2.frontend.WorldTab;

/**
 *
 * @author Neop
 */
public class ExportImageDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    JFrame parent;
    WorldTab worldtab;

    JFileChooser filechooser;
    JRadioButton rb_layer, rb_cur_view, rb_selection, rb_each_layer;
    ButtonGroup rb_group;
    JSpinner spinner_tile_size;
    JLabel label_image_width;
    JCheckBox checkbox_transparent;
    ColorChooserButton colorchooser;

    int image_width, image_height;
    WorldCoordinate center_position;

    int x_min, x_max, y_min, y_max;
    int layer_x_min, layer_x_max, layer_y_min, layer_y_max;

    public ExportImageDialog(JFrame parent, WorldTab tab) {
        super(parent, "Export layer to image", true);
        this.parent = parent;
        worldtab = tab;
    }

    @Override
    void create() {
        Layer layer = worldtab.getWorld().getLayer(worldtab.getWorldPanel().getPosition().getLayer());
        layer_x_min = x_min = layer.getXMin();
        layer_x_max = x_max = layer.getXMax();
        layer_y_min = y_min = layer.getYMin();
        layer_y_max = y_max = layer.getYMax();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = constraints.gridy = 1;
        add(new JLabel("Export:"), constraints);
        ++constraints.gridx;
        add(rb_cur_view = new JRadioButton("current view"), constraints);
        ++constraints.gridx;
        add(rb_layer = new JRadioButton("everything on layer"), constraints);
        ++constraints.gridx;
        add(rb_selection = new JRadioButton("selection"), constraints);
        if(worldtab.getWorldPanel().placeGroupGetSelection().isEmpty()) rb_selection.setEnabled(false);
        ++constraints.gridx;
        add(rb_each_layer = new JRadioButton("each layer"), constraints);

        rb_group = new ButtonGroup();
        rb_group.add(rb_layer);
        rb_group.add(rb_cur_view);
        rb_group.add(rb_selection);
        rb_group.add(rb_each_layer);

        constraints.gridx = 1;
        ++constraints.gridy;

        add(new JLabel("Tile size:"), constraints);
        ++constraints.gridx;

        spinner_tile_size = new JSpinner(new SpinnerNumberModel((int) worldtab.getWorldPanel().getTileSize(),
                WorldPanel.TILE_SIZE_MIN, WorldPanel.TILE_SIZE_MAX, 1));
        add(spinner_tile_size, constraints);
        spinner_tile_size.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updateImageWidth();
            }
        });
        /*
        rb_cur_view.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                JRadioButton radiobutton = (JRadioButton) e.getSource();
                boolean enable = !(radiobutton).isSelected();
                if(!enable) spinner_tile_size.setValue(worldtab.getTileSize());

                spinner_tile_size.setEnabled(enable);
            }
        });*/
        rb_cur_view.setSelected(true);
        spinner_tile_size.setEnabled(false);
        rb_cur_view.addActionListener(new RadioButtonActionListener());
        rb_each_layer.addActionListener(new RadioButtonActionListener());
        rb_layer.addActionListener(new RadioButtonActionListener());
        rb_selection.addActionListener(new RadioButtonActionListener());

        ++constraints.gridx;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        add(label_image_width = new JLabel(), constraints);
        updateImageWidth();

        constraints.gridx = 1;
        constraints.gridwidth = 2;
        ++constraints.gridy;
        add(checkbox_transparent = new JCheckBox("Transparent Background"), constraints);
        checkbox_transparent.setSelected(true);

        constraints.gridwidth = 1;
        constraints.gridx += 2;
        add(new JLabel("Background color:"), constraints);

        ++constraints.gridx;
        constraints.fill = GridBagConstraints.BOTH;
        add(colorchooser = new ColorChooserButton(this, Color.white), constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        ++constraints.gridy;
        constraints.gridwidth = constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;

        add(filechooser = new JFileChooser(), constraints);
        filechooser.setDialogType(JFileChooser.SAVE_DIALOG);
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() ||
                       f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG images";
            }
        };
        filechooser.addChoosableFileFilter(filter);
        filechooser.setFileFilter(filter);

        filechooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equalsIgnoreCase("ApproveSelection"))
                    save();
                else dispose();
            }
        });

        pack();
        setResizable(false);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    private void save(){
        try {
            File file = filechooser.getSelectedFile();
            if(!file.getName().toLowerCase().endsWith(".png"))
                file = new File(file.getAbsolutePath() + ".png");

            if(!rb_each_layer.isSelected()){ // draw current layer
                file.createNewFile();
                drawLayer(file, center_position);
            } else { // draw each layer
                int ret = JOptionPane.showConfirmDialog(parent,
                        "Do you want to export " + worldtab.getWorld().getLayers().size() + " layers?", "Export layer to image",
                        JOptionPane.OK_CANCEL_OPTION);

                if(ret == JOptionPane.OK_OPTION){
                    String filename = file.getAbsolutePath();
                    filename = filename.substring(0, filename.lastIndexOf('.'));

                    int num = 0;
                    for(Layer layer: worldtab.getWorld().getLayers()){
                        worldtab.showMessage("Exporting layer " + ++num + " of " + worldtab.getWorld().getLayers().size());

                        center_position.setLayer(layer.getId());
                        x_min = layer.getXMin();
                        x_max = layer.getXMax();
                        y_min = layer.getYMin();
                        y_max = layer.getYMax();
                        updateImageWidth();

                        file = new File(filename + layer.getId() + ".png");
                        file.createNewFile();

                        drawLayer(file, center_position);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ExportImageDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(parent,
                "Could not export image " + ex.getLocalizedMessage(),
                "Export layer to image", JOptionPane.ERROR_MESSAGE);
        }

        dispose();
    }

    void drawLayer(File file, WorldCoordinate center) throws IOException{
        if(file.canWrite()){
            Integer tile_size = (Integer) spinner_tile_size.getValue();

            BufferedImage image;
            Graphics2D graphics;

            if(checkbox_transparent.isSelected()){
                image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);
                graphics = image.createGraphics();
                graphics.setClip(0, 0, image_width, image_height);
                graphics.setBackground(new java.awt.Color(255, 255, 255, 0));
            } else {
                image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_RGB);
                graphics = image.createGraphics();
                graphics.setClip(0, 0, image_width, image_height);
                graphics.setBackground(colorchooser.getColor());
            }
            graphics.setFont(worldtab.getFont());

            MapPainterDefault mappainter = new MapPainterDefault();
            mappainter.paint(graphics, tile_size, image_width, image_height,
                    worldtab.getWorld().getLayer(center.getLayer()),
                    center);

            ImageIO.write(image, "PNG", file);
            worldtab.showMessage("Image " + file.getName() + " exported");
        } else JOptionPane.showMessageDialog(parent,
            "Could not write to file " + file.getPath(),
            "Export layer to image", JOptionPane.ERROR_MESSAGE);
            worldtab.showMessage("Could not export image");
    }

    void updateImageWidth(){
        if(center_position == null) center_position = worldtab.getWorldPanel().getPosition().clone();

        if(rb_cur_view.isSelected()){
            center_position = worldtab.getWorldPanel().getPosition().clone();
            image_width = worldtab.getWorldPanel().getWidth();
            image_height = worldtab.getWorldPanel().getHeight();
        } else if(rb_layer.isSelected() || rb_each_layer.isSelected()){
            layer_x_max = x_max;
            layer_x_min = x_min;
            layer_y_max = y_max;
            layer_y_min = y_min;
        } else if(rb_selection.isSelected()){
            HashSet<Place> places = worldtab.getWorldPanel().placeGroupGetSelection();
            if(!places.isEmpty()){
                layer_x_max = layer_x_min = places.iterator().next().getX();
                layer_y_max = layer_y_min = places.iterator().next().getY();

                for(Place place: places){
                    layer_x_max = Math.max(layer_x_max, place.getX());
                    layer_x_min = Math.min(layer_x_min, place.getX());
                    layer_y_max = Math.max(layer_y_max, place.getY());
                    layer_y_min = Math.min(layer_y_min, place.getY());
                }
            } else layer_x_max = layer_x_min = layer_y_max = layer_y_min = 0;
        } else {
            image_height = image_width = 0;
        }

        if(rb_layer.isSelected() || rb_selection.isSelected() || rb_each_layer.isSelected()){
            Integer tile_size = (Integer) spinner_tile_size.getValue();

            image_width = layer_x_max - layer_x_min + 1;
            image_height = layer_y_max - layer_y_min + 1;
            center_position.setX(0.5 * (double) image_width + (double) layer_x_min);
            center_position.setY(0.5 * (double) image_height + (double) layer_y_min - 1);
            image_width *= tile_size;
            image_height *= tile_size;
        }

        if(rb_each_layer.isSelected()) label_image_width.setText("");
        else label_image_width.setText("Image size: " + image_width + "x" + image_height + "px");
    }

    private class RadioButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            spinner_tile_size.setEnabled(e.getSource() != rb_cur_view);

            updateImageWidth();
        }
    }
}
