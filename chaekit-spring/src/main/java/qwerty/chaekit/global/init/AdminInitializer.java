package qwerty.chaekit.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.member.Member;
import qwerty.chaekit.domain.member.MemberRepository;
import qwerty.chaekit.domain.member.enums.Role;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.domain.member.user.UserProfileRepository;
import qwerty.chaekit.global.properties.AdminProperties;
import qwerty.chaekit.service.member.MemberJoinHelper;
import qwerty.chaekit.service.member.admin.AdminService;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class AdminInitializer implements ApplicationRunner {
    private final AdminProperties adminProperties;
    private final MemberJoinHelper memberJoinHelper;
    private final AdminService adminService;
    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String adminName = adminProperties.name();
        String adminEmail = adminProperties.email();
        String adminPassword = adminProperties.password();
        Role adminRole = Role.ROLE_ADMIN;

        Member adminMember = memberRepository.findByEmail(adminEmail).orElseGet(
                () -> {
                    // 관리자가 없으면 생성
                    Member newMember = memberJoinHelper.saveMember(adminEmail, adminPassword, adminRole);
                    log.info("관리자가 생성되었습니다. memberId = {}", newMember.getId());
                    return newMember;
                }
        );

        Optional<UserProfile> user = userProfileRepository.findByMember_Email(adminEmail);
        UserProfile adminUser = user.orElseGet(() -> {
            UserProfile newProfile = userProfileRepository.save(
                    UserProfile.builder()
                            .member(adminMember)
                            .nickname(adminName)
                            .profileImageKey("profile-image/logo.png")
                            .build()
            );
            log.info("관리자 사용자 프로필이 추가되었습니다.");
            return newProfile;
        });

        adminService.setAdminUserId(adminUser.getId());
        log.info("관리자 설정이 완료되었습니다. email = {}, memberId = {}, userId = {}",
                adminEmail, adminMember.getId(), adminUser.getId()
        );
    }
}
