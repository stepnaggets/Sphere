package com.mydiploma.docgen.generators; // Убедитесь, что имя пакета совпадает

import com.mydiploma.docgen.model.documentation.ClassDoc; // Импорт классов модели документации
import com.mydiploma.docgen.model.documentation.FileDoc;
import com.mydiploma.docgen.model.documentation.ProjectDoc;
import com.mydiploma.docgen.model.documentation.MethodDoc;
import com.mydiploma.docgen.model.documentation.FieldDoc; // Хотя FieldDoc может явно не использоваться в генерации HTML, импорт может быть полезен
import com.mydiploma.docgen.model.documentation.ParameterDoc; // Может понадобиться для тегов параметров


import org.springframework.beans.factory.annotation.Autowired; // Импорт для внедрения зависимостей
import org.springframework.beans.factory.annotation.Value; // Импорт для чтения значений из application.properties
import org.springframework.stereotype.Component; // Импорт аннотации @Component

import org.thymeleaf.TemplateEngine; // Импорт движка шаблонов Thymeleaf
import org.thymeleaf.context.Context; // Импорт контекста Thymeleaf (для передачи данных в шаблон)

import java.io.IOException; // Импорт для обработки ошибок ввода/вывода
import java.nio.charset.StandardCharsets; // Импорт для кодировки символов
import java.nio.file.Files; // Импорт класса Files для работы с файловой системой
import java.nio.file.Path; // Импорт интерфейса Path
import java.nio.file.Paths; // Импорт класса Paths (для создания Path из строки)
import java.nio.file.StandardCopyOption; // Импорт для опций копирования файлов
import java.util.UUID; // Импорт для генерации уникальных идентификаторов
import java.util.HashMap; // Импорт для HashMap (если понадобится)
import java.util.Map; // Импорт для Map (если понадобится)


/**
 * Generator for creating documentation in HTML format using Thymeleaf templates.
 * Generates a set of linked HTML files and supporting resources (like CSS)
 * into a unique temporary directory.
 */
// Указываем Spring, что это компонент, которым он должен управлять,
// и даем ему имя "html" для использования в @Autowired Map<String, DocumentationGenerator>
@Component("html")
// Теперь класс реализует интерфейс DocumentationGenerator, как ожидает DocumentationService
public class HtmlGenerator implements DocumentationGenerator {

    private final TemplateEngine templateEngine; // Объект движка шаблонов Thymeleaf

    // Читаем базовую временную директорию из application.properties
    @Value("${documentation.html.output.temp-dir:generated-docs-html}") // Читаем значение, по умолчанию "generated-docs-html"
    private String baseHtmlTempOutputDirString; // Будет хранить путь в виде строки

    // Конструктор для внедрения движка шаблонов Thymeleaf
    @Autowired
    public HtmlGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates HTML documentation. This method implements the interface method.
     * It delegates the actual generation to the internal generate(ProjectDoc, Path) method.
     *
     * @param projectDoc The documentation model (ProjectDoc object) to generate from.
     * @return The absolute Path to the unique output directory containing the generated HTML files.
     * @throws IOException if an I/O error occurs during file operations.
     * @throws Exception   For potential errors during template processing.
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public Object generate(ProjectDoc projectDoc) throws IOException, Exception {
        // Преобразуем строку пути из properties в объект Path
        Path baseTempOutputDir = Paths.get(baseHtmlTempOutputDirString);
        // Вызываем основной метод генерации, передавая модель и базовую папку
        return generateInternal(projectDoc, baseTempOutputDir);
    }

    /**
     * Returns the format name that this generator handles.
     * @return The format name ("html").
     */
    @Override // Указываем, что этот метод реализует метод из интерфейса
    public String getFormatName() {
        return "html";
    }


