package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

// Класс FieldDoc сам по себе не требует импортов, кроме DocBlock,
// если он используется в полях.

/**
 * Represents the documentation and structural information for a class field (variable).
 * It includes general documentation for the field itself.
 */
public class FieldDoc {

    private String name;       // Имя поля (например, "myField")
    private String type;       // Тип поля (например, "int", "String", "List<String>")
    private DocBlock docBlock; // Блок документации/комментарий (Javadoc) прямо перед объявлением поля

    // Конструктор по умолчанию
     public FieldDoc() {
    }

    // Конструктор с базовыми параметрами
     public FieldDoc(String name, String type) {
        this.name = name;
        this.type = type;
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

    // Сеттер для блока документации. Парсер будет использовать его.
    public void setDocBlock(DocBlock docBlock) {
        this.docBlock = docBlock;
    }
}