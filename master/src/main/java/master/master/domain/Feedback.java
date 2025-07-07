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
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime date;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}