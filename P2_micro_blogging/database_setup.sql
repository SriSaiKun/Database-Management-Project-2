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

-- Create the post table.
CREATE TABLE post (
    postId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    postDate DATETIME NOT NULL,
    FOREIGN KEY (userId) REFERENCES user(userId)
);

-- Create the follow table.
CREATE TABLE follow (
    followerId INT NOT NULL,
    followeeId INT NOT NULL,
    PRIMARY KEY (followerId, followeeId),
    FOREIGN KEY (followerId) REFERENCES user(userId),
    FOREIGN KEY (followeeId) REFERENCES user(userId)
);

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

-- Create the likes table.
CREATE TABLE heart (
    userId INT NOT NULL,
    postId INT NOT NULL,
    PRIMARY KEY (userId, postId),
    FOREIGN KEY (userId) REFERENCES user(userId),
    FOREIGN KEY (postId) REFERENCES post(postId)
);

-- Create the bookmarks table
CREATE TABLE bookmark (
    userId INT NOT NULL,
    postId INT NOT NULL,
    PRIMARY KEY (userId, postId),
    FOREIGN KEY (userId) REFERENCES user(userId),
    FOREIGN KEY (postId) REFERENCES post(postId)
);

-- Create the hashtags table.
CREATE TABLE hashtag (
    postId INT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (postId, tag),
    FOREIGN KEY (postId) REFERENCES post(postId)
);
