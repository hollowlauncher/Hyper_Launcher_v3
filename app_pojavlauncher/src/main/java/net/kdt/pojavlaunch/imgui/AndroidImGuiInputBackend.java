package net.kdt.pojavlaunch.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

public final class AndroidImGuiInputBackend {
    private static final KeyMapper KEYS = new KeyMapper();
    private static boolean initialized;

    private AndroidImGuiInputBackend() {}

    public static void initialize() {
        ImGui.getIO().setBackendPlatformName("pojav-aar-input");
        initialized = true;
    }


    public static void cursorPosition(double x, double y) {
        if (initialized) ImGui.getIO().addMousePosEvent((float) x, (float) y);
    }

    public static void mouseButton(int button, int action) {
        if (initialized && button >= 0 && button < 5 && action != GLFW.GLFW_REPEAT) {
            ImGui.getIO().addMouseButtonEvent(button, action == GLFW.GLFW_PRESS);
        }
    }

    public static void scroll(double horizontal, double vertical) {
        if (initialized) ImGui.getIO().addMouseWheelEvent((float) horizontal, (float) vertical);
    }

    public static void character(int codepoint) {
        if (initialized && Character.isValidCodePoint(codepoint)) ImGui.getIO().addInputCharacter(codepoint);
    }

    public static void key(int key, int scancode, int action, int modifiers) {
        if (!initialized || action == GLFW.GLFW_REPEAT) return;

        ImGuiIO io = ImGui.getIO();
        boolean down = action == GLFW.GLFW_PRESS;

        io.addKeyEvent(ImGuiKey.ModCtrl, (modifiers & GLFW.GLFW_MOD_CONTROL) != 0);
        io.addKeyEvent(ImGuiKey.ModShift, (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
        io.addKeyEvent(ImGuiKey.ModAlt, (modifiers & GLFW.GLFW_MOD_ALT) != 0);
        io.addKeyEvent(ImGuiKey.ModSuper, (modifiers & GLFW.GLFW_MOD_SUPER) != 0);

        int mapped = KEYS.map(key);
        if (mapped != ImGuiKey.None) {
            io.addKeyEvent(mapped, down);
            io.setKeyEventNativeData(mapped, key, scancode);
        }
    }

    private static final class KeyMapper extends ImGuiImplGlfw {
        int map(int glfwKey) {
            return glfwKeyToImGuiKey(glfwKey);
        }
    }
}
