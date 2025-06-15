package io.inkHeart.repository;

import io.inkHeart.dto.JournalEntryResponse;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    // Belong to the user which are now visible after visibleAfter timestamp
    List<JournalEntry> findByUserAndCreatedAtBeforeAndVisibleAfterBefore(User user, LocalDateTime now1, LocalDateTime now2);
    List<JournalEntry> findAllByUserEmail(String userName);

    List<JournalEntryResponse> findTop10ByUserOrderByCreatedAtDesc(User user);
    Optional<JournalEntry> findByIdAndUser(Long id, User user);
}
