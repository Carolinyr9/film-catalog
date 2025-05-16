
-- Inserindo roles
INSERT INTO ROLES (role_name) VALUES ('USER');
INSERT INTO ROLES (role_name) VALUES ('ADMIN');

-- Password: password
INSERT INTO USERS (nome, email, username, password, tipo)
VALUES ('Administrador', 'admin@example.com', 'admin', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ADMIN');

INSERT INTO USER_ROLES (user_id, role_id) VALUES (1, 1);
