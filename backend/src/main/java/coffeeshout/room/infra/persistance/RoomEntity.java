package coffeeshout.room.infra.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String joinCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    public RoomEntity(String joinCode) {
        this.joinCode = joinCode;
        this.createdAt = LocalDateTime.now();
    }

    public void finish() {
        this.finishedAt = LocalDateTime.now();
    }
}
