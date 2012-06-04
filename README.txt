GestureMath

Intro...

Database access:

This application was tested on an ASUS Transformer TF-101 tablet.  This affects the
way the application copies the SQLite database to the microSD card for browsing.
In GestureMathDataOpenHelper.backupDb(), we hard code the path to /Removable/MicroSD,
as the value returned by Environment.getExternalStorageDirectory() is /mnt/sdcard,
which doesn't work.  Using the File Manager application on the tablet reports the
correct path to the microSD card.

Simply use a microSD->SD card adapter to copy the .sqlite database file from the
microSD card to your local machine.  On Mac OS X, this file does not show up in
the Android File Transfer application window.