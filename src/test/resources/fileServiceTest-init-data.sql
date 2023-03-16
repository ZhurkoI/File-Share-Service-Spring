INSERT INTO users (id, first_name, last_name, email, username, password, status, created, updated) VALUES
(2, 'Moderator', 'Moderator', 'moderator@test.com', 'moderator', '$2a$10$qqtvRP80Mak/UoenWgHWMe3A66R0.fYSf52SlCz6uU76xoWTACcj.', DEFAULT, now(), now()),
(3, 'User', 'User', 'user@test.com', 'user', '$2a$10$xaeIrTfZYDcqvOAhfUU3COLR06evn8KXCXVU6Z6iwrkxXs4V/VhJy', DEFAULT, now(), now());

INSERT INTO user_role (user_id,role_id) VALUES
(2,2),
(2,3),
(3,3);

INSERT INTO files (id, path, name, status, created, updated) VALUES
(1, 'test.file-share-service.bucket/user', 'test_4 (3rd copy).txt', DEFAULT, now(), now()),
(2, 'test.file-share-service.bucket/user', 'test_4 (4th copy).txt', DEFAULT, now(), now()),
(3, 'test.file-share-service.bucket/user', 'test_4 (5th copy).txt', DEFAULT, now(), now()),
(4, 'test.file-share-service.bucket/moderator', 'test_4 (6th copy).txt', DEFAULT, now(), now());

INSERT INTO events (id, type, status, created, updated, file_id, user_id) VALUES
(1, 'UPLOADED', DEFAULT, now(), now(), 1, 3),
(2, 'UPLOADED', DEFAULT, now(), now(), 2, 3),
(3, 'UPLOADED', DEFAULT, now(), now(), 3, 3),
(4, 'UPLOADED', DEFAULT, now(), now(), 4, 2);