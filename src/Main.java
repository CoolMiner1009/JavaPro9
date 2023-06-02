import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// Клас винятку, що викликається при досягненні максимального розміру файлу
class FileMaxSizeReachedException extends Exception {
    public FileMaxSizeReachedException(String message) {
        super(message);
    }
}

// Перелік рівнів логування
enum LoggingLevel {
    INFO, DEBUG
}

// Клас конфігурації логування
class FileLoggerConfiguration {
    private String file;
    private LoggingLevel level;
    private long maxSize;
    private String format;

    public FileLoggerConfiguration(String file, LoggingLevel level, long maxSize, String format) {
        this.file = file;
        this.level = level;
        this.maxSize = maxSize;
        this.format = format;
    }

    public String getFile() {
        return file;
    }

    public LoggingLevel getLevel() {
        return level;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public String getFormat() {
        return format;
    }
}

// Клас, що завантажує конфігурацію логування з файлу
class FileLoggerConfigurationLoader {
    public FileLoggerConfiguration load(String configFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        String file = null;
        LoggingLevel level = null;
        long maxSize = 0;
        String format = null;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("FILE:")) {
                file = line.substring(5).trim();
            } else if (line.startsWith("LEVEL:")) {
                String levelStr = line.substring(6).trim();
                level = LoggingLevel.valueOf(levelStr);
            } else if (line.startsWith("MAX-SIZE:")) {
                String maxSizeStr = line.substring(9).trim();
                maxSize = Long.parseLong(maxSizeStr);
            } else if (line.startsWith("FORMAT:")) {
                format = line.substring(7).trim();
            }
        }

        reader.close();

        return new FileLoggerConfiguration(file, level, maxSize, format);
    }
}

// Клас логування
class FileLogger {
    private FileLoggerConfiguration configuration;
    private File currentLogFile;
    private FileWriter fileWriter;
    private long currentSize;

    public FileLogger(FileLoggerConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.currentLogFile = new File(configuration.getFile());
        this.fileWriter = new FileWriter(currentLogFile, true);
        this.currentSize = currentLogFile.length();
    }

    private void rotateLogFile() throws IOException {
        fileWriter.close();

        // Отримуємо дату для імені нового файлу
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
        String date = dateFormat.format(new Date());

        // Формуємо ім'я нового файлу
        String fileName = "Log_" + date + ".txt";
        currentLogFile = new File(fileName);
        fileWriter = new FileWriter(currentLogFile, true);
        currentSize = currentLogFile.length();
    }

    public void debug(String message) throws IOException, FileMaxSizeReachedException {
        if (configuration.getLevel() == LoggingLevel.DEBUG) {
            writeLog("DEBUG", message);
        }
    }

    public void info(String message) throws IOException, FileMaxSizeReachedException {
        if (configuration.getLevel() == LoggingLevel.INFO || configuration.getLevel() == LoggingLevel.DEBUG) {
            writeLog("INFO", message);
        }
    }

    private void writeLog(String level, String message) throws IOException, FileMaxSizeReachedException {
        if (currentSize + message.length() > configuration.getMaxSize()) {
            rotateLogFile();
        }

        String logMessage = "[" + getCurrentTime() + "][" + level + "] Повідомлення: " + message + "\n";
        fileWriter.write(logMessage);
        fileWriter.flush();
        currentSize += logMessage.length();
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(new Date());
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            // Завантаження конфігурації з файлу
            FileLoggerConfigurationLoader loader = new FileLoggerConfigurationLoader();
            FileLoggerConfiguration configuration = loader.load("config.txt");

            // Створення об'єкту логгера
            FileLogger logger = new FileLogger(configuration);

            // Використання логгера
            logger.debug("Debug message");
            logger.info("Info message");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileMaxSizeReachedException e) {
            System.out.println(e.getMessage());
        }
    }
}

