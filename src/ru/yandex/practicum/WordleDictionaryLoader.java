package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Загрузчик словарей из файлов
 */
public class WordleDictionaryLoader {

    /**
     * Загружает словарь из файла
     * @param filename имя файла со словарем
     * @return загруженный словарь
     * @throws IOException если произошла ошибка чтения файла
     */
    public WordleDictionary loadDictionary(String filename) throws IOException {
        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Пропускаем пустые строки
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Нормализуем слово (приводим к нижнему регистру, заменяем ё на е)
                String normalizedWord = normalizeWord(line.trim());

                // Добавляем только слова из 5 букв
                if (normalizedWord.length() == 5) {
                    words.add(normalizedWord);
                }
            }
        }

        if (words.isEmpty()) {
            throw new IllegalArgumentException("Словарь пуст или не содержит слов из 5 букв");
        }

        return new WordleDictionary(words);
    }

    /**
     * Нормализует слово для игры
     * @param word исходное слово
     * @return нормализованное слово
     */
    public static String normalizeWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .replace('Ё', 'е');
    }
}