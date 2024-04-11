import App.ByteArray;
import App.GUI;
import Test.ByteArrayTest;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
        });
    }
}