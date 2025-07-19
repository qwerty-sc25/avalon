package qwerty.chaekit.service.ebook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.member.user.UserProfile;

import qwerty.chaekit.dto.ebook.upload.EbookDownloadResponse;
import qwerty.chaekit.global.security.resolver.UserToken;
import qwerty.chaekit.service.util.EntityFinder;
import qwerty.chaekit.service.util.FileService;


import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EbookFileServiceTest {
    @InjectMocks
    private EbookFileService ebookFileService;

    @Mock
    private EbookPolicy ebookPolicy;
    @Mock
    private FileService fileService;
    @Mock
    private EntityFinder entityFinder;

    @Test
    @DisplayName("이북 다운로드 URL 조회 성공 - 사용자")
    void getPresignedEbookUrlForUser_Success() {
        // given
        Long userId = 1L;
        Long ebookId = 1L;
        UserToken userToken = UserToken.of(userId, userId, "user@test.com");
        UserProfile user = UserProfile.builder().id(userId).build();
        Ebook ebook = Ebook.builder()
                .id(ebookId)
                .fileKey("test-file-key")
                .build();

        when(entityFinder.findUser(userId)).thenReturn(user);
        when(entityFinder.findEbook(ebookId)).thenReturn(ebook);
        when(fileService.getEbookDownloadUrl("test-file-key")).thenReturn("http://test.com/download");

        // when
        EbookDownloadResponse response = ebookFileService.getPresignedEbookUrlForUser(userToken, ebookId);

        // then
        assertThat(response.presignedUrl()).isEqualTo("http://test.com/download");
        verify(ebookPolicy).assertEBookRegistered(user, ebook);
    }
} 