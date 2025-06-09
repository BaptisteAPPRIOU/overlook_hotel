package master.master.web.rest.dto;

import master.master.domain.RoomType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RoomDto {
    private String roomNumber;
    private RoomType roomType;
    private boolean occupied;

}
