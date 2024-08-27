import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HexTableModel extends AbstractTableModel {
    private RandomAccessFile file;
    private int bytesPerRow; // Количество байтов в одной строке
    private long fileLength;
    private int initialRows = 50;
    private Byte[][] data;

    public HexTableModel(int bytesPerRow) {
        this.bytesPerRow = bytesPerRow;
        data = new Byte[initialRows][bytesPerRow];
    }

    public void setFile(File file) throws IOException {
        if (this.file != null) { //если файл открыт, то программа его закрывает
            this.file.close();
        }

        this.file = new RandomAccessFile(file, "rw");
        this.fileLength = this.file.length();

        // Очистка данных для новой загрузки
        data = null;

        fireTableDataChanged(); // Обновить таблицу после загрузки файла
    }

    @Override
    public int getRowCount() {
        if (file == null) {
            return data.length; // Возвращаем количество строк для пустой таблицы
        }
        return (int) Math.ceil((double) fileLength / bytesPerRow) + 1;//для точного определения кол-вва строк, чтобы все влезло
    }

    public void setRowCount(int rowCount){
        data = new Byte[rowCount][bytesPerRow];
        fireTableDataChanged();
    }

    public void clearTable(){
        data = new Byte[0][];

        //если открыт какой-то файл, то закрываем его
        if(file != null){
            try{
                file.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            file = null;
        }
        fileLength = 0;

        fireTableDataChanged();
        setRowCount(50);
    }

    @Override
    public int getColumnCount() {
        return bytesPerRow + 1; // Дополнительный столбец для offset
    }

    public void addColumn() {
        bytesPerRow++;
        Byte[][] newData = new Byte[data.length][bytesPerRow];

        // Копируем существующие данные в новый массив
        for (int row = 0; row < data.length; row++) {
            System.arraycopy(data[row], 0, newData[row], 0, data[row].length);
        }
        // Обновляем data на новый массив
        data = newData;
        // Уведомляем таблицу об изменении структуры
        fireTableStructureChanged();

    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return String.format("%08X", rowIndex * bytesPerRow);
        }

        if (file == null) {
            // Если файл не загружен, возвращаем данные из массива
            Byte value = data[rowIndex][columnIndex - 1];
            return value == null ? "" : String.format("%02X", value);
        }

        int byteIndex = rowIndex * bytesPerRow + (columnIndex - 1);
        if (byteIndex < fileLength) {
            try {
                file.seek(byteIndex);
                int value = file.read();

                return String.format("%02X", value);
            } catch (IOException e) {
                e.printStackTrace();

                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null || columnIndex == 0) {
            return;
        }

        try {
            byte newValue = (byte) Integer.parseInt(aValue.toString(), 16);

            if(file == null){
                if (rowIndex >= data.length) {
                    Byte[][] newData = new Byte[rowIndex + 1][bytesPerRow];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    data = newData;
                }
                data[rowIndex][columnIndex - 1] = newValue;

            }else {
                int byteIndex = rowIndex * bytesPerRow + (columnIndex - 1);

                if (byteIndex >= fileLength) {
                    file.seek(fileLength);

                    while (fileLength < byteIndex) {
                        file.write(0); // Заполняем пустые места нулями
                        fileLength++;
                    }
                    file.write(newValue);
                    fileLength++;

                } else {
                    file.seek(byteIndex);
                    file.write(newValue);
                }
            }

            fireTableCellUpdated(rowIndex, columnIndex);

            // добавляем строку, если редактируется последняя строка
            if (rowIndex == getRowCount() - 1 && columnIndex == bytesPerRow) {
                addEmptyRow();
            }

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
           }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Offset";
        } else {
            return String.format("%02X", column - 1);
        }
    }

    private void addEmptyRow() {
        if (file == null) {
            Byte[][] newData = new Byte[data.length + 1][bytesPerRow];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
            fireTableRowsInserted(data.length - 1, data.length - 1);
        }
    }

    public void saveChanges() throws IOException {
        if (file != null) {
            file.getChannel().force(true); // Обеспечить запись всех изменений в файл
        }
    }


    //Подумать над вариантом обновления таблицы путем создания новго массива с данными
    public void pasteDataWithShift(byte[] newData, int row, int col) throws IOException {
        int byteIndex = row * getColumnCount() + (col - 1);

        //если работаем с пустой таблицей
        if(file == null){
            if (row >= data.length){
                Byte[][] newDatas = new Byte[data.length + 1][bytesPerRow];
                System.arraycopy(data, 0, newDatas, 0, data.length);
                data = newDatas;
                fireTableRowsInserted(data.length - 1, data.length - 1);

            }

            for (int i = newData.length -1 ; i > row; i--) {
                data[i] = data[i - newData.length];
            }

            for (int i = 0; i < newData.length; i++) {
                data[row][col -1 + i] = newData[i];
            }
        }else {
            if(byteIndex < fileLength){
                for (long i = fileLength-1; i >= byteIndex; i--) {
                    file.seek(i);
                    int value = file.read();
                    file.seek(i + newData.length);
                    file.write(value);
                }
            }
            file.seek(byteIndex);
            file.write(newData);
            fileLength += newData.length;
        }
        fireTableDataChanged();
    }
}
