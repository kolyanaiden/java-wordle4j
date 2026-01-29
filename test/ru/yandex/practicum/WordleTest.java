package ru.yandex.practicum;

import org.junit.jupiter.api.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static WordleDictionary testDictionary;
    private static PrintWriter testLog;

    @BeforeAll
    static void setUpAll() {
        // Создаем тестовый словарь
        List<String> testWords = Arrays.asList(
                "стол", "стул", "ручка", "книга", "окно",
                "ручка", "лист", "река", "лес", "дом",
                "вода", "земля", "воздух", "огонь", "метал"
        );

        // Добавляем 5-буквенные слова
        testWords = testWords.stream()
                .filter(word -> word.length() == 5)
                .toList();

        testDictionary = new WordleDictionary(testWords);

        // Создаем тестовый лог
        testLog = new PrintWriter(System.out);
    }

    @Test
    void testDictionaryNormalization() {
        String normalized = WordleDictionaryLoader.normalizeWord("СловО");
        assertEquals("слово", normalized);

        normalized = WordleDictionaryLoader.normalizeWord("ЁЛКА");
        assertEquals("елка", normalized);
    }

    @Test
    void testDictionaryContains() {
        assertTrue(testDictionary.contains("ручка"));
        assertFalse(testDictionary.contains("компьютер"));
    }

    @Test
    void testWordComparison() {
        // Точное совпадение
        assertEquals("+++++", WordleDictionary.compareWords("ручка", "ручка"));

        // Буквы на других позициях
        assertEquals("^---^", WordleDictionary.compareWords("круча", "ручка"));

        // Смешанный случай
        assertEquals("+^---", WordleDictionary.compareWords("речка", "ручка"));

        // Буквы отсутствуют
        assertEquals("-----", WordleDictionary.compareWords("домен", "ручка"));
    }

    @Test
    void testGameInitialization() {
        WordleGame game = new WordleGame(testDictionary, testLog);

        assertNotNull(game.getAnswer());
        assertEquals(6, game.getRemainingSteps());
        assertFalse(game.isWordGuessed());
        assertFalse(game.isGameOver());
    }

    @Test
    void testWordCheck() throws WordleException {
        WordleGame game = new WordleGame(testDictionary, testLog);
        String answer = game.getAnswer();

        // Проверяем правильное слово
        String result = game.checkWord(answer);
        assertEquals("+++++", result);
        assertTrue(game.isWordGuessed());
        assertTrue(game.isGameOver());
    }

    @Test
    void testInvalidWord() {
        WordleGame game = new WordleGame(testDictionary, testLog);

        // Слово не из 5 букв
        assertThrows(WordleException.class, () -> game.checkWord("дом"));

        // Слово не из словаря
        assertThrows(WordleException.class, () -> game.checkWord("абвгд"));
    }

    @Test
    void testHintSystem() throws WordleException {
        WordleGame game = new WordleGame(testDictionary, testLog);

        // Получаем подсказку
        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(testDictionary.contains(hint));

        // Подсказка не должна повторяться
        String hint2 = game.getHint();
        assertNotEquals(hint, hint2);
    }

    @Test
    void testGameStepsDecrease() throws WordleException {
        WordleGame game = new WordleGame(testDictionary, testLog);

        int initialSteps = game.getRemainingSteps();
        game.checkWord("ручка");

        assertEquals(initialSteps - 1, game.getRemainingSteps());
    }

    @AfterAll
    static void tearDownAll() {
        if (testLog != null) {
            testLog.close();
        }
    }
}