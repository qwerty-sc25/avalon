package qwerty.chaekit.service.ebook;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.ebook.shelf.repository.EbookShelfRepository;
import qwerty.chaekit.domain.group.activity.Activity;
import qwerty.chaekit.domain.group.activity.activitymember.ActivityMember;
import qwerty.chaekit.domain.group.activity.activitymember.ActivityMemberRepository;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.dto.ebook.shelf.ReadingProgressRequest;
import qwerty.chaekit.dto.ebook.shelf.ReadingProgressResponse;
import qwerty.chaekit.dto.page.PageResponse;
import qwerty.chaekit.global.enums.ErrorCode;
import qwerty.chaekit.global.exception.ForbiddenException;
import qwerty.chaekit.global.security.resolver.UserToken;
import qwerty.chaekit.mapper.ReadingProgressMapper;
import qwerty.chaekit.service.util.EntityFinder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadingProgressService {
    
    private final EbookShelfRepository ebookShelfRepository;
    private final ActivityMemberRepository activityMemberRepository;
    private final ReadingProgressMapper readingProgressMapper;
    private final EntityFinder entityFinder;


    @Transactional
    public void saveMyProgress(UserToken userToken, Long bookId, ReadingProgressRequest request) {
        UserProfile user = entityFinder.findUser(userToken.userId());
        Ebook ebook = entityFinder.findEbook(bookId);

        EbookShelfItem ebookShelfItem = ebookShelfRepository.findByUserAndEbook(user, ebook)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.EBOOK_NOT_REGISTERED));

        ebookShelfItem.saveProgress(request.cfi(), request.percentage());
    }

    public ReadingProgressResponse getMyProgress(UserToken userToken, Long bookId) {
        UserProfile user = entityFinder.findUser(userToken.userId());
        Ebook ebook = entityFinder.findEbook(bookId);

        EbookShelfItem ebookShelfItem = ebookShelfRepository.findByUserAndEbook(user,ebook)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.EBOOK_NOT_REGISTERED));

        return readingProgressMapper.toResponse(ebookShelfItem);
    }

    public PageResponse<ReadingProgressResponse> getProgressFromActivity(Long activityId, Pageable pageable) {
        Activity activity = entityFinder.findActivity(activityId);

        Page<ActivityMember> activityMembers = activityMemberRepository.findByActivity(activity, pageable);
        List<Long> userIdList = activityMembers
                .map(activityMember -> activityMember.getUser().getId())
                .stream().toList();
        List<EbookShelfItem> shelfItems = ebookShelfRepository.findByUserIdInAndEbook(userIdList, activity.getBook());
        Page<EbookShelfItem> page = new PageImpl<>(shelfItems, pageable, shelfItems.size());

        return PageResponse.of(page.map(readingProgressMapper::toResponse));
    }
}