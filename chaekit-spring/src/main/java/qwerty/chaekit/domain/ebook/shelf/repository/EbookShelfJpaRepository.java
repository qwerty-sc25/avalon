package qwerty.chaekit.domain.ebook.shelf.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.member.user.UserProfile;

import java.util.List;
import java.util.Optional;

@Repository
public interface EbookShelfJpaRepository extends JpaRepository<EbookShelfItem, Long> {
    @Query("SELECT es FROM EbookShelfItem es JOIN FETCH es.ebook e WHERE es.user.id = :userId")
    Page<EbookShelfItem> findByUserIdWithEbook(Long userId, Pageable pageable);

    Optional<EbookShelfItem> findByUserAndEbook(UserProfile user, Ebook ebook);
    List<EbookShelfItem> findByUserIdInAndEbook(List<Long> userIds, Ebook ebook);

    boolean existsByUser_IdAndEbook_Id(Long userId, Long ebookId);
}