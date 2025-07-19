package qwerty.chaekit.dto.ebook.shelf;

public record ReadingProgressRequest(
    String cfi,
    Long percentage
) {}