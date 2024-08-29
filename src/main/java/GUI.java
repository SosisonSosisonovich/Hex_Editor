import javax.swing.*;
import java.awt.Rectangle;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;

public class GUI implements Serializable {
    private final JFrame frame;
    private JPanel panel;
    private JPanel panel1;
    private static JTable hexTable;
    private static JTable charTable;
    static JTable activeTable;
    private static JLabel label;
    private boolean updating  = false;
    private static int selectedRow = -1;
    private static int selectedCol = -1;
    private  static  int editingRow = -1;
    private  static  int editingCol = -1;
    private static boolean isEdited = false;

    public GUI() throws IOException {
        frame = new JFrame("Hex-Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        run();
    }

    void run() throws IOException {
        panel = new JPanel(new GridLayout());
        panel1 = new JPanel(new BorderLayout());

        HexTableModel hexModel = new HexTableModel(32);
        CharTableModel charModel = new CharTableModel(32);

        hexTable = new JTable(hexModel){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (hexModel.getSearchResults().contains(row)) {
                    c.setBackground(new Color(135, 206, 235)); // Подсветка найденных строк
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        charTable = new JTable(charModel);

        //флаг для определения активной на данной момент, дефолтная активная таблица hexTable
        activeTable = hexTable;

        JMenuBar jMenuBar = new JMenuBar();
        new MenuBarFile(jMenuBar, hexModel, charModel);
        //new MenuBarFile(jMenuBar,hexModel);
        new MenuBarEdit(jMenuBar,hexModel,charModel,hexTable,charTable);
        new MenuBarView(jMenuBar,hexModel,hexTable);

        hexTable.getTableHeader().setReorderingAllowed(false);
        charTable.getTableHeader().setReorderingAllowed(false);
        ColumnsAndRows();

        JScrollPane hexSP = new JScrollPane(hexTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollBar sharedScrollBar = hexSP.getVerticalScrollBar();
        JScrollPane charSP = new JScrollPane(charTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charSP.setVerticalScrollBar(sharedScrollBar);
        charSP.setBorder(new EmptyBorder(0, 0, 0, 110));
        Border border = BorderFactory.createLineBorder(new Color(76, 118, 181), 2);
        charSP.setBorder(border);
        hexSP.setBorder(border);

        charModel.addTableModelListener(e -> {
            if (!updating) {
                updating = true;
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (row >= 0 && column >= 0) {
                    Object asciiSimb = charModel.getValueAt(row, column);
                    if (asciiSimb != null) {
                        String asciiChar = asciiSimb.toString();
                        if (!asciiChar.isEmpty()) {
                            int Char = asciiChar.charAt(0);
                            String hexValue = Integer.toHexString(Char).toUpperCase();
                            hexModel.setValueAt(hexValue, row, column);
                        }
                    }
                }
                updating = false;
            }
        });

        hexModel.addTableModelListener(e -> {
            if (!updating) {
                updating = true;
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (row >= 0 && column >= 0) {
                    Object hexSimb = hexModel.getValueAt(row, column);
                    if (hexSimb != null) {
                        String hexChar = hexSimb.toString();
                        if (!hexChar.isEmpty()) {
                            int Char = Integer.parseInt(hexChar, 16);
                            char charValue = (char) Char;
                            charModel.setValueAt(charValue, row, column);
                        }
                    }
                }
                updating = false;
            }
        });

        label = new JLabel();
        panel1.add(label);

        panel.add(hexSP);
        panel.add(charSP);
        panel.setBorder(new EmptyBorder(0, 0, 20, 100));

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.add(panel, BorderLayout.CENTER);
        panelMain.add(panel1, BorderLayout.PAGE_START);

        frame.add(panelMain);
        frame.setJMenuBar(jMenuBar);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //внешний вид ячеек
    private void ColumnsAndRows() {
        hexTable.setFont(new Font("Courier New", Font.BOLD, 13));
        charTable.setFont(new Font("Courier New", Font.BOLD, 13));

        hexTable.setShowHorizontalLines(false);
        hexTable.setRowHeight(25);
        charTable.setShowHorizontalLines(false);
        charTable.setRowHeight(25);

        hexTable.setCellSelectionEnabled(true);
        charTable.setCellSelectionEnabled(true);

        hexTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        charTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        hexTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        charTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        hexTable.getColumnModel().getColumn(0).setMaxWidth(100);
        for (int i = 1; i <hexTable.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setMaxWidth(35);
        }
        //подумать об размерах ячеек и setBorder, после закоммитить
        charTable.getColumnModel().getColumn(0).setMaxWidth(100);
        for (int i = 1; i <charTable.getColumnCount(); i++) {
            charTable.getColumnModel().getColumn(i).setMaxWidth(35);
        }

        hexTable.getTableHeader().setPreferredSize(new Dimension(hexTable.getTableHeader().getPreferredSize().width,25));
        charTable.getTableHeader().setPreferredSize(new Dimension(hexTable.getTableHeader().getPreferredSize().width,25));

        hexTable.setDefaultRenderer(Object.class, new CellRenderForHex());
        charTable.setDefaultRenderer(Object.class, new CellRenderForChar());

        hexTable.addMouseListener(new mouseListener(hexTable,charTable));
        charTable.addMouseListener(new mouseListener(charTable,hexTable));

        hexTable.setDefaultEditor(Object.class, new ActiveCellEditorForHex(hexTable));
        charTable.setDefaultEditor(Object.class, new ActiveCellEditorForChar(charTable));
    }

    //синхронная пометка ячеек
    static class mouseListener extends MouseAdapter {
        private final JTable sourceTable;
        private final JTable targetTable;

        public mouseListener(JTable sourceTable, JTable targetTable){
            this.sourceTable = sourceTable;
            this.targetTable = targetTable;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = sourceTable.rowAtPoint(e.getPoint());
            int col = sourceTable.columnAtPoint(e.getPoint());

            // Установка цвета фона только для выбранной ячейки
            if (row >= 0 && col > 0) {
                selectedRow = row;
                selectedCol = col;
                sourceTable.changeSelection(row, col, false, false);

                activeTable = sourceTable;

                try {
                    label.setText(DecimalView());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                sourceTable.repaint();
                targetTable.repaint();
            }
        }
    }
    //центрироваеие, выделение ячеек, offset
    public static class CellRenderForHex extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component render = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //центрирование
            setHorizontalAlignment(JLabel.CENTER);

            //offset
            if (column == 0){
                setText(String.valueOf(row));
            }else {
                render.setBackground(isSelected || (column == selectedCol && row == selectedRow && isCellVisible(table, row, column)) ? table.getSelectionBackground() : Color.WHITE);
            }


        return render;
    }
    }
    private static class CellRenderForChar extends DefaultTableCellRenderer{
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component render = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //центрирование
            setHorizontalAlignment(JLabel.CENTER);

            //изменение цвета ячееек
            if (column == 0){
                setText(String.valueOf(row));
            }else {
                render.setBackground(isSelected || (column == selectedCol && row == selectedRow && isCellVisible(table, row, column)) ? table.getSelectionBackground() : Color.WHITE);
            }
            return render;
        }
    }

    private static class ActiveCellEditorForHex extends AbstractCellEditor implements TableCellEditor{
        private final JTextField text;
        private final JTable table;

       public ActiveCellEditorForHex(JTable table){
           this.table = table;

           text = new JTextField();
           text.setBorder(null);

            ((PlainDocument)text.getDocument()).setDocumentFilter(new DocumentFilter(){
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if((fb.getDocument().getLength()+ string.length())<=2 && string.matches("[0-9A-Fa-f]*")){
                        super.insertString(fb, offset, string, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if((fb.getDocument().getLength()+text.length()-length)<=2 && text.matches("[0-9A-Fa-f]*")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });
       }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            editingCol = column;

            if (isEdited||isSelected){
                text.setText(value != null ? value.toString() : "");
                text.setBackground(new Color(135, 206, 235));
                isEdited = false;

            return text;
            } else{
                text.setBackground(table.getBackground());
                return null;}
        }

        @Override
        public Object getCellEditorValue() {
            return text.getText();
        }

    }
    private static class ActiveCellEditorForChar extends AbstractCellEditor implements TableCellEditor{
       private final JTextField text;
       private final JTable table;

       public ActiveCellEditorForChar(JTable table) {
           this.table = table;

            text = new JTextField();
            text.setBorder(null);


            ((PlainDocument)text.getDocument()).setDocumentFilter(new DocumentFilter(){
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if((fb.getDocument().getLength()+string.length())<=1){
                        super.insertString(fb, offset, string, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if((fb.getDocument().getLength()+text.length()-length)<=1) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });
        }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editingRow = row;
        editingCol = column;
        if (isEdited||isSelected){

            text.setText(value != null ? value.toString() : "");
            text.setBackground(new Color(135, 206, 235));
            isEdited = false;

            return text;
        } else{
            text.setBackground(table.getBackground());
            return null;
        }
    }

    @Override
    public Object getCellEditorValue() {
        return text.getText();
    }
    }


    // Метод для проверки, видима ли ячейка в таблице
    private static boolean isCellVisible(JTable table, int row, int column) {
        if (table.getParent() instanceof JViewport) {
            JViewport viewport = (JViewport) table.getParent();
            Rectangle rect = table.getCellRect(row, column, true);
            Rectangle viewRect = viewport.getViewRect();
            return viewRect.intersects(rect);
        }
        return false;
   }

    private static String DecimalView() throws IOException {
        String value = "";
        int row = hexTable.getSelectedRow();
        int col = hexTable.getSelectedColumn();

        if (row == -1 || col == -1) {
            return "Ячейка не выбрана.";
        }
        int signedSymb = 0;
        int unsignedSymb = 0;

        String symb = (String) hexTable.getValueAt(row, col);
        int maxValue = 127; // 127- это максимальное положительное число 8-битного числа, если речь идет о числах со знаком
        if (symb != null && !symb.isEmpty()){
            unsignedSymb = Integer.parseInt(symb, 16);
            signedSymb = unsignedSymb;
            if (unsignedSymb > maxValue){
                signedSymb = unsignedSymb-(1<<8);//берется 8,т.к. изначальное число может занимать максимум 8 бит
            }
        }

        value ="Число со знаком: "+ signedSymb+ " Число без знака: " + unsignedSymb;
        return value;
    }
}