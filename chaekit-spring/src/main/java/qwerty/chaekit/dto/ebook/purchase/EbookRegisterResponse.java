package qwerty.chaekit.dto.ebook.purchase;

import lombok.Builder;
import qwerty.chaekit.domain.ebook.Ebook;

@Builder
public record EbookRegisterResponse(
        Long userId,
        Long bookId,
        String title,
        String author,
        String presignedDownloadURL
) {
    /**
     * Creates a response representing an eBook that has been added to a user's shelf.
     */
    public static EbookRegisterResponse of(
            Long userId,
            Ebook ebook,
            String presignedDownloadURL
    ) {
        return EbookRegisterResponse.builder()
                .userId(userId)
                .bookId(ebook.getId())
                .title(ebook.getTitle())
                .author(ebook.getAuthor())
                .presignedDownloadURL(presignedDownloadURL)
                .build();
    }
}

