use db_movies;

INSERT INTO roles (role_name, created_at, updated_at) 
VALUES ('ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (role_name, created_at, updated_at) 
VALUES ('ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

INSERT INTO USERS (name, email, username, password, roles, updated_at) VALUES 
('Usuário', 'user@email.com', 'user', '$2a$10$GiseHkdvwOFr7A9KRWbeiOmg/PYPhWVjdm42puLfOzR/gIAQrsAGy', 'ROLE_USER', CURRENT_TIMESTAMP);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);