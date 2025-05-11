# DocGenerator 
**Автоматическая генерация документации из исходного кода**

[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-green)](https://spring.io/)

## 🚀 Возможности
- Генерация документации в PDF/XML/HTML
- Поддержка загрузки файлов и GitHub-репозиториев
- Шаблоны для Python
- Экспорт результатов

## 🛠️ Установка
1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/yourusername/doc-generator.git
   ```
2. Соберите проект:
   ```bash
   mvn clean install
   ```
3. Запустите:
   ```bash
   mvn spring-boot:run
   ```

## 📖 Использование
1. **Загрузка файлов**:
   - Перейдите на `http://localhost:8080`
   - Выбете файлы и формат

2. **GitHub-репозиторий**:
   ```bash
   curl -X POST -H "Content-Type: application/json" \
   -d '{"repoUrl":"https://github.com/example/repo", "format":"PDF"}' \
   http://localhost:8080/api/docs/github
   ```

## 📄 Примеры
Сгенерированный PDF:  
![Пример PDF](docs/sample-pdf.png)

## 📜 Лицензия
MIT License. Подробнее в [LICENSE](LICENSE).