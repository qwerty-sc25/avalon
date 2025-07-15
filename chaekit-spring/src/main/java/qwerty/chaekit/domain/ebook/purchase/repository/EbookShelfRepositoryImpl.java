package qwerty.chaekit.domain.ebook.purchase.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.ebook.purchase.EbookShelfItem;
import qwerty.chaekit.domain.member.user.UserProfile;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EbookShelfRepositoryImpl implements EbookShelfRepository {
    private final EbookPurchaseJpaRepository ebookPurchaseJpaRepository;

    @Override
    public EbookShelfItem save(EbookShelfItem ebookShelfItem) {
        return ebookPurchaseJpaRepository.save(ebookShelfItem);
    }

    @Override
    public Page<EbookShelfItem> findByUserIdWithEbook(Long userId, Pageable pageable) {
        return ebookPurchaseJpaRepository.findByUserIdWithEbook(userId, pageable);
    }

    @Override
    public Optional<EbookShelfItem> findByUserAndEbook(UserProfile user, Ebook ebook) {
        return ebookPurchaseJpaRepository.findByUserAndEbook(user, ebook);
    }

    @Override
    public boolean existsByUserIdAndEbookId(Long userId, Long ebookId) {
        return ebookPurchaseJpaRepository.existsByUser_IdAndEbook_Id(userId, ebookId);
    }

    @Override
    public List<EbookShelfItem> findByUserIdInAndEbook(List<Long> userIdList, Ebook ebook) {
        return ebookPurchaseJpaRepository.findByUserIdInAndEbook(userIdList, ebook);
    }

    @Override
    public void saveAll(List<EbookShelfItem> ebookShelfItems) {
        ebookPurchaseJpaRepository.saveAll(ebookShelfItems);
    }
}