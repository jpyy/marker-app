Android app
---------------------
Build:
This folder is an Android Studio 1.0 project.
You can just import and build it.

Usage:
1. Open the application on your phone.
2. Open Settings from the overflow menu in the action bar.
3. Set the server URL to "tcp://x.x.x.x:5000" where x.x.x.x
   is the IP address of the computer running the Pupil software.
4. Exit out of the application completely and restart it.


Pupil plugin
---------------------
Installation:
1. Install the official Pupil software from https://github.com/pupil-labs/pupil
   Revision 96d30afd on 28 October 2014 is known to work with the plugin.
2. Copy pupil-plugin/AndroidPlugin.py into <pupil install>/pupil_src/shared_modules.
3. In <pupil install>/pupil_src/capture/world.py, do the following modifications:
    Add
      from AndroidPlugin import AndroidPlugin
    to the list of imports at the top and
      g_pool.plugins.append(AndroidPlugin(g_pool))
    where other plugins are loaded (line 342 in the aforementioned revision).

Usage:
1. Start Pupil Capture
2. Start the marker detection and server plugins from the controls
3. Configure the server plugin:
   Set the URL to "tcp://x.x.x.x:5000" where x.x.x.x is the computer's IP address
   on the same network the phone is on and press ENTER.
   Make sure to let the application through the firewall.
4. Configure the Marker detection plugin:
  a. Start the Android app
  b. Place the phone in the world camera's field of view such that the camera
     can see all four markers on the screen.
  c. Add a new surface and name it "phone" (without the quotes)
5. Switch on "real data" in the android coordinate plugin window

