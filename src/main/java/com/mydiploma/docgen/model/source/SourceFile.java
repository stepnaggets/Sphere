package com.mydiploma.docgen.model.source; // Убедитесь, что имя пакета совпадает

// Этот класс не требует дополнительных импортов, кроме стандартных Java,
// которые используются внутри методов (например, для работы со строками).

/**
 * Represents a single source code file input to the documentation generator.
 * Contains the file's name, its path within the input set (e.g., relative path in archive/repo),
 * the raw content of the file, and the determined programming language.
 */
public class SourceFile {

    private String fileName; // Имя файла (например, "MyClass.java")
    private String filePath; // Путь к файлу относительно корня входных данных (например, "src/main/java/com/example/MyClass.java")
                             // Для загруженных файлов это может быть просто имя файла или пустая строка.
    private String content;  // Сырое содержимое файла в виде строки
    private String language; // Определенный язык программирования файла (например, "java", "python", "xml", "unknown")

    // Конструктор по умолчанию
    public SourceFile() {
    }

    /**
     * Constructs a SourceFile object.
     * @param fileName The name of the file.
     * @param filePath The path of the file relative to the input source root.
     * @param content The raw content of the file.
     */
    public SourceFile(String fileName, String filePath, String content) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.content = content;
        // При создании объекта сразу определяем язык на основе имени файла
        this.language = determineLanguage(fileName);
    }

    // --- Геттеры (Сеттеры могут быть не нужны, если SourceFile считаем неизменяемым после создания) ---
    // Добавим сеттеры для гибкости, но в идеале SourceFile должен быть почти неизменяемым.

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        // При смене имени файла может поменяться и язык
        this.language = determineLanguage(fileName);
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    // Метод для определения языка по расширению файла
    // Это очень простая эвристика. Для более точного определения нужны более сложные методы (например, анализ содержимого).
    private String determineLanguage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown"; // Не можем определить, если нет имени
        }
        // Ищем последнее вхождение точки
        int lastDotIndex = fileName.lastIndexOf('.');
        // Если точка найдена и не является первым или последним символом
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase(); // Извлекаем расширение и переводим в нижний регистр
            switch (extension) {
                case "java":
                    return "java";
                case "py":
                    return "python";
                case "xml":
                    return "xml"; // Можно парсить XML файлы как исходники, если в них есть документированные структуры
                case "html":
                    return "html"; // Или HTML/TSX/JSX для фронтенда
                case "css":
                    return "css";
                case "js":
                    return "javascript";
                case "ts":
                    return "typescript";
                 case "c":
                 case "cpp":
                 case "h":
                 case "hpp":
                     return "cpp"; // Упрощенно для C/C++
                 case "cs":
                     return "csharp"; // C#
                // TODO: Добавить другие расширения файлов и соответствующие им языки
                default:
                    return "unknown"; // Если расширение не распознано
            }
        }
        return "unknown"; // Если у файла нет расширения
    }

    // TODO: В будущем можно добавить определение кодировки файла, если она не UTF-8

    // Метод toString() может быть полезен для отладки
    @Override
    public String toString() {
        return "SourceFile{" +
               "fileName='" + fileName + '\'' +
               ", filePath='" + filePath + '\'' +
               ", language='" + language + '\'' +
               // Осторожно с выводом всего контента при отладке!
               // ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 100)) + "..." : "null") + '\'' +
               '}';
    }
}