package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.regex.Pattern;

public class MenuBarEdit {
    private JMenuBar jMenuBar;
    private JTable hexTable;
    private JTable charTable;
    private DefaultTableModel hexModel;
    private DefaultTableModel charModel;

    public MenuBarEdit(JMenuBar jMenuBar, DefaultTableModel hexModel, DefaultTableModel charModel, JTable hexTable, JTable charTable){
        this.jMenuBar = jMenuBar;
        this.hexTable = hexTable;
        this.charTable = charTable;
        this.hexModel = hexModel;
        this.charModel = charModel;

        hexTable.addMouseListener(new GUI.mouseListener(hexTable,charTable));
        charTable.addMouseListener(new GUI.mouseListener(charTable,hexTable));

        JMenu edit = new JMenu("Редактирование");
        jMenuBar.add(edit);

        JMenuItem cut = edit.add(new JMenuItem("Вырезать"));
        JMenuItem copy = edit.add(new JMenuItem("Копировать"));
        JMenuItem paste = edit.add(new JMenuItem("Вставить"));
        edit.addSeparator();
        JMenuItem find = edit.add(new JMenuItem("Найти"));
        JMenuItem addCol = edit.add(new JMenuItem("Добавить столбец"));

        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int [] selectedCol = GUI.activeTable.getSelectedColumns();
                int [] selectedRow = GUI.activeTable.getSelectedRows();
                JTable activeTable = GUI.activeTable;

                cutToClipboard(selectedCol, selectedRow, activeTable);
            }
        });
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int [] selectedCol = GUI.activeTable.getSelectedColumns();
                int [] selectedRow = GUI.activeTable.getSelectedRows();
                JTable activeTable = GUI.activeTable;

                copyToClipboard(selectedCol, selectedRow, activeTable);
            }
        });

        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable activeTable = GUI.activeTable;
                JDialog dialog = dialog(activeTable);
            }
        });
        find.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findInTable();
            }
        });
        addCol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hexModel.addColumn(Integer.toHexString(hexModel.getColumnCount()+1));
                charModel.addColumn(charModel.getColumnCount()+1);
            }
        });
    }

    public void cutToClipboard(int[] selectedCol, int[] selectedRow, JTable activeTable){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder cutData = new StringBuilder();

        TableModel model = activeTable.getModel();

        for (int row : selectedRow) {
            for (int col : selectedCol) {
                cutData.append(model.getValueAt(row, col)).append("\t");
                model.setValueAt(0, row, col);
            }
            cutData.deleteCharAt(cutData.length() - 1); // Удаляем последний символ (табуляцию)
            cutData.append("\n");
        }
        StringSelection stringSelection = new StringSelection(cutData.toString());
        clipboard.setContents(stringSelection, null);
    }
    public void copyToClipboard(int[] selectedCol, int[] selectedRow, JTable activeTable){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder copyData = new StringBuilder();

        TableModel model = activeTable.getModel();

        for (int row : selectedRow) {
            for (int col : selectedCol) {
                copyData.append(model.getValueAt(row, col)).append("\t");
            }
            copyData.deleteCharAt(copyData.length() - 1); // Удаляем последний символ (табуляцию)
            copyData.append("\n");
        }
        StringSelection stringSelection = new StringSelection(copyData.toString());
        clipboard.setContents(stringSelection, null);
    }
    public void pasteFromClipboardToHex(JTable activeTable){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                // Вставляем каждый символ в соответствующую ячейку текущей активной таблицы
                int startRow = activeTable.getSelectedRow();
                int startCol = activeTable.getSelectedColumn();

                int row = startRow;
                int col = startCol;

                if (activeTable == hexTable){
                    String[] characters = pasteData.split(" ");

                    for (String character : characters) {
                        if (row < activeTable.getRowCount() && col < activeTable.getColumnCount()) {
                            String value = character;

                            activeTable.setValueAt(value, row, col);
                            col++;

                            // Если достигнут конец строки таблицы, переходим на следующую строку
                            if (col == activeTable.getColumnCount()) {
                                col = 1;
                                row++;
                            }
                        } else {
                            break;
                        }
                    }
                }
                else{
                    char[] characters = pasteData.toCharArray();

                    for (char character : characters) {
                    if (row < activeTable.getRowCount() && col < activeTable.getColumnCount()) {
                        String value = String.valueOf(character);

                        activeTable.setValueAt(value, row, col);
                        col++;

                        // Если достигнут конец строки таблицы, переходим на следующую строку
                        if (col == activeTable.getColumnCount()) {
                            col = 1;
                            row++;
                        }
                    } else {
                        break;
                    }
                }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void pasteFromClipboardToChar(JTable activeTable){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                // Разбиваем строку на символы
                char[] characters = pasteData.toCharArray();

                // Вставляем каждый символ в соответствующую ячейку текущей активной таблицы
                int startRow = activeTable.getSelectedRow();
                int startCol = activeTable.getSelectedColumn();

                int row = startRow;
                int col = startCol;

                for (char character : characters) {
                    if (row < activeTable.getRowCount() && col < activeTable.getColumnCount() ) {
                        String value = String.valueOf(character);

                        if (activeTable == hexTable) {
                            value = String.format("%02X", (int) character);
                            activeTable.setValueAt(value, row, col);
                            col++;
                            System.out.println("hex");

                            // Если достигнут конец строки таблицы, переходим на следующую строку
                            if (col == activeTable.getColumnCount()) {
                                col = 1;
                                row++;
                            }
                        } else {
                            value = String.valueOf(character);

                            activeTable.setValueAt(value, row, col);
                            col++;

                            System.out.println("char");

                            // Если достигнут конец строки таблицы, переходим на следующую строку
                            if (col == activeTable.getColumnCount()) {
                                col = 1;
                                row++;
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private JDialog dialog(JTable activeTable){
        JDialog dialog = new JDialog((JFrame)null, "Вставка");

        JLabel lbal = new JLabel("Как интерпретировать даные?");

        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("Текст");
        comboBox.addItem("Шестнадцетиричные значения");

        JButton buttOk = new JButton("OK");

        JPanel radioButtPanel = new JPanel();
        radioButtPanel.add(comboBox);
        radioButtPanel.add(buttOk);

        JPanel labelPanel = new JPanel();
        labelPanel.add(lbal);

        buttOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((String)comboBox.getSelectedItem() == "Текст"){
                    try {
                        pasteFromClipboardToChar(activeTable);
                    } catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                } else{
                    try {
                        pasteFromClipboardToHex(activeTable);
                    } catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                }
            }
        });

        dialog.add(labelPanel,BorderLayout.NORTH);
        dialog.add(radioButtPanel, BorderLayout.SOUTH);

        dialog.setSize(300,100);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return dialog;
    }
    private void findInTable(){
        JDialog searchDialog = new JDialog((JFrame)null, "Поиск");

        JPanel textPanel = new JPanel();
        JPanel buttPanel = new JPanel();

        JTextField textField = new JTextField("Байты вводите через пробел", 20);
        JButton textButt = new JButton("Поиск по тексту");
        JButton hexButt = new JButton("Поиск байт");

        textPanel.add(textField);
        buttPanel.add(textButt);
        buttPanel.add(hexButt);

        textButt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText().toLowerCase();
                String[] arr = text.split("");

                find(charTable, hexTable, charModel, arr);
            }
        });

        hexButt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText().toLowerCase();
                String resultText = text.replaceAll("0x", "");//удаляем маску, если она есть
                String[] arr = resultText.split(" ");

                find(hexTable, charTable, hexModel, arr);
            }
        });

        searchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hexTable.setRowSorter(null);
                charTable.setRowSorter(null);
            }
        });

        searchDialog.add(textPanel, BorderLayout.NORTH);
        searchDialog.add(buttPanel, BorderLayout.SOUTH);

        searchDialog.setSize(300,100);
        searchDialog.setLocationRelativeTo(null);
        searchDialog.setVisible(true);
    }

    public void find(JTable table1, JTable table2, DefaultTableModel model, String[] arr){
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table1.setRowSorter(sorter);
            table2.setRowSorter(sorter);

            RowFilter<Object, Object> rowFilter = new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (String term : arr) {
                        boolean termFound = false;

                        for (int i = 0; i < entry.getValueCount(); i++) {
                            Object value = entry.getValue(i);
                            if (value != null && Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE).matcher(value.toString()).find()) {
                                termFound = true;
                                break;
                            }
                        }
                        if (!termFound) {
                            return false; // Если хотя бы один из элементов не найден, исключаем строку
                        }
                    }
                    return true; // Все элементы найдены
                }
            };
            sorter.setRowFilter(rowFilter);
    }
}
