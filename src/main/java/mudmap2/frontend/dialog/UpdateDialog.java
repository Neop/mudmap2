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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mudmap2.frontend.GUIElement.LinkLabel;
import org.json.JSONObject;

/**
 * This dialog checks for new releases on GitHub and shows a download link
 * @author neop
 */
public class UpdateDialog extends ActionDialog {

    final String urlLatest = "https://github.com/Neop/mudmap2/releases/latest";
    final String urlApiLatest = "https://api.github.com/repos/Neop/mudmap2/releases/latest";

    JPanel panel = null;

    public UpdateDialog(JFrame parent) {
        super(parent, "Update MUD Map", true);
    }

    @Override
    protected void create() {
        setLayout(new BorderLayout());

        panel = new JPanel();
        add(panel, BorderLayout.CENTER);

        panel.add(new JLabel("Checking for updates"));

        JButton button_ok = new JButton("Close");
        add(button_ok, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);

        (new Updater()).start();
    }

    private void setNoNewVersion(){
        remove(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel, BorderLayout.CENTER);

        panel.add(new JLabel("You are using the most recent version of MUD Map."));

        pack();
    }

    private void setNewVersion(String version){
        remove(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel, BorderLayout.CENTER);

        panel.add(new JLabel("There is a new version of MUD Map: " + version));
        panel.add(new LinkLabel("Download it here", urlLatest));

        pack();
    }

    private void setFailure(){
        remove(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel, BorderLayout.CENTER);

        panel.add(new JLabel("Check for updates failed."));
        panel.add(new LinkLabel("Latest release", urlLatest));

        pack();
    }

    private class Updater extends Thread {

        @Override
        public void run() {
            boolean success = false;

            try {
                URL url = new URL(urlApiLatest);
                String data = "";
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String line;
                    while((line = reader.readLine()) != null){
                        data = data + line;
                    }
                }

                if(!data.isEmpty()){
                    JSONObject jRoot = new JSONObject(data);

                    if(jRoot.has("tag_name")){
                        String tagName = jRoot.getString("tag_name");

                        String localVersion = AboutDialog.class.getPackage().getImplementationVersion();
                        if(localVersion == null){
                            /* version information is only included in packaged
                             * JAR release
                             */
                            setNewVersion(tagName);
                        } else {
                            if(isNewer(tagName, localVersion)){
                                setNewVersion(tagName);
                            } else {
                                setNoNewVersion();
                            }
                            success = true;
                        }
                    }
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(UpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(!success){
                setFailure();
            }
        }

        boolean isNewer(String recentVersion, String localVersion){
            try {
                recentVersion = recentVersion.replaceAll("[^0-9.]", "");
                localVersion = localVersion.replaceAll("[0-9.]^", "");

                String[] recentSplit = recentVersion.split("\\.");
                String[] localSplit = localVersion.split("\\.");

                for(int i = 0; i < recentSplit.length && i < localSplit.length; ++i){
                    Integer recentVal = Integer.decode(recentSplit[i]);
                    Integer localVal = Integer.decode(localSplit[i]);

                    if(recentVal > localVal){
                        return true;
                    } else if(recentVal < localVal){
                        break;
                    }
                }
            } catch (NumberFormatException ex){
                Logger.getLogger(UpdateDialog.class.getName()).log(Level.SEVERE, null, ex);
            }

            return false;
        }

    }

}
