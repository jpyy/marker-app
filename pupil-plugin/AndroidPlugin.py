# example of a plugin with atb bar and its own private window + gl-context:

from glfw import *
from plugin import Plugin

from ctypes import c_int,c_bool,c_float
import atb
from gl_utils import adjust_gl_view,clear_gl_screen,basic_gl_setup


# window calbacks
def on_resize(window,w, h):
    active_window = glfwGetCurrentContext()
    glfwMakeContextCurrent(window)
    adjust_gl_view(w,h,window)
    glfwMakeContextCurrent(active_window)

class AndroidPlugin(Plugin):

    def __init__(self,g_pool,atb_pos=(0,0)):
        Plugin.__init__(self)

        self.window_should_open = False
        self.window_should_close = False
        self._window = None
        self.fullscreen = c_bool(0)
        self.realdata = c_bool(False)
        self.x = c_float(0)
        self.y = c_float(0)

        atb_label = "android coordinate plugin"
        # Creating an ATB Bar.
        self._bar = atb.Bar(name =self.__class__.__name__, label=atb_label,
            help="ref detection parameters", color=(50, 50, 50), alpha=100,
            text='light', position=atb_pos,refresh=.3, size=(300, 100))
        self._bar.add_var("Real data", self.realdata)
        self._bar.add_var("X", self.x)
        self._bar.define("min=0 max=1 step=0.01", "X")
        self._bar.add_var("Y", self.y)
        self._bar.define("min=0 max=1 step=0.01", "Y")


    def on_key(self,window, key, scancode, action, mods):
        if not atb.TwEventKeyboardGLFW(key,int(action == GLFW_PRESS)):
            if action == GLFW_PRESS:
                if key == GLFW_KEY_ESCAPE:
                    pass



    def update(self,frame,recent_pupil_positions,events):
        x = None
        y = None
        if self.realdata.value:
            for p in recent_pupil_positions:
                if "realtime gaze on phone" in p:
                    x, y = p["realtime gaze on phone"]
                    y = 1 - y
        else:
            x = self.x.value
            y = self.y.value
        
        if x != None and y != None:
            events.append({'type': 'android_coordinates', 
                           'x': x,
                           'y': y,
                           'timestamp' : frame.timestamp})


    def gl_display(self):
        """
        use gl calls to render on world window
        """

        # gl stuff that will show on the world window goes here:

        pass





    def cleanup(self):
        """gets called when the plugin get terminated.
        This happends either volunatily or forced.
        if you have an atb bar or glfw window destroy it here.
        """
        self._bar.destroy()
