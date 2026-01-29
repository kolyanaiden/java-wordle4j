package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

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

    public static String normalizeWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .replace('Ё', 'е');
    }
}