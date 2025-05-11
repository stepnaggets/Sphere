package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

// Этот класс не требует дополнительных импортов.

/**
 * Represents the documentation for a method or constructor parameter,
 * typically extracted from an @param tag in a documentation block.
 */
public class ParameterDoc {

    private String name;        // Имя параметра (например, "inputString")
    private String description; // Описание параметра, взятое из текста после имени в @param теге

    // Конструктор по умолчанию
    public ParameterDoc() {
    }

    // Конструктор с основными параметрами (имя и описание)
    public ParameterDoc(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // --- Геттеры и Сеттеры ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Метод toString() может быть полезен для отладки
    @Override
    public String toString() {
        return "ParameterDoc{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}