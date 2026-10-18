package com.austinbordeaux.irrigation.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Report {

    public static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final String title;
    private final LocalDate from;
    private final LocalDate to;
    private final LocalDateTime generatedAt;

    protected Report(String title, LocalDate from, LocalDate to) {
        this.title = title;
        this.from = from;
        this.to = to;
        this.generatedAt = LocalDateTime.now();
    }

    public abstract List<String> headers();

    public abstract List<List<String>> rows();

    public final String toCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(cell(title)).append("\n");
        csv.append(cell("Period: " + from + " to " + to)).append("\n");
        csv.append(cell("Generated: " + generatedAt.format(GENERATED_FORMAT))).append("\n\n");
        csv.append(line(headers())).append("\n");
        for (List<String> row : rows()) {
            csv.append(line(row)).append("\n");
        }
        return csv.toString();
    }

    private static String line(List<String> cells) {
        return cells.stream().map(Report::cell).collect(Collectors.joining(","));
    }

    static String cell(String value) {
        if (value == null) {
            return "";
        }
        String text = value;
        boolean number = text.matches("-?[\\d.,]+");
        if (!number && text.matches("^[=+\\-@\\t\\r].*")) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}