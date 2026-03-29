-- DML file

-- Database used
USE csx370_mb_platform;

-- User Registration
-- http://localhost:8081/register
-- creates a new user whenever a registration is submitted
INSERT INTO user (username, password, firstName, lastName)
VALUES (?, ?, ?, ?);

-- User Login
-- http://localhost:8081/login
-- look up the user by their unique username and password
SELECT userId, username, password, firstName, lastName
FROM user
WHERE username = ?;

-- Home Page
-- http://localhost:8081/
-- fetch and display all posts (user and followers) in order from newest to oldest
SELECT p.postId, p.userId, p.content, DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM post p
WHERE p.userId = ? OR p.userId IN (SELECT followeeId FROM follow WHERE followerId = ?)
ORDER BY p.postDate DESC;
-- count how many likes a post receives
SELECT COUNT(*) AS NumComments
FROM heart
WHERE postId = ?;
-- count how many comments a post has
SELECT COUNT(*) AS NumComments
FROM comment
WHERE postId = ?;
-- checking if the user that is logged in currently has liked a certain post
SELECT *
FROM heart
WHERE postId = ? AND userId = ?;
-- checking if the user that is logged in has bookmarked a certain post
SELECT *
FROM bookmark
WHERE postId = ? AND userId = ?;
-- get the name of the user who wrote the post
SELECT firstName, lastName
FROM user
WHERE userId = ?;
-- insert a new post into database
INSERT INTO post (userId, content, postDate)
VALUES (?, ?, NOW());
-- insert a new hashtag into database
INSERT IGNORE INTO hashtag (postId, tag)
VALUES (?, ?);

-- Profile Page
-- http://localhost:8081/profile
-- http://localhost:8081/profile/{userId}
-- fetch all posts from the logged in user in order from most recent to the oldest
SELECT p.postId, p.userId, p.content,
       DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM post p
WHERE p.userId = ?
ORDER BY p.postDate DESC;

-- People Page
-- http://localhost:8081/people
-- fetch all users except the one that is currently logged in
SELECT userId, username, firstName, lastName
FROM user
WHERE userId <> ?;
-- get the last post from the user to show their last active time
SELECT DATE_FORMAT(postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM post
WHERE userId = ?
ORDER BY postDate DESC
    LIMIT 1;
-- check if the logged-in user follows one of the other users
SELECT followerId
FROM follow
WHERE followerId = ? AND followeeId = ?;
-- insert data that the user follows another account (also making sure you can't follow the same person twice)
INSERT IGNORE INTO follow (followerId, followeeId)
VALUES (?, ?);
-- remove data that the user follows another account
DELETE FROM follow
WHERE followerId = ? AND followeeId = ?;

-- Bookmarks Page
-- http://localhost:8081/bookmarks
-- fetch all posts the logged-in user has bookmarked, ordered newest first
SELECT p.postId, p.userId, p.content,
       DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM bookmark bm
         JOIN post p ON bm.postId = p.postId
WHERE bm.userId = ?
ORDER BY p.postDate DESC;

-- Post Detail Page
-- http://localhost:8081/post/{postId}
-- fetch a single post with all its details
SELECT postId, userId, content,
       DATE_FORMAT(postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM post
WHERE postId = ?;
-- fetch all comments for a post ordered from most recent to oldest
SELECT commentId, userId, content, commentDate
FROM comment
WHERE postId = ?
ORDER BY commentDate DESC;
-- get the name of the person who wrote the comment
SELECT firstName, lastName
FROM user
WHERE userId = ?;
-- check if the author of the post follows the author of the comment
SELECT True AS followed
FROM follow
WHERE followerId = ? AND followeeId = ?;
-- insert data in database for a new comment
INSERT INTO comment (postId, userId, content, commentDate)
VALUES (?, ?, ?, NOW());
-- insert data into database for when the logged-in user likes a post (make sure you don't like the same post twice)
INSERT IGNORE INTO heart (userId, postId)
VALUES (?, ?);
-- delete data from database for when the logged-in user unlikes a post
DELETE FROM heart
WHERE userId = ? AND postId = ?;
-- insert data into database for when the logged-in user bookmarks a post (make sure you don't bookmark the same post twice)
INSERT IGNORE INTO bookmark (userId, postId)
VALUES (?, ?);
-- delete data from database for when the logged-in user removes a bookmark from the post
DELETE FROM bookmark
WHERE userId = ? AND postId = ?;

-- Hashtag Search
-- http://localhost:8081/hashtagsearch?hashtags=...
-- Search posts by a specific hashtag used in the post and displays in order from newest to oldest
SELECT DISTINCT p.postId, p.userId, p.content, DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate
FROM post p
         JOIN hashtag h ON p.postId = h.postId
WHERE h.tag IN (?)
GROUP BY p.postId, p.userId, p.content, p.postDate
HAVING COUNT(DISTINCT h.tag) = ?
ORDER BY p.postDate DESC;