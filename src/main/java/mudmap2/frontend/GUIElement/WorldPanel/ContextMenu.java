package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import mudmap2.CopyPaste;
import mudmap2.backend.Layer;
import mudmap2.backend.LayerElement;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.frontend.dialog.PathConnectDialog;
import mudmap2.frontend.dialog.PathConnectNeighborsDialog;
import mudmap2.frontend.dialog.PlaceDialog;
import mudmap2.frontend.dialog.PlaceRemoveDialog;
import mudmap2.frontend.dialog.PlaceSelectionDialog;
import mudmap2.frontend.dialog.placeGroup.PlaceGroupDialog;
import mudmap2.utils.KeystrokeHelper;
import mudmap2.utils.MenuHelper;
import mudmap2.utils.PlaceXComparator;
import mudmap2.utils.PlaceYComparator;
import mudmap2.utils.StringHelper;

// constructs the context menu (on right click)
public class ContextMenu extends JPopupMenu implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final String ACTION_FIND_PATH = "find_path";
    private static final String ACTION_PASTE = "paste";
    private static final String ACTION_CUT = "cut";
    private static final String ACTION_COPY = "copy";
    private static final String ACTION_CONNECT_CHILD = "connect_child";
    private static final String ACTION_CREATE_CHILD_NEW_LAYER = "create_child_new_layer";
    private static final String ACTION_CREATE_PLACEHOLDER = "create_placeholder";
    private static final String ACTION_EXPAND_ALL = "expand_all";
    private static final String ACTION_EXPAND_NORTH = "expand_north";
    private static final String ACTION_EXPAND_NORTHEAST = "expand_northeast";
    private static final String ACTION_EXPAND_EAST = "expand_east";
    private static final String ACTION_EXPAND_SOUTHEAST = "expand_southeast";
    private static final String ACTION_EXPAND_SOUTH = "expand_south";
    private static final String ACTION_EXPAND_SOUTHWEST = "expand_southwest";
    private static final String ACTION_EXPAND_WEST = "expand_west";
    private static final String ACTION_EXPAND_NORTHWEST = "expand_northwest";

    final WorldPanel parent;
    final Layer layer; //map
    final Place place;
    final Integer posX;
    final Integer posY;

    /**
     * Constructs a context menu at position (x,y)
     * @param parent
     * @param px screen / panel coordinate x
     * @param py screen / panel coordinate y
     */
    public ContextMenu(final WorldPanel parent, final Integer px, final Integer py) {
        this.parent = parent;
        addPopupMenuListener(new TabContextPopMenuListener(parent));
        final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(parent);

        this.posX = px;
        this.posY = py;
        layer = parent.getWorld().getLayer(parent.getPosition().getLayer());
        place = layer != null ? layer.get(posX, posY) : null;

        parent.setCursor(posX, posY);

        if (layer != null && place != null) { // if place exists
            if (!parent.isPassive()) {
                MenuHelper.addMenuItem(this, "Edit place", KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), new PlaceDialog(rootFrame, parent.getWorld(), place));

                final HashSet<Place> placeGroup = parent.placeGroupGetSelection();

                if (placeGroup.isEmpty()) {
                    MenuHelper.addMenuItem(this, "Remove place", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new PlaceRemoveDialog(rootFrame, parent.getWorld(), place), "Remove this place");
                } else {
                    MenuHelper.addMenuItem(this, "*Remove places", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new PlaceRemoveDialog(rootFrame, parent.getWorld(), placeGroup), "Remove all selected places");
                }

                if (placeGroup.isEmpty()) {
                    MenuHelper.addMenuItem(this, "Edit place group", new PlaceGroupDialog(rootFrame, parent.getWorld(), place), "Edit the place group of this place");
                }
            }

            // ------------- Paths ------------------
            final JMenu mPaths = new JMenu("Paths / Exits");
            if (!parent.isPassive() || !place.getPaths().isEmpty()) {
                add(mPaths);
            }

            if (!parent.isPassive()) {
                final JMenu mPathConnect = MenuHelper.addMenu(mPaths, "Connect", "Connect a path from this place to another one");
                MenuHelper.addMenuItem(mPathConnect, "Select", KeystrokeHelper.ctrl(KeyEvent.VK_NUMPAD5), new PathConnectDialog(parent.getParentFrame(), place), "Select any place from the map");
                MenuHelper.addMenuItem(mPathConnect, "Neighbors", new PathConnectNeighborsDialog(rootFrame, place), "Choose from surrounding places");

                final LinkedList<Place> places = layer.getNeighbors(posX, posY, 1);
                if (!places.isEmpty()) {
                    mPathConnect.addSeparator();

                    for (final LayerElement neighbor : places) {
                        // only show, if no connection exists, yet
                        if (place.getPaths((Place) neighbor).isEmpty()) {
                            String dir1 = "", dir2 = "";

                            if (neighbor.getY() > place.getY()) {
                                dir1 = "n";
                                dir2 = "s";
                            } else if (neighbor.getY() < place.getY()) {
                                dir1 = "s";
                                dir2 = "n";
                            }
                            if (neighbor.getX() > place.getX()) {
                                dir1 = StringHelper.join(dir1, "e");
                                dir2 = StringHelper.join(dir2, "w");
                            } else if (neighbor.getX() < place.getX()) {
                                dir1 = StringHelper.join(dir1, "w");
                                dir2 = StringHelper.join(dir2, "e");
                            }

                            // if exits aren't occupied yet -> add menu item
                            if (place.getPathsTo(dir1).isEmpty() && ((Place) neighbor).getPathsTo(dir2).isEmpty()) {
                                final JMenuItem mi_path_connect = MenuHelper.addMenuItem(mPathConnect, StringHelper.join("[", dir1, "] ", ((Place) neighbor).getName()), new ConnectPathActionListener(place, (Place) neighbor, dir1, dir2));

                                // add accelerator
                                final int dirnum = Path.getDirNum(dir1);
                                if (dirnum > 0 & dirnum <= 9) {
                                    mi_path_connect.setAccelerator(KeystrokeHelper.ctrl(KeyEvent.VK_NUMPAD0 + dirnum));
                                }
                            }
                        }
                    }
                }
            }

            // getPlace all connected places
            final HashSet<Path> paths = place.getPaths();

            if (!paths.isEmpty()) {
                JMenu mPathRemove = null;
                if (!parent.isPassive()) {
                    mPathRemove = MenuHelper.addMenu(mPaths, "Remove", "Remove a path");
                    mPaths.addSeparator();
                }

                for (final Path path : paths) {
                    final Place otherPlace = path.getOtherPlace(place);
                    MenuHelper.addMenuItem(mPaths, StringHelper.join("Go to [", path.getExit(place), "] ", otherPlace.getName()), new GotoPlaceActionListener(parent, otherPlace));

                    if (!parent.isPassive()) {
                        final String dir = path.getExit(place);
                        final JMenuItem miPathRemove = MenuHelper.addMenuItem(mPathRemove, StringHelper.join("Remove [", dir, "] ", otherPlace.getName()), new RemovePathActionListener(path));

                        // add accelerator
                        final int dirnum = Path.getDirNum(dir);
                        if (dirnum > 0 & dirnum <= 9) {
                            miPathRemove.setAccelerator(KeystrokeHelper.alt(KeyEvent.VK_NUMPAD0 + dirnum));
                        }
                    }
                }

                if (!parent.isPassive()) {
                    mPaths.addSeparator();
                    MenuHelper.addMenuItem(mPaths, "Find shortest path", ContextMenu.ACTION_FIND_PATH, this);
                }
            }

            // ------------- layers / maps ------------------
            final JMenu mChildren = new JMenu("Maps");
            mChildren.setToolTipText("Related places, eg. for maps within maps");
            if (!parent.isPassive() || !place.getChildren().isEmpty()) {
                add(mChildren);
            }

            if (!parent.isPassive()) {
                MenuHelper.addMenuItem(mChildren, "Connect with existing place", ContextMenu.ACTION_CONNECT_CHILD, this, StringHelper.join("Connect another place with \"", place.getName(), "\""));
                MenuHelper.addMenuItem(mChildren, "New place on new map", ContextMenu.ACTION_CREATE_CHILD_NEW_LAYER, this, StringHelper.join("Creates a new place on a new map layer and connects it with \"", place.getName(), "\""));
            }

            final HashSet<Place> children = place.getChildren();
            if (!children.isEmpty()) {
                if (!parent.isPassive()) {
                    final JMenu m_sa_remove = new JMenu("Remove");
                    mChildren.add(m_sa_remove);

                    for (final Place child : children) {
                        MenuHelper.addMenuItem(m_sa_remove, StringHelper.join("Remove ", child.getName(), " (", child.getLayer().getName(), ")"), new RemoveChildrenActionListener(place, child));
                    }
                }

                mChildren.addSeparator();

                for (final Place child : children) {
                    MenuHelper.addMenuItem(mChildren, StringHelper.join("Go to ", child.getName(), " (", child.getLayer().getName(), ")"), new GotoPlaceActionListener(parent, child));
                }
            }

            final HashSet<Place> parents = place.getParents();
            if (!parents.isEmpty()) {
                mChildren.addSeparator();

                for (final Place child : parents) {
                    MenuHelper.addMenuItem(mChildren, StringHelper.join("Go to ", child.getName(), " (", child.getLayer().getName(), ")"), new GotoPlaceActionListener(parent, child));
                }
            }

        } else { // if layer doesn't exist or no place exists at position x,y
            MenuHelper.addMenuItem(this, "New place", KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), new PlaceDialog(rootFrame, parent.getWorld(), layer, posX, posY));
            MenuHelper.addMenuItem(this, "New placeholder", ContextMenu.ACTION_CREATE_PLACEHOLDER, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), this);
        }

        //"Expand" menu
        addExpansionMenu();

        // cut / copy / paste for selected places
        final boolean can_paste = layer != null && mudmap2.CopyPaste.canPaste(posX, posY, layer);
        final boolean has_paste_places = layer != null && mudmap2.CopyPaste.hasCopyPlaces();
        final boolean has_selection = parent.placeGroupHasSelection();

        if (layer != null && place != null || has_selection || has_paste_places) {
            addSeparator();
        }

        if (layer != null && place != null || has_selection) {
            MenuHelper.addMenuItem(this, StringHelper.join("Cut", has_selection ? " selection" : " place"), ContextMenu.ACTION_CUT, KeystrokeHelper.ctrl(KeyEvent.VK_X), this);
            MenuHelper.addMenuItem(this, StringHelper.join("Copy", has_selection ? " selection" : " place"), ContextMenu.ACTION_COPY, KeystrokeHelper.ctrl(KeyEvent.VK_C), this);
        }

        if (has_paste_places) {
            final JMenuItem miPastePlace = MenuHelper.addMenuItem(this, "Paste", ContextMenu.ACTION_PASTE, KeystrokeHelper.ctrl(KeyEvent.VK_V), this);
            if (!can_paste) {
                miPastePlace.setEnabled(false);
            }
        }

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFrame rootFrame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());

        switch (e.getActionCommand()) {
            case ContextMenu.ACTION_CREATE_PLACEHOLDER:
                parent.getWorld().putPlaceholder(parent.getPosition().getLayer(), posX, posY);
                repaint();
                break;
            case ContextMenu.ACTION_CREATE_CHILD_NEW_LAYER:
                // create new place
                final PlaceDialog dlg = new PlaceDialog(rootFrame, parent.getWorld(), null, 0, 0);
                dlg.setVisible(true);

                final Place place_new = dlg.getPlace();
                if (place_new != null) {
                    // connect new place with place as a child
                    place.connectChild(place_new);
                    // go to new place
                    parent.pushPosition(place_new.getCoordinate());
                }
                break;
            case ContextMenu.ACTION_CONNECT_CHILD:
                final PlaceSelectionDialog psdlg1 = new PlaceSelectionDialog(rootFrame, parent.getWorld(), parent.getPosition(), true);
                psdlg1.setVisible(true);
                final Place child = psdlg1.getSelection();
                if (psdlg1.getSelected() && child != null && child != place) {
                    final int ret = JOptionPane.showConfirmDialog(rootFrame, StringHelper.join("Connect \"", child.getName(), "\" to \"", place.getName(), "\"?"), "Connect child place", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        place.connectChild(child);
                        repaint();
                    }
                }
                break;
            case ContextMenu.ACTION_COPY:
                if (parent.placeGroupHasSelection()) {
                    mudmap2.CopyPaste.copy(parent.placeGroupGetSelection(), posX, posY);
                } else {
                    final HashSet<Place> set = new HashSet<>();
                    set.add(place);
                    mudmap2.CopyPaste.copy(set, posX, posY);
                }
                repaint();
                break;
            case ContextMenu.ACTION_CUT:
                if (parent.placeGroupHasSelection()) {
                    mudmap2.CopyPaste.cut(parent.placeGroupGetSelection(), posX, posY);
                } else {
                    final HashSet<Place> set = new HashSet<>();
                    set.add(place);
                    mudmap2.CopyPaste.cut(set, posX, posY);
                }
                repaint();
                break;
            case ContextMenu.ACTION_PASTE:
                mudmap2.CopyPaste.paste(posX, posY, layer);
                repaint();
                break;
            case ContextMenu.ACTION_FIND_PATH:
                final PlaceSelectionDialog psdlg2 = new PlaceSelectionDialog(rootFrame, parent.getWorld(), parent.getPosition(), true);
                psdlg2.setVisible(true);
                final Place end = psdlg2.getSelection();
                if (end != null) {
                    parent.placeGroupReset();
                    Place place_it = parent.getWorld().breadthSearch(place, end);
                    if (place_it == null) {
                        parent.callMessageListeners("No Path found");
                        JOptionPane.showMessageDialog(this, StringHelper.join("Could not find path to ", end.getName()));
                    } else {
                        int path_length = 0;
                        while (place_it != null) {
                            parent.getPlaceGroup().add(place_it);
                            place_it = place_it.getBreadthSearchData().predecessor;
                            ++path_length;
                        }
                        //repaint();
                        parent.worldChanged(place); // workaround: why doesn't repaint work?
                        parent.callMessageListeners(StringHelper.join("Path found, length: ", path_length - 1));
                    }

                }
                break;
            case ContextMenu.ACTION_EXPAND_ALL:
                //north
                for (final Place pl : layer.getPlacesList(PlaceYComparator.BACKWARD)) {
                    if (pl.getY() > posY) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() + 1);
                    }
                }
                //east
                for (final Place pl : layer.getPlacesList(PlaceXComparator.BACKWARD)) {
                    if (pl.getX() > posX) {
                        CopyPaste.move(pl, pl.getX() + 1, pl.getY());
                    }
                }
                //south
                for (final Place pl : layer.getPlacesList(PlaceYComparator.FORWARD)) {
                    if (pl.getY() < posY) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() - 1);
                    }
                }
                //west
                for (final Place pl : layer.getPlacesList(PlaceXComparator.FORWARD)) {
                    if (pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX() - 1, pl.getY());
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_NORTH:
                for (final Place pl : layer.getPlacesList(PlaceYComparator.BACKWARD)) {
                    if (pl.getY() > posY) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() + 1);
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_NORTHEAST:
                //if match north AND east, move north
                for (final Place pl : layer.getPlacesList(PlaceYComparator.BACKWARD)) {
                    if (pl.getY() > posY && pl.getX() > posX) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() + 1);
                    }
                }
                //if match north AND east, move east
                for (final Place pl : layer.getPlacesList(PlaceXComparator.BACKWARD)) {
                    if (pl.getY() > posY && pl.getX() > posX) {
                        CopyPaste.move(pl, pl.getX() + 1, pl.getY());
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_EAST:
                for (final Place pl : layer.getPlacesList(PlaceXComparator.BACKWARD)) {
                    if (pl.getX() > posX) {
                        CopyPaste.move(pl, pl.getX() + 1, pl.getY());
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_SOUTHEAST:
                //if match south AND east, move east
                for (final Place pl : layer.getPlacesList(PlaceXComparator.BACKWARD)) {
                    if (pl.getX() > posX && pl.getY() < posY) {
                        CopyPaste.move(pl, pl.getX() + 1, pl.getY());
                    }
                }
                //if match south AND east, move south
                for (final Place pl : layer.getPlacesList(PlaceYComparator.FORWARD)) {
                    if (pl.getX() > posX && pl.getY() < posY) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() - 1);
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_SOUTH:
                for (final Place pl : layer.getPlacesList(PlaceYComparator.FORWARD)) {
                    if (pl.getY() < posY) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() - 1);
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_SOUTHWEST:
                //if match south AND west, move south
                for (final Place pl : layer.getPlacesList(PlaceYComparator.FORWARD)) {
                    if (pl.getY() < posY && pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() - 1);
                    }
                }
                //if match south AND west, move west
                for (final Place pl : layer.getPlacesList(PlaceXComparator.FORWARD)) {
                    if (pl.getY() < posY && pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX() - 1, pl.getY());
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_WEST:
                for (final Place pl : layer.getPlacesList(PlaceXComparator.FORWARD)) {
                    if (pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX() - 1, pl.getY());
                    }
                }
                break;
            case ContextMenu.ACTION_EXPAND_NORTHWEST:
                //if match north AND west, move north
                for (final Place pl : layer.getPlacesList(PlaceYComparator.BACKWARD)) {
                    if (pl.getY() > posY && pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX(), pl.getY() + 1);
                    }
                }
                //if match north AND west, move west
                for (final Place pl : layer.getPlacesList(PlaceXComparator.FORWARD)) {
                    if (pl.getY() > posY && pl.getX() < posX) {
                        CopyPaste.move(pl, pl.getX() - 1, pl.getY());
                    }
                }
                break;
            default:
                System.out.println(StringHelper.join("Invalid action command ", e.getActionCommand()));
                JOptionPane.showMessageDialog(this, StringHelper.join("Runtime Error: Invalid action command ", e.getActionCommand()));
        }
    }

    private void addExpansionMenu() {
        if (layer != null && layer.getPlaces().size() > 0) {
            final int componentCount = getComponentCount();
            if (componentCount > 0 && getComponent(componentCount - 1) instanceof JSeparator == false) {
                addSeparator();
            }
            final JMenu expand = MenuHelper.addMenu(this, "Expand", "Make room around this Place in all directions");
            MenuHelper.addMenuItem(expand, "Expand All Directions", ContextMenu.ACTION_EXPAND_ALL, this, "Make room in all directions");
            MenuHelper.addMenuItem(expand, "Expand North", ContextMenu.ACTION_EXPAND_NORTH, this, "Make room to the north");
            MenuHelper.addMenuItem(expand, "Expand Northeast", ContextMenu.ACTION_EXPAND_NORTHEAST, this, "Make room to the northeast");
            MenuHelper.addMenuItem(expand, "Expand East", ContextMenu.ACTION_EXPAND_EAST, this, "Make room east");
            MenuHelper.addMenuItem(expand, "Expand Southeast", ContextMenu.ACTION_EXPAND_SOUTHEAST, this, "Make room southeast");
            MenuHelper.addMenuItem(expand, "Expand South", ContextMenu.ACTION_EXPAND_SOUTH, this, "Make room south");
            MenuHelper.addMenuItem(expand, "Expand Southwest", ContextMenu.ACTION_EXPAND_SOUTHWEST, this, "Make room southwest");
            MenuHelper.addMenuItem(expand, "Expand West", ContextMenu.ACTION_EXPAND_WEST, this, "Make room west");
            MenuHelper.addMenuItem(expand, "Expand Northwest", ContextMenu.ACTION_EXPAND_NORTHWEST, this, "Make room northwest");
        }
    }

}
