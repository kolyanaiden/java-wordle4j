package ru.yandex.practicum;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        PrintWriter logWriter = null;

        try {
            // Создаем лог-файл
            logWriter = new PrintWriter(new FileWriter("wordle.log", true), true);

            // Загружаем словарь
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

            logWriter.println("Словарь загружен, слов: " + dictionary.getWords().size());

            // Создаем игру
            WordleGame game = new WordleGame(dictionary, logWriter);

            // Создаем сканер для ввода
            Scanner scanner = new Scanner(System.in);

            logWriter.println("Игра начата");

            // Игровой цикл
            while (!game.isGameOver()) {
                System.out.println("\nПопыток осталось: " + game.getRemainingSteps());
                System.out.print("Введите слово из 5 букв (или нажмите Enter для подсказки): ");

                String input = scanner.nextLine().trim();

                try {
                    if (input.isEmpty()) {
                        // Запрос подсказки
                        String hint = game.getHint();
                        System.out.println("Подсказка: " + hint);
                    } else {
                        // Проверка слова
                        String result = game.checkWord(input);
                        System.out.println(input);
                        System.out.println(result);

                        if (game.isWordGuessed()) {
                            System.out.println("\n🎉 Поздравляем! Вы угадали слово!");
                            break;
                        }
                    }
                } catch (WordleException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }

            // Если закончились попытки
            if (!game.isWordGuessed() && game.getRemainingSteps() <= 0) {
                System.out.println("\n😔 Попытки закончились. Загаданное слово: " + game.getAnswer());
            }

            scanner.close();

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлами: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка загрузки словаря: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();

            // Записываем в лог, если возможно
            if (logWriter != null) {
                logWriter.println("Критическая ошибка: " + e.getMessage());
                e.printStackTrace(logWriter);
            }
        } finally {
            // Закрываем лог-файл
            if (logWriter != null) {
                logWriter.println("Игра завершена");
                logWriter.close();
            }
        }
    }
}