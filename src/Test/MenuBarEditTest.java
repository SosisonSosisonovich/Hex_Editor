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
        hexModel = new DefaultTableModel(new Object[][]{{"A","B"},{"C","D"},{"E","F"}}, new Object[]{"Column 1", "Column 2"});
        charModel = new DefaultTableModel(new Object[][]{{"B","A"},{"D","C"},{"F","E"}}, new Object[]{"Column 1", "Column 2"});
        charTable = new JTable(charModel);
        hexTable = new JTable(hexModel);

        menuBarEdit = new MenuBarEdit(jMenuBar, hexModel,charModel,hexTable,charTable);
        //устанавливаем выбранный интервал
        hexTable.setRowSelectionInterval(0, 0);
        hexTable.setColumnSelectionInterval(0, 1);

    }
    @Test
    public void cutToClipboardTestHex(){
        menuBarEdit.cutToClipboard(new int[]{0,1}, new int []{0,0}, hexTable);

        assertEquals(0, hexModel.getValueAt(0, 0));
        assertEquals(0, hexModel.getValueAt(0, 1));


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
    public void copyToClipboardTest(){}
    @Test
    public void pasteFromClipboardToHexTest(){}
    @Test
    public void pasteFromClipboardToCharTest(){}
}
