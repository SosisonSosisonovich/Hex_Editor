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
import java.util.List;
import java.util.regex.Pattern;

public class MenuBarEdit {
    private final JMenuBar jMenuBar;
    private final JTable hexTable;
    private final JTable charTable;
    private final HexTableModel hexModel;
    private final CharTableModel charModel;
    private int currentSearchIndex = -1;

    public MenuBarEdit(JMenuBar jMenuBar, HexTableModel hexModel, CharTableModel charModel, JTable hexTable, JTable charTable){
        this.jMenuBar = jMenuBar;
        this.hexTable = hexTable;
        this.charTable = charTable;
        this.hexModel = hexModel;
        this.charModel = charModel;

        hexTable.addMouseListener(new GUI.mouseListener(hexTable,charTable));
        charTable.addMouseListener(new GUI.mouseListener(charTable,hexTable));

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
                JDialog dialog = pasteDialog(activeTable, hexModel);
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
                hexModel.addColumn();
                for (int i = 1; i < hexTable.getColumnCount(); i++) {
                    hexTable.getColumnModel().getColumn(i).setMaxWidth(35);
                }
                //charModel.addColumn(charModel.getColumnCount()+1);
                charModel.addColumn();
                for (int i = 1; i < charTable.getColumnCount(); i++) {
                    charTable.getColumnModel().getColumn(i).setMaxWidth(35);
                }
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
        int startRow = selectedRow[0];
        int startColumn = selectedCol[0];
        int endRow = selectedRow[selectedRow.length - 1];
        int endColumn = selectedCol[selectedCol.length - 1];

        // Определение начальной и конечной позиции удаления в байтах
        int startByte = startRow * hexModel.getBytesPerRow() + (startColumn - 1);
        int endByte = endRow * hexModel.getBytesPerRow() + (endColumn - 1);

        // Количество байтов для удаления
        int numBytesToDelete = endByte - startByte + 1;

        // Выполнение удаления
        hexModel.deleteBytes(startRow, startColumn, numBytesToDelete);
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
    public  void pasteWithShiftToHex(JTable activeTable, HexTableModel model){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                int selectedRow = activeTable.getSelectedRow();
                int selectedColumn = activeTable.getSelectedColumn();

                if (selectedRow == -1 || selectedColumn <1){
                    JOptionPane.showMessageDialog(null,"Выберите ячейку!");
                    return;
                }

                String[] byteStrings = pasteData.trim().split("\\s+");
                byte[] bytesToInsert = new byte[byteStrings.length];

                try {
                    for (int i = 0; i < byteStrings.length; i++) {
                        bytesToInsert[i] = (byte) Integer.parseInt(byteStrings[i], 16);
                    }

                    model.insertBytes(selectedRow, selectedColumn, bytesToInsert);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Неверные значения!");
                }
            } catch (UnsupportedFlavorException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private JDialog pasteDialog(JTable activeTable, HexTableModel model){
        JDialog dialog = new JDialog((JFrame)null, "Вставка");

        JPanel radioBoxPanel = new JPanel(new GridLayout(0, 1, 0, 5));

        ButtonGroup group = new ButtonGroup();
        JRadioButton replace = new JRadioButton("Вставка с заменой.");
        JRadioButton withOffset = new JRadioButton("Вставка со сдвигом в сторону больших байт.");

        group.add(replace);
        group.add(withOffset);

        JLabel label = new JLabel("Байты пишите через пробел");

        radioBoxPanel.add(replace, FlowLayout.LEFT);
        radioBoxPanel.add(withOffset);
        radioBoxPanel.add(label);
        radioBoxPanel.setBorder(BorderFactory.createTitledBorder("Что вы хотите сделать?"));

        JComboBox<String> comboBox = new JComboBox<>();
        //comboBox.addItem("Текст");
        comboBox.addItem("Шестнадцетиричные значения");

        JButton buttOk = new JButton("OK");

        JPanel comboButtPanel = new JPanel();
        comboButtPanel.add(comboBox);
        comboButtPanel.add(buttOk);
        comboButtPanel.setBorder(BorderFactory.createTitledBorder("Как интерпретировать даные?"));

        buttOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /*if ((String)comboBox.getSelectedItem() == "Текст"){
                    try {
                        if (replace.isSelected()) {
                            pasteFromClipboardToChar(activeTable);
                        } else {
                            pasteWithShiftToChar(activeTable);
                        }
                    } catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                }*/

                //else{
                    try {
                        if (replace.isSelected()) {
                            pasteFromClipboardToHex(activeTable);
                        }else {
                            pasteWithShiftToHex(activeTable, model);
                        }
                    } catch (Exception ex){
                        JOptionPane.showMessageDialog(null,"Неверные значения!");
                    }
                //}
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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

                if (selectedRow.length == 0 || selectedCol.length == 0) {
                    JOptionPane.showMessageDialog(null, "No cells selected");
                    return;
                }

                if(replace.isSelected()){
                    cutToClipboard(selectedCol,selectedRow,activeTable);
                } else{
                    cutWithShift(activeTable,selectedCol,selectedRow);
                }
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        JButton nextButton = new JButton(">");
        JButton prevButton = new JButton("<");

        textPanel.add(textField);
        //buttPanel.add(textButt);
        buttPanel.add(hexButt);
        buttPanel.add(prevButton);
        buttPanel.add(nextButton);

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
                String text = textField.getText().toUpperCase().trim();
                String resultText = text.replaceAll("0x", "");//удаляем маску, если она есть
                String[] arr = resultText.split("\\s+");

                find(hexTable, hexModel, arr);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateSearchResults(1, hexModel, hexTable);
            }
        });

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateSearchResults(-1, hexModel, hexTable);
            }
        });

        searchDialog.add(textPanel, BorderLayout.NORTH);
        searchDialog.add(buttPanel, BorderLayout.SOUTH);

        searchDialog.setSize(350,100);
        searchDialog.setLocationRelativeTo(null);
        searchDialog.setAlwaysOnTop(true);
        searchDialog.setVisible(true);

        return searchDialog;
    }

    //поиск по таблице
    public void find(JTable table, HexTableModel model, String[] arr){
        byte[] searchBytes = new byte[arr.length];
        try {
            for (int i = 0; i < arr.length; i++) {
                searchBytes[i] = (byte) Integer.parseInt(arr[i], 16);
            }
            model.searchBytes(searchBytes);
            currentSearchIndex = 0; // Сброс к первому найденному результату
            highlightSearchResults(table, model);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Неверные значения!");
        }
    }

    //выделение найденых строк и прокрутка
    private void highlightSearchResults(JTable table, HexTableModel model) {
        List<Integer> searchResults = model.getSearchResults();
        if (!searchResults.isEmpty()) {
            table.setRowSelectionInterval(searchResults.get(currentSearchIndex), searchResults.get(currentSearchIndex));
            table.scrollRectToVisible(table.getCellRect(searchResults.get(currentSearchIndex), 0, true));
        } else {
            JOptionPane.showMessageDialog(null, "Не найдено.");
        }
    }

    //переход к следующему/предыдущему результату поиска
    private void navigateSearchResults(int direction, HexTableModel model, JTable table) {
        List<Integer> searchResults = model.getSearchResults();
        if (!searchResults.isEmpty()) {
            currentSearchIndex = (currentSearchIndex + direction) % searchResults.size();
            if (currentSearchIndex < 0) {
                currentSearchIndex = searchResults.size() - 1;
            }
            highlightSearchResults(table, model);
        } else {
            JOptionPane.showMessageDialog(null, "No search results to navigate.");
        }
    }

}
