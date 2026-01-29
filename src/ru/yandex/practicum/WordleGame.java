package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс, представляющий игру Wordle
 */
public class WordleGame {

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final List<String> userGuesses;
    private final List<String> hints;
    private final Set<Character> knownLetters;
    private final Set<Character> excludedLetters;
    private final Map<Integer, Character> correctPositions;
    private final Map<Integer, Set<Character>> wrongPositions;

    /**
     * Конструктор игры
     */
    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.userGuesses = new ArrayList<>();
        this.hints = new ArrayList<>();
        this.knownLetters = new HashSet<>();
        this.excludedLetters = new HashSet<>();
        this.correctPositions = new HashMap<>();
        this.wrongPositions = new HashMap<>();

        log.println("Игра начата. Загадано слово: " + answer);
    }

    /**
     * Получить загаданное слово
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Получить количество оставшихся попыток
     */
    public int getRemainingSteps() {
        return steps;
    }

    /**
     * Проверить слово пользователя
     */
    public String checkWord(String word) throws WordleException {
        // Нормализуем слово
        String normalizedWord = WordleDictionaryLoader.normalizeWord(word);

        // Проверяем длину
        if (normalizedWord.length() != 5) {
            throw new WordleException("Слово должно состоять из 5 букв");
        }

        // Проверяем наличие в словаре
        if (!dictionary.contains(normalizedWord)) {
            throw new WordleException("Слово не найдено в словаре");
        }

        // Уменьшаем количество попыток
        steps--;

        // Добавляем в историю
        userGuesses.add(normalizedWord);

        // Сравниваем слова
        String comparison = WordleDictionary.compareWords(normalizedWord, answer);

        // Обновляем информацию о буквах
        updateLetterInfo(normalizedWord, comparison);

        log.println("Попытка: " + normalizedWord + ", результат: " + comparison);

        return comparison;
    }

    /**
     * Обновить информацию о буквах на основе результата сравнения
     */
    private void updateLetterInfo(String word, String comparison) {
        // Создаем копию букв ответа для отслеживания использования
        Map<Character, Integer> availableLetters = new HashMap<>();
        for (char c : answer.toCharArray()) {
            availableLetters.put(c, availableLetters.getOrDefault(c, 0) + 1);
        }

        // Первый проход: точные совпадения
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            char result = comparison.charAt(i);

            if (result == '+') {
                knownLetters.add(letter);
                correctPositions.put(i, letter);
                availableLetters.put(letter, availableLetters.get(letter) - 1);
            }
        }

        // Второй проход: другие символы
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            char result = comparison.charAt(i);

            if (result == '+') {
                continue; // Уже обработали
            }

            if (result == '^') {
                knownLetters.add(letter);
                wrongPositions.computeIfAbsent(i, k -> new HashSet<>()).add(letter);

                // Уменьшаем количество доступных букв
                if (availableLetters.getOrDefault(letter, 0) > 0) {
                    availableLetters.put(letter, availableLetters.get(letter) - 1);
                }
            } else if (result == '-') {
                // Буква может отсутствовать полностью ИЛИ все экземпляры уже использованы
                if (!availableLetters.containsKey(letter) || availableLetters.get(letter) == 0) {
                    excludedLetters.add(letter);
                } else {
                    // Буква есть, но все экземпляры уже использованы для других позиций
                    knownLetters.add(letter);
                    wrongPositions.computeIfAbsent(i, k -> new HashSet<>()).add(letter);
                }
            }
        }
    }

    /**
     * Получить подсказку
     */
    public String getHint() throws WordleException {
        if (steps <= 0) {
            throw new WordleException("Попытки закончились");
        }

        // Простая подсказка - случайное слово из словаря
        String hint = dictionary.getRandomWord();
        while (hints.contains(hint)) {
            hint = dictionary.getRandomWord();
        }

        hints.add(hint);
        log.println("Подсказка: " + hint);

        return hint;
    }

    /**
     * Проверяет, что слово соответствует ограничениям по позициям
     */
    private boolean matchesWrongPositions(String word) {
        for (Map.Entry<Integer, Set<Character>> entry : wrongPositions.entrySet()) {
            int position = entry.getKey();
            Set<Character> wrongLetters = entry.getValue();

            for (char letter : wrongLetters) {
                // Буква не должна быть на этой позиции
                if (word.charAt(position) == letter) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Проверить, отгадано ли слово
     */
    public boolean isWordGuessed() {
        if (userGuesses.isEmpty()) {
            return false;
        }
        return userGuesses.getLast().equals(answer);
    }

    /**
     * Проверить, закончилась ли игра
     */
    public boolean isGameOver() {
        return steps <= 0 || isWordGuessed();
    }

}

