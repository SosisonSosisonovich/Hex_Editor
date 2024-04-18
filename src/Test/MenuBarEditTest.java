package Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import App.MenuBarEdit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class MenuBarEditTest {
    private MenuBarEdit menuBarEdit;
    private DefaultTableModel hexModel;
    private DefaultTableModel charModel;
    private JTable hexTable;
    private  JTable charTable;
    @BeforeEach
    public void setUp(){
        JMenuBar jMenuBar = new JMenuBar();
        hexModel = new DefaultTableModel(new Object[][]{{"1","A","B"},{"2","C","D"},{"3","E","F"}}, new Object[]{"offset","Column 1", "Column 2"});
        charModel = new DefaultTableModel(new Object[][]{{"1","B","A"},{"2","D","C"},{"3","F","E"}}, new Object[]{"offset","Column 1", "Column 2"});
        charTable = new JTable(charModel);
        hexTable = new JTable(hexModel);

        menuBarEdit = new MenuBarEdit(jMenuBar, hexModel,charModel,hexTable,charTable);
    }
    @Test
    public void cutToClipboardTestHex(){
        menuBarEdit.cutToClipboard(new int[]{1,2}, new int []{0}, hexTable);

        assertEquals(0, hexModel.getValueAt(0, 1));
        assertEquals(0, hexModel.getValueAt(0, 2));

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        String clipboardContent = "";
        try {
            clipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String expectedClipboardContent = "A\tB\n";
        assertEquals(expectedClipboardContent, clipboardContent);
    }
    @Test
    public void cutToClipboardTestChar(){
        menuBarEdit.cutToClipboard(new int[]{1,2}, new int []{0}, charTable);

        assertEquals(0, charModel.getValueAt(0, 1));
        assertEquals(0, charModel.getValueAt(0, 2));

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        String clipboardContent = "";
        try {
            clipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String expectedClipboardContent = "B\tA\n";
        assertEquals(expectedClipboardContent, clipboardContent);
    }
    @Test
    public void copyToClipboardTestHex(){
        menuBarEdit.copyToClipboard(new int[]{1,2}, new int []{0}, hexTable);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        String clipboardContent = "";
        try {
            clipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String expectedClipboardContent = "A\tB\n";
        assertEquals(expectedClipboardContent, clipboardContent);
    }
    @Test
    public void copyToClipboardTestChar(){
        menuBarEdit.copyToClipboard(new int[]{1,2}, new int []{0}, charTable);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        String clipboardContent = "";
        try {
            clipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String expectedClipboardContent = "B\tA\n";
        assertEquals(expectedClipboardContent, clipboardContent);
    }
    @Test
    public void pasteFromClipboardToHexTest(){
        charTable.setRowSelectionInterval(0, 0);
        charTable.setColumnSelectionInterval(1,2);

        hexTable.setRowSelectionInterval(0, 0);
        hexTable.setColumnSelectionInterval(1,2);

        String testData = "61 2d";
        StringSelection stringSelection = new StringSelection(testData);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);

        menuBarEdit.pasteFromClipboardToHex(charTable);

        assertEquals("6", charTable.getValueAt(0, 1));
        assertEquals("1", charTable.getValueAt(0, 2));
        assertEquals(" ", charTable.getValueAt(1, 1));
        assertEquals("2", charTable.getValueAt(1, 2));
        assertEquals("d", charTable.getValueAt(2, 1));

        menuBarEdit.pasteFromClipboardToHex(hexTable);

        assertEquals("61", hexTable.getValueAt(0, 1));
        assertEquals("2d", hexTable.getValueAt(0, 2));
    }
    @Test
    public void pasteFromClipboardToCharTest(){
        charTable.setRowSelectionInterval(0, 0);
        charTable.setColumnSelectionInterval(1,2);

        hexTable.setRowSelectionInterval(0, 0);
        hexTable.setColumnSelectionInterval(1,2);

        String testData = "61 2d";
        StringSelection stringSelection = new StringSelection(testData);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);

        menuBarEdit.pasteFromClipboardToChar(charTable);

        assertEquals("6", charTable.getValueAt(0, 1));
        assertEquals("1", charTable.getValueAt(0, 2));
        assertEquals(" ", charTable.getValueAt(1, 1));
        assertEquals("2", charTable.getValueAt(1, 2));
        assertEquals("d", charTable.getValueAt(2, 1));

    }
    @Test
    public void findTest(){
        String[] arr = {"E"};
        menuBarEdit.find(hexTable, charTable, hexModel,arr);

        assertEquals(1, hexTable.getRowCount());
        assertEquals("E", hexTable.getValueAt(0, 0));
        assertEquals("F", hexTable.getValueAt(0, 1));

        assertEquals(1, charTable.getRowCount());
        assertEquals("F", charTable.getValueAt(0, 0));
        assertEquals("E", charTable.getValueAt(0, 1));
    }
}
