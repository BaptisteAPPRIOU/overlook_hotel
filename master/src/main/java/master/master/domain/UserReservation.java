package master.master.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "user_reservation")
@IdClass(UserReservationId.class)
public class UserReservation {
    @Id
    private Integer userId;

    @Id
    private Integer roomId;

    @Id
    private LocalDate reservationDateStart;

    private LocalDate reservationDateEnd;

    private Boolean isPayed;

    private Boolean isAccepted;

    private BigDecimal price;
}

class UserReservationId implements Serializable {
    private Integer userId;
    private Integer roomId;
    private LocalDate reservationDateStart;
}