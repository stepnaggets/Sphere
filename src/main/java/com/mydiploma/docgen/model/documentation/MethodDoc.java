package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

import java.util.ArrayList; // Импорт для ArrayList
import java.util.List; // Импорт для List

/**
 * Represents the documentation and structural information for a method or function.
 * It includes the method's signature, return type, associated documentation block,
 * and a list of documented parameters.
 */
public class MethodDoc {

    private String name;          // Имя метода (например, "myMethod")
    private String signature;     // Полная или упрощенная сигнатура метода (например, "public void myMethod(String arg1, int arg2)")
                                  // Полный парсинг сигнатуры может быть сложным для всех языков и случаев.
    private String returnType;    // Тип возвращаемого значения (например, "void", "String", "int[]"). Может быть null или "void".
    private DocBlock docBlock;    // Блок документации/комментарий (Javadoc) прямо перед объявлением метода
    private List<ParameterDoc> parameters; // Список документированных параметров, извлеченных из @param тегов

    // Конструктор по умолчанию
    public MethodDoc() {
         this.parameters = new ArrayList<>(); // Инициализируем список параметров
    }

     // Конструктор с основными параметрами метода
     public MethodDoc(String name, String signature, String returnType) {
        this.name = name;
        this.signature = signature;
        this.returnType = returnType;
         this.parameters = new ArrayList<>(); // Инициализируем список параметров
    }

    // --- Геттеры и Сеттеры ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public DocBlock getDocBlock() {
        return docBlock;
    }

    // Сеттер для блока документации. Парсер будет использовать его.
    public void setDocBlock(DocBlock docBlock) {
        this.docBlock = docBlock;
        // Как только docBlock установлен, можем сразу распарсить параметры из него
         if (docBlock != null) {
             this.parameters = docBlock.getParameterDocs(); // Используем метод DocBlock для получения ParameterDoc
         } else {
              this.parameters = new ArrayList<>(); // Если DocBlock нет, список параметров пустой
         }
    }

    public List<ParameterDoc> getParameters() {
        return parameters;
    }

    // Метод для добавления параметра (используется в основном внутри setDocBlock или парсером напрямую)
    public void addParameter(ParameterDoc parameter) {
         if (this.parameters == null) { // Проверка на случай, если список не был инициализирован
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(parameter);
    }

    // TODO: Возможно, добавить список исключений, выбрасываемых методом, из тегов @throws
}