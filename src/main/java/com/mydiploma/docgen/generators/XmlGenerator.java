package com.mydiploma.docgen.generators; // Убедитесь, что имя пакета совпадает

import com.fasterxml.jackson.databind.ObjectMapper; // Импорт основных классов Jackson
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper; // Импорт XmlMapper для работы с XML

import com.mydiploma.docgen.model.documentation.ProjectDoc; // Импорт модели данных ProjectDoc

import org.springframework.stereotype.Component; // Импорт аннотации @Component

import java.io.ByteArrayOutputStream; // Импорт для записи данных в память
import java.io.IOException; // Импорт для обработки ошибок ввода/вывода
import java.nio.charset.StandardCharsets; // Импорт для кодировки символов


/**
 * Generator for creating documentation in XML format from ProjectDoc.
 * Uses Jackson library (jackson-dataformat-xml) for serialization.
 */
@Component // Указываем Spring, что это компонент, которым он должен управлять и который можно внедрять
public class XmlGenerator implements DocumentationGenerator { // Реализуем интерфейс DocumentationGenerator

    // XmlMapper - это специальный ObjectMapper для работы с XML
    private final XmlMapper xmlMapper;

    // Конструктор для инициализации XmlMapper
    public XmlGenerator() {
        xmlMapper = new XmlMapper();
        // Настраиваем XmlMapper для форматированного (читаемого) вывода XML
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Опционально: настроить другие параметры Jackson при необходимости
        // Например, отключить генерацию корневого элемента <ProjectDoc> если он не нужен
        // xmlMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }

    /**
     * Generates XML documentation from the ProjectDoc model as a String.
     * This is the internal specific generation method.
     *
     * @param projectDoc The documentation model.
     * @return XML content as a String.
     * @throws IOException if an error occurs during XML serialization.
     */
    public String generateXmlString(ProjectDoc projectDoc) throws IOException {
         // Используем ByteArrayOutputStream для записи сгенерированного XML в память
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Записываем объект ProjectDoc как XML в поток вывода
        xmlMapper.writeValue(outputStream, projectDoc);
        // Преобразуем содержимое потока (байты XML) в строку, используя кодировку UTF-8
        return outputStream.toString(StandardCharsets.UTF_8.name()); // StandardCharsets из java.nio.charset
    }

    // --- Реализация методов интерфейса DocumentationGenerator ---

    /**
     * Implements the generate method from DocumentationGenerator.
     * Calls the internal XML generation method and returns the result as Object.
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public Object generate(ProjectDoc projectDoc) throws IOException, Exception {
        // Вызываем нашу специфическую для XML генерацию и возвращаем результат
        // Возвращаем String как Object, как требует интерфейс
        return generateXmlString(projectDoc);
    }

    /**
     * Implements the getFormatName method from DocumentationGenerator.
     * @return The name of the format ("xml").
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public String getFormatName() {
        return "xml";
    }
}