package com.mydiploma.docgen.model.documentation; // Убедитесь, что имя пакета совпадает

import java.util.ArrayList; // Импорт для ArrayList
import java.util.HashMap; // Импорт для HashMap
import java.util.List; // Импорт для List
import java.util.Map; // Импорт для Map
import java.util.regex.Matcher; // Импорт для работы с регулярными выражениями
import java.util.regex.Pattern; // Импорт для работы с регулярными выражениями

/**
 * Represents a documentation block associated with a code element (class, method, field).
 * Stores the full text of the comment and provides methods to extract the main description and tags.
 * Currently focused on parsing Javadoc-like comments.
 */
public class DocBlock {

    private String fullText;        // Полный сырой текст комментария (включая символы /**, */, *)
    private String mainDescription; // Основное описание, которое идет до первого тега (@...)
    // Map для хранения распарсенных тегов. Ключ - имя тега (например, "param"),
    // Значение - список строк со значениями этого тега (т.к. @param может быть несколько)
    private Map<String, List<String>> tags;

    // Регулярное выражение для поиска тегов Javadoc: @tagname [text...]
    // Находит @tagname, за которым следует один или несколько пробелов (\s+), а затем любой текст (.*?),
    // до следующего тега (позитивный просмотр вперед ?=@) или до конца строки ($).
    // Pattern.DOTALL (. совпадает с символами переноса строки) нужен, если тег и его описание занимают несколько строк.
    private static final Pattern TAG_PATTERN = Pattern.compile("@[a-zA-Z0-9_]+\\s+.*?(?=\\s+@[a-zA-Z0-9_]+|\\Z)", Pattern.DOTALL);
    // Исправленное regex для более точного парсинга тегов, учитывая пробелы и конец блока

    // Конструктор по умолчанию
    public DocBlock() {
        this.tags = new HashMap<>(); // Инициализируем карту тегов
    }

    // Конструктор с полным текстом комментария. Парсинг произойдет позже.
    public DocBlock(String fullText) {
        this.fullText = fullText;
        this.tags = new HashMap<>();
        // Парсинг основного описания и тегов будет выполнен методом parseTextForDescriptionAndTags()
    }

    // --- Геттеры и Сеттеры ---

    public String getFullText() {
        return fullText;
    }

    // Сеттер для полного текста комментария. Парсер будет использовать его, чтобы собрать текст.
    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getMainDescription() {
         // Убедимся, что парсинг был выполнен, если fullText есть, но mainDescription еще нет
         if (this.mainDescription == null && this.fullText != null) {
             parseTextForDescriptionAndTags(); // Вызываем парсинг при первом запросе, если не парсили
         }
        return mainDescription != null ? mainDescription : ""; // Возвращаем пустую строку, если описания нет
    }

    // Парсер будет устанавливать основное описание после извлечения тегов
    private void setMainDescription(String mainDescription) {
         // В будущем можно добавить обработку Markdown или HTML форматирования здесь
        this.mainDescription = mainDescription != null ? mainDescription.trim() : "";
    }


    public Map<String, List<String>> getTags() {
         // Убедимся, что парсинг был выполнен
         if (this.tags.isEmpty() && this.fullText != null && this.mainDescription == null) { // Проверяем, если теги пусты и похоже, что еще не парсили
             parseTextForDescriptionAndTags();
         }
        return tags;
    }

    /**
     * Adds a tag value to the documentation block during parsing.
     * @param tagName The name of the tag (e.g., "param", "return").
     * @param tagValue The raw value of the tag (text after tag name).
     */
    public void addTag(String tagName, String tagValue) {
        // Добавляем значение тега в список для данного имени тега.
        // Если такого имени тега еще нет в карте, создаем новый список.
        this.tags.computeIfAbsent(tagName, k -> new ArrayList<>()).add(tagValue != null ? tagValue.trim() : "");
    }

    /**
     * Gets all raw values for a specific tag name.
     * @param tagName The name of the tag.
     * @return A list of raw values (String) for the tag, or an empty list if the tag is not present.
     */
    public List<String> getTagValues(String tagName) {
         // Убедимся, что парсинг был выполнен
         if (this.tags.isEmpty() && this.fullText != null && this.mainDescription == null) {
             parseTextForDescriptionAndTags();
         }
        return this.tags.getOrDefault(tagName, new ArrayList<>()); // Возвращаем пустой список, если тег не найден
    }

