package qwerty.chaekit.domain.ebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import qwerty.chaekit.domain.ebook.Ebook;

@Repository
public interface EbookJpaRepository extends JpaRepository<Ebook, Long> {
    boolean existsByTitle(String title);

    @Modifying
    @Query("UPDATE Ebook e SET e.viewCount = e.viewCount + 1 WHERE e.id = :ebookId")
    void incrementViewCount(Long ebookId);
} 