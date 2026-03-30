package com.minecraft.mcpe.client;

import java.awt.event.*;

/**
 * Handles keyboard and mouse input
 */
public class InputHandler extends KeyAdapter implements MouseListener, MouseMotionListener, MouseWheelListener {
    private boolean[] keyPressed = new boolean[256];
    private boolean[] keyReleased = new boolean[256];
    private int mouseX, mouseY;
    private int mouseLastX, mouseLastY;
    private int mouseDX, mouseDY;
    private boolean leftMousePressed;
    private boolean rightMousePressed;
    private boolean middleMousePressed;
    private boolean leftClickQueued;
    private boolean rightClickQueued;
    private boolean middleClickQueued;
    private int mouseWheelDelta;

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < 256) {
            keyPressed[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < 256) {
            keyPressed[code] = false;
            keyReleased[code] = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
            leftClickQueued = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightMousePressed = true;
            rightClickQueued = true;
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            middleMousePressed = true;
            middleClickQueued = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) leftMousePressed = false;
        if (e.getButton() == MouseEvent.BUTTON3) rightMousePressed = false;
        if (e.getButton() == MouseEvent.BUTTON2) middleMousePressed = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLastX = mouseX;
        mouseLastY = mouseY;
        mouseX = e.getX();
        mouseY = e.getY();
        mouseDX = mouseX - mouseLastX;
        mouseDY = mouseY - mouseLastY;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelDelta += e.getWheelRotation();
    }

    // Getters
    public boolean isKeyPressed(int keyCode) {
        return keyCode < 256 && keyPressed[keyCode];
    }

    public boolean isKeyReleased(int keyCode) {
        if (keyCode < 256 && keyReleased[keyCode]) {
            keyReleased[keyCode] = false;
            return true;
        }
        return false;
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public int getMouseDX() { return mouseDX; }
    public int getMouseDY() { return mouseDY; }
    public boolean isLeftMousePressed() { return leftMousePressed; }
    public boolean isRightMousePressed() { return rightMousePressed; }
    public boolean isMiddleMousePressed() { return middleMousePressed; }

    public boolean consumeLeftClick() {
        if (leftClickQueued) {
            leftClickQueued = false;
            return true;
        }
        return false;
    }

    public boolean consumeRightClick() {
        if (rightClickQueued) {
            rightClickQueued = false;
            return true;
        }
        return false;
    }

    public int consumeMouseWheelDelta() {
        int delta = mouseWheelDelta;
        mouseWheelDelta = 0;
        return delta;
    }

    public boolean consumeMiddleClick() {
        if (middleClickQueued) {
            middleClickQueued = false;
            return true;
        }
        return false;
    }

    public void resetMouseDelta() {
        mouseDX = 0;
        mouseDY = 0;
    }
}
