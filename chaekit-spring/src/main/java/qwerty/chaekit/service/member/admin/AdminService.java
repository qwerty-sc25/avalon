package qwerty.chaekit.service.member.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.domain.member.user.UserProfileRepository;
import qwerty.chaekit.dto.member.UserInfoResponse;
import qwerty.chaekit.dto.page.PageResponse;

import qwerty.chaekit.service.util.FileService;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final FileService fileService;
    private final UserProfileRepository userRepository;

    @Getter
    @Setter
    private Long adminUserId;

    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> getUsers(Pageable pageable) {
        Pageable pageableWithSort = getPageableOrderedByCreatedAt(pageable);
        Page<UserProfile> page = userRepository.findAll(pageableWithSort);
        return PageResponse.of(page.map(user -> UserInfoResponse.of(
                user,
                fileService.convertToPublicImageURL(user.getProfileImageKey()),
                null, // 최근 활동 ID는 여기서 처리하지 않음
                null, // 최근 활동 책 이미지 URL은 여기서 처리하지 않음
                null
        )));
    }

    private static Pageable getPageableOrderedByCreatedAt(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isEmpty() ? Sort.by(Sort.Order.desc("createdAt")) : pageable.getSort()
        );
    }
}
