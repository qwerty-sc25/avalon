package qwerty.chaekit.service.ebook;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.ebook.shelf.repository.EbookShelfRepository;
import qwerty.chaekit.domain.ebook.repository.EbookRepository;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.domain.member.user.UserProfileRepository;
import qwerty.chaekit.dto.ebook.EbookFetchResponse;
import qwerty.chaekit.dto.ebook.shelf.EbookRegisterResponse;
import qwerty.chaekit.dto.page.PageResponse;
import qwerty.chaekit.global.enums.ErrorCode;
import qwerty.chaekit.global.exception.BadRequestException;
import qwerty.chaekit.global.exception.NotFoundException;
import qwerty.chaekit.service.util.FileService;

@Service
@Transactional
@RequiredArgsConstructor
public class EbookShelfService {
    private final EbookShelfRepository ebookShelfRepository;

    private final UserProfileRepository userRepository;
    private final EbookRepository ebookRepository;
    private final FileService fileService;

    @Transactional
    public EbookRegisterResponse registerEbook(Long ebookId, Long userId) {
        UserProfile buyer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Ebook ebook = ebookRepository.findById(ebookId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EBOOK_NOT_FOUND));

        if(ebookShelfRepository.existsByUserIdAndEbookId(userId, ebookId)) {
            throw new BadRequestException(ErrorCode.EBOOK_ALREADY_REGISTERED);
        }

        ebookShelfRepository.save(
                EbookShelfItem.builder()
                        .user(buyer)
                        .ebook(ebook)
                        .build()
        );

        return EbookRegisterResponse.of(
                userId,
                ebook,
                fileService.getEbookDownloadUrl(ebook.getFileKey())
        );
    }

    public PageResponse<EbookFetchResponse> getMyBooks(Long userId, Pageable pageable) {
        Page<EbookShelfItem> shelfItems = ebookShelfRepository.findByUserIdWithEbook(userId, pageable);
        return PageResponse.of(
                shelfItems.map(
                shelfItem -> EbookFetchResponse.of(
                        shelfItem.getEbook(),
                        fileService.convertToPublicImageURL(shelfItem.getEbook().getCoverImageKey()),
                        true
                )));
    }
}
