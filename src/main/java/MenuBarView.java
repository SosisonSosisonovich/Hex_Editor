import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.Vector;

public class MenuBarView {
    private JMenuBar jMenuBar;
    private JTable hexTable;
    private JTable charTable;
    private DefaultTableModel hexModel;
    private DefaultTableModel charModel;

    public MenuBarView(JMenuBar jMenuBar, DefaultTableModel hexModel, DefaultTableModel charModel, JTable hexTable, JTable charTable){
        this.jMenuBar = jMenuBar;
        this.hexTable = hexTable;
        this.charTable = charTable;
        this.hexModel = hexModel;
        this.charModel = charModel;

        JMenu view = new JMenu("Вид");
        jMenuBar.add(view);

        JMenuItem DecimalView = view.add(new JMenuItem("Десятичный вид"));
        view.addSeparator();
        JMenuItem twoBytes = view.add(new JMenuItem("2 байта"));
        JMenuItem fourBytes = view.add(new JMenuItem("4 байта"));
        JMenuItem eightBytes = view.add(new JMenuItem("8 байта"));

        DecimalView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decimalViewFrame(hexModel);
            }
        });
        twoBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = hexModel.getRowCount();
                int colCount = hexModel.getColumnCount();
                Object[][] newData = new Object[rowCount][(colCount + 1) / 2];

                for (int row = 0; row < rowCount; row++) {
                    for (int col = 1; col < colCount; col += 2) {
                        String byte1 = (hexModel.getValueAt(row, col) != null) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов

                        newData[row][col / 2] = Integer.parseInt(byte1+byte2,16);
                    }
                }
                BytesView(hexModel, newData);
            }
        });
        fourBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = hexModel.getRowCount();
                int colCount = hexModel.getColumnCount();
                Object[][] newData = new Object[rowCount][(colCount + 1) / 2];

                for (int row = 0; row < rowCount; row++) {
                    for (int col = 1; col < colCount; col += 4) {
                        String byte1 = (hexModel.getValueAt(row, col) != null) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null) ? (String) hexModel.getValueAt(row, col + 1) : "00";
                        String byte3 = (col + 2 < colCount && hexModel.getValueAt(row, col + 2) != null) ? (String) hexModel.getValueAt(row, col + 2) : "00";
                        String byte4 = (col + 3 < colCount && hexModel.getValueAt(row, col + 3) != null) ? (String) hexModel.getValueAt(row, col + 3) : "00";

                        newData[row][col / 4] = Integer.parseInt(byte1+byte2+byte3+byte4,16);
                    }
                }
                BytesView(hexModel, newData);
            }
        });
        eightBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = hexModel.getRowCount();
                int colCount = hexModel.getColumnCount();
                Object[][] newData = new Object[rowCount][(colCount + 1) / 2];

                for (int row = 0; row < rowCount; row++) {
                    for (int col = 1; col < colCount; col += 8) {
                        String byte1 = (hexModel.getValueAt(row, col) != null) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null) ? (String) hexModel.getValueAt(row, col + 1) : "00";
                        String byte3 = (col + 2 < colCount && hexModel.getValueAt(row, col + 2) != null) ? (String) hexModel.getValueAt(row, col + 2) : "00";
                        String byte4 = (col + 3 < colCount && hexModel.getValueAt(row, col + 3) != null) ? (String) hexModel.getValueAt(row, col + 3) : "00";
                        String byte5 = (col + 4 < colCount && hexModel.getValueAt(row, col + 4) != null) ? (String) hexModel.getValueAt(row, col + 4) : "00";
                        String byte6 = (col + 5 < colCount && hexModel.getValueAt(row, col + 5) != null) ? (String) hexModel.getValueAt(row, col + 5) : "00";
                        String byte7 = (col + 6 < colCount && hexModel.getValueAt(row, col + 6) != null) ? (String) hexModel.getValueAt(row, col + 6) : "00";
                        String byte8 = (col + 7 < colCount && hexModel.getValueAt(row, col + 7) != null) ? (String) hexModel.getValueAt(row, col + 7) : "00";

                        String combinedValue = byte1+byte2+byte3+byte4+byte5+byte6+byte7+byte8;
                        BigInteger value = new BigInteger(combinedValue,16);
                        newData[row][col / 8] = value;
                    }
                }
                BytesView(hexModel, newData);
            }
        });
    }

    //десятичное представление байт
    private JFrame decimalViewFrame(DefaultTableModel hexModel){
        JFrame decimalFrame = new JFrame("Десятичный вид");

        JPanel decimalPanel = new JPanel(new GridLayout());

        //копирование данных из hexmodel
        Vector<Vector> data = hexModel.getDataVector();
        Vector<Vector> decimalData = new Vector<>();

        Vector<String> columnNames = new Vector<>();//копирование имен столбцов
        int columnCount = hexModel.getColumnCount();
        for (int i = 1; i < columnCount; i++) {
            columnNames.add(hexModel.getColumnName(i));
        }

        // преобразование данных из 16-ричного кода в 10-ричный
        for (Vector row : data) {
            Vector<String> newRow = new Vector<>();
            for (int i = 1; i < row.size(); i++) {
                String hexValue = (String) row.get(i);
                if (hexValue == null) {
                    newRow.add("0");
                }else {
                    int decimalValue = Integer.parseInt(hexValue, 16);
                    newRow.add(String.valueOf(decimalValue));
                }
            }
            decimalData.add(newRow);
        }

        DefaultTableModel decimalModel = new DefaultTableModel();
        decimalModel.setDataVector(decimalData, columnNames);

        JTable decimalTable = new JTable(decimalModel);
        decimalTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane decimalSP = new JScrollPane(decimalTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        decimalPanel.add(decimalSP);

        decimalTable.setFont(new Font("Courier New", Font.BOLD, 13));
        decimalTable.setShowHorizontalLines(false);
        decimalTable.setRowHeight(25);
        decimalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        decimalFrame.add(decimalPanel);
        decimalFrame.setSize(800,450);
        decimalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        decimalFrame.setLocationRelativeTo(null);
        decimalFrame.setAlwaysOnTop(true);
        decimalFrame.setVisible(true);

        return decimalFrame;
    }

    //чтение последовательностей из 2 байт
    private JFrame BytesView(DefaultTableModel hexModel, Object[][] newData ){
        JFrame frame = new JFrame("Последовательности из 2 байт");
        JPanel panel = new JPanel(new GridLayout());

        int colCount = hexModel.getColumnCount();

        int newColCount = (colCount + 1) / 2; // Учёт случая с нечетным количеством столбцов
        String[] newColumnNames = new String[newColCount];

        for (int i = 0; i < newColCount; i++) {
            newColumnNames[i] = String.valueOf(i);
        }

        DefaultTableModel tbModel = new DefaultTableModel(newData, newColumnNames);
        JTable tbTable = new JTable(tbModel);
        tbTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tbSP = new JScrollPane(tbTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(tbSP);

        frame.add(panel);
        frame.setSize(800,450);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        return frame;
    }
}