    /**
     * Gets the first raw value for a specific tag name (useful for single-value tags like @return).
     * @param tagName The name of the tag.
     * @return The first raw value as a String, or null if the tag is not present or has no value.
     */
    public String getFirstTagValue(String tagName) {
        List<String> values = getTagValues(tagName);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Parses the fullText of the documentation block to extract the main description and tags.
     * This method should be called by the parser after the fullText has been fully assembled.
     */
    public void parseTextForDescriptionAndTags() {
        // Очищаем предыдущие результаты парсинга перед началом
        this.mainDescription = "";
        this.tags.clear();

        if (this.fullText == null || this.fullText.trim().isEmpty()) {
            return; // Нечего парсить, если текст пустой
        }

        // Удаляем начальные и конечные пробелы, а также символы комментария Javadoc /**, */, и ведущие *
        String cleanText = this.fullText.trim();
        // Удаляем /** в начале и */ в конце, если они есть
        if (cleanText.startsWith("/**")) cleanText = cleanText.substring(3);
        if (cleanText.endsWith("*/")) cleanText = cleanText.substring(0, cleanText.length() - 2);

        // Удаляем ведущие пробелы и '*' с каждой строки внутри блока (кроме первой строки описания)
        String[] lines = cleanText.split("\\R"); // Разбиваем на строки
        StringBuilder processedTextBuilder = new StringBuilder();
        boolean firstLine = true;
        for (String line : lines) {
            if (firstLine) {
                processedTextBuilder.append(line.trim()); // Первую строку просто обрезаем от пробелов
                firstLine = false;
            } else {
                 // Для последующих строк удаляем ведущие пробелы и необязательный символ '*'
                 String trimmedLine = line.trim();
                 if (trimmedLine.startsWith("*")) {
                     trimmedLine = trimmedLine.substring(1).trim();
                 }
                 processedTextBuilder.append("\n").append(trimmedLine); // Добавляем строку (с переносом)
            }
        }
        String textToParse = processedTextBuilder.toString().trim(); // Финальный текст для парсинга

        // Если после очистки текст пуст
        if (textToParse.isEmpty()) {
             this.mainDescription = "";
             return;
        }


        Matcher tagMatcher = TAG_PATTERN.matcher(textToParse);
        int firstTagStart = textToParse.length(); // Изначально считаем, что тегов нет

        // Находим позицию начала первого тега
        if (tagMatcher.find()) {
             firstTagStart = tagMatcher.start(); // Позиция начала первого найденного тега
        }

        // Текст до первого тега - это основное описание
        this.setMainDescription(textToParse.substring(0, firstTagStart).trim());

        // Парсим все теги после нахождения первого
        if (firstTagStart < textToParse.length()) {
             // Сбрасываем матчер, чтобы начать поиск тегов с начала текста (или после описания)
             // Лучше искать теги в части текста ПОСЛЕ основного описания
             tagMatcher = TAG_PATTERN.matcher(textToParse.substring(firstTagStart)); // Ищем только в части с тегами

             while (tagMatcher.find()) {
                 String tagBlock = tagMatcher.group(0).trim(); // Получаем весь блок тега "@tagname text..."
                 int firstSpace = tagBlock.indexOf(' '); // Ищем первый пробел после имени тега

                 if (firstSpace > 1) { // Если есть пробел после '@' и имя тега непустое
                     String tagName = tagBlock.substring(1, firstSpace); // Имя тега без '@'
                     String tagValue = tagBlock.substring(firstSpace).trim(); // Значение тега
                     this.addTag(tagName, tagValue); // Добавляем распарсенный тег
                     //System.out.println("DocBlock Parser: Parsed Tag: @" + tagName + " -> " + tagValue); // Логгируем для отладки
                 } else if (tagBlock.length() > 1 && tagBlock.startsWith("@")) { // Если есть только имя тега без значения (например, @deprecated), и оно начинается с @
                      String tagName = tagBlock.substring(1); // Имя тега без '@'
                       this.addTag(tagName, ""); // Значение пустое
                      // System.out.println("DocBlock Parser: Parsed Tag: @" + tagName + " (no value)"); // Логгируем для отладки
                 }
             }
        }
         //System.out.println("DocBlock Parser: Parsed Main Description (snippet): " + this.mainDescription.substring(0, Math.min(this.mainDescription.length(), 100)) + "..."); // Логгируем snippet
    }

    /**
     * Extracts and parses @param tags into a list of ParameterDoc objects.
     * Assumes @param tag value is in the format "paramName Description".
     * @return List of ParameterDoc objects.
     */
    public List<ParameterDoc> getParameterDocs() {
        // Убедимся, что парсинг был выполнен
        if (this.tags.isEmpty() && this.fullText != null && this.mainDescription == null) {
            parseTextForDescriptionAndTags();
        }

        List<ParameterDoc> parameterDocs = new ArrayList<>();
        List<String> paramValues = getTagValues("param"); // Получаем все сырые значения тега @param

        for (String paramValue : paramValues) {
             int firstSpace = paramValue.indexOf(' '); // Ищем первый пробел для отделения имени параметра от описания
             if (firstSpace > 0) { // Если пробел найден и не является первым символом
                 String paramName = paramValue.substring(0, firstSpace); // Имя параметра
                 String paramDescription = paramValue.substring(firstSpace).trim(); // Описание параметра (обрезаем пробелы в начале и конце)
                 parameterDocs.add(new ParameterDoc(paramName, paramDescription)); // Создаем и добавляем ParameterDoc
             } else {
                 // Если пробела нет, вся строка считается именем параметра (описания нет)
                 parameterDocs.add(new ParameterDoc(paramValue.trim(), "")); // Убедимся, что имя обрезано от пробелов
             }
        }
        return parameterDocs;
    }

     /**
      * Extracts the description from the @return tag.
      * Assumes there is only one @return tag.
      * @return The description of the return value, or null if @return tag is not present.
      */
     public String getReturnDescription() {
         // Убедимся, что парсинг был выполнен
         if (this.tags.isEmpty() && this.fullText != null && this.mainDescription == null) {
             parseTextForDescriptionAndTags();
         }
         return getFirstTagValue("return"); // Используем вспомогательный метод для получения первого значения тега "return"
     }

    // TODO: Добавить методы для парсинга других специфичных тегов (@throws, @author, @version и т.д.)

    // TODO: В будущем добавить поддержку Markdown или HTML внутри описания и тегов
}