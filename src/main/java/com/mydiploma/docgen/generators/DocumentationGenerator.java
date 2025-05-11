package com.mydiploma.docgen.generators; // Убедитесь, что имя пакета совпадает

import com.mydiploma.docgen.model.documentation.ProjectDoc; // Импортируем модель данных

import java.io.IOException; // Интерфейс может выбрасывать исключения

/**
 * Interface for documentation generators.
 * Implementations should generate documentation in a specific format (e.g., XML, HTML, PDF).
 */
public interface DocumentationGenerator { // Объявляем как интерфейс

    /**
     * Generates documentation from the ProjectDoc model.
     * The return type is Object because generators can return different things:
     * String (for XML/text), byte[] (for binary like PDF), Path (for output folder like HTML).
     * The calling service should handle the specific return type based on the generator used.
     *
     * @param projectDoc The documentation model to generate from.
     * @return The generated documentation content as an Object (String, byte[], Path), or null if generation fails.
     * @throws IOException If an I/O error occurs during generation (e.g., writing files).
     * @throws Exception For other generation-specific errors.
     */
    Object generate(ProjectDoc projectDoc) throws IOException, Exception; // Общий метод генерации

    /**
     * Returns the name of the format this generator handles.
     * @return The format name (e.g., "xml", "html", "pdf").
     */
    String getFormatName(); // Метод для получения имени формата
}