package dev.helmocraft.mcassetindexextractor.main;

import dev.helmocraft.mcassetindexextractor.gui.MainWindow;

public class Main {

    public static void main(String[] args) {
        boolean debugEnabled = false;

        if(args.length > 0) {
            if(args[0].equals("--debug")) {
                debugEnabled = true;
            }
        }

        MainWindow.initialize(debugEnabled);
    }
}