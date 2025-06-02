
-- Inserindo roles
INSERT INTO ROLES (role_name) VALUES ('ROLE_USER');
INSERT INTO ROLES (role_name) VALUES ('ROLE_ADMIN');

-- Password: password
INSERT INTO USERS (name, email, username, password, roles)
VALUES ('Administrador', 'admin@example.com', 'admin', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ROLE_ADMIN');

INSERT INTO USER_ROLES (user_id, role_id) VALUES (1, 1);
