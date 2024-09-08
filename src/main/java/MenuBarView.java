import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

public class MenuBarView {
    private JMenuBar jMenuBar;
    private HexTableModel hexModel;

    public MenuBarView(JMenuBar jMenuBar, HexTableModel hexModel){
        this.jMenuBar = jMenuBar;
        this.hexModel = hexModel;

        JMenu view = new JMenu("Вид");
        jMenuBar.add(view);

        JMenuItem twoBytes = view.add(new JMenuItem("2 байта"));
        JMenuItem fourBytes = view.add(new JMenuItem("4 байта"));
        JMenuItem eightBytes = view.add(new JMenuItem("8 байта"));

        twoBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBytesView(2);
            }
        });
        fourBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBytesView(4);
            }
        });
        eightBytes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBytesView(8);
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

    private void openBytesView(int numBytes) {
        int rowCount = hexModel.getRowCount();
        int colCount = hexModel.getColumnCount();

        int newColCount = (colCount + numBytes - 1) / numBytes; // округление вверх

        Object[][] newData = new Object[rowCount][newColCount];

        // Заполнение новой таблицы
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col += numBytes) {
                String combinedValue = "";

                for (int i = 0; i < numBytes; i++) {
                    if (col + i < colCount) {
                        String byteValue = "";
                        Object value = hexModel.getValueAt(row, col);
                        if (value == null || value.toString().isEmpty()) {
                            byteValue = "00"; // Если значение пустое или null, возвращаем "00"
                        } else {
                            byteValue = value.toString();
                        }
                        combinedValue += byteValue;
                    } else {
                        combinedValue += "00"; // Заполнение недостающих байтов нулями
                    }
                }

                BigInteger value = new BigInteger(combinedValue, 16);
                newData[row][col / numBytes] = value;
            }
        }
        BytesView(hexModel, newData);
    }
}
