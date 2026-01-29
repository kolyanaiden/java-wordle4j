package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

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

    public String getAnswer() {
        return answer;
    }

    public int getRemainingSteps() {
        return steps;
    }

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

    private void updateLetterInfo(String word, String comparison) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            char result = comparison.charAt(i);

            switch (result) {
                case '+':
                    // Буква на правильной позиции
                    knownLetters.add(letter);
                    correctPositions.put(i, letter);
                    break;
                case '^':
                    // Буква есть, но не на этой позиции
                    knownLetters.add(letter);
                    wrongPositions.computeIfAbsent(i, k -> new HashSet<>()).add(letter);
                    break;
                case '-':
                    // Буквы нет в слове
                    excludedLetters.add(letter);
                    break;
            }
        }
    }

    public String getHint() throws WordleException {
        if (steps <= 0) {
            throw new WordleException("Попытки закончились");
        }

        // Получаем все слова, соответствующие текущим условиям
        List<String> possibleWords = dictionary.getFilteredWords(knownLetters, excludedLetters, correctPositions);

        // Дополнительная фильтрация по неправильным позициям
        possibleWords = possibleWords.stream()
                .filter(word -> matchesWrongPositions(word))
                .collect(Collectors.toList());

        // Убираем слова, которые уже были предложены
        possibleWords.removeAll(hints);

        if (possibleWords.isEmpty()) {
            throw new WordleException("Нет подходящих слов для подсказки");
        }

        // Выбираем случайное слово
        Random random = new Random();
        String hint = possibleWords.get(random.nextInt(possibleWords.size()));
        hints.add(hint);

        log.println("Подсказка: " + hint);

        return hint;
    }

    private boolean matchesWrongPositions(String word) {
        for (Map.Entry<Integer, Set<Character>> entry : wrongPositions.entrySet()) {
            int position = entry.getKey();
            Set<Character> wrongLetters = entry.getValue();

            for (char letter : wrongLetters) {
                // Буква не должна быть на этой позиции
                if (word.charAt(position) == letter) {
                    return false;
                }

                // Буква должна быть в слове где-то еще
                boolean foundElsewhere = false;
                for (int i = 0; i < word.length(); i++) {
                    if (i != position && word.charAt(i) == letter) {
                        foundElsewhere = true;
                        break;
                    }
                }

                if (!foundElsewhere) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isWordGuessed() {
        if (userGuesses.isEmpty()) {
            return false;
        }
        return userGuesses.get(userGuesses.size() - 1).equals(answer);
    }

    public boolean isGameOver() {
        return steps <= 0 || isWordGuessed();
    }

    public List<String> getUserGuesses() {
        return Collections.unmodifiableList(userGuesses);
    }

    public List<String> getHints() {
        return Collections.unmodifiableList(hints);
    }
}

class WordleException extends Exception {
    public WordleException(String message) {
        super(message);
    }
}

class WordleRuntimeException extends RuntimeException {
    public WordleRuntimeException(String message) {
        super(message);
    }

    public WordleRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}