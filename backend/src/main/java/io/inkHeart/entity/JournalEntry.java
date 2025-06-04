package io.inkHeart.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
/*
Why ManyToOne? and by join coloumn does it mean,
user_id is an foreign key (of Journal Entry) pointed at User table?
Why have we not specified column annotation for other fields?
are they not part of JournalEntry table?
What does the tag and mood significance here?
 */
@Entity
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User user;

    @Column(nullable = false)
    private String encryptedTitle;

    @Lob
    @Column(nullable = false)
    private String encryptedContent;

    private String encryptedMood;
    private List<String> encryptedTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime visibleAfter;
    private LocalDateTime expiresAt;
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getEncryptedTitle() {
        return encryptedTitle;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public String getEncryptedMood() {
        return encryptedMood;
    }

    public List<String> getEncryptedTags() {
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

    public void setUser(User user) {
        this.user = user;
    }

    public void setEncryptedTitle(String encryptedTitle) {
        this.encryptedTitle = encryptedTitle;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public void setEncryptedMood(String encryptedMood) {
        this.encryptedMood = encryptedMood;
    }

    public void setEncryptedTags(List<String> encryptedTags) {
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
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(encryptedTitle, that.encryptedTitle) && Objects.equals(encryptedContent, that.encryptedContent) && Objects.equals(encryptedMood, that.encryptedMood) && Objects.equals(encryptedTags, that.encryptedTags) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(visibleAfter, that.visibleAfter) && Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, encryptedTitle, encryptedContent, encryptedMood, encryptedTags, createdAt, updatedAt, visibleAfter, expiresAt);
    }


}
