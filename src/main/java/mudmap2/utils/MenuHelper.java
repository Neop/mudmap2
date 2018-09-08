package mudmap2.utils;

import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;

public class MenuHelper {

    /**
     * Build a JCheckBoxMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param changeListener the ChangeListener to be added
     * @return
     */
    public static JCheckBoxMenuItem addCheckboxMenuItem(final JMenu parent, final String label, final ChangeListener changeListener) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
        item.addChangeListener(changeListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JCheckBoxMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param changeListener the ChangeListener to be added
     * @return
     */
    public static JCheckBoxMenuItem addCheckboxMenuItem(final JMenu parent, final String label, final KeyStroke keystroke, final ChangeListener changeListener) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
        item.addChangeListener(changeListener);
        item.setAccelerator(keystroke);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenu and add it to the given JMenuBar
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenu addMenu(final JMenu parent, final String label, final String tooltip) {
        final JMenu menu = new JMenu(label);
        menu.setToolTipText(tooltip);
        parent.add(menu);
        return menu;
    }

    /**
     * Build a JMenu and add it to the given JMenuBar
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param mnemonic the key code which represents the mnemonic
     * @return
     */
    public static JMenu addMenu(final JMenuBar parent, final String label, final int mnemonic) {
        final JMenu menu = new JMenu(label);
        menu.setMnemonic(mnemonic);
        parent.add(menu);
        return menu;
    }

    /**
     * Build a JMenu and add it to the given JMenuItem
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param mnemonic the key code which represents the mnemonic
     * @return
     */
    public static JMenu addMenu(final JMenuItem parent, final String label, final int mnemonic) {
        final JMenu menu = new JMenu(label);
        menu.setMnemonic(mnemonic);
        parent.add(menu);
        return menu;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionListener the ActionListener to be added
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final ActionListener actionListener, final String tooltip) {
        final JMenuItem item = new JMenuItem(label);
        item.setToolTipText(tooltip);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param mnemonic the key code which represents the mnemonic
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final int mnemonic, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.addActionListener(actionListener);
        item.setMnemonic(mnemonic);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param mnemonic the key code which represents the mnemonic
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final int mnemonic, final KeyStroke keystroke, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.setMnemonic(mnemonic);
        item.setAccelerator(keystroke);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final KeyStroke keystroke, final ActionListener actionListener, final String tooltip) {
        final JMenuItem item = new JMenuItem(label);
        item.setToolTipText(tooltip);
        item.addActionListener(actionListener);
        item.setAccelerator(keystroke);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionCommand the action command for the menu item
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final String actionCommand, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.setActionCommand(actionCommand);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionCommand the action command for the menu item
     * @param actionListener the ActionListener to be added
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final String actionCommand, final ActionListener actionListener, final String tooltip) {
        final JMenuItem item = new JMenuItem(label);
        item.setActionCommand(actionCommand);
        item.addActionListener(actionListener);
        item.setToolTipText(tooltip);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionCommand the action command for the menu item
     * @param mnemonic the key code which represents the mnemonic
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final String actionCommand, final int mnemonic, final KeyStroke keystroke, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.setActionCommand(actionCommand);
        item.setMnemonic(mnemonic);
        item.setAccelerator(keystroke);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionCommand the action command for the menu item
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JMenu parent, final String label, final String actionCommand, final KeyStroke keystroke, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.setActionCommand(actionCommand);
        item.setAccelerator(keystroke);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JPopupMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionListener the ActionListener to be added
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenuItem addMenuItem(final JPopupMenu parent, final String label, final ActionListener actionListener, final String tooltip) {
        final JMenuItem item = new JMenuItem(label);
        item.setToolTipText(tooltip);
        item.addActionListener(actionListener);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JPopupMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JPopupMenu parent, final String label, final KeyStroke keystroke, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.addActionListener(actionListener);
        item.setAccelerator(keystroke);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JPopupMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param actionCommand the action command for the menu item
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @return
     */
    public static JMenuItem addMenuItem(final JPopupMenu parent, final String label, final String actionCommand, final KeyStroke keystroke, final ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(label);
        item.addActionListener(actionListener);
        item.setAccelerator(keystroke);
        item.setActionCommand(actionCommand);
        parent.add(item);
        return item;
    }

    /**
     * Build a JMenuItem and add it to the given JPopupMenu
     *
     * @param parent the parent container
     * @param label the String used to set the text
     * @param keystroke the KeyStroke which will serve as an accelerator
     * @param actionListener the ActionListener to be added
     * @param tooltip the string to display; if the text is null, the tool tip is turned off for this component
     * @return
     */
    public static JMenuItem addMenuItem(final JPopupMenu parent, final String label, final KeyStroke keystroke, final ActionListener actionListener, final String tooltip) {
        final JMenuItem item = new JMenuItem(label);
        item.setToolTipText(tooltip);
        item.addActionListener(actionListener);
        item.setAccelerator(keystroke);
        parent.add(item);
        return item;
    }

}
