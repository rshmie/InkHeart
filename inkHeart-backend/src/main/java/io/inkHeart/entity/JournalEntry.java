package io.inkHeart.entity;

import io.inkHeart.converter.EncryptedPayloadConverter;
import io.inkHeart.dto.EncryptedPayload;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, updatable = false)
    private UUID entryUUID; // The client-generated, security-focused ID.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User user;

    @Column(nullable = false)
    @Convert(converter = EncryptedPayloadConverter.class)
    private EncryptedPayload encryptedTitle;

    @Lob
    @Column(nullable = false)
    @Convert(converter = EncryptedPayloadConverter.class)
    private EncryptedPayload encryptedContent;

    @Convert(converter = EncryptedPayloadConverter.class)
    private EncryptedPayload encryptedMood;
    @Convert(converter = EncryptedPayloadConverter.class)
    @ElementCollection
    private List<EncryptedPayload> encryptedTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime visibleAfter;
    private LocalDateTime expiresAt;
    public Long getId() {
        return id;
    }
    public UUID getEntryUUID() {
        return entryUUID;
    }

    public User getUser() {
        return user;
    }

    public EncryptedPayload getEncryptedTitle() {
        return encryptedTitle;
    }

    public EncryptedPayload getEncryptedContent() {
        return encryptedContent;
    }

    public EncryptedPayload getEncryptedMood() {
        return encryptedMood;
    }

    public List<EncryptedPayload> getEncryptedTags() {
        return encryptedTags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getVisibleAfter() {
        return visibleAfter;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setEntryUUID(UUID entryUUID) {
        this.entryUUID = entryUUID;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEncryptedTitle(EncryptedPayload encryptedTitle) {
        this.encryptedTitle = encryptedTitle;
    }

    public void setEncryptedContent(EncryptedPayload encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public void setEncryptedMood(EncryptedPayload encryptedMood) {
        this.encryptedMood = encryptedMood;
    }

    public void setEncryptedTags(List<EncryptedPayload> encryptedTags) {
        this.encryptedTags = encryptedTags;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVisibleAfter(LocalDateTime visibleAfter) {
        this.visibleAfter = visibleAfter;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalEntry that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getEntryUUID(), that.getEntryUUID()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getEncryptedTitle(), that.getEncryptedTitle()) && Objects.equals(getEncryptedContent(), that.getEncryptedContent()) && Objects.equals(getEncryptedMood(), that.getEncryptedMood()) && Objects.equals(getEncryptedTags(), that.getEncryptedTags()) && Objects.equals(getCreatedAt(), that.getCreatedAt()) && Objects.equals(getUpdatedAt(), that.getUpdatedAt()) && Objects.equals(getVisibleAfter(), that.getVisibleAfter()) && Objects.equals(getExpiresAt(), that.getExpiresAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEntryUUID(), getUser(), getEncryptedTitle(), getEncryptedContent(), getEncryptedMood(), getEncryptedTags(), getCreatedAt(), getUpdatedAt(), getVisibleAfter(), getExpiresAt());
    }

}
