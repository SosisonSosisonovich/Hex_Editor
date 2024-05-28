import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MenuBarFileTest {
    private MenuBarFile menuBarFile;
    private DefaultTableModel hexModel;
    private DefaultTableModel charModel;
    private JTable hexTable;
    private  JTable charTable;
    static byte[] bytes = {10, 20, 30, 40, 50};
    File file = new File("C:/Users/kakaw/Downloads/0");


    @BeforeEach
    public void setUp(){
        JMenuBar jMenuBar = new JMenuBar();
        hexModel = new DefaultTableModel(new Object[][]{{"A","B"},{"C","D"},{"E","F"}}, new Object[]{"Column 1", "Column 2"});
        charModel = new DefaultTableModel(new Object[][]{{"B","A"},{"D","C"},{"F","E"}}, new Object[]{"Column 1", "Column 2"});
        charTable = new JTable(charModel);
        hexTable = new JTable(hexModel);

        menuBarFile = new MenuBarFile(jMenuBar, hexModel,charModel);
    }

    @Test
    public void newsTest(){
        assertEquals(3, hexModel.getRowCount());
        assertEquals(3, charModel.getRowCount());

        menuBarFile.news(hexModel);
        menuBarFile.news(charModel);

        assertEquals(50, hexModel.getRowCount());
        assertEquals(50, charModel.getRowCount());
    }

    @Test
    public void openTest() throws IOException {
        hexModel.setRowCount(0);
        charModel.setRowCount(0);
        hexModel.setRowCount(1);
        charModel.setRowCount(1);

        //создаем временный файл на 1 гб
       /* File testFile = File.createTempFile("test",".txt");
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testFile))) {
                bos.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        hexModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getLastRow();

                    // Проверка, что редактируемая ячейка находится в последней строке
                    if (row == hexModel.getRowCount() - 1) {
                        hexModel.addRow(new Object[hexModel.getColumnCount()]);
                    }
                }
            }
        });

        menuBarFile.open(file, hexModel);

        assertEquals(12,(long) hexModel.getColumnCount() * hexModel.getRowCount());
        file.deleteOnExit();
    }

    @Test
    public void saveTest() throws IOException {
        File testFile = File.createTempFile("test",".bin");
        List<String[]> tableData = Arrays.asList(
                new String[]{"offset 1", "7F"},
                new String[]{"offset 2", "b"},
                new String[]{"offset 3", "c"}
        );

        menuBarFile.save(testFile,tableData);

        assertTrue(testFile.exists());

        byte[] Bytes = Files.readAllBytes(testFile.toPath());
        // берем значения в десятичной системе счисления, т.к. в них и сохраняет
        byte[] expBytes ={127, 11, 12};
        assertArrayEquals(expBytes, Bytes);

        testFile.deleteOnExit();
    }
}
