package master.master.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents feedback provided by a user.
 * <p>
 * Each Feedback instance contains the feedback content, the date it was given,
 * and a reference to the user who submitted it.
 * </p>
 *
 * Fields:
 * <ul>
 *   <li>id - Unique identifier for the feedback.</li>
 *   <li>user - The user who submitted the feedback.</li>
 *   <li>content - The textual content of the feedback.</li>
 *   <li>date - The date and time when the feedback was submitted.</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_id_seq")
    @SequenceGenerator(name = "feedback_id_seq", sequenceName = "feedback_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;

    @Column(length = 255)
    private String answer;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}
