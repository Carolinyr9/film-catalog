-- Inserindo roles
INSERT INTO roles (role_name, created_at, updated_at) 
VALUES ('ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (role_name, created_at, updated_at) 
VALUES ('ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Password: password
INSERT INTO USERS (name, email, username, password, roles, updated_at) VALUES 
('Administrador', 'admin@example.com', 'admin', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ROLE_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- INSERT INTO movies (title, synopsis, release_year, duration, content_rating, created_at, updated_at) VALUES 
-- ('Forrest Gump', 'A vida de um homem com QI abaixo da média', 1994, 142, 'A14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- ('A Origem', 'Um ladrão que rouba segredos ao invadir sonhos', 2010, 148, 'A12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- ('Toy Story', 'Brinquedos ganham vida quando ninguém está olhando', 1995, 81, 'A10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- ('Clube da Luta', 'Homem cria clube secreto de lutas', 1999, 139, 'A18', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USERS (name, email, username, password, roles, updated_at) VALUES 
('Usuário', 'user@email.com', 'user', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ROLE_USER', CURRENT_TIMESTAMP);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO USERS (name, email, username, password, roles, updated_at) VALUES 
('Usuário 2', 'user2@email.com', 'user2', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ROLE_USER', CURRENT_TIMESTAMP);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 2);