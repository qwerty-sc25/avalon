package qwerty.chaekit.domain.ebook.shelf.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.shelf.EbookShelfItem;
import qwerty.chaekit.domain.member.user.UserProfile;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EbookShelfRepositoryImpl implements EbookShelfRepository {
    private final EbookShelfJpaRepository ebookShelfJpaRepository;

    @Override
    public EbookShelfItem save(EbookShelfItem ebookShelfItem) {
        return ebookShelfJpaRepository.save(ebookShelfItem);
    }

    @Override
    public Page<EbookShelfItem> findByUserIdWithEbook(Long userId, Pageable pageable) {
        return ebookShelfJpaRepository.findByUserIdWithEbook(userId, pageable);
    }

    @Override
    public Optional<EbookShelfItem> findByUserAndEbook(UserProfile user, Ebook ebook) {
        return ebookShelfJpaRepository.findByUserAndEbook(user, ebook);
    }

    @Override
    public boolean existsByUserIdAndEbookId(Long userId, Long ebookId) {
        return ebookShelfJpaRepository.existsByUser_IdAndEbook_Id(userId, ebookId);
    }

    @Override
    public List<EbookShelfItem> findByUserIdInAndEbook(List<Long> userIdList, Ebook ebook) {
        return ebookShelfJpaRepository.findByUserIdInAndEbook(userIdList, ebook);
    }

    @Override
    public void saveAll(List<EbookShelfItem> ebookShelfItems) {
        ebookShelfJpaRepository.saveAll(ebookShelfItems);
    }
}