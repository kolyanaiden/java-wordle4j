package ru.yandex.practicum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Словарь слов для игры Wordle
 */
public class WordleDictionary {

    private final List<String> words;
    private final Set<String> wordSet;

    public WordleDictionary(List<String> words) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Список слов не может быть пустым");
        }
        this.words = new ArrayList<>(words);
        this.wordSet = new HashSet<>(words);
    }

    /**
     * Получить все слова словаря
     */
    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    /**
     * Проверить, содержит ли словарь слово
     */
    public boolean contains(String word) {
        return wordSet.contains(word);
    }

    /**
     * Получить случайное слово из словаря
     */
    public String getRandomWord() {
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    /**
     * Получить слова, соответствующие фильтрам
     * @param includeLetters буквы, которые должны быть в слове
     * @param excludeLetters буквы, которых не должно быть в слове
     * @param positionInfo информация о позициях букв (Map<позиция, буква>)
     */
    public List<String> getFilteredWords(Set<Character> includeLetters,
                                         Set<Character> excludeLetters,
                                         Map<Integer, Character> positionInfo) {

        return words.stream()
                .filter(word -> matchesFilters(word, includeLetters, excludeLetters, positionInfo))
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, соответствует ли слово фильтрам
     */
    private boolean matchesFilters(String word,
                                   Set<Character> includeLetters,
                                   Set<Character> excludeLetters,
                                   Map<Integer, Character> positionInfo) {

        // Проверка исключаемых букв
        for (char c : excludeLetters) {
            if (word.indexOf(c) != -1) {
                return false;
            }
        }

        // Проверка обязательных букв
        for (char c : includeLetters) {
            if (word.indexOf(c) == -1) {
                return false;
            }
        }

        // Проверка позиций букв
        for (Map.Entry<Integer, Character> entry : positionInfo.entrySet()) {
            int position = entry.getKey();
            char expectedChar = entry.getValue();

            if (position < 0 || position >= word.length()) {
                continue;
            }

            if (word.charAt(position) != expectedChar) {
                return false;
            }
        }

        return true;
    }

    public static String compareWords(String guess, String answer) {
        if (guess.length() != answer.length()) {
            throw new IllegalArgumentException("Слова должны быть одинаковой длины");
        }

        // Нормализуем оба слова
        guess = WordleDictionaryLoader.normalizeWord(guess);
        answer = WordleDictionaryLoader.normalizeWord(answer);

        int length = guess.length();
        char[] result = new char[length];

        // Массивы для отслеживания использованных позиций
        boolean[] guessUsed = new boolean[length];
        boolean[] answerUsed = new boolean[length];

        // Шаг 1: Ищем точные совпадения (правильная буква на правильном месте)
        for (int i = 0; i < length; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                guessUsed[i] = true;
                answerUsed[i] = true;
            }
        }

        // Шаг 2: Ищем буквы на неправильных позициях
        for (int i = 0; i < length; i++) {
            if (result[i] == '+') {
                continue; // Уже обработано
            }

            char currentChar = guess.charAt(i);

            // Ищем эту букву в ответе на других позициях
            for (int j = 0; j < length; j++) {
                if (!answerUsed[j] && currentChar == answer.charAt(j)) {
                    result[i] = '^';
                    answerUsed[j] = true;
                    break;
                }
            }

            // Если не нашли совпадение
            if (result[i] == 0) {
                result[i] = '-';
            }
        }

        // Заполняем оставшиеся позиции
        for (int i = 0; i < length; i++) {
            if (result[i] == 0) {
                result[i] = '-';
            }
        }

        return new String(result);
    }
}