package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

import java.util.ArrayList; // Импорт для ArrayList
import java.util.List; // Импорт для List

/**
 * Represents the documentation and structural information for a class
 * or similar programming construct (e.g., interface, enum, annotation).
 * It includes general documentation for the class itself, its fields, and its methods.
 */
public class ClassDoc {

    private String name;         // Имя класса (например, "MyClass")
    private String type;         // Тип конструкции (например, "class", "interface", "enum", "@interface")
    private DocBlock docBlock;   // Блок документации/комментарий (Javadoc) прямо перед объявлением класса
    private List<FieldDoc> fields; // Список полей (переменных) класса, каждый со своей документацией
    private List<MethodDoc> methods; // Список методов класса, каждый со своей документацией
    // private List<ClassDoc> innerClasses; // Можно добавить поддержку внутренних классов в будущем

    // Конструктор по умолчанию
    public ClassDoc() {
        this.fields = new ArrayList<>(); // Инициализируем списки при создании объекта
        this.methods = new ArrayList<>();
    }

     // Конструктор с базовыми параметрами
     public ClassDoc(String name, String type) {
        this.name = name;
        this.type = type;
        this.fields = new ArrayList<>(); // Инициализируем списки
        this.methods = new ArrayList<>();
    }

    // --- Геттеры и Сеттеры ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DocBlock getDocBlock() {
        return docBlock;
    }

    // Сеттер для блока документации. Парсер будет использовать его, чтобы связать DocBlock с классом.
    public void setDocBlock(DocBlock docBlock) {
        this.docBlock = docBlock;
    }

    public List<FieldDoc> getFields() {
        return fields;
    }

    // Метод для добавления поля в список полей класса
     public void addField(FieldDoc field) {
         if (this.fields == null) { // Проверка на случай, если список не был инициализирован
            this.fields = new ArrayList<>();
        }
        this.fields.add(field);
     }

    public List<MethodDoc> getMethods() {
        return methods;
    }

    // Метод для добавления метода в список методов класса
    public void addMethod(MethodDoc method) {
         if (this.methods == null) { // Проверка на случай, если список не был инициализирован
            this.methods = new ArrayList<>();
        }
        this.methods.add(method);
    }

     // Геттер и сеттер для innerClasses, если они будут добавлены позже
}