    /**
     * Internal method for generating HTML documentation into a temporary directory.
     * Creates a unique subdirectory within the base temporary output directory for each generation.
     *
     * @param projectDoc        The documentation model (ProjectDoc object) to generate from.
     * @param baseTempOutputDir The base directory where unique temporary documentation folders will be created (e.g., "generated-docs-html").
     * @return The absolute Path to the unique output directory containing the generated HTML files.
     * @throws IOException if an error occurs during file operations (creating directories, writing files, copying files).
     * @throws Exception   For potential errors during template processing (less common if templates are correct).
     */
    public Path generateInternal(ProjectDoc projectDoc, Path baseTempOutputDir) throws IOException, Exception {
        // Генерируем уникальный ID для этой сессии генерации, чтобы создать отдельную папку
        String uniqueId = UUID.randomUUID().toString();
        // Создаем полный путь к папке, где будут сохранены файлы этой сессии
        Path outputDir = baseTempOutputDir.resolve(uniqueId); // baseTempOutputDir/uniqueId
        // Создаем эту папку и все необходимые родительские папки, если их нет
        Files.createDirectories(outputDir); // Files из java.nio.file

        System.out.println("HtmlGenerator: Generating HTML documentation in: " + outputDir.toAbsolutePath()); // Логируем путь

        // --- Шаг 1: Подготовка данных для шаблонов ---
        // Создаем контекст Thymeleaf. Это объект, через который мы передаем данные в шаблон.
        Context context = new Context();
        // Добавляем всю модель ProjectDoc в контекст под именем "project".
        // В шаблонах мы будем обращаться к ней как ${project}
        context.setVariable("project", projectDoc);


        // --- Шаг 2: Генерация главной страницы документации (index.html) ---
        // templateEngine.process() выполняет рендеринг шаблона.
        // Первый аргумент - это имя шаблона БЕЗ расширения (.html) и БЕЗ пути до папки "src/main/resources/templates/".
        // Thymeleaf автоматически ищет шаблоны в configured template locations (по умолчанию src/main/resources/templates/).
        // Мы указали "documentation/index", поэтому он ищет src/main/resources/templates/documentation/index.html
        String indexHtmlContent = templateEngine.process("documentation/index", context);
        // Определяем полный путь, куда сохранить сгенерированный index.html
        Path indexFile = outputDir.resolve("index.html"); // outputDir/index.html
        // Записываем сгенерированное содержимое в файл, используя кодировку UTF-8
        Files.write(indexFile, indexHtmlContent.getBytes(StandardCharsets.UTF_8)); // StandardCharsets из java.nio.charset

        System.out.println("HtmlGenerator: Generated index.html");


        // --- Шаг 3: Генерация страниц для каждого файла/класса (или другой структуры) ---
        // Здесь мы генерируем отдельную HTML страницу для каждого FileDoc, который содержит классы.
        // В шаблоне file.html мы будем отображать детали классов, методов и полей из этого файла.
        for (FileDoc fileDoc : projectDoc.getFiles()) {
             if (fileDoc.getClasses() != null && !fileDoc.getClasses().isEmpty()) { // Генерируем страницу только если в файле есть классы для документации

                 // Создаем отдельный контекст для страницы каждого файла (можно использовать тот же, но лучше для ясности)
                 Context fileContext = new Context();
                 fileContext.setVariable("file", fileDoc); // Передаем модель конкретного FileDoc под именем "file" (${file})
                 fileContext.setVariable("project", projectDoc); // Также полезно передать модель всего проекта (${project})

                 // Определяем имя файла для страницы. Заменяем точки в имени исходного файла на подчеркивания,
                 // чтобы избежать проблем с путями в URL при обслуживании статики.
                 // Добавляем расширение .html
                 String filePageName = fileDoc.getFileName().replace(".", "_") + ".html";
                 // Рендерим шаблон "documentation/file.html", передавая контекст с данными файла
                 String filePageContent = templateEngine.process("documentation/file", fileContext); // Thymeleaf ищет src/main/resources/templates/documentation/file.html

                 // Определяем полный путь, куда сохранить эту сгенерированную страницу
                 Path filePageFile = outputDir.resolve(filePageName); // outputDir/имя_файла.html
                 // Записываем сгенерированное содержимое в файл
                 Files.write(filePageFile, filePageContent.getBytes(StandardCharsets.UTF_8));
                 System.out.println("HtmlGenerator: Generated file page: " + filePageName);

                 // TODO: Если нужно, добавить генерацию отдельных страниц для каждого класса, метода и т.д.
             }
        }


        // --- Шаг 4: Копирование статических ресурсов (например, CSS) ---
        // Копируем CSS файл, который используется для оформления сгенерированной документации.
        // Предполагаем, что этот CSS файл находится в src/main/resources/static/doc-styles/style.css
        // В идеале, путь к статике для документации тоже должен быть настраиваемым.
        Path sourceDocStaticDir = Paths.get("src/main/resources/static/doc-styles");
        Path sourceCssFile = sourceDocStaticDir.resolve("style.css");

        // Определяем путь, куда скопировать CSS внутри выходной папки (outputDir/css/style.css)
        Path outputCssDir = outputDir.resolve("css"); // Папка css внутри выходной папки
        Path outputCssFile = outputCssDir.resolve("style.css"); // Путь к файлу style.css внутри css папки

        if (Files.exists(sourceCssFile)) { // Проверяем, что исходный CSS файл существует
             Files.createDirectories(outputCssDir); // Создаем папку css внутри выходной папки, если ее нет
             // Копируем файл, используя опцию REPLACE_EXISTING для перезаписи
             Files.copy(sourceCssFile, outputCssFile, StandardCopyOption.REPLACE_EXISTING);
             System.out.println("HtmlGenerator: Copied documentation styles.");
        } else {
             System.err.println("HtmlGenerator: Documentation CSS file not found at: " + sourceCssFile.toAbsolutePath()); // Логируем ошибку
        }


        // TODO: Добавить копирование других статических ресурсов (картинки, JS, шрифты), если они нужны для документации


        System.out.println("HtmlGenerator: HTML Generation process finished.");
        // Возвращаем полный путь к сгенерированной папке. Контроллер будет использовать его.
        return outputDir;
    }
}