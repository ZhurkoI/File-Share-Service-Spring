INSERT INTO users (id, first_name, last_name, email, username, password, status, created, updated) VALUES
(2, 'Moderator', 'Moderator', 'moderator@test.com', 'moderator', '$2a$10$qqtvRP80Mak/UoenWgHWMe3A66R0.fYSf52SlCz6uU76xoWTACcj.', DEFAULT, now(), now()),
(3, 'User', 'User', 'user@test.com', 'user', '$2a$10$xaeIrTfZYDcqvOAhfUU3COLR06evn8KXCXVU6Z6iwrkxXs4V/VhJy', DEFAULT, now(), now());

INSERT INTO user_role (user_id,role_id) VALUES
(2,2),
(2,3),
(3,3);

INSERT INTO files (id, path, name, status, created, updated) VALUES
(1, 'test.file-share-service.bucket/user', 'file_1.txt', DEFAULT, now(), now()),
(2, 'test.file-share-service.bucket/user', 'file_2.txt', DEFAULT, now(), now()),
(3, 'test.file-share-service.bucket/user', 'file_3.txt', DEFAULT, now(), now()),
(4, 'test.file-share-service.bucket/moderator', 'file_4.txt', DEFAULT, now(), now());

INSERT INTO events (id, type, status, created, updated, file_id, user_id) VALUES
(1, 'UPLOADED', DEFAULT, now(), now(), 1, 3),
(2, 'UPLOADED', DEFAULT, now(), now(), 2, 3),
(3, 'UPLOADED', DEFAULT, now(), now(), 3, 3),
(4, 'UPLOADED', DEFAULT, now(), now(), 4, 2);