package org.example.oakfusion;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Calculator {

    private static final String DEFAULT_DELIMITER = "[,\n]";
    private static final String DELIMITER_GROUP = "delimiter";
    private static final String NUMBERS_GROUP = "numbers";
    private static final Pattern SINGLE_CHARACTER_DELIMITER_PATTERN = Pattern.compile("^//(?<" + DELIMITER_GROUP + ">.)\n(?<" + NUMBERS_GROUP + ">.+)$");
    private static final Pattern MULTIPLE_MULTI_CHARACTER_DELIMITER_PATTERN = Pattern.compile("^//(?<" + DELIMITER_GROUP + ">(?:\\[.+\\])+)\n(?<" + NUMBERS_GROUP + ">.+)$");
    private static final int NUMBER_LIMIT = 1001;

    public static double add(String numbers) {
        ParseResult parseStrategyResult = parse(numbers);

        List<Map.Entry<String, Double>> numberEntries = parseNumbers(parseStrategyResult.numbers, parseStrategyResult.delimiters);

        assertPositiveNumbers(numberEntries);

        List<Double> doubles = numberEntries.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        doubles = skipNumbersGreaterThanLimit(doubles);

        return sumNumbers(doubles);
    }

    private static List<Map.Entry<String, Double>> parseNumbers(String numbers, String separator) {
        return Arrays.stream(numbers.split(separator))
                .map(Calculator::parseNumber)
                .collect(Collectors.toList());
    }

    private static Map.Entry<String, Double> parseNumber(String number) {
        double value;

        if (number.isEmpty()) {
            value = 0;
        } else {
            value = Double.parseDouble(number);
        }

        return new AbstractMap.SimpleEntry<>(number, value);
    }

    private static void assertPositiveNumbers(Collection<Map.Entry<String, Double>> numberEntries) {
        List<String> negativeValues = numberEntries
                .stream()
                .filter(entry -> entry.getValue() < 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!negativeValues.isEmpty()) {
            throw new NegativesNotAllowedException(negativeValues);
        }
    }

    private static List<Double> skipNumbersGreaterThanLimit(List<Double> doubles) {
        return doubles
                .stream()
                .filter(number -> number < NUMBER_LIMIT)
                .collect(Collectors.toList());
    }

    private static double sumNumbers(Collection<Double> numberEntries) {
        return numberEntries
                .stream()
                .reduce((sum, item) -> sum += item)
                .orElse(0.0);
    }

    private static ParseResult parse(String numbers) {
        Matcher multipleSingleCharacterCustomDelimiterPattern = MULTIPLE_MULTI_CHARACTER_DELIMITER_PATTERN.matcher(numbers);

        if (multipleSingleCharacterCustomDelimiterPattern.find()) {
            String group = multipleSingleCharacterCustomDelimiterPattern.group(DELIMITER_GROUP);
            String joinedDelimiters = String.join("", group.substring(1, group.length() - 1).split("\\]\\["));
            String delimiters = "[" + Pattern.quote(joinedDelimiters) + "]";

            return new ParseResult(
                    delimiters,
                    multipleSingleCharacterCustomDelimiterPattern.group(NUMBERS_GROUP));
        }

        Matcher singleCharacterCustomDelimiterMatcher = SINGLE_CHARACTER_DELIMITER_PATTERN.matcher(numbers);
        if (singleCharacterCustomDelimiterMatcher.find()) {
            return new ParseResult(
                    Pattern.quote(singleCharacterCustomDelimiterMatcher.group(DELIMITER_GROUP)),
                    singleCharacterCustomDelimiterMatcher.group(NUMBERS_GROUP));
        }

        return new ParseResult(DEFAULT_DELIMITER, numbers);
    }

    private static class ParseResult {
        final String delimiters;
        final String numbers;

        ParseResult(String delimiters, String numbers) {
            this.delimiters = delimiters;
            this.numbers = numbers;
        }
    }

    static class NegativesNotAllowedException extends RuntimeException {

        private static final String MESSAGE_PATTERN = "Negatives not allowed: ";
        private static final String NUMBER_DELIMITER = ", ";

        public NegativesNotAllowedException(Collection<String> negativeNumbers) {
            super(MESSAGE_PATTERN + String.join(NUMBER_DELIMITER, negativeNumbers));
        }
    }
}
