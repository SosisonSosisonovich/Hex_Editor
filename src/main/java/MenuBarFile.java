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
                    new HexFileReader(selectedFile, hexModel).execute();

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
    //внутренний класс для чтения файла, наследует StringWorker, чтобы чтение проходило на фоне
    public class HexFileReader extends SwingWorker<Void, Object[]>{

        private static final int BUFFER_SIZE = 1024;
        private final File selectedFile;
        private final DefaultTableModel hexModel;

        public HexFileReader(File selectedFile, DefaultTableModel hexModel) {
            this.selectedFile = selectedFile;
            this.hexModel = hexModel;
        }
        @Override
        protected Void doInBackground() throws Exception {

            try(RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")){
                long fileLength = selectedFile.length();
                byte[] buff = new byte[BUFFER_SIZE];

                for (int i = 0; i < fileLength; i+= BUFFER_SIZE) {
                    int bytesRead = raf.read(buff, 0, (int)Math.min(BUFFER_SIZE, fileLength - i));
                    for (int j = 0; j < bytesRead; j++) {
                        int value = buff[j] & 0xFF;
                        String hexValue = String.format("%02X",value);
                        int offset = i + j;
                        publish(new Object[]{offset, hexValue});
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void process(List<Object[]> chunks) {
            for (Object[] chunk : chunks) {
                Number offsetNumber = (Number) chunk[0];
                long offset = offsetNumber.longValue();
                String hexString = (String) chunk[1];

                int row = (int) (offset / hexModel.getColumnCount());
                int col = (int) (offset % hexModel.getColumnCount()) + 1;

                if (col >= hexModel.getColumnCount()){
                    row++;
                    col = 1;
                }

                hexModel.setValueAt(hexString, row, col);
            }
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
