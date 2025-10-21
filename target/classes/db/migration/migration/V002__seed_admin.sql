INSERT INTO users(username, password_hash) VALUES ('admin', '{bcrypt}$2a$10$J0pD4W9Fh0hXr3oA0WwW3.9n8bKk2JqM3J7v7gN8p3m2y0Qe1b7dW');
INSERT INTO users_roles(user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username='admin';