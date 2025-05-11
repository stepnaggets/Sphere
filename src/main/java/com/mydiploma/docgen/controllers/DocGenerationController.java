package com.mydiploma.docgen.controllers; // Убедитесь, что имя пакета совпадает

import com.mydiploma.docgen.model.source.SourceFile; // Импорт модели исходного файла
import com.mydiploma.docgen.services.DocumentationService; // Импорт сервиса генерации документации

import org.springframework.beans.factory.annotation.Autowired; // Для внедрения зависимостей
import org.springframework.http.HttpHeaders; // Для настройки HTTP заголовков
import org.springframework.http.HttpStatus; // Для статусов HTTP
import org.springframework.http.MediaType; // Для типов медиа (MIME types)
import org.springframework.http.ResponseEntity; // Для формирования ответов HTTP
import org.springframework.stereotype.Controller; // Аннотация @Controller для контроллера веб-запросов
import org.springframework.ui.Model; // Для передачи данных в шаблоны Thymeleaf
import org.springframework.web.bind.annotation.GetMapping; // Для обработки GET запросов
import org.springframework.web.bind.annotation.PathVariable; // Для извлечения переменных из URL
import org.springframework.web.bind.annotation.PostMapping; // Для обработки POST запросов
import org.springframework.web.bind.annotation.RequestParam; // Для извлечения параметров запроса (файлы, URL, формат)
import org.springframework.web.bind.annotation.ResponseBody; // Для отправки данных напрямую в теле ответа (для XML)
import org.springframework.web.multipart.MultipartFile; // Для обработки загруженных файлов
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Для передачи данных при редиректе

import java.io.IOException; // Для обработки ошибок ввода/вывода
import java.net.URI; // Для работы с URI (для редиректов)
import java.nio.file.Files; // Для работы с файлами
import java.nio.file.Path; // Для работы с путями к файлам/папкам
import java.nio.file.Paths; // Для создания объектов Path
import java.util.ArrayList; // Для ArrayList
import java.util.List; // Для List
import java.util.UUID; // Для генерации уникальных идентификаторов сессий
import java.util.HashMap; // Для HashMap


/**
 * Controller handling web requests related to documentation generation.
 * Provides endpoints for the main page, documentation generation,
 * viewing generated HTML, downloading XML, and viewing the guide.
 */
@Controller // Указываем Spring, что это контроллер
public class DocGenerationController {

    // Вспомогательный класс для хранения временных данных сессии (папка для HTML, возможно другие данные)
    // В реальном приложении нужно использовать более надежный способ управления сессиями и временными файлами.
    // Этот класс пока просто для примера хранения пути.
    private static class SessionData {
        Path htmlTempDir;

        public SessionData(Path htmlTempDir) {
            this.htmlTempDir = htmlTempDir;
        }

        public Path getHtmlTempDir() {
            return htmlTempDir;
        }
    }

    // Простая мапа для хранения данных сессий по их ID.
    // В реальном приложении это может быть хранилище в памяти или другом месте.
    // TODO: Добавить механизм очистки старых сессий и временных папок.
    private final java.util.Map<String, SessionData> sessions = new java.util.HashMap<>(); // <--- ПЕРЕМЕЩЕНО СЮДА


    private final DocumentationService documentationService; // Сервис для выполнения логики генерации
    // Определим базовую временную директорию, куда будут сохраняться сгенерированные HTML файлы.
    // В идеале этот путь должен читаться из application.properties и быть настроен в DocumentationService.
    // Здесь он используется для формирования URL для просмотра HTML.
    private final Path baseHtmlTempOutputDir = Paths.get("generated-docs-html"); // Должен совпадать с DocumentationService


    // Внедряем DocumentationService с помощью аннотации @Autowired
    @Autowired
    public DocGenerationController(DocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    /**
     * Handles requests for the main page with the documentation form.
     * Accessible via GET request to the root URL "/".
     * @param model Model for passing data to the Thymeleaf template.
     * @return The name of the Thymeleaf template ("index.html").
     */
    @GetMapping("/") // Обрабатываем GET запросы на корневой URL
    public String index(Model model) {
        // В будущем здесь можно добавить передачу поддерживаемых языков/форматов в модель
        // model.addAttribute("supportedLanguages", documentationService.getSupportedLanguages());
        // model.addAttribute("supportedFormats", documentationService.getSupportedOutputFormats());
        System.out.println("Controller: Serving index page.");
        return "index"; // Возвращаем имя шаблона Thymeleaf (index.html)
    }

    /**
     * Handles documentation generation requests.
     * Accepts file uploads or a GitHub URL and the desired output format.
     * Accessible via POST request to "/generate".
     * @param files Uploaded source code files.
     * @param githubUrl GitHub repository URL (optional).
     * @param outputFormat Desired output format ("html", "xml", "pdf").
     * @param redirectAttributes Used to add flash attributes for messages redirected to /result.
     * @return Redirects to a result page or directly serves XML/redirects to HTML view.
     * @throws IOException If an I/O error occurs during file processing.
     */
    @PostMapping("/generate") // Обрабатываем POST запросы на /generate
    public ResponseEntity<?> generateDocumentation( //<?> означает, что тип ответа может быть любым
            @RequestParam(value = "files", required = false) MultipartFile[] files, // Принимаем массив загруженных файлов (необязательно)
            @RequestParam(value = "githubUrl", required = false) String githubUrl, // Принимаем URL GitHub (необязательно)
            @RequestParam("outputFormat") String outputFormat, // Принимаем выбранный формат вывода (обязательно)
            RedirectAttributes redirectAttributes) throws IOException { // Для передачи сообщений после редиректа

        System.out.println("Controller: Received generation request.");
        System.out.println("  Output format: " + outputFormat);
        System.out.println("  Files received: " + (files != null ? files.length : 0));
        System.out.println("  GitHub URL: " + (githubUrl != null && !githubUrl.isEmpty() ? githubUrl : "none"));


        // --- Валидация входных данных ---
        if ((files == null || files.length == 0 || allFilesEmpty(files)) && (githubUrl == null || githubUrl.trim().isEmpty())) {
            System.out.println("Controller: No files or GitHub URL provided.");
            redirectAttributes.addFlashAttribute("errorMessage", "Пожалуйста, загрузите файлы или укажите URL репозитория GitHub.");
            // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/")).build(); // Редирект обратно на главную с сообщением
        }

        // TODO: Добавить более строгую валидацию URL GitHub


        // --- Подготовка SourceFile объектов ---
        List<SourceFile> sourceFiles = new ArrayList<>();
        if (files != null && files.length > 0 && !allFilesEmpty(files)) {
            System.out.println("Controller: Processing uploaded files.");
            for (MultipartFile file : files) {
                 if (!file.isEmpty()) {
                     try {
                         // Читаем содержимое файла
                         String content = new String(file.getBytes()); // В будущем учесть кодировку

                         // Создаем объект SourceFile
                         // filePath пока просто равно fileName для загруженных файлов, можно улучшить
                         SourceFile sourceFile = new SourceFile(file.getOriginalFilename(), file.getOriginalFilename(), content);
                         sourceFiles.add(sourceFile);
                         System.out.println("Controller: Added SourceFile: " + sourceFile.getFileName() + " (" + sourceFile.getLanguage() + ")");
                     } catch (IOException e) {
                         System.err.println("Controller: Error reading uploaded file " + file.getOriginalFilename() + ": " + e.getMessage());
                         // Логируем ошибку, но продолжаем обрабатывать другие файлы
                         redirectAttributes.addFlashAttribute("warningMessage", "Не удалось прочитать файл: " + file.getOriginalFilename());
                         // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                         // return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/?warningMessage=Не удалось прочитать файл: " + file.getOriginalFilename())).build();
                     }
                 }
            }
            if (sourceFiles.isEmpty()) {
                 System.out.println("Controller: All uploaded files were empty or unreadable.");
                 redirectAttributes.addFlashAttribute("errorMessage", "Загруженные файлы пусты или нечитаемы.");
                 // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                 return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/")).build();
            }

        } else if (githubUrl != null && !githubUrl.trim().isEmpty()) {
            System.out.println("Controller: Processing GitHub URL (NOT IMPLEMENTED).");
            // TODO: Реализовать логику скачивания файлов с GitHub
            // Для диплома, возможно, достаточно обработать случай загрузки файлов.
             redirectAttributes.addFlashAttribute("warningMessage", "Скачивание с GitHub еще не реализовано. Пожалуйста, загрузите файлы вручную.");
             // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
             return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/")).build();
        }


        // --- Вызов сервиса для генерации документации ---
        try {
            System.out.println("Controller: Calling DocumentationService...");
            Object generationResult = documentationService.generateDocumentation(sourceFiles, outputFormat);
            System.out.println("Controller: DocumentationService finished.");

            // --- Обработка результата генерации в зависимости от формата ---
            if (outputFormat.equalsIgnoreCase("xml")) {
                // Для XML - вернуть содержимое как строку в теле ответа
                if (generationResult instanceof String) {
                    String xmlContent = (String) generationResult;
                    System.out.println("Controller: Generated XML, returning as response body.");
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_XML);
                    // Добавляем заголовок Content-Disposition, чтобы браузер предложил скачать файл
                    headers.setContentDispositionFormData("attachment", "documentation.xml");
                    return ResponseEntity.ok().headers(headers).body(xmlContent);
                } else {
                     System.err.println("Controller: Unexpected result type for XML generation.");
                     redirectAttributes.addFlashAttribute("errorMessage", "Ошибка генерации XML: Неожиданный формат данных.");
                     // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                     return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();
                }

            } else if (outputFormat.equalsIgnoreCase("html")) {
                // Для HTML - редирект на страницу просмотра сгенерированной доки
                if (generationResult instanceof Path) {
                    // generationResult - это путь к корневой папке сгенерированной HTML документации
                    Path htmlOutputDir = (Path) generationResult;
                    System.out.println("Controller: Generated HTML at: " + htmlOutputDir.toAbsolutePath());

                    // Генерируем уникальный ID сессии для доступа к этой папке через контроллер
                    String sessionId = UUID.randomUUID().toString();
                    // Сохраняем путь к папке для этой сессии
                    sessions.put(sessionId, new SessionData(htmlOutputDir));
                    System.out.println("Controller: Created session " + sessionId + " for HTML docs.");

                    // Редирект на URL, который будет обрабатываться методом viewDocumentation
                    // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/view/docs/" + sessionId + "/index.html")).build();

                } else {
                     System.err.println("Controller: Unexpected result type for HTML generation.");
                     redirectAttributes.addFlashAttribute("errorMessage", "Ошибка генерации HTML: Неожиданный формат данных.");
                     // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                     return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();
                }

            } else if (outputFormat.equalsIgnoreCase("pdf")) {
                // Для PDF - вернуть содержимое как byte[]
                 if (generationResult instanceof byte[]) {
                     byte[] pdfContent = (byte[]) generationResult;
                     System.out.println("Controller: Generated PDF, returning as byte[].");
                     HttpHeaders headers = new HttpHeaders();
                     headers.setContentType(MediaType.APPLICATION_PDF);
                     // Добавляем заголовок Content-Disposition, чтобы браузер предложил скачать файл
                     headers.setContentDispositionFormData("attachment", "documentation.pdf");
                     headers.setContentLength(pdfContent.length); // Устанавливаем размер содержимого
                     return ResponseEntity.ok().headers(headers).body(pdfContent);
                 } else if (generationResult instanceof String && ((String) generationResult).startsWith("Error:")) {
                      // Если генератор PDF вернул сообщение об ошибке строкой
                      System.err.println("Controller: PDF Generator returned error message.");
                      redirectAttributes.addFlashAttribute("errorMessage", "Ошибка генерации PDF: " + generationResult);
                      // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                      return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();
                 }
                 else {
                     System.err.println("Controller: Unexpected result type for PDF generation.");
                     redirectAttributes.addFlashAttribute("errorMessage", "Ошибка генерации PDF: Неожиданный формат данных.");
                     // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                     return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();
                }

            } else {
                // Неподдерживаемый формат (хотя валидация должна быть раньше)
                System.err.println("Controller: Unsupported output format requested: " + outputFormat);
                redirectAttributes.addFlashAttribute("errorMessage", "Неподдерживаемый формат вывода: " + outputFormat);
                 // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/")).build();
            }

        } catch (IllegalArgumentException e) {
             // Ошибка, если DocumentationService не нашел парсер или генератор
             System.err.println("Controller: Configuration Error: " + e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", "Ошибка конфигурации: " + e.getMessage());
             // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
             return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/")).build();

        } catch (IOException e) {
            // Ошибка ввода/вывода при генерации (например, запись файла)
            System.err.println("Controller: I/O Error during documentation generation: " + e.getMessage());
            e.printStackTrace(); // Логируем полный стек-трейс для отладки
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка ввода/вывода при генерации: " + e.getMessage());
             // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();

        } catch (Exception e) {
            // Любая другая неожиданная ошибка в процессе парсинга/генерации
            System.err.println("Controller: Unexpected error during documentation generation: " + e.getMessage());
            e.printStackTrace(); // Логируем полный стек-трейс для отладки
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла внутренняя ошибка при генерации: " + e.getMessage());
             // ИЗМЕНЕНИЕ: Используем status().location() вместо seeOther()
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/result")).build();
        }
    }

    /**
     * Handles requests to view generated HTML documentation files.
     * Accessible via GET request to "/view/docs/{sessionId}/{fileName}".
     * {sessionId} is a unique identifier for the generated documentation batch.
     * {fileName} is the name of the HTML file within the documentation batch (e.g., "index.html", "MyClass_java.html").
     * @param sessionId Unique session ID for the documentation batch.
     * @param fileName The name of the HTML file to view.
     * @return The content of the requested HTML file, or a 404 error if not found.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    // Обрабатываем запросы для просмотра сгенерированных HTML файлов
    @GetMapping("/view/docs/{sessionId}/{fileName}")
    @ResponseBody // Указываем Spring, что результат метода должен быть записан прямо в тело ответа
    public ResponseEntity<byte[]> viewDocumentation(
            @PathVariable String sessionId, // Извлекаем sessionId из URL
            @PathVariable String fileName) throws IOException { // Извлекаем fileName из URL

        System.out.println("Controller: Received request to view doc file: " + fileName + " for session: " + sessionId);

        // Находим данные сессии по ID
        SessionData sessionData = sessions.get(sessionId);
        if (sessionData == null) {
            System.err.println("Controller: Session not found: " + sessionId);
            return ResponseEntity.notFound().build(); // Если сессия не найдена, вернуть 404
        }

        // Определяем полный путь к запрашиваемому файлу внутри папки этой сессии
        Path requestedFile = sessionData.getHtmlTempDir().resolve(fileName);
        System.out.println("Controller: Attempting to serve file: " + requestedFile.toAbsolutePath());


        // Проверяем, что файл существует И что путь к нему находится внутри папки сессии
        // Это важная мера безопасности против "Path Traversal" атак
        if (!Files.exists(requestedFile) || !requestedFile.normalize().startsWith(sessionData.getHtmlTempDir().normalize())) {
            System.err.println("Controller: File not found or path traversal attempt: " + requestedFile.toAbsolutePath());
            return ResponseEntity.notFound().build(); // Если файл не существует или путь некорректен, вернуть 404
        }

        // Определяем MIME тип файла на основе расширения (очень упрощенно)
        String mimeType = "text/html"; // По умолчанию HTML
        if (fileName.endsWith(".css")) {
            mimeType = "text/css";
        }
        // TODO: Добавить обработку других MIME типов (js, png, jpg и т.п.) если будут статические ресурсы кроме CSS

        // Читаем содержимое файла
        byte[] fileContent = Files.readAllBytes(requestedFile);

        // Формируем ответ с содержимым файла
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType)); // Устанавливаем MIME тип
        headers.setContentLength(fileContent.length); // Устанавливаем размер содержимого

        System.out.println("Controller: Successfully serving file: " + fileName);
        return ResponseEntity.ok().headers(headers).body(fileContent); // Возвращаем содержимое файла
    }


    /**
     * Handles requests for the guide page.
     * Accessible via GET request to "/guide".
     * @return The name of the Thymeleaf template ("guide.html").
     */
    @GetMapping("/guide") // Обрабатываем GET запросы на /guide
    public String guide() {
        System.out.println("Controller: Serving guide page.");
        return "guide"; // Возвращаем имя шаблона Thymeleaf (guide.html)
    }

    /**
     * Handles requests for the result page (for displaying messages).
     * Accessible via GET request to "/result".
     * @param model Model for passing data (flash attributes) to the Thymeleaf template.
     * @return The name of the Thymeleaf template ("result.html").
     */
    @GetMapping("/result") // Обрабатываем GET запросы на /result
    public String result(Model model) {
         // Flash attributes (errorMessage, warningMessage, infoMessage)
         // автоматически добавляются в Model при редиректе на этот URL
        System.out.println("Controller: Serving result page.");
        return "result"; // Возвращаем имя шаблона Thymeleaf (result.html)
    }


    // --- Вспомогательные методы ---

    // Проверяет, являются ли все файлы в массиве пустыми
    private boolean allFilesEmpty(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return true;
        }
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                return false; // Найден непустой файл
            }
        }
        return true; // Все файлы пустые
    }


    // TODO: Добавить более robustное управление временными папками (очистка по таймеру и т.п.)
    // TODO: Возможно, добавить логику для загрузки ZIP-архива и его распаковки.
    // TODO: Реализовать скачивание файлов с GitHub.
    // TODO: Добавить более детальное логирование процесса парсинга/генерации для пользователя.
}