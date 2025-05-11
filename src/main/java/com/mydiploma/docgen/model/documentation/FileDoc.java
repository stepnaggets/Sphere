package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

import java.util.ArrayList; // Импорт для ArrayList
import java.util.List; // Импорт для List

/**
 * Represents the documentation and structural information extracted from a single source code file.
 * It holds metadata about the file and a list of documented elements found within it,
 * such as classes for object-oriented languages.
 */
public class FileDoc {

    private String fileName;    // Имя файла (например, "MyClass.java")
    private String filePath;    // Относительный или полный путь к файлу (например, "src/main/java/com/example/MyClass.java")
    private String language;    // Язык программирования файла (например, "java", "python")
    private List<ClassDoc> classes; // Список классов, интерфейсов, enum и т.п., найденных в этом файле

    // TODO: Можно добавить другие элементы, если нужно документировать функции вне классов (например, в Python)


    // Конструктор по умолчанию
    public FileDoc() {
        this.classes = new ArrayList<>(); // Инициализируем список классов
    }

    // Конструктор с основными параметрами файла
    public FileDoc(String fileName, String filePath, String language) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.language = language;
        this.classes = new ArrayList<>(); // Инициализируем список классов
    }

    // --- Геттеры и Сеттеры ---

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<ClassDoc> getClasses() {
        return classes;
    }

    // Метод для добавления найденного класса в список классов этого файла
    public void addClass(ClassDoc classDoc) {
         if (this.classes == null) { // Проверка на случай, если список не был инициализирован
            this.classes = new ArrayList<>();
        }
        this.classes.add(classDoc);
    }

     // Геттеры и сеттеры для других элементов, если они будут добавлены (например, functions)
}