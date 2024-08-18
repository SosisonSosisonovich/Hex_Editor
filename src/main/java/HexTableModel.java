import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HexTableModel extends AbstractTableModel {
    private RandomAccessFile file;
    private final int bytesPerRow; // Количество байтов в одной строке
    private long fileLength;

    public HexTableModel(File file, int bytesPerRow) throws IOException {
        this.file = new RandomAccessFile(file, "rw"); // Открываем файл для чтения и записи
        this.fileLength = this.file.length();
        this.bytesPerRow = bytesPerRow;
    }
    @Override
    public int getRowCount() {
        return (int) Math.ceil((double) fileLength / bytesPerRow) + 1;//для точного определения кол-вва строк, чтобы все влезло
    }

    @Override
    public int getColumnCount() {
        return bytesPerRow + 1; // Дополнительный столбец для offset
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return String.format("%08X", rowIndex * bytesPerRow);
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
        if (columnIndex > 0 && aValue != null) {
            int byteIndex = rowIndex * bytesPerRow + (columnIndex - 1);

            try {
                byte newValue = (byte) Integer.parseInt(aValue.toString(), 16);

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

                fireTableCellUpdated(rowIndex, columnIndex);

                if (byteIndex == fileLength - 1) {
                    fireTableRowsInserted(getRowCount(), getRowCount());
                }
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
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
}
