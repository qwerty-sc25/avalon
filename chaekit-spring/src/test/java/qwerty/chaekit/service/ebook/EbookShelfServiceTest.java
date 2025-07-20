package qwerty.chaekit.service.ebook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import qwerty.chaekit.service.util.FileService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EbookShelfServiceTest {

    @InjectMocks
    private EbookShelfService ebookShelfService;

    @Mock
    private EbookShelfRepository ebookShelfRepository;

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private EbookRepository ebookRepository;

    @Mock
    private FileService fileService;

    @Test
    @DisplayName("서재 등록 성공")
    void registerEbook_Success() {
        // given
        Long userId = 1L;
        Long ebookId = 1L;

        UserProfile user = UserProfile.builder()
                .id(userId)
                .build();

        Ebook ebook = Ebook.builder()
                .id(ebookId)
                .title("Test Book")
                .author("Test Author")
                .fileKey("test-file-key")
                .coverImageKey("test-cover-key")
                .build();

        EbookShelfItem shelfItem = EbookShelfItem.builder()
                .user(user)
                .ebook(ebook)
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ebookRepository.findById(ebookId)).thenReturn(Optional.of(ebook));
        when(ebookShelfRepository.existsByUserIdAndEbookId(userId, ebookId)).thenReturn(false);
        when(ebookShelfRepository.save(any())).thenReturn(shelfItem);
        when(fileService.getEbookDownloadUrl(any())).thenReturn("http://test.com/download");

        EbookRegisterResponse response = ebookShelfService.registerEbook(ebookId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.bookId()).isEqualTo(ebookId);
        assertThat(response.presignedDownloadURL()).isEqualTo("http://test.com/download");
        verify(ebookShelfRepository).save(any());
    }

    @Test
    @DisplayName("서재 등록 실패 - 이미 등록한 이북")
    void registerEbook_Failure_AlreadyRegistered() {
        // given
        Long userId = 1L;
        Long ebookId = 1L;

        UserProfile user = UserProfile.builder()
                .id(userId)
                .build();


        Ebook ebook = Ebook.builder()
                .id(ebookId)
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ebookRepository.findById(ebookId)).thenReturn(Optional.of(ebook));
        when(ebookShelfRepository.existsByUserIdAndEbookId(userId, ebookId)).thenReturn(true);

        // then
        assertThatThrownBy(() -> ebookShelfService.registerEbook(ebookId, userId))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EBOOK_ALREADY_REGISTERED.getCode());
    }

    @Test
    @DisplayName("내 서재 조회 성공")
    void getMyBooks_Success() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        UserProfile user = UserProfile.builder()
                .id(userId)
                .build();

        Ebook ebook = Ebook.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .coverImageKey("test-cover-key")
                .build();

        EbookShelfItem shelfItem = EbookShelfItem.builder()
                .user(user)
                .ebook(ebook)
                .build();

        // when
        when(ebookShelfRepository.findByUserIdWithEbook(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(shelfItem)));
        when(fileService.convertToPublicImageURL(any())).thenReturn("http://test.com/cover.jpg");

        PageResponse<EbookFetchResponse> response = ebookShelfService.getMyBooks(userId, pageable);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("Test Book");
        assertThat(response.content().get(0).author()).isEqualTo("Test Author");
        assertThat(response.content().get(0).bookCoverImageURL()).isEqualTo("http://test.com/cover.jpg");
    }
} 