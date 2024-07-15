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
                    System.out.println(selectedDirectory);

                    if (!selectedDirectory.exists()) {
                        File file = new File(String.valueOf(selectedDirectory) + ".bin");
                        save(file, tableData);
                    }
                    else { //если файл есть
                        save(selectedDirectory, tableData);
                    }
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

    /*public void open(File selectedFile, DefaultTableModel hexModel){
        try(BufferedReader reader = new BufferedReader(new FileReader(selectedFile))){
            String line;
            int indexRow = 0;
            int indexCol = 1; // Начинаем с 1, чтобы пропустить первый столбец

            while ((line = reader.readLine()) != null) {
                byte[] byteData = line.getBytes();
                int index = 0;

                while (index < byteData.length) {
                    if (indexCol >= hexModel.getColumnCount()) {
                        indexCol = 1; // Сброс к первому столбцу
                        indexRow++; // Перейти на следующую строку
                    }

                    if (indexRow >= hexModel.getRowCount()) {
                        break; // Остановиться, если достигнут конец таблицы
                    }

                    String hex = Integer.toHexString(byteData[index] & 0xFF);
                    hexModel.setValueAt(hex, indexRow, indexCol);
                    index++;
                    indexCol++;
                }
            }
        } catch (IOException e) {
                throw new RuntimeException(e);
        }
    }*/

    public void open(File selectedFile, DefaultTableModel hexModel){
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(selectedFile, "r")){
            long fileLength = randomAccessFile.length();
            byte[] buffer = new byte[2048];
            long index = 0;
            int indexRow = 0;
            int indexCol = 1; // Начинаем с 1, чтобы пропустить первый столбец

            while (index < fileLength){

                int bytesRead = randomAccessFile.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                for (int i = 0; i < bytesRead; i++) {
                    if (indexCol >= hexModel.getColumnCount()) {
                        indexCol = 1; // Сброс к первому столбцу
                        indexRow++; // Перейти на следующую строку
                    }

                    if (indexRow >= hexModel.getRowCount()) {
                        break; // Остановиться, если достигнут конец таблицы
                    }

                    String hex = String.format("%02X", buffer[i]);
                    hexModel.setValueAt(hex, indexRow, indexCol);

                    index++;
                    indexCol++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
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
