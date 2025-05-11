package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

import java.util.ArrayList; // Импорт для ArrayList
import java.util.List; // Импорт для List

/**
 * Represents the aggregated documentation for an entire project
 * or a collection of source files processed together.
 * It serves as the root of the documentation model tree.
 */
public class ProjectDoc {

    private String projectName; // Название проекта (можно получить из URL репозитория, имени архива или запросить у пользователя)
    private List<FileDoc> files; // Список всех файлов исходного кода, которые были распарсены

    // Конструктор по умолчанию
    public ProjectDoc() {
        this.files = new ArrayList<>(); // Инициализируем список файлов
    }

    // TODO: Можно добавить конструктор с названием проекта, если оно известно сразу

    // --- Геттеры и Сеттеры ---

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<FileDoc> getFiles() {
        return files;
    }

    // Метод для добавления распарсенного файла в список файлов проекта
    public void addFile(FileDoc file) {
        if (this.files == null) { // Проверка на случай, если список не был инициализирован
            this.files = new ArrayList<>();
        }
        this.files.add(file);
    }

    // TODO: Возможно, добавить другие методы для удобного доступа или поиска (например, findClassByName)
}