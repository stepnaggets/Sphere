package com.mydiploma.docgen.parsers; // Убедитесь, что имя пакета совпадает

import com.mydiploma.docgen.model.documentation.FileDoc; // Импорт нашей модели FileDoc
import com.mydiploma.docgen.model.source.SourceFile; // Импорт нашей модели SourceFile

import java.io.IOException; // Парсер может работать с файлами и выбрасывать ошибки I/O

/**
 * Interface for code parsers for different programming languages.
 * Each parser implementation should handle a specific language, read a SourceFile,
 * and produce a structured FileDoc representing the documented elements.
 */
public interface CodeParser {

    /**
     * Parses a single source file and extracts documentation and structural information.
     * The parser should read the content of the SourceFile, identify documented elements
     * (classes, methods, fields, comments), and populate a FileDoc object.
     *
     * @param sourceFile The source file containing the code to parse.
     * @return A FileDoc object containing the parsed structure and documentation,
     * or null if parsing fails or the file content is not relevant/parsable by this parser.
     * @throws IOException If an I/O error occurs while accessing source file content (less likely if content is already a String).
     * @throws Exception If a parsing-specific error occurs (e.g., malformed comment, syntax issue).
     */
    FileDoc parse(SourceFile sourceFile) throws IOException, Exception; // Метод для парсинга файла

    /**
     * Returns the language that this parser implementation is designed to handle.
     * This string should match the language identifier determined by SourceFile (e.g., "java", "python").
     *
     * @return The language string (e.g., "java", "python").
     */
    String getLanguage(); // Метод для получения поддерживаемого языка
}