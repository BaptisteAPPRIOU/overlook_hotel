CREATE TABLE clients (
    id_user INTEGER PRIMARY KEY,
    fidelity_points INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_client_user
        FOREIGN KEY (id_user)
        REFERENCES users (id_user)
        ON DELETE CASCADE,

    CONSTRAINT chk_client_fidelity_points
        CHECK (fidelity_points >= 0)
);