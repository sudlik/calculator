package org.example.oakfusion;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Calculator {

    private static final String DEFAULT_DELIMITER = "[,\n]";
    private static final String DELIMITER_GROUP = "delimiter";
    private static final String NUMBERS_GROUP = "numbers";
    private static final Pattern SINGLE_CHARACTER_DELIMITER_PATTERN = Pattern.compile("^//(?<" + DELIMITER_GROUP + ">.)\n(?<" + NUMBERS_GROUP + ">.*)$");
    private static final Pattern MULTIPLE_MULTI_CHARACTER_DELIMITER_PATTERN = Pattern.compile("^//(?<" + DELIMITER_GROUP + ">(?:\\[.+\\])+)\n(?<" + NUMBERS_GROUP + ">.*)$");
    private static final int NUMBER_LIMIT = 1001;

    public static int Add(String numbers) {
        ParseResult parseStrategyResult = parse(numbers);

        Collection<Integer> integers = parseNumbers(parseStrategyResult.numbers, parseStrategyResult.delimiters);

        assertPositiveNumbers(integers);

        integers = skipNumbersGreaterThanLimit(integers);

        return sumNumbers(integers);
    }

    private static List<Integer> parseNumbers(String numbers, String delimiters) {
        return Arrays.stream(numbers.split(delimiters))
                .map(number -> number.isEmpty() ? 0 : Integer.parseInt(number))
                .collect(Collectors.toList());
    }

    private static void assertPositiveNumbers(Collection<Integer> numbers) {
        Collection<Integer> negativeValues = numbers
                .stream()
                .filter(number -> number < 0)
                .collect(Collectors.toList());

        if (!negativeValues.isEmpty()) {
            throw new NegativesNotAllowedException(negativeValues);
        }
    }

    private static Collection<Integer> skipNumbersGreaterThanLimit(Collection<Integer> integers) {
        return integers
                .stream()
                .filter(number -> number < NUMBER_LIMIT)
                .collect(Collectors.toList());
    }

    private static int sumNumbers(Collection<Integer> numberEntries) {
        return numberEntries
                .stream()
                .reduce((sum, item) -> sum += item)
                .orElse(0);
    }

    private static ParseResult parse(String numbers) {
        Matcher multipleSingleCharacterCustomDelimiterPattern = MULTIPLE_MULTI_CHARACTER_DELIMITER_PATTERN.matcher(numbers);
        if (multipleSingleCharacterCustomDelimiterPattern.find()) {
            return parseMultipleSingleCharacterCustomDelimiter(multipleSingleCharacterCustomDelimiterPattern);
        }

        Matcher singleCharacterCustomDelimiterMatcher = SINGLE_CHARACTER_DELIMITER_PATTERN.matcher(numbers);
        if (singleCharacterCustomDelimiterMatcher.find()) {
            return parseSingleCharacterDelimiter(singleCharacterCustomDelimiterMatcher);
        }

        return parseDefault(numbers);
    }

    private static ParseResult parseMultipleSingleCharacterCustomDelimiter(Matcher matcher) {
        String group = matcher.group(DELIMITER_GROUP);
        String joinedDelimiters = String.join("", group.substring(1, group.length() - 1).split("\\]\\["));
        String delimiters = "[" + Pattern.quote(joinedDelimiters) + "]";

        return new ParseResult(delimiters, matcher.group(NUMBERS_GROUP));
    }

    private static ParseResult parseSingleCharacterDelimiter(Matcher matcher) {
        return new ParseResult(Pattern.quote(matcher.group(DELIMITER_GROUP)), matcher.group(NUMBERS_GROUP));
    }

    private static ParseResult parseDefault(String numbers) {
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

        public NegativesNotAllowedException(Collection<Integer> negativeNumbers) {
            super(MESSAGE_PATTERN + joinIntegers(negativeNumbers));
        }

        private static String joinIntegers(Collection<Integer> negativeNumbers) {
            return negativeNumbers.stream().map(Objects::toString).collect(Collectors.joining(NUMBER_DELIMITER));
        }
    }
}
