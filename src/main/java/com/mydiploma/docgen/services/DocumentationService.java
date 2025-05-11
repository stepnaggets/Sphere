package com.mydiploma.docgen.services; // Убедитесь, что имя пакета совпадает

// --- Импорты моделей данных ---
import com.mydiploma.docgen.model.documentation.FileDoc;
import com.mydiploma.docgen.model.documentation.ProjectDoc;
import com.mydiploma.docgen.model.source.SourceFile;

// --- Импорты парсеров ---
import com.mydiploma.docgen.parsers.CodeParser;
// Spring может автоматически собрать их в List или Map по интерфейсу CodeParser.
// Нет необходимости импортировать конкретные реализации здесь, если они аннотированы как компоненты.
// import com.mydiploma.docgen.parsers.JavaCodeParser;
// import com.mydiploma.docgen.parsers.PythonCodeParser;

// --- Импорты генераторов ---
import com.mydiploma.docgen.generators.DocumentationGenerator; // Импорт интерфейса генератора
// Нет необходимости импортировать конкретные реализации здесь, если они аннотированы как компоненты.
// import com.mydiploma.docgen.generators.XmlGenerator;
// import com.mydiploma.docgen.generators.HtmlGenerator; // Этот импорт был нужен для instanceOf, но теперь не нужен
// import com.mydiploma.docgen.generators.PdfGenerator;

// --- Импорты Spring Framework ---
import org.springframework.beans.factory.annotation.Autowired; // Для внедрения зависимостей
import org.springframework.stereotype.Service; // Аннотация @Service для сервисного слоя
// import org.springframework.beans.factory.annotation.Qualifier; // Теперь не нужен, т.к. не делаем приведение типов


// --- Импорты для работы с файлами и путями ---
import java.io.IOException; // Для ошибок ввода/вывода
// import java.nio.file.Path; // Не нужен, т.к. путь к временной папке теперь внутри HtmlGenerator
// import java.nio.file.Paths; // Не нужен


// --- Импорты коллекций и утилит ---
import java.util.HashMap; // Для реализации Map
import java.util.List; // Для списков файлов
import java.util.Map; // Для карт (мап) парсеров и генераторов
import java.util.Set; // Для набора ключей (поддерживаемых языков/форматов)
// import java.util.ArrayList; // Не нужен


/**
 * Service responsible for orchestrating the documentation generation process.
 * It selects the appropriate parser based on the source file language,
 * builds the internal documentation model, and calls the appropriate generator
 * based on the requested output format.
 */
@Service // Указываем Spring, что это сервис, которым он должен управлять
public class DocumentationService {

    // Мапа для хранения парсеров, ключ - язык (строка), значение - объект парсера
    private final Map<String, CodeParser> parsers;

    // Мапа для хранения генераторов, ключ - имя формата (строка из getFormatName()), значение - объект генератора
    // Spring автоматически найдет все классы с @Component/@Service, реализующие DocumentationGenerator,
    // и передаст их в этот List, из которого мы заполним мапу.
    private final Map<String, DocumentationGenerator> generators;


    // Конструктор для внедрения всех необходимых зависимостей
    // Spring автоматически найдет все классы с @Component/@Service, реализующие CodeParser и DocumentationGenerator,
    // и передаст их в эти списки.
    @Autowired // Аннотация для автоматического внедрения
    public DocumentationService(List<CodeParser> codeParsers, List<DocumentationGenerator> documentationGenerators) {

        // Инициализируем мапу парсеров
        this.parsers = new HashMap<>();
        // Заполняем мапу, используя язык как ключ
        for (CodeParser parser : codeParsers) {
            this.parsers.put(parser.getLanguage(), parser);
            System.out.println("DocumentationService: Registered parser for language: " + parser.getLanguage()); // Логируем, какие парсеры найдены
        }
        // TODO: Возможно, добавить проверку на дубликаты языков у парсеров

        // Инициализируем мапу генераторов
        this.generators = new HashMap<>();
        // Заполняем мапу, используя имя формата как ключ
        for (DocumentationGenerator generator : documentationGenerators) {
            this.generators.put(generator.getFormatName(), generator);
            System.out.println("DocumentationService: Registered generator for format: " + generator.getFormatName()); // Логируем, какие генераторы найдены
        }
        // TODO: Возможно, добавить проверку на дубликаты имен форматов у генераторов
    }

    /**
     * Orchestrates the documentation generation process.
     * Selects appropriate parsers and generators based on input.
     *
     * @param sourceFiles  List of source files to process.
     * @param outputFormat Chosen output format ("html", "pdf", "xml").
     * @return The result of the generation, depending on the format:
     * - For "xml": String containing XML content.
     * - For "html": Path to the temporary output directory containing HTML files.
     * - For "pdf": byte[] containing PDF content.
     * @throws IOException              if an I/O error occurs during parsing or generation.
     * @throws IllegalArgumentException if the requested output format is not supported.
     * @throws Exception                for other unexpected errors during parsing or generation.
     */
    // Возвращаемый тип Object, так как результат может быть разным (String, Path, byte[])
    public Object generateDocumentation(List<SourceFile> sourceFiles, String outputFormat) throws IOException, Exception {
        System.out.println("\n--- Documentation Service Called ---");
        System.out.println("Received " + sourceFiles.size() + " source files.");
        System.out.println("Output format requested: " + outputFormat);

        // Проверяем, есть ли генератор для запрошенного формата
        // Используем toLowerCase() на случай, если формат в запросе был с большой буквы
        DocumentationGenerator generator = generators.get(outputFormat.toLowerCase());
        if (generator == null) {
            System.err.println("Error: No generator available for format: " + outputFormat);
            // Если генератор не найден, выбрасываем исключение
            throw new IllegalArgumentException("Unsupported output format requested: " + outputFormat);
        }


        // --- Шаг 1: Парсинг файлов и построение внутренней модели ---
        ProjectDoc projectDoc = new ProjectDoc();
        projectDoc.setProjectName("Generated Documentation"); // TODO: Позволить пользователю задавать имя проекта

        System.out.println("DocumentationService: Starting parsing process...");
        for (SourceFile sourceFile : sourceFiles) {
            String language = sourceFile.getLanguage(); // Получаем язык файла
            CodeParser parser = parsers.get(language); // Находим парсер для этого языка

            if (parser != null) {
                System.out.println("DocumentationService: Parsing file: " + sourceFile.getFileName() + " (Language: " + language + ")");
                try {
                    FileDoc fileDoc = parser.parse(sourceFile); // Вызываем парсер
                    if (fileDoc != null) {
                        projectDoc.addFile(fileDoc); // Добавляем распарсенный FileDoc в ProjectDoc
                        System.out.println("DocumentationService:   Parsed successfully. Found " + fileDoc.getClasses().size() + " classes in " + sourceFile.getFileName());
                        // TODO: Добавить более детальное логирование структуры projectDoc для отладки
                    } else {
                         System.out.println("DocumentationService:   Parser returned null for file: " + sourceFile.getFileName() + ". File might not contain relevant code elements or is empty.");
                    }
                } catch (Exception e) {
                    // Ловим ошибки парсинга для конкретного файла, но продолжаем обрабатывать другие
                    System.err.println("DocumentationService: Error parsing file: " + sourceFile.getFileName() + " - " + e.getMessage());
                    e.printStackTrace(); // Выводим стек-трейс для отладки
                    // TODO: Возможно, добавить информацию об этой ошибке в ProjectDoc или отдельный лог для пользователя
                }

            } else {
                System.out.println("DocumentationService: No parser available for language: " + language + " (File: " + sourceFile.getFileName() + "), skipping file.");
                // TODO: Возможно, добавить FileDoc с сообщением об ошибке/предупреждении для этого файла
            }
        }
        System.out.println("DocumentationService: Parsing complete. ProjectDoc created with " + projectDoc.getFiles().size() + " processed files.");


        // --- Шаг 2: Генерация выходного формата ---
        System.out.println("DocumentationService: Starting generation for format: " + outputFormat);
        try {
             // Вызываем универсальный метод generate у найденного генератора, передавая ProjectDoc.
             // Логика определения пути вывода (для HTML) или возврата контента (XML/PDF)
             // находится внутри самого генератора.
             Object generatedContent = generator.generate(projectDoc); // <-- ИСПРАВЛЕНО: Вызываем generate с ОДНИМ аргументом


             System.out.println("DocumentationService: Generation complete for format: " + outputFormat);
             System.out.println("--- Documentation Service Finished ---\n");

             return generatedContent; // Возвращаем результат генерации (String, Path или byte[])

        } catch (IOException e) {
            // Ошибки I/O при генерации (например, запись файла для HTML/PDF)
            System.err.println("DocumentationService: I/O Error during documentation generation for format " + outputFormat + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Перевыбрасываем исключение
        } catch (Exception e) {
             // Другие ошибки при генерации (например, DocumentException для PDF, ошибки Thymeleaf для HTML)
             System.err.println("DocumentationService: Unexpected error during documentation generation for format " + outputFormat + ": " + e.getMessage());
              e.printStackTrace();
             throw e; // Перевыбрасываем исключение
        }
    }

    /**
     * Returns the set of supported languages based on available parsers.
     * This can be used by the frontend to show available options.
     * @return A set of language strings.
     */
    public Set<String> getSupportedLanguages() {
        return parsers.keySet();
    }

    /**
     * Returns the set of supported output formats based on available generators.
     * This can be used by the frontend to show available options.
     * @return A set of format name strings.
     */
     public Set<String> getSupportedOutputFormats() {
          return generators.keySet();
      }

    // TODO: Реализовать логику очистки старых временных папок документации HTML
}