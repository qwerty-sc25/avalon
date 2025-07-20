package qwerty.chaekit.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.dto.ebook.shelf.ReadingProgressResponse;
import qwerty.chaekit.service.util.FileService;

@Component
@RequiredArgsConstructor
public class ReadingProgressMapper {
    private final FileService fileService;

    public String convertToPublicImageURL(String imageKey) {
        return fileService.convertToPublicImageURL(imageKey);
    }

    public ReadingProgressResponse toResponse(EbookShelfItem ebookShelfItem) {
        return ReadingProgressResponse.builder()
                .bookId(ebookShelfItem.getEbook().getId())
                .userId(ebookShelfItem.getUser().getId())
                .userNickname(ebookShelfItem.getUser().getNickname())
                .userProfileImageURL(convertToPublicImageURL(ebookShelfItem.getUser().getProfileImageKey()))
                .cfi(ebookShelfItem.getCfi())
                .percentage(ebookShelfItem.getPercentage())
                .build();
    }
}
