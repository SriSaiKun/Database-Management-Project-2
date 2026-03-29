-- Create the database.
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);
-- User Data Insertion:
-- All unhashed passwords are "password"
INSERT INTO user (userId, username, password, firstName, lastName) VALUES
    (1, 'harry', '$2a$10$s.qhK5Z2dlwB5Jld8A9gUOB1vM9bSB9omkciYNG6QNf6cO8p9SgiW', 'Harry', 'Potter'),
    (2, 'hermione', '$2a$10$..C/3XazrTSEXEQ8NqjQwu6FydJqXgAY6z13XzIG6f8mhLZG9OKTa', 'Hermione',   'Granger'),
    (3, 'ron', '$2a$10$H55qwAz1TC9MJalyulJlk.lA1VQxAZ23uz5xwitfmZHlu73bXOc9q', 'Ron',   'Weasley'),
    (4, 'draco', '$2a$10$GrHk9jA9fn//0rDL5skQZ.9YEf6wYDDOKobjWGYJXRz2LALabZnTK', 'Draco', 'Malfoy'),
    (5, 'severus', '$2a$10$aufPUxfwRUlSMvfvKAcwZuXQUwjaP893Ln7ncIJIinMYr4.VLzRDa', 'Severus',  'Snape'),
    (6, 'tom', '$2a$10$SFWQyyfGwPlrJ1HPC5KH4.xfxpZgS9giZ.dAEUc.Hm33/U.tAQD5O', 'Tom',  'Riddle');

-- Create the post table.
CREATE TABLE post (
    postId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    postDate DATETIME NOT NULL,
    FOREIGN KEY (userId) REFERENCES user(userId)
);
-- Post Data Insertion:
INSERT INTO post (postId, userId, content, postDate) VALUES
    (1,  1, 'Hello this is my first post #test', '2024-03-07 22:54:00'),
    (2,  2, 'My second post #springboot', '2024-03-08 11:00:00'),
    (4,  2, 'Test post from followed user #hello', '2026-03-24 00:10:45'),
    (5,  1, 'hello #test', '2026-03-24 00:17:25'),
    (6,  1, '#springboot', '2026-03-25 20:51:49'),
    (7,  1, 'after fixing melayah error test', '2026-03-27 12:39:34'),
    (8,  1, 'is posting working? #?', '2026-03-27 16:35:43'),
    (9,  1, '#possum', '2026-03-27 16:36:12'),
    (10, 3, 'Loving the new features in this app! #java #springboot', '2026-03-26 09:00:00'),
    (11, 4, 'Just followed some cool people #social', '2026-03-26 10:30:00'),
    (12, 5, 'Hello everyone! First post here #hello', '2026-03-26 11:00:00'),
    (13, 6, 'Excited to be here #springboot #java', '2026-03-26 12:00:00'),
    (14, 3, 'Working on a database project #java #database', '2026-03-27 08:00:00'),
    (15, 4, 'Spring Boot is amazing #springboot', '2026-03-27 09:00:00');


-- Create the follow table.
CREATE TABLE follow (
    followerId INT NOT NULL,
    followeeId INT NOT NULL,
    PRIMARY KEY (followerId, followeeId),
    FOREIGN KEY (followerId) REFERENCES user(userId),
    FOREIGN KEY (followeeId) REFERENCES user(userId)
);
-- Follow Data Insertion:
INSERT INTO follow (followerId, followeeId) VALUES
    (2, 1),
    (3, 1),
    (5, 1),
    (6, 1),
    (1, 2),
    (4, 2),
    (1, 3),
    (2, 3),
    (1, 4),
    (3, 4);

-- Create the comment table.
CREATE TABLE comment (
    commentId INT PRIMARY KEY AUTO_INCREMENT,
    postId INT NOT NULL,
    userId INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    commentDate DATETIME NOT NULL,
    FOREIGN KEY (postId) REFERENCES post(postId),
    FOREIGN KEY (userId) REFERENCES user(userId)
);
-- Comment Data Insertion:
INSERT INTO comment (commentId, postId, userId, content, commentDate) VALUES
    (1, 2,  1, 'testing the commenting #tester', '2026-03-25 20:51:10'),
    (2, 6,  1, 'hello?', '2026-03-27 01:09:28'),
    (3, 7,  1, 'tester of fixed code', '2026-03-27 12:39:44'),
    (4, 1,  2, 'Welcome to the platform!', '2026-03-24 08:00:00'),
    (5, 2,  1, 'Great post about Spring Boot!', '2026-03-24 09:00:00'),
    (6, 10, 1, 'Totally agree!', '2026-03-26 09:30:00'),
    (7, 12, 2, 'Welcome Barry!', '2026-03-26 11:15:00'),
    (8, 14, 1, 'Good luck with the project!', '2026-03-27 08:30:00');

-- Create the likes table.
CREATE TABLE heart (
    userId INT NOT NULL,
    postId INT NOT NULL,
    PRIMARY KEY (userId, postId),
    FOREIGN KEY (userId) REFERENCES user(userId),
    FOREIGN KEY (postId) REFERENCES post(postId)
);
-- Heart Data Insertion:
INSERT INTO heart (userId, postId) VALUES
    (2, 1),
    (5, 1),
    (1, 2),
    (3, 2),
    (6, 2),
    (1, 7),
    (3, 7),
    (1, 10),
    (4, 10);

-- Create the bookmarks table
CREATE TABLE bookmark (
    userId INT NOT NULL,
    postId INT NOT NULL,
    PRIMARY KEY (userId, postId),
    FOREIGN KEY (userId) REFERENCES user(userId),
    FOREIGN KEY (postId) REFERENCES post(postId)
);
-- Bookmark Data Insertion:
INSERT INTO bookmark (userId, postId) VALUES
    (3, 1),
    (1, 2),
    (2, 2),
    (4, 2),
    (1, 6),
    (1, 7),
    (2, 7),
    (1, 9),
    (1, 10);

-- Create the hashtags table.
CREATE TABLE hashtag (
    postId INT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (postId, tag),
    FOREIGN KEY (postId) REFERENCES post(postId)
);
-- Hashtag Data Insertion:
INSERT INTO hashtag (postId, tag) VALUES
    (1,  'test'),
    (2,  'springboot'),
    (4,  'hello'),
    (5,  'test'),
    (6,  'springboot'),
    (9,  'possum'),
    (10, 'java'),
    (10, 'springboot'),
    (11, 'social'),
    (12, 'hello'),
    (13, 'java'),
    (13, 'springboot'),
    (14, 'database'),
    (14, 'java'),
    (15, 'springboot');
