=== GestureMath ===

This is an Android tablet application designed to replicate the study presented
by Dr. Susan Goldin-Meadow in her 2009 paper, "Gesturing Gives Children New Ideas
About Math".  This application follows the experimental design outlined in
experiment_design.txt.



=== Requirements ===

No effort was made to provide device compatibility for this application.  This
application was developed on an ASUS Transformer TF-101 tablet running Android
4.0.3.  This device has a 10.1" 1280x800 display.

The application also assumed that a MicroSD card is present.  More detail about
data retrieval in the next section.



=== Database access ===

This application was tested on an ASUS Transformer TF-101 tablet, which affects the
way the application copies the SQLite database to the microSD card for browsing.
In GestureMathDataOpenHelper.backupDb(), we hard code the path to /Removable/MicroSD,
as the value returned by Environment.getExternalStorageDirectory() is /mnt/sdcard,
which doesn't work.  Using the File Manager application on the tablet reports the
correct path to the microSD card.

Simply use a microSD->SD card adapter to copy the gesture_math.sqlite database file
from the microSD card to your local machine.  On Mac OS X, this file does seem to
show up in the Android File Transfer application window, but sometimes requires you
to disconnect and reconnect the device, perhaps to refresh the file list.

To use the applciation, a microSD card must be present, or else you have to comment
out some calls to backupDb() in the source code.

IMPORTANT:

If you change the database schema (defined in GestureMathDataOpenHelper.java), you
will need to uninstall the application from your device, and then reinstall from
Eclipse.  This is because the onCreate() method only gets called if the database
is not present.  Uninstalling the application is the easiest way to erase the database
from the device, unless you want to add some code to do so.