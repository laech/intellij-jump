# Jump

Jump to any visible location of any visible editor quickly, using the keyboard.

This package provides a single action *Jump to Char*, which you can assign a
keyboard shortcut to, under keymap settings -> Plug-ins -> Jump. When invoked,
it will ask you to enter the character you want to jump to, then all these
characters in all editors in all windows will be replaced by quick key overlays,
to go to one, simply type in the overlay keys.

![screenshot](screenshot.png)

# Build

To build the plugin locally, run `./gradlew clean build`, then
build/distributions/intellij-jump-xxx.zip will be created.
It can be installed by going to the IntelliJ's plugins settings,
and choose install from disk.
