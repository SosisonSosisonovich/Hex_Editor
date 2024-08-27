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
    private final JMenuBar jMenuBar;
    private final JTable hexTable;
    private final JTable charTable;
    private final HexTableModel hexModel;
    private final DefaultTableModel charModel;

    public MenuBarEdit(JMenuBar jMenuBar, HexTableModel hexModel, DefaultTableModel charModel, JTable hexTable, JTable charTable){
        this.jMenuBar = jMenuBar;
        this.hexTable = hexTable;
        this.charTable = charTable;
        this.hexModel = hexModel;
        this.charModel = charModel;

        hexTable.addMouseListener(new GUI.mouseListener(hexTable,charTable));
        //charTable.addMouseListener(new GUI.mouseListener(charTable,hexTable));

        hexTable.addMouseListener(new GUI.mouseListener(hexTable, charTable));

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
                JTable activeTable = GUI.activeTable;

                cutDialog(activeTable);
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
                JDialog dialog = pasteDialog(activeTable);
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
                hexModel.addColumn(hexTable);
                for (int i = 1; i < hexTable.getColumnCount(); i++) {
                    hexTable.getColumnModel().getColumn(i).setMaxWidth(35);
                }
                //charModel.addColumn(charModel.getColumnCount()+1);
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

    public void cutWithShift(JTable activeTable, int[] selectedCol, int[] selectedRow){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder cutData = new StringBuilder();
        TableModel model = activeTable.getModel();

        int rowCount = activeTable.getRowCount();
        int colCount = activeTable.getColumnCount();

        //копируем данные
        for(int row: selectedRow){
            for (int col : selectedCol){
                cutData.append(model.getValueAt(row, col)).append("\t");
                model.setValueAt(null, row, col);
            }
            cutData.append("\n");
        }

        StringSelection stringSelection = new StringSelection(cutData.toString());
        clipboard.setContents(stringSelection, null);

        // Удаление выбранных ячеек
        for (int row : selectedRow) {
            for (int col : selectedCol) {
                model.setValueAt(null, row, col);
            }
        }

        // Сдвиг данных влево и вверх
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                if (model.getValueAt(row, col) == null) {
                    int srcRow = row;
                    int srcCol = col;

                    // Найти следующую непустую ячейку
                    do {
                        srcCol++;
                        if (srcCol >= colCount) {
                            srcCol = 0;
                            srcRow++;
                        }
                    } while (srcRow < rowCount && model.getValueAt(srcRow, srcCol) == null);

                    if (srcRow < rowCount) {
                        Object value = model.getValueAt(srcRow, srcCol);
                        model.setValueAt(value, row, col);
                        model.setValueAt(null, srcRow, srcCol);
                    }
                }
            }
        }

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

    //вставка в таблицу 16-ричного кода
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

    //вставка в таблицу символов
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
                        String value;

                        if (activeTable == hexTable) {
                            value = String.format("%02X", (int) character);
                            activeTable.setValueAt(value, row, col);
                            col++;

                            // Если достигнут конец строки таблицы, переходим на следующую строку
                            if (col == activeTable.getColumnCount()) {
                                col = 1;
                                row++;
                            }

                        } else {
                            value = String.valueOf(character);

                            activeTable.setValueAt(value, row, col);
                            col++;

                            // Если достигнут конец строки таблицы, переходим на следующую строку
                            if (col == activeTable.getColumnCount()) {
                                col = 1;
                                row++;
                            }
                        }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //вставка со сдвигом
    public void pasteWithShiftToHex(JTable activeTable){
        DefaultTableModel model = (DefaultTableModel) activeTable.getModel();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                // Разбиваем строку на символы
                String[] characters = pasteData.split("\\s");
                int rowCount = model.getRowCount();
                int colCount = model.getColumnCount();

                int charLength = characters.length;
                int selectedRow = activeTable.getSelectedRow();
                int selectedCol = activeTable.getSelectedColumn();

                //сдвиг данных вправо
                for (int row = rowCount-1; row >= selectedRow; row--) {
                    for (int col = colCount-1; col >= selectedCol + charLength ; col--) {
                        //если количества столбцов в строке не хватает, то переходим на предыдущую строку
                        int srcRow = (col - charLength < 1) ? row - 1 : row;
                        //если количества столбцов в строке не хватает, то переходим на последний столбец предыдущей строки плюс смещение
                        int srcCol = (col - charLength < 1) ? colCount - 1 + (col - charLength) : col - charLength;

                        if (srcCol < 0) {
                            srcCol += colCount;
                            srcRow--;
                        }

                        if (srcRow >= 0) {
                            Object value = model.getValueAt(srcRow, srcCol);
                            model.setValueAt(value, row, col);
                        }
                    }
                }

                int row = selectedRow;
                int col = selectedCol;

                if (activeTable == hexTable){
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
                    String[] character = characters.toString().split("");
                    for (String arr : character) {
                        if (row < activeTable.getRowCount() && col < activeTable.getColumnCount()) {
                            String value = String.valueOf(arr);

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
                throw new RuntimeException(ex);
            }
        }
    }

    public void pasteWithShiftToChar(JTable activeTable){
        DefaultTableModel model = (DefaultTableModel) activeTable.getModel();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                // Разбиваем строку на символы
                char[] characters = pasteData.toCharArray();
                int rowCount = model.getRowCount();
                int colCount = model.getColumnCount();

                int charLength = characters.length;
                int selectedRow = activeTable.getSelectedRow();
                int selectedCol = activeTable.getSelectedColumn();

                //сдвиг данных вправо
                for (int row = rowCount-1; row >= selectedRow; row--) {
                    for (int col = colCount-1; col >= selectedCol + charLength ; col--) {
                        //если количества столбцов в строке не хватает, то переходим на предыдущую строку
                        int srcRow = (col - charLength < 1) ? row - 1 : row;
                        //если количества столбцов в строке не хватает, то переходим на последний столбец предыдущей строки плюс смещение
                        int srcCol = (col - charLength < 1) ? colCount - 1 + (col - charLength) : col - charLength;

                        if (srcCol < 0) {
                            srcCol += colCount;
                            srcRow--;
                        }

                        if (srcRow >= 0) {
                            Object value = model.getValueAt(srcRow, srcCol);
                            model.setValueAt(value, row, col);
                        }
                    }
                }


                //вводим новые переменные, чтобы избежать изменения старых
                int row = selectedRow;
                int col = selectedCol;
                for (char character : characters) {
                    String value;

                    if (activeTable == hexTable) {
                        value = String.format("%02X", (int) character);
                        activeTable.setValueAt(value, row, col);
                        col++;

                        // Если достигнут конец строки таблицы, переходим на следующую строку
                        if (col == activeTable.getColumnCount()) {
                            col = 1;
                            row++;
                        }

                    } else {
                        value = String.valueOf(character);

                        activeTable.setValueAt(value, row, col);
                        col++;

                        // Если достигнут конец строки таблицы, переходим на следующую строку
                        if (col == activeTable.getColumnCount()) {
                            col = 1;
                            row++;
                        }
                    }
                }

            } catch (UnsupportedFlavorException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private JDialog pasteDialog(JTable activeTable){
        JDialog dialog = new JDialog((JFrame)null, "Вставка");

        JPanel radioBoxPanel = new JPanel(new GridLayout(0, 1, 0, 5));

        ButtonGroup group = new ButtonGroup();
        JRadioButton replace = new JRadioButton("Вставка с заменой");
        JRadioButton withOffset = new JRadioButton("Вставка со сдвигом в сторону больших байт.");

        group.add(replace);
        group.add(withOffset);

        radioBoxPanel.add(replace, FlowLayout.LEFT);
        radioBoxPanel.add(withOffset);
        radioBoxPanel.setBorder(BorderFactory.createTitledBorder("Что вы хотите сделать?"));

        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("Текст");
        comboBox.addItem("Шестнадцетиричные значения");

        JButton buttOk = new JButton("OK");

        JPanel comboButtPanel = new JPanel();
        comboButtPanel.add(comboBox);
        comboButtPanel.add(buttOk);
        comboButtPanel.setBorder(BorderFactory.createTitledBorder("Как интерпретировать даные?"));

        buttOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((String)comboBox.getSelectedItem() == "Текст"){
                    try {
                        if (replace.isSelected()) {
                            pasteFromClipboardToChar(activeTable);
                        } else {
                            pasteWithShiftToChar(activeTable);
                        }
                    } catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                }

                else{
                    try {
                        if (replace.isSelected()) {
                            pasteFromClipboardToHex(activeTable);
                        }else {
                            pasteWithShiftToHex(activeTable);
                        }
                    } catch (Exception ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                }
            }
        });

        dialog.add(radioBoxPanel);
        dialog.add(comboButtPanel, BorderLayout.SOUTH);

        dialog.setSize(350,175);
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        return dialog;
    }

    private JDialog cutDialog(JTable activeTable){
        JDialog dialog = new JDialog((JFrame)null, "Вырезание");

        JPanel radioBoxPanel = new JPanel(new GridLayout(0, 1, 100, 5));

        ButtonGroup group = new ButtonGroup();
        JRadioButton replace = new JRadioButton("Вырезание с заменой на 0");
        JRadioButton withOffset = new JRadioButton("Вырезание со сдвигом в сторону меньших байт");

        group.add(replace);
        group.add(withOffset);

        JButton buttOk = new JButton("OK");

        radioBoxPanel.add(replace, FlowLayout.LEFT);
        radioBoxPanel.add(withOffset);
        radioBoxPanel.add(buttOk);
        radioBoxPanel.setBorder(BorderFactory.createTitledBorder("Что вы хотите сделать?"));

        buttOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int [] selectedCol = GUI.activeTable.getSelectedColumns();
                int [] selectedRow = GUI.activeTable.getSelectedRows();

                if(replace.isSelected()){
                    cutToClipboard(selectedCol,selectedRow,activeTable);
                } else{
                    cutWithShift(activeTable,selectedCol,selectedRow);
                }
            }
        });

        dialog.add(radioBoxPanel);

        dialog.setSize(350,175);
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        return dialog;
    }

    private JDialog findInTable(){
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

                //find(charTable, hexTable, charModel, arr);
            }
        });

        hexButt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText().toLowerCase();
                String resultText = text.replaceAll("0x", "");//удаляем маску, если она есть
                String[] arr = resultText.split(" ");

                //find(hexTable, charTable, hexModel, arr);
            }
        });

        searchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hexTable.setRowSorter(null);
                //charTable.setRowSorter(null);
            }
        });

        searchDialog.add(textPanel, BorderLayout.NORTH);
        searchDialog.add(buttPanel, BorderLayout.SOUTH);

        searchDialog.setSize(300,100);
        searchDialog.setLocationRelativeTo(null);
        searchDialog.setAlwaysOnTop(true);
        searchDialog.setVisible(true);

        return searchDialog;
    }

    //поиск по таблице
    public void find(JTable table1, JTable table2, DefaultTableModel model, String[] arr){
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table1.setRowSorter(sorter);
            table2.setRowSorter(sorter);

        RowFilter<Object, Object> rowFilter = new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                // Создаем строку из значений текущей строки
                StringBuilder rowValues = new StringBuilder();
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null) {
                        rowValues.append(value.toString());
                    }
                }

                // Создаем строку из терминов, заданных пользователем
                StringBuilder searchTerm = new StringBuilder();
                for (String term : arr) {
                    searchTerm.append(term);
                }

                // Проверяем, содержится ли заданная пользователем последовательность в текущей строке
                return rowValues.toString().contains(searchTerm.toString());
            }
        };

        sorter.setRowFilter(rowFilter);
    }
}
