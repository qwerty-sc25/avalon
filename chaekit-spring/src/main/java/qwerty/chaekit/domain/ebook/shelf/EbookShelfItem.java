package qwerty.chaekit.domain.ebook.shelf;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qwerty.chaekit.domain.BaseEntity;
import qwerty.chaekit.domain.ebook.Ebook;
import qwerty.chaekit.domain.member.user.UserProfile;

@Entity
@Getter
@Table(name = "ebook_shelf_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EbookShelfItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="book_id")
    private Ebook ebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserProfile user;

    @Column
    private String cfi;

    @Column
    private long percentage = 0L;

    @Builder
    public EbookShelfItem(Ebook ebook, UserProfile user, String cfi, long percentage) {
        this.ebook = ebook;
        this.user = user;
        this.cfi = cfi;
        this.percentage = percentage;
    }

    public void saveProgress(String cfi, long percentage) {
        this.cfi = cfi;
        this.percentage = percentage;
    }
}
