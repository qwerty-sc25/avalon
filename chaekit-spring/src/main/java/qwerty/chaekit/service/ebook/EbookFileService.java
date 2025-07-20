package qwerty.chaekit.service.ebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.dto.ebook.upload.EbookDownloadResponse;
import qwerty.chaekit.global.security.resolver.UserToken;
import qwerty.chaekit.service.util.EntityFinder;
import qwerty.chaekit.service.util.FileService;

@Service
@Transactional
@RequiredArgsConstructor
public class EbookFileService {
    private final EbookPolicy ebookPolicy;
    private final FileService fileService;
    private final EntityFinder entityFinder;

    @Transactional
    public EbookDownloadResponse getPresignedEbookUrlForUser(UserToken userToken, Long ebookId) {
        UserProfile user = entityFinder.findUser(userToken.userId());
        Ebook ebook = entityFinder.findEbook(ebookId);

        ebookPolicy.assertEBookRegistered(user, ebook);
        
        String ebookFileKey = ebook.getFileKey();
        String downloadUrl = fileService.getEbookDownloadUrl(ebookFileKey);

        return EbookDownloadResponse.of(downloadUrl);
    }
}

