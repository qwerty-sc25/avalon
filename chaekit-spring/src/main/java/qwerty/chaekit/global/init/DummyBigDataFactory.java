package qwerty.chaekit.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.ebook.shelf.repository.EbookShelfRepository;
import qwerty.chaekit.domain.ebook.repository.EbookRepository;
import qwerty.chaekit.domain.group.ReadingGroup;
import qwerty.chaekit.domain.group.activity.Activity;
import qwerty.chaekit.domain.group.activity.repository.ActivityRepository;
import qwerty.chaekit.domain.group.repository.GroupRepository;
import qwerty.chaekit.domain.member.Member;
import qwerty.chaekit.domain.member.enums.Role;
import qwerty.chaekit.domain.member.user.UserProfile;
import qwerty.chaekit.domain.member.user.UserProfileRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyBigDataFactory {
    private final GroupRepository groupRepository;
    private final UserProfileRepository userProfileRepository;
    private final EbookRepository ebookRepository;
    private final EbookShelfRepository ebookShelfRepository;
    private final ActivityRepository activityRepository;

    private final LocalDate startDate = LocalDate.of(2024, 6, 1);
    private final LocalDate endDate = LocalDate.of(2025, 5, 31);

    @Transactional
    public void generateDummyDataForTest() {
        // 1. 출판물 5권 조회 (더미 도서)
        Random random = new Random();
        List<Ebook> ebooks = ebookRepository.findAll(PageRequest.of(0, 5))
                .stream()
                .toList();
        log.info("더미 도서 데이터가 {}개 조회되었습니다.", ebooks.size());
        ebooks.forEach(ebook -> ebook.resetViewCount(1000L + random.nextInt(2001)));

        // 2. 일반 사용자 100명 생성
        if (userProfileRepository.findByMember_Email("test1@dummy.com").isPresent()) {
            log.info("더미 사용자 데이터가 이미 존재합니다. 생성을 건너뜁니다.");
            return;
        }
        List<UserProfile> users = IntStream.range(0, 100)
                .mapToObj(i -> userProfileRepository.save(generateDummyUser("test" + i)))
                .toList();

        // 3. 사용자별 책 3권 무작위 구매
        List<EbookShelfItem> shelfItems = new ArrayList<>();
        for (UserProfile user : users) {
            List<Ebook> randomBooks = getRandomSubset(ebooks, 3);
            for (Ebook book : randomBooks) {
                EbookShelfItem ep = EbookShelfItem.builder()
                        .ebook(book)
                        .user(user)
                        .build();
                ep.resetCreatedAt(biasedRandomDate());
                shelfItems.add(ep);
            }
        }
        ebookShelfRepository.saveAll(shelfItems);

        // 4. 사용자 중 30명을 랜덤으로 뽑아 모임 생성
        List<Activity> activities = new ArrayList<>();
        List<UserProfile> leaders = getRandomSubset(users, 30);
        for (UserProfile leader : leaders) {
            ReadingGroup group = groupRepository.save(ReadingGroup.builder()
                    .name(leader.getNickname() + "의 모임")
                    .description("더미 모임입니다.")
                    .groupLeader(leader)
                    .build());

            // 5. 모임장 본인이 구매한 책 중 1권으로 활동 생성
            List<Ebook> leaderBooks = getRandomSubset(ebooks, 3);
            for( Ebook selectedBook : leaderBooks) {
                Activity ac = activityRepository.save(Activity.builder()
                        .group(group)
                        .description("이 활동은 " + selectedBook.getTitle() + "을 읽는 것입니다.")
                        .book(selectedBook)
                        .startTime(LocalDate.now())
                        .endTime(LocalDate.now().plusWeeks(1))
                        .build());
                ac.resetCreatedAt(biasedRandomDate());
                activities.add(ac);
            }
        }
        activityRepository.saveAll(activities);
    }
    
    private UserProfile generateDummyUser(String username) {
        Member account = Member.builder()
                .email(username + "@dummy.com")
                .password("password") // 비밀번호는 실제로는 암호화되어야 합니다.
                .role(Role.ROLE_USER)
                .build();
        return UserProfile.builder()
                .member(account)
                .nickname(username)
                .build();
    }

    private <T> List<T> getRandomSubset(List<T> list, int count) {
        List<T> mutable = new ArrayList<>(list);
        Collections.shuffle(mutable);
        return mutable.stream().limit(count).toList();
    }
    
    private LocalDateTime biasedRandomDate() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        // 선형 증가 분포를 위한 역변환 샘플링
        double u = Math.random(); // uniform(0, 1)
        double biased = 0.3 * u + 0.7 * Math.sqrt(u); // 오른쪽으로 치우친 분포

        long offsetDays = (long) (biased * days);
        return startDate.plusDays(offsetDays).atStartOfDay();
    }
    
}
