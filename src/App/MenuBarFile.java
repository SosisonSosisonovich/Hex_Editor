package App;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MenuBarFile {

    public MenuBarFile(JMenuBar jMenuBar, DefaultTableModel hexModel, DefaultTableModel charModel){
        JMenu file = new JMenu("Файл");
        jMenuBar.add(file);

        JMenuItem news = file.add(new JMenuItem("Новый"));
        JMenuItem open = file.add(new JMenuItem("Открыть"));
        JMenuItem save = file.add(new JMenuItem("Сохранить"));


        news.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));


        news.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                news(hexModel);
                news(charModel);
            }
        });
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                news(hexModel);
                news(charModel);

                JFileChooser fileChooser = new JFileChooser();
                int a = fileChooser.showOpenDialog(null);

                if(a != JFileChooser.CANCEL_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    open(selectedFile, hexModel);

                }else {
                    System.out.println("Отмена действия.");
                }
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int a = fileChooser.showSaveDialog(null);
                List<String[]> tableData = new ArrayList<>();

                for (int i = 0; i < hexModel.getRowCount(); i++) {
                    String[] rowData = new String[hexModel.getColumnCount()];
                    for (int j = 1; j < hexModel.getColumnCount(); j++) {
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
                    File file = new File(String.valueOf(selectedDirectory)+".bin");

                    save(selectedDirectory, tableData);
                } else{
                    System.out.println("Отмена действия.");
                }
            }
        });
    }

    //обновление таблицы
    public void news(DefaultTableModel model){
        model.setRowCount(0);
        model.setRowCount(50);
    }

    public void open(File selectedFile, DefaultTableModel hexModel){
        try {
            ByteArray byteArray = new ByteArray(selectedFile);
            int index = 0;
            int count = hexModel.getColumnCount()*hexModel.getRowCount();

            //заполнение ячеек таблицы
            for (int i = 0; i < hexModel.getRowCount(); i++) {
                for (int j = 1; j < hexModel.getColumnCount(); j++) {
                    if(index<byteArray.getSize()) {
                        String hex = Integer.toHexString(byteArray.getByte(index) & 0xFF);
                        hexModel.setValueAt(hex, i, j);
                        index++;
                    } else break;
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
