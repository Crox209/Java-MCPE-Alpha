#!/bin/bash
export DISPLAY=:99
export LIBGL_ALWAYS_SOFTWARE=1
export MESA_GL_VERSION_OVERRIDE=3.3
export MESA_GLSL_VERSION_OVERRIDE=330

cd /workspaces/Java-MCPE-Alpha/MinecraftJava
./gradlew run