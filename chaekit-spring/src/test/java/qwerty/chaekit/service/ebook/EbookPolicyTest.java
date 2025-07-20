package qwerty.chaekit.service.ebook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.repository.EbookShelfRepository;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.global.enums.ErrorCode;
import qwerty.chaekit.global.exception.ForbiddenException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EbookPolicyTest {

    @InjectMocks
    private EbookPolicy ebookPolicy;

    @Mock
    private EbookShelfRepository ebookShelfRepository;

    @Test
    @DisplayName("이북 등록 확인 성공")
    void assertEBookRegistered_Success() {
        // given
        Long userId = 1L;
        Long ebookId = 1L;

        UserProfile user = mock(UserProfile.class);
        when(user.getId()).thenReturn(userId);

        Ebook ebook = mock(Ebook.class);
        when(ebook.getId()).thenReturn(ebookId);

        // when
        when(ebookShelfRepository.existsByUserIdAndEbookId(userId, ebookId)).thenReturn(true);

        // then
        ebookPolicy.assertEBookRegistered(user, ebook);
    }

    @Test
    @DisplayName("이북 등록 확인 실패 - 등록하지 않은 이북")
    void assertEBookRegistered_Failure_NotRegistered() {
        // given
        Long userId = 1L;
        Long ebookId = 1L;

        UserProfile user = mock(UserProfile.class);
        when(user.getId()).thenReturn(userId);

        Ebook ebook = mock(Ebook.class);
        when(ebook.getId()).thenReturn(ebookId);

        // when
        when(ebookShelfRepository.existsByUserIdAndEbookId(userId, ebookId)).thenReturn(false);

        // then
        assertThatThrownBy(() -> ebookPolicy.assertEBookRegistered(user, ebook))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EBOOK_NOT_REGISTERED.getCode());
    }
} 