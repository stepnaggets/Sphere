package com.mydiploma.docgen.parsers; // Убедитесь, что имя пакета совпадает

import com.mydiploma.docgen.model.documentation.*; // Импорт всех классов модели документации
import com.mydiploma.docgen.model.source.SourceFile; // Импорт SourceFile

import java.io.IOException; // Импорт для обработки ошибок ввода/вывода (при чтении файла, хотя контент уже в строке)
import java.nio.charset.StandardCharsets; // Импорт для кодировки
import java.util.ArrayList; // Импорт для ArrayList
import java.util.List; // Импорт для List
import java.util.regex.Matcher; // Импорт для работы с регулярными выражениями
import java.util.regex.Pattern; // Импорт для работы с регулярными выражениями

/**
 * Basic parser for Java source code and Javadoc comments.
 * This parser reads the source file line by line, identifies Javadoc comments,
 * classes, fields, and methods, and populates the documentation model (FileDoc).
 * NOTE: This is a simplified parser suitable for a diploma project.
 * It may not handle all complex Java syntax or Javadoc variations perfectly.
 */
public class JavaCodeParser implements CodeParser { // Реализуем интерфейс CodeParser

    // Простые регулярные выражения для поиска основных структур.
    // !!! Эти regex очень упрощены и могут давать ложные срабатывания или пропускать сложные случаи синтаксиса Java !!!
    // Например, они не учитывают строки в коде, комментарии /* */, или глубоко вложенные скобки.
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\s*(public|protected|private|abstract|final)?\\s*(class|interface|enum|@interface)\\s+([a-zA-Z0-9_]+)");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\s*(public|protected|private|static|final|transient|volatile)?\\s+([^\\s]+)\\s+([a-zA-Z0-9_]+)\\s*(=[^;]*)?;"); // Ищем "тип имя = значение;" или "тип имя;"
    private static final Pattern METHOD_PATTERN = Pattern.compile("\\s*(public|protected|private|static|final|abstract|synchronized)?\\s+([^\\s]+)\\s+([a-zA-Z0-9_]+)\\s*\\([^)]*\\)\\s*(\\{|;)"); // Ищем "тип имя(", параметры ")", и ожидаем "{" или ";"


    /**
     * Parses a single Java source file.
     * @param sourceFile The source file containing Java code.
     * @return A FileDoc object populated with parsed Java classes, methods, fields, and documentation.
     * @throws IOException if reading source file content fails (shouldn't happen if content is string).
     * @throws Exception if a parsing issue is encountered.
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public FileDoc parse(SourceFile sourceFile) throws IOException, Exception {
        // Проверяем, что файл существует и имеет содержимое
        if (sourceFile == null || sourceFile.getContent() == null || sourceFile.getContent().trim().isEmpty()) {
            System.out.println("JavaCodeParser: Source file is null or empty, skipping parsing.");
            return null; // Возвращаем null, если парсить нечего
        }
        // Убеждаемся, что парсер предназначен для этого языка
        if (!"java".equalsIgnoreCase(sourceFile.getLanguage())) {
             System.out.println("JavaCodeParser: File is not marked as Java, skipping parsing.");
             return null;
        }


        FileDoc fileDoc = new FileDoc(sourceFile.getFileName(), sourceFile.getFilePath(), getLanguage());

        String content = sourceFile.getContent();
        String[] lines = content.split("\\R"); // Разбиваем содержимое на строки, \\R учитывает разные символы конца строки (\n, \r, \r\n)

        DocBlock currentDocBlock = null; // Переменная для хранения текущего собираемого Javadoc блока
        boolean inDocBlock = false;      // Флаг: находимся ли мы сейчас внутри /** ... */
        boolean inMultiLineComment = false; // Флаг: находимся ли мы сейчас внутри /* ... */ (эти комментарии игнорируем для документации)
        // boolean inSingleLineComment = false; // Однострочные комментарии //... игнорируются построчно и не требуют флага состояния для блока

        // Переменные для отслеживания контекста в коде (внутри класса, метода и т.д.)
        ClassDoc currentClass = null;
        // MethodDoc currentMethod = null; // В этом упрощенном парсере не отслеживаем контекст внутри метода

        // Проходим по каждой строке файла
        for (String line : lines) {
            String trimmedLine = line.trim(); // Удаляем ведущие и завершающие пробелы

            // --- Обработка Комментариев ---

            // Начало Javadoc комментария
            if (trimmedLine.startsWith("/**")) {
                inDocBlock = true;
                currentDocBlock = new DocBlock(); // Создаем новый DocBlock
                // Добавляем первую строку комментария (без /**)
                String docLineContent = trimmedLine.substring(3).trim();
                currentDocBlock.setFullText(docLineContent);
                continue; // Переходим к следующей строке, так как эта строка уже обработана
            }

            // Начало обычного многострочного комментария (игнорируем его)
            if (trimmedLine.startsWith("/*")) {
                 // Проверяем, не является ли это также концом комментария на той же строке
                 if (!trimmedLine.endsWith("*/")) {
                     inMultiLineComment = true;
                 } else {
                      // Если комментарий /* ... */ на одной строке, просто игнорируем ее
                 }
                 continue; // Пропускаем эту строку
            }

            // Однострочный комментарий (игнорируем его)
            if (trimmedLine.startsWith("//")) {
                 continue; // Пропускаем эту строку
            }

            // --- Обработка строк внутри Javadoc комментария ---
            if (inDocBlock) {
                // Конец Javadoc комментария
                if (trimmedLine.endsWith("*/")) {
                    inDocBlock = false; // Выходим из состояния Javadoc комментария
                    // Добавляем последнюю строку комментария (без */)
                    String docLineContent = trimmedLine.substring(0, trimmedLine.length() - 2).trim();
                     // Добавляем содержимое строки к полному тексту DocBlock, с переносом строки
                    currentDocBlock.setFullText(currentDocBlock.getFullText() + "\n" + docLineContent);

                    // !!! ИСПРАВЛЕНИЕ: Парсим текст DocBlock теперь, когда он полностью собран !!!
                    // Вызываем НЕТАТИЧЕСКИЙ метод parseTextForDescriptionAndTags() у созданного объекта currentDocBlock
                    currentDocBlock.parseTextForDescriptionAndTags();

                    // currentDocBlock теперь содержит распарсенные данные (описание и теги).
                    // Он готов к связыванию со следующим элементом кода (классом, методом, полем), который будет найден после этого комментария.
                    // Мы не сбрасываем currentDocBlock в null СРАЗУ, ждем следующей строки кода,
                    // чтобы связать комментарий с элементом.
                    continue; // Переходим к следующей строке
                } else {
                     // Строка внутри Javadoc комментария (между /** и */)
                     // Удаляем необязательный ведущий символ '*' и пробелы после него
                     String docLineContent = trimmedLine;
                     if (docLineContent.startsWith("*")) {
                         docLineContent = docLineContent.substring(1).trim();
                     }
                     // Добавляем содержимое строки к полному тексту DocBlock, с переносом строки
                     currentDocBlock.setFullText(currentDocBlock.getFullText() + "\n" + docLineContent);
                     continue; // Переходим к следующей строке
                }
            }

            // --- Обработка строк внутри обычного многострочного комментария ---
            if (inMultiLineComment) {
                // Конец обычного многострочного комментария
                if (trimmedLine.endsWith("*/")) {
                    inMultiLineComment = false; // Выходим из состояния многострочного комментария
                }
                continue; // Пропускаем строки многострочных комментариев
            }


            // --- Парсинг Структуры Кода (если мы не внутри какого-либо комментария) ---

            // Если строка пустая или содержит только скобки/точки с запятой на верхнем уровне класса/метода
            if (trimmedLine.isEmpty() || trimmedLine.equals("{") || trimmedLine.equals("}") || trimmedLine.equals(";")) {
                 // Можем сбросить currentDocBlock, если он еще не был связан.
                 // Это помогает избежать связывания комментария с пустой строкой или скобкой.
                 currentDocBlock = null; // Сбрасываем неиспользованный Javadoc
                 continue; // Пропускаем такие строки
            }


            // Ищем объявление класса (или интерфейса, enum, annotation)
            Matcher classMatcher = CLASS_PATTERN.matcher(trimmedLine);
            if (classMatcher.find()) {
                String type = classMatcher.group(2); // class, interface, enum, @interface
                String name = classMatcher.group(3); // Имя класса

                currentClass = new ClassDoc(name, type); // Создаем новый объект ClassDoc
                // Если перед объявлением класса был собран Javadoc блок, связываем его с этим классом
                if (currentDocBlock != null) {
                    currentClass.setDocBlock(currentDocBlock); // Устанавливаем DocBlock для класса
                    currentDocBlock = null; // Сбрасываем, так как Javadoc был использован
                }
                fileDoc.addClass(currentClass); // Добавляем этот ClassDoc в список классов файла
                //System.out.println("JavaCodeParser: Parsed Class: " + type + " " + name); // Логгируем для отладки
                continue; // Переходим к следующей строке

                // TODO: В более сложном парсере, здесь нужно начать отслеживать тело класса {...}
            }

            // Если мы находимся внутри класса (currentClass не null), ищем поля или методы
            if (currentClass != null) {

                // Ищем объявление метода
                Matcher methodMatcher = METHOD_PATTERN.matcher(trimmedLine);
                 // Ищем сигнатуру метода: тип имя ( параметры )...
                 // Regex очень упрощен. Ищем что-то похожее на сигнатуру, заканчивающуюся на "{" или ";"
                 if (methodMatcher.find()) { //&& trimmedLine.contains("(")) { // Дополнительная проверка на наличие открывающей скобки, хотя regex уже это делает

                     String name = methodMatcher.group(3); // Имя метода
                     String returnType = methodMatcher.group(2); // Тип возвращаемого значения (очень приблизительно, т.к. может быть много модификаторов)
                     String signature = trimmedLine; // Пока берем всю строку как сигнатуру (очень неточно)

                     MethodDoc methodDoc = new MethodDoc(name, signature, returnType); // Создаем новый MethodDoc
                     // Если перед методом был собран Javadoc блок, связываем его с этим методом
                     if (currentDocBlock != null) {
                         methodDoc.setDocBlock(currentDocBlock); // Устанавливаем DocBlock для метода
                         // DocBlock.setDocBlock в MethodDoc теперь автоматически парсит параметры из DocBlock.
                         currentDocBlock = null; // Сбрасываем, так как Javadoc был использован
                     }
                     currentClass.addMethod(methodDoc); // Добавляем метод в текущий класс
                     //System.out.println("JavaCodeParser:   Parsed Method: " + name + "()"); // Логгируем
                     // TODO: В более сложном парсере, здесь нужно начать отслеживать тело метода {...}
                     continue; // Переходим к следующей строке
                }

                // Ищем объявление поля
                // Ищем строку, которая выглядит как объявление переменной и заканчивается на ";"
                Matcher fieldMatcher = FIELD_PATTERN.matcher(trimmedLine);
                 if (fieldMatcher.find()) { // && trimmedLine.endsWith(";")) { // Regex уже проверяет ; или =...;
                      String type = fieldMatcher.group(2); // Тип поля (очень приблизительно)
                      String name = fieldMatcher.group(3); // Имя поля

                      FieldDoc fieldDoc = new FieldDoc(name, type); // Создаем новый FieldDoc
                       // Если перед полем был собран Javadoc блок, связываем его с этим полем
                       if (currentDocBlock != null) {
                           fieldDoc.setDocBlock(currentDocBlock); // Устанавливаем DocBlock для поля
                           currentDocBlock = null; // Сбрасываем, так как Javadoc был использован
                       }
                       currentClass.addField(fieldDoc); // Добавляем поле в текущий класс
                       //System.out.println("JavaCodeParser:   Parsed Field: " + type + " " + name); // Логгируем
                       continue; // Переходим к следующей строке
                 }
            }

             // Если строка не была комментарием, началом класса/метода/поля,
             // и мы не внутри многострочного комментария, просто пропускаем ее.
             // Это могут быть пустые строки, закрывающие скобки }, операторы внутри методов и т.д.
             // Если был собран Javadoc, но мы не нашли элемент кода после него, этот Javadoc будет проигнорирован.
             currentDocBlock = null; // Сбрасываем неиспользованный Javadoc


        } // Конец цикла по строкам

        // После прохода по всем строкам, fileDoc содержит собранную информацию для этого файла

        System.out.println("JavaCodeParser: Parsing complete for file: " + sourceFile.getFileName());
        System.out.println("JavaCodeParser: Found " + fileDoc.getClasses().size() + " classes."); // Логируем итоги

        return fileDoc; // Возвращаем распарсенный FileDoc
    }

    /**
     * Returns the language that this parser handles.
     * @return The string "java".
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public String getLanguage() {
        return "java"; // Этот парсер обрабатывает Java
    }

     // TODO: Добавить более сложную логику парсинга (например, с использованием AST парсера)
     // TODO: Улучшить распознавание сигнатур методов и типов полей/возвращаемых значений
     // TODO: Обработка вложенных классов, анонимных классов, лямбда-выражений и т.д.
     // TODO: Обработка стандартных комментариев, чтобы не путать их с Javadoc, если regex не идеален
}