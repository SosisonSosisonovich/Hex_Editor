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
    private HexTableModel hexModel;

    public MenuBarView(JMenuBar jMenuBar, HexTableModel hexModel,  JTable hexTable){
        this.jMenuBar = jMenuBar;
        this.hexTable = hexTable;
        this.hexModel = hexModel;

        JMenu view = new JMenu("Вид");
        jMenuBar.add(view);

        JMenuItem twoBytes = view.add(new JMenuItem("2 байта"));
        JMenuItem fourBytes = view.add(new JMenuItem("4 байта"));
        JMenuItem eightBytes = view.add(new JMenuItem("8 байта"));

        twoBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = hexModel.getRowCount();
                int colCount = hexModel.getColumnCount();
                Object[][] newData = new Object[rowCount][(colCount + 1) / 2];

                for (int row = 0; row < rowCount; row++) {
                    for (int col = 1; col < colCount; col += 2) {
                        String byte1 = ((hexModel.getValueAt(row, col) != null) && (hexModel.getValueAt(row, col).toString().isEmpty())) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов

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
                        String byte1 = ((hexModel.getValueAt(row, col) != null) && (hexModel.getValueAt(row, col).toString().isEmpty())) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов
                        String byte3 = (col + 2 < colCount && hexModel.getValueAt(row, col + 2) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00";
                        String byte4 = (col + 3 < colCount && hexModel.getValueAt(row, col + 3) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00";

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
                        String byte1 = ((hexModel.getValueAt(row, col) != null) && (hexModel.getValueAt(row, col).toString().isEmpty())) ? (String) hexModel.getValueAt(row, col) : "00";
                        String byte2 = (col + 1 < colCount && hexModel.getValueAt(row, col + 1) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов
                        String byte3 = (col + 2 < colCount && hexModel.getValueAt(row, col + 2) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00";
                        String byte4 = (col + 3 < colCount && hexModel.getValueAt(row, col + 3) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00";
                        String byte5 = (col + 4 < colCount && hexModel.getValueAt(row, col + 4) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов
                        String byte6 = (col + 5 < colCount && hexModel.getValueAt(row, col + 5) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов
                        String byte7 = (col + 6 < colCount && hexModel.getValueAt(row, col + 6) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов
                        String byte8 = (col + 7 < colCount && hexModel.getValueAt(row, col + 7) != null && (hexModel.getValueAt(row, col) != "")) ? (String) hexModel.getValueAt(row, col + 1) : "00"; // Учет нечетного числа столбцов

                        String combinedValue = byte1+byte2+byte3+byte4+byte5+byte6+byte7+byte8;
                        BigInteger value = new BigInteger(combinedValue,16);
                        newData[row][col / 8] = value;
                    }
                }
                BytesView(hexModel, newData);
            }
        });
    }
    //окно с представлением значения последовательности байт
    private JFrame BytesView(HexTableModel hexModel, Object[][] newData ){
        JFrame frame = new JFrame();
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
