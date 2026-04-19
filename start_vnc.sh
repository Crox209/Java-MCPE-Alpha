#!/bin/bash
export DISPLAY=:99
export LIBGL_ALWAYS_SOFTWARE=1
export MESA_GL_VERSION_OVERRIDE=3.3
export MESA_GLSL_VERSION_OVERRIDE=330

rm -f /tmp/.X99-lock
Xvfb :99 -screen 0 1280x720x24 &
XVFB_PID=$!
sleep 2

# Start a simple window manager
openbox-session &

# Start VNC server (No password for local container)
x11vnc -display :99 -nopw -listen localhost -xkb -ncache 10 -ncache_cr -forever &

# Start WebSockets proxy for NoVNC
websockify --web /usr/share/novnc 6080 localhost:5900 &
WEBSOCKIFY_PID=$!

echo "==========================================================="
echo "🎮 VNC Server is running!"
echo "Go to the 'PORTS' tab in VS Code and click the local address for port 6080."
echo "Click 'Connect' (no password) to see the display."
echo "==========================================================="

wait $XVFB_PID