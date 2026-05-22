CREATE TABLE rooms_amenities (
    id_room INTEGER NOT NULL,
    id_amenity INTEGER NOT NULL,

    PRIMARY KEY (id_room, id_amenity),

    CONSTRAINT fk_room_amenity_room
        FOREIGN KEY (id_room)
        REFERENCES room (id_room)
        ON DELETE CASCADE,

    CONSTRAINT fk_room_amenity_amenity
        FOREIGN KEY (id_amenity)
        REFERENCES amenity (id_amenity)
        ON DELETE RESTRICT
);