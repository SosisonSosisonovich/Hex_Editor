import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class HexTableModel extends AbstractTableModel {
    private RandomAccessFile file;
    private int bytesPerRow; // Количество байтов в одной строке
    private long fileLength;
    private int initialRows = 50;
    private Byte[][] data;
    private List<Integer> searchResults;

    public HexTableModel(int bytesPerRow) {
        this.bytesPerRow = bytesPerRow;
        this.searchResults = new ArrayList<>();
        data = new Byte[initialRows][bytesPerRow];
    }

    public void setFile(File file) throws IOException {
        if (this.file != null) { //если файл открыт, то программа его закрывает
            this.file.close();
        }

        this.file = new RandomAccessFile(file, "rw");
        this.fileLength = this.file.length();
        this.searchResults.clear();

        // Очистка данных для новой загрузки
        data = null;

        fireTableDataChanged(); // Обновить таблицу после загрузки файла
    }

    @Override
    public int getRowCount() {
        if (file == null) {
            return data.length; // Возвращаем количество строк для пустой таблицы
        }
        return (int) Math.ceil((double) fileLength / bytesPerRow) + 1;//для точного определения кол-ва строк, чтобы все влезло
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

    public int getBytesPerRow() {
        return bytesPerRow;
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

    public void searchBytes(byte[] searchBytes) {
        SwingUtilities.invokeLater(() -> {
            searchResults.clear();

            if(file != null) {
                try {
                    RandomAccessFile raf = this.file;
                    long segmentSize = 1024 * 1024;//для уменьшения нагрузки будем считывать данные по 1 мб за раз
                    long position = 0;// текущее положение в файле

                    while (position < fileLength) {
                        long remaining = fileLength - position;
                        long currentSegmentSize = Math.min(segmentSize, remaining);

                        byte[] segment = new byte[(int) currentSegmentSize];//данные, которые сейчас проверяются
                        raf.seek(position);
                        raf.readFully(segment);

                        for (int i = 0; i <= segment.length - searchBytes.length; i++) {
                            boolean match = true;
                            for (int j = 0; j < searchBytes.length; j++) {
                                if (segment[i + j] != searchBytes[j]) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                searchResults.add((int) (position / bytesPerRow + i / bytesPerRow));
                            }
                        }

                        position += currentSegmentSize;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else{
                int rowCount = getRowCount();
                int colCount = getColumnCount();

                for (int row = 0; row < rowCount; row++) {
                    List<Byte> rowBytes = new ArrayList<>();

                    // Проходим по всем столбцам в строке
                    for (int col = 0; col < colCount; col++) {
                        Object cellValue = getValueAt(row, col);

                        if (cellValue != null && !cellValue.toString().trim().isEmpty()) {

                            String hexString = cellValue.toString();
                            int len = hexString.length();
                            byte[] data = new byte[len / 2];

                            for (int i = 0; i < len; i += 2) {
                                data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                        + Character.digit(hexString.charAt(i+1), 16));
                            }

                            byte[] cellBytes = data;
                            for (byte b : cellBytes) {
                                rowBytes.add(b);
                            }
                        }
                    }

                    byte[] rowByteArray = new byte[rowBytes.size()];
                    for (int i = 0; i < rowBytes.size(); i++) {
                        rowByteArray[i] = rowBytes.get(i);
                    }

                    // Ищем последовательность байт в строке
                    for (int i = 0; i <= rowByteArray.length - searchBytes.length; i++) {
                        boolean match = true;
                        for (int j = 0; j < searchBytes.length; j++) {
                            if (rowByteArray[i + j] != searchBytes[j]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            searchResults.add(row);
                            break; // Если нашли совпадение в строке, больше не ищем в этой строке
                        }
                    }
                }

                fireTableDataChanged();
            }
            fireTableDataChanged();
        });
    }

    public List<Integer> getSearchResults() {
        return searchResults;
    }

    public void insertBytes(int startRow, int startColumn, byte[] newBytes) {
        try {
            // Вычисление позиции вставки
            int insertPosition = startRow * bytesPerRow + (startColumn - 1);

            if (file == null) {
                Byte[] flatData = flattenDataArray();
                int oldLength = flatData.length;

                Byte[] newData = new Byte[oldLength + newBytes.length];

                // Копирование данных до позиции вставки
                System.arraycopy(flatData, 0, newData, 0, insertPosition);

                // Вставка новых байтов
                for (int i = 0; i < newBytes.length; i++) {
                    newData[insertPosition + i] = newBytes[i];
                }

                // Копирование данных после позиции вставки
                System.arraycopy(flatData, insertPosition, newData, insertPosition + newBytes.length, oldLength - insertPosition);

                expandDataArray(newData);
            } else {
                // Сдвигаем данные в файле, чтобы освободить место для новых байтов
                long newFileLength = fileLength + newBytes.length;
                file.setLength(newFileLength);

                // Сдвигаем существующие байты вниз, начиная с конца файла
                for (long i = fileLength - 1; i >= insertPosition; i--) {
                    file.seek(i);
                    byte b = file.readByte();
                    file.seek(i + newBytes.length);
                    file.writeByte(b);
                }

                // Вставляем новые байты
                file.seek(insertPosition);
                file.write(newBytes);

                fileLength = newFileLength;
            }

            fireTableDataChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteBytes(int startRow, int startColumn, int numBytesToDelete) {
        try {
            // Вычисление позиции удаления
            int deletePosition = startRow * bytesPerRow + (startColumn - 1);

            if (file == null) {
                // Работа с массивом данных
                Byte[] flatData = flattenDataArray();

                int oldLength = flatData.length;

                // Проверка, чтобы не удалить больше, чем есть данных
                if (deletePosition + numBytesToDelete > oldLength) {
                    numBytesToDelete = oldLength - deletePosition;
                }

                Byte[] newData = new Byte[oldLength - numBytesToDelete];

                // Копирование данных до позиции удаления
                System.arraycopy(flatData, 0, newData, 0, deletePosition);

                // Копирование данных после удаления
                System.arraycopy(flatData, deletePosition + numBytesToDelete, newData, deletePosition, oldLength - deletePosition - numBytesToDelete);

                expandDataArray(newData);

            } else {
                // Проверка, чтобы не удалить больше, чем есть данных
                if (deletePosition + numBytesToDelete > fileLength) {
                    numBytesToDelete = (int) (fileLength - deletePosition);
                }

                // Сдвигаем существующие байты вверх, начиная с позиции после удаления
                for (long i = deletePosition + numBytesToDelete; i < fileLength; i++) {
                    file.seek(i);
                    byte b = file.readByte();
                    file.seek(i - numBytesToDelete);
                    file.writeByte(b);
                }

                // Уменьшаем длину файла
                long newFileLength = fileLength - numBytesToDelete;
                file.setLength(newFileLength);
                fileLength = newFileLength;
            }

            fireTableDataChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //преобразование двумерного массива в одномерный массив
    private Byte[] flattenDataArray() {
        Byte[] flatData = new Byte[data.length * bytesPerRow];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(data[i], 0, flatData, i * bytesPerRow, bytesPerRow);
        }
        return flatData;
    }

    //преобразование одномерного массива обратно в двумерный
    private void expandDataArray(Byte[] newData) {
        int rows = (int) Math.ceil((double) newData.length / bytesPerRow);
        data = new Byte[rows][bytesPerRow];
        for (int i = 0; i < newData.length; i++) {
            data[i / bytesPerRow][i % bytesPerRow] = newData[i];
        }
    }
}
