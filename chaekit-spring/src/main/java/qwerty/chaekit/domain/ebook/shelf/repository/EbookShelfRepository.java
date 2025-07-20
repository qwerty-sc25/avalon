package qwerty.chaekit.domain.ebook.shelf.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.member.user.UserProfile;

import java.util.List;
import java.util.Optional;

@Repository
public interface EbookShelfRepository {
    EbookShelfItem save(EbookShelfItem ebook);
    Page<EbookShelfItem> findByUserIdWithEbook(Long userId, Pageable pageable);
    Optional<EbookShelfItem> findByUserAndEbook(UserProfile user, Ebook ebook);
    boolean existsByUserIdAndEbookId(Long userId, Long ebookId);
    List<EbookShelfItem> findByUserIdInAndEbook(List<Long> userIdList, Ebook ebook);
    void saveAll(List<EbookShelfItem> ebookShelfItems);
}
