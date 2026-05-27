CREATE TABLE employees (
    id_user BIGINT PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    team VARCHAR(100),
    employee_status VARCHAR(30) NOT NULL,
    hire_date DATE NOT NULL,

    CONSTRAINT fk_employee_user
        FOREIGN KEY (id_user)
        REFERENCES users (id_user)
        ON DELETE CASCADE
);
