/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2014  Neop (email: mneop@web.de)
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
 *  A label that scrolls its text horizontally, if it is too small
 *  If a message is pushed to its stack, the message will be displayed
 *  for a short amount of time, then the default text will be displayed again
 */
package mudmap2.frontend.GUIElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

/**
 * A label that scrolls its text horizontally, if it is too small
 * If a message is pushed to its stack, the message will be displayed
 * for a short amount of time, then the default text will be displayed again
 * @author neop
 */
public final class ScrollLabel extends JPanel implements Runnable{

    Thread thread;
    final Thread parent;
    boolean run = true;

    LinkedList<String> messages;
    long message_start_time;

    String current_text, default_text;
    boolean is_default_text;

    final long min_message_time = 5000; // 4s
    final long wait_time = 2000; // wait a second before start moving

    public ScrollLabel() {
        super();
        messages = new LinkedList<>();
        is_default_text = true;
        setText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                nextMessage();
            }
        });
        parent = Thread.currentThread();
    }

    public ScrollLabel(String text){
        setText(text);
        is_default_text = true;
        messages = new LinkedList<>();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                nextMessage();
            }
        });
        parent = Thread.currentThread();
    }

    /**
     * Starts the thread
     */
    public void startThread(){
        run = true;
        if(thread == null){
            (thread = new Thread(this)).start();
        }
    }

    /**
     * Sets the default text
     * @param s
     */
    public void setText(String s){
        default_text = s;
        if(is_default_text) setDisplayedText(s);
    }

    /**
     * Gets the default text
     * @return
     */
    public String getText(){
        if(default_text == null) return "";
        return default_text;
    }

    /**
     * Stops the thread
     */
    public void stopThread(){
        if(thread != null && run){
            synchronized (Thread.currentThread()){
                try{
                    run = false;
                    Thread.currentThread().wait();
                } catch (InterruptedException e){}
            }
        } else run = false;
    }

    /**
     * Adds a messsage to the message stack
     * @param message
     */
    synchronized public void showMessage(String message){
        messages.addLast(message);
        if(is_default_text) nextMessage();
    }

    /**
     * Removes the currently shown message from the stack and shows the next one
     */
    synchronized private void nextMessage(){
        if(!(is_default_text = messages.isEmpty())){
            setDisplayedText(messages.pollFirst());
        } else setDisplayedText(getText());
    }

    /**
     * Sets the shown text and resets the message timer
     * @param s
     */
    synchronized private void setDisplayedText(String s){
        if(s == null) s = getText();
        current_text = s;
        message_start_time = getTimeMS();
        repaint();
    }

    /**
     * Get the current time in milliseconds
     */
    static private long getTimeMS(){
        return TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();

        final int border = 5;
        final int width = (int) (Math.ceil(clipBounds.getWidth())) - 2 * border;

        g.setColor(getBackground());
        g.fillRect(0, 0, (int) clipBounds.getWidth() + 1, (int) clipBounds.getHeight() + 1);

        if(current_text != null && !current_text.isEmpty()){
            int y = (int) ((clipBounds.getHeight() + g.getFontMetrics().getHeight() * 0.75) / 2);

            int string_width = g.getFontMetrics().stringWidth(current_text);
            int x = 0;

            long dtime = getTimeMS() - message_start_time; // time difference since message start

            // if string is too long: move the string
            if(string_width > width){
                if(dtime < wait_time){
                    x = 0;
                } else {
                    //double dtime_perc = ((double) (dtime - wait_time) / (min_message_time - wait_time)); // time difference in percent
                    double dx_perc = (double)(dtime - wait_time) / 5000;
                    //x = (int) -((double) (string_width - clipBounds.getWidth()) * dx_perc);
                    x = (int) (-dx_perc * 200);
                    if(Math.abs(x) >= 1.0 * string_width) nextMessage();
                }
            } else if(dtime > min_message_time) nextMessage();

            g.setColor(Color.BLACK);
            g.drawString(current_text, x + border, y);
        }

        g.setColor(getBackground());
        g.fillRect(width + border, 0, (int) clipBounds.getWidth() + 1, (int) clipBounds.getHeight() + 1);
    }

    @Override
    public void run() {
        long dtime;
        while(run){
            // time since the message wwas shown first
            dtime = getTimeMS() - message_start_time;
            // change message, if it was shown for some time
            /*if(dtime > min_message_time){
                if(!is_default_text) next_message();
                else message_start_time = get_time_ms();
            }*/

            repaint();

            try {
                Thread.sleep(40); // sleep 40ms -> 25 fps
            } catch(InterruptedException e){}
        }

        synchronized (parent){
            parent.notify();
        }
    }

}
