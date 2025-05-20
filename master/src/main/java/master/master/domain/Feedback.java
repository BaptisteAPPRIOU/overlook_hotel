package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_id_seq")
    @SequenceGenerator(name = "feedback_id_seq", sequenceName = "feedback_id_seq", allocationSize = 1)
    private Integer id;

    private Integer userId;

    @Column(nullable = false)
    private String content;

    private LocalDateTime date;

    private String answer;
}