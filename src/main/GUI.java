package main;

import javax.swing.*;
import java.awt.Rectangle;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

public class GUI implements Serializable {
    private final JFrame frame;
    private JPanel panel;
    private static JTable hexTable;
    private static JTable charTable;
    static JTable activeTable;
    private boolean updating  = false;
    private static int selectedRow = -1;
    private static int selectedCol = -1;
    private  static  int editingRow = -1;
    private  static  int editingCol = -1;
    private static boolean isEdited = false;

    public GUI() {
        frame = new JFrame("Hex-Editor");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        run();
    }


    void run(){
        panel = new JPanel(new GridLayout());
        //запрет на изменение offset'а
        DefaultTableModel hexModel = new DefaultTableModel(colNames(),50) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        DefaultTableModel charModel = new DefaultTableModel(50, 17){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        charModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (!updating) {
                    updating = true;

                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    if (row >= 0 && column >=0) {
                        Object asciiSimb = charModel.getValueAt(row, column);

                        if (asciiSimb != null) {
                            String asciiChar = asciiSimb.toString();
                            if (!asciiChar.isEmpty()) {
                                int Char = asciiChar.charAt(0);
                                String hexValue = Integer.toHexString(Char);

                                hexModel.setValueAt(hexValue, row, column);
                            }
                        }
                    }
                    updating = false;
                }
            }
        });
        hexModel.addTableModelListener(new TableModelListener() {
           @Override
           public void tableChanged(TableModelEvent e) {
               if (!updating) {
                   updating = true;
                   int row = e.getFirstRow();
                   int column = e.getColumn();

                   if (row >= 0 && column >=0) {
                       Object HexSimb = hexModel.getValueAt(row, column);

                       if (HexSimb != null) {
                           String HexChar = HexSimb.toString();
                           if (!HexChar.isEmpty()) {
                               int Char = Integer.parseInt(HexChar, 16);
                               char charValue = (char) Char;

                               charModel.setValueAt(charValue, row, column);
                           }
                       }
                   }
                   updating = false;
               }
           }
       });

        hexTable = new JTable(hexModel);
        charTable = new JTable(charModel);

        activeTable = hexTable;

        JMenuBar jMenuBar = new JMenuBar();
        new MenuBarFile(jMenuBar,hexModel, charModel);
        new MenuBarEdit(jMenuBar,hexModel,charModel,hexTable,charTable);

        hexTable.getTableHeader().setReorderingAllowed(false);
        charTable.getTableHeader().setReorderingAllowed(false);
        ColumnsAndRows();

        JScrollPane hexSP = new JScrollPane(hexTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollBar sharedScrollBar = hexSP.getVerticalScrollBar();
        JScrollPane charSP = new JScrollPane(charTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charSP.setVerticalScrollBar(sharedScrollBar);
        charSP.setBorder(new EmptyBorder(0, 0, 0, 100));

        panel.add(hexSP);
        panel.add(charSP);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        frame.add(panel);
        frame.setJMenuBar(jMenuBar);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();
    }


    private static Object[] colNames() {
        Object[] object = new Object[17];
        object[0] = "offset";
        for (int i = 1; i < 17; i++) {
            String hexName = Integer.toHexString(i - 1);
            object[i] = hexName;
        }
        return object;
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

        hexTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        charTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        hexTable.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        charTable.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        hexTable.getColumnModel().getColumn(0).setMaxWidth(70);
        for (int i = 1; i <hexTable.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setMaxWidth(25);
        }

        charTable.getColumnModel().getColumn(0).setMaxWidth(36);
        for (int i = 1; i <charTable.getColumnCount(); i++) {
            charTable.getColumnModel().getColumn(i).setMaxWidth(20);
        }

        hexTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                hexTable.getColumnModel().getColumn(0).setMinWidth(70);
                hexTable.getColumnModel().getColumn(e.getToIndex()).setMaxWidth(25);
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {}

            @Override
            public void columnMoved(TableColumnModelEvent e) {}

            @Override
            public void columnMarginChanged(ChangeEvent e) {}

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {}
        });
        charTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                charTable.getColumnModel().getColumn(0).setMinWidth(36);
                charTable.getColumnModel().getColumn(e.getToIndex()).setMaxWidth(20);
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {}

            @Override
            public void columnMoved(TableColumnModelEvent e) {}

            @Override
            public void columnMarginChanged(ChangeEvent e) {}

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {}
        });

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

                sourceTable.repaint();
                targetTable.repaint();
            }
        }
    }
    public static class CellRenderForHex extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component render = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //центрирование
            setHorizontalAlignment(JLabel.CENTER);

            //offset
            if (column == 0){
                setText(String.format("%08X",row*16));
            }
            //изменение цвета ячееек
            if (isSelected || (column == selectedCol && row == selectedRow && isCellVisible(table, row, column))){
                render.setBackground(new Color(135, 206, 235));
            } else {
                render.setBackground(table.getBackground());
            }
        return render;
    }
    }

    //центрироваеие, выделение ячеек, offset
    private static class CellRenderForChar extends DefaultTableCellRenderer{
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component render = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //центрирование
            setHorizontalAlignment(JLabel.CENTER);

            //изменение цвета ячееек
            if (column == 0){
                setText(String.format("%d",row));
            }
            if (isSelected || (column == selectedCol && row == selectedRow && isCellVisible(table, row, column))){
                render.setBackground(new Color(135, 206, 235));
            } else {
                render.setBackground(table.getBackground());
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

           addNewRow(table);
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
            addNewRow(table);
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

   // Новая строка, если редактируется последняя строка таблицы
   private static void addNewRow(JTable table){
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addTableModelListener(new TableModelListener() {
           @Override
           public void tableChanged(TableModelEvent e) {
               if (e.getType() == TableModelEvent.UPDATE) {
                   int row = e.getLastRow();

                   // Проверка, что редактируемая ячейка находится в последней строке
                   if (row == model.getRowCount() - 1) {
                       model.addRow(new Object[model.getColumnCount()]);
                   }
               }
           }
       });
   }
}