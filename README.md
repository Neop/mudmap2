#MUD Map v2 (mudmap2)

A mapping tool for text-based games, like text adventures, MUDs and MUSHs
An improved and compatible rewrite of MUD Map v1

license: GPLv3
use it on your own risk!

##MUD Map Websites:
GitHub (Sources, Information, etc): https://github.com/Neop/mudmap2
Sourceforge (download packages, MUD Map v1 + v2): https://sourceforge.net/projects/mudmap/
MUD Map v1 user manual / quick start guide: http://mudmap.sf.net

Developer contact deatails can be found in doc/README.

If you encounter any bugs or other problems please contact me or write
a bug report on GitHub (https://github.com/Neop/mudmap2/issues), so I
can fix it.

##Some notes about the beta version:
Everything important (except for moving places) should be implemented.
If you think I forgot something, please contact me or write a short bug
report on GitHub.

Things that aren't implemented yet:
- curved paths
- moveable places (copy-paste)
- areas can't be removed from the worlds
- portable mode
- an unimportant, but interesting feature that I don't want to talk
about, yet ;)

If you really need one of these things, please use MUD Map v1 or contact
me.

##Installation
--tbf-- (try to run mudmap2.jar with a jre)

##Updating
Just replace the old mudmap2.jar file with a new one.

##Compatibility to MUD Map 1
MUD Map 2 uses an improved version of the file format of MUD Map 1. It
can read worlds that were created with MUD Map 1 and saves them in a
compatible format if compatibility is enabled (is enabled by 
default) Note that MUD Map 1 might be updated to use the same file
format after version 1.4

##How to use MUD Map:
I didn't create a manual for MUD Map 2 yet, so please refer to the 
manual of MUD Map 1, which is very similar: http://mudmap.sf.net
If you've still questions, feel free to contact me ;)

###Keyboard commands
Keyboard commands can be used to navigate and alter the map if keyboard
place selection is enabled by pressing p. A red box should be visible on
the screen. You might have to click at the map once after you opened it
for it to be able to receive keyboard events.

The keys used here are not final and might not be optimal. Please let me
know if you think that other keys should be used ;)

			context menu key	show the context menu of the selected
								place (like right click on that place)

+/- 		page up/down		increment and decrement the tile size
p:								enable / disable place selection

w/a/s/d 	arrow keys			shift the selection
e 			insert key			create / edit place at selection
f								create or remove a placeholder place
r 			delete key			remove selected place
c								edit place comments
q								modify area of selected place. Note that
								this also affects other places that
								belong to the same area. If no place is
								selected or selection is disabled, a new
								area will be created
h			home key			go to home location
l								show place list
