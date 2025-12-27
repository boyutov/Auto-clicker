package org.example;

import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements NativeMouseListener {

    private static final AtomicBoolean clicking = new AtomicBoolean(false);
    private static Robot robot;
    private static long lastToggleTime = 0; // антидребезг

    public static void main(String[] args) throws Exception {

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        robot = new Robot();

        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeMouseListener(new Main());

        // Поток автоклика (ПКМ)
        Thread clickThread = new Thread(() -> {
            while (true) {
                if (clicking.get()) {
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK); // ПКМ
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                }
                try {
                    Thread.sleep(2); // скорость клика
                } catch (InterruptedException ignored) {}
            }
        });

        clickThread.start();

        System.out.println("СКМ — включить/выключить автокликер (кликает ПКМ)");

        Thread.currentThread().join();
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        // BUTTON2 = СКМ
        if (e.getButton() == NativeMouseEvent.BUTTON3) {
            long now = System.currentTimeMillis();
            if (now - lastToggleTime < 300) return; // антидребезг
            lastToggleTime = now;

            boolean newState = !clicking.get();
            clicking.set(newState);
            System.out.println(newState ? "Автокликер ВКЛ" : "Автокликер ВЫКЛ");
        }
    }
}