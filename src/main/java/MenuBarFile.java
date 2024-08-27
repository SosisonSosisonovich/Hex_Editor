import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MenuBarFile {
    public MenuBarFile(JMenuBar jMenuBar, HexTableModel hexModel, CharTableModel charModel){

        JMenu file = new JMenu("Файл");
        jMenuBar.add(file);

        JMenuItem news = file.add(new JMenuItem("Новый"));
        JMenuItem open = file.add(new JMenuItem("Открыть"));
        JMenuItem save = file.add(new JMenuItem("Сохранить"));

        news.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                news(hexModel, charModel);
            }
        });
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                news(hexModel, charModel);
                //news(hexModel);

                JFileChooser fileChooser = new JFileChooser();
                int a = fileChooser.showOpenDialog(null);

                if(a != JFileChooser.CANCEL_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        hexModel.setFile(selectedFile);
                        charModel.setFile(selectedFile);

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                }else {
                    return;
                }
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int a = fileChooser.showSaveDialog(null);
                List<String[]> tableData = new ArrayList<>();

                int colCount = hexModel.getColumnCount();
                int rowCount = hexModel.getRowCount();

                for (int i = 0; i < rowCount; i++) {
                    String[] rowData = new String[colCount];
                    for (int j = 1; j < colCount; j++) {
                        if (hexModel.getValueAt(i, j) != null) {
                            rowData[j] = hexModel.getValueAt(i, j).toString();
                        } else {
                            rowData[j] = String.valueOf(String.format("0",0xff));
                        }
                    }
                    tableData.add(rowData);
                }

                if (a != JFileChooser.CANCEL_OPTION){
                    File selectedDirectory = fileChooser.getSelectedFile();

                    if (!selectedDirectory.exists()) {
                        File file = new File(String.valueOf(selectedDirectory) + ".bin");
                        save(file, tableData);
                    }
                    else { //если файл есть
                        try {
                            hexModel.saveChanges();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } else{
                    System.out.println("Отмена действия.");
                }
            }
        });
    }

    //обновление таблицы
    public void news(HexTableModel hexModel, CharTableModel charModel){
        hexModel.clearTable();
        charModel.clearTable();
        //charModel.setRowCount(0);
        //charModel.setRowCount(50);
    }

    public void save(File file,  List<String[]> tableData){
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            for (String[] array: tableData){
                for (int i = 1; i<array.length; i++){
                    if (array[i] == null){
                        bos.write((byte) 0xFF);
                    }else {
                        int arr = Integer.parseInt(array[i], 16);
                        bos.write((byte)arr);
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
