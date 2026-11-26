package shop.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A customer's feedback on a product: a rating (1-5), a comment, or both.
 * Immutable; a user has at most one Review per product (changes create a modified copy).
 * Only a masked display name (first name) is kept, so personal data is never exposed (story 32).
 */
public final class Review {
    private final String id;
    private final String userId;
    private final String reviewerName;
    private final Integer score;
    private final String comment;
    private final LocalDateTime createdAt;

    private Review(String userId, String reviewerName, Integer score, String comment) {
        if (Validators.isBlank(userId)) {
            throw new IllegalArgumentException("A review must belong to a user.");
        }
        if (score == null && comment == null) {
            throw new IllegalArgumentException("A review needs a rating or a comment.");
        }
        if (score != null && (score < 1 || score > 5)) {
            throw new IllegalArgumentException("The rating must be between 1 and 5.");
        }
        if (comment != null) {
            if (Validators.isBlank(comment)) {
                throw new IllegalArgumentException("The comment cannot be empty.");
            }
            if (comment.trim().length() > Validators.MAX_COMMENT_LENGTH) {
                throw new IllegalArgumentException(
                        "The comment cannot be longer than " + Validators.MAX_COMMENT_LENGTH + " characters.");
            }
        }
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.reviewerName = firstName(reviewerName);
        this.score = score;
        this.comment = comment == null ? null : comment.trim();
        this.createdAt = LocalDateTime.now();
    }

    public static Review rating(String userId, String reviewerFullName, int score) {
        return new Review(userId, reviewerFullName, score, null);
    }

    public static Review comment(String userId, String reviewerFullName, String comment) {
        return new Review(userId, reviewerFullName, null, comment == null ? "" : comment);
    }

    public Review withScore(int newScore) {
        return new Review(userId, reviewerName, newScore, comment);
    }

    public Review withComment(String newComment) {
        return new Review(userId, reviewerName, score, newComment == null ? "" : newComment);
    }

    private static String firstName(String fullName) {
        if (Validators.isBlank(fullName)) {
            return "Customer";
        }
        return fullName.trim().split("\\s+")[0];
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    /** Masked display name (first name only). */
    public String getReviewerName() {
        return reviewerName;
    }

    /** May be null when the user only wrote a comment. */
    public Integer getScore() {
        return score;
    }

    /** May be null when the user only gave a rating. */
    public String getComment() {
        return comment;
    }

    public boolean hasScore() {
        return score != null;
    }

    public boolean hasComment() {
        return comment != null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
