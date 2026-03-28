/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

 *  *This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

/**
 * This service contains post related functions.
 */
@Service
public class PostService {

    private final DataSource dataSource;

    @Autowired
    public PostService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Post> getBookmarkedPosts(String currentUserId) {
        List<Post> posts = new ArrayList<>();

        final String sql
                = "SELECT p.postId, p.content, DATE_FORMAT(p.postDate, '%b %d, %Y, %h:%i %p') as postDate, "
                + "u.userId, u.firstName, u.lastName "
                + "FROM bookmark b "
                + "JOIN post p ON b.postId = p.postId "
                + "JOIN user u ON p.userId = u.userId "
                + "WHERE b.userId = ? "
                + "ORDER BY p.postDate DESC";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(currentUserId));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(buildPost(
                            rs,
                            isHearted(rs.getString("postId"), currentUserId, dataSource),
                            true
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    public static User getPoster(String posterId, DataSource dataSource) {
        final String sql = "SELECT firstName, lastName "
                + "FROM user "
                + "WHERE userId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, posterId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                if (rs.next()) {
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    User user = new User(posterId, firstName, lastName);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getHeartsCount(String postId, DataSource dataSource) {
        final String sql = "SELECT COUNT(*) as NumComments "
                + "FROM heart "
                + "WHERE postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int numRows = 0;
                // the if statement should run every time, but if for some reason it doesn't
                // then skip
                if (rs.next()) {
                    numRows = rs.getInt("NumComments");
                }

                return numRows;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getCommentsCount(String postId, DataSource dataSource) {
        final String sql = "SELECT COUNT(*) as NumComments "
                + "FROM comment "
                + "WHERE postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int numRows = 0;
                // the if statement should run every time, but if for some reason it doesn't
                // then skip
                if (rs.next()) {
                    numRows = rs.getInt("NumComments");
                }

                return numRows;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Boolean isHearted(String postId, String userId, DataSource dataSource) {
        final String sql = "SELECT * "
                + "FROM heart "
                + "WHERE postId = ? AND userId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);
            pstmt.setString(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean isBookmarked(String postId, String userId, DataSource dataSource) {
        final String sql = "SELECT * "
                + "FROM bookmark "
                + "WHERE postId = ? AND userId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);
            pstmt.setString(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Post getPost(String postId, String userId) {
        final String sql = "SELECT postId, userId, content,"
                + "DATE_FORMAT(postDate, '%b %d, %Y, %h:%i %p') as postDate "
                + "FROM post "
                + "WHERE postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                if (rs.next()) {
                    String posterId = rs.getString("userId");
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");
                    User poster = getPoster(posterId, dataSource);
                    int heartCount = getHeartsCount(postId, dataSource);
                    int commentCount = getCommentsCount(postId, dataSource);
                    Boolean isHearted = isHearted(postId, userId, dataSource);
                    Boolean isBookmarked = isBookmarked(postId, userId, dataSource);
                    Post post = new Post(postId, content, postDate, poster, heartCount, commentCount, isHearted, isBookmarked);
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Comment> getComments(String postId, String posterId) {

        final String sql
                = "SELECT c.commentId as commentId, c.userId as userId, c.content as content, "
                + "DATE_FORMAT(c.commentDate, '%b %d, %Y, %h:%i %p') as commentDate, "
                + "u.firstName, u.lastName "
                + "FROM comment as c, user as u "
                + "WHERE c.postId = ? AND c.userId = u.userId "
                + "ORDER BY commentDate desc;";

        final String sqlIsFollowed = "SELECT True as followed "
                + "FROM follow "
                + "WHERE followerId = ? AND followeeId = ?";
        final String sqlDate = "SELECT DATE_FORMAT(postDate, '%b %d, %Y, %h:%i %p') as postDate from post WHERE userId = ? ORDER BY postDate DESC LIMIT 1;";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = rs.getString("commentId");
                    String userId = rs.getString("userId");
                    String content = rs.getString("content");
                    String commentDate = rs.getString("commentDate");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    PreparedStatement pstmtFollow = conn.prepareStatement(sqlIsFollowed);
                    pstmtFollow.setString(1, posterId);
                    pstmtFollow.setString(2, userId);
                    ResultSet followSet = pstmtFollow.executeQuery();
                    Boolean isFollowed = false;

                    PreparedStatement pstmtDate = conn.prepareStatement(sqlDate);
                    pstmtDate.setString(1, userId);
                    ResultSet dateSet = pstmtDate.executeQuery();
                    String userLastPostDate = "Unknown";
                    if (followSet.next()) {
                        // Because a row will only be returned when the Poster's userId
                        // == followerId AND the commenter's user id == followeeId,
                        // if this condition returns true, we can assume isFollow is true
                        isFollowed = true;
                    }

                    if (dateSet.next()) {
                        userLastPostDate = dateSet.getString("postDate");
                    }

                    FollowableUser user = new FollowableUser(userId, firstName, lastName, isFollowed, userLastPostDate);

                    comments.add(new Comment(commentId, content, commentDate, user));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return comments;
    }

    public List<Post> searchPostsByHashtags(List<String> hashtags, String userId) {
        List<Post> posts = new ArrayList<>();

        if (hashtags == null || hashtags.isEmpty()) {
            return posts;
        }

        String placeholders = String.join(",", Collections.nCopies(hashtags.size(), "?"));

        final String sql
                = "SELECT p.postId as postId, p.content, DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate, "
                + "u.userId, u.firstName, u.lastName "
                + "FROM post p "
                + "JOIN user u ON p.userId = u.userId "
                + "JOIN hashtag h ON p.postId = h.postId "
                + "WHERE h.tag IN (" + placeholders + ") "
                + "GROUP BY p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName "
                + "HAVING COUNT(DISTINCT h.tag) = ? "
                + "ORDER BY p.postDate DESC";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (String hashtag : hashtags) {
                pstmt.setString(index++, hashtag);
            }
            pstmt.setInt(index, hashtags.size());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(buildPost(rs, isHearted(rs.getString("postId"), userId, dataSource), false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    public boolean addBookmark(String userId, String postId) {
        if (isBookmarked(postId, userId, dataSource)) {
            return true;
        }

        final String sql = "INSERT INTO bookmark (userId, postId) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(postId));
            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBookmark(String userId, String postId) {
        if (!isBookmarked(postId, userId, dataSource)) {
            return true;
        }

        final String sql = "DELETE FROM bookmark WHERE userId = ? AND postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(postId));
            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addHeart(String userId, String postId) {
        if (isHearted(postId, userId, dataSource)) {
            return true;
        }

        final String sql = "INSERT INTO heart (userId, postId) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);

            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeHeart(String userId, String postId) {
        if (!isHearted(postId, userId, dataSource)) {
            return true;
        }

        final String sql = "DELETE FROM heart WHERE userId = ? AND postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);

            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Post buildPost(ResultSet rs, boolean isHearted, boolean isBookmarked) throws SQLException {
        String postId = rs.getString("postId");

        User user = new User(
                rs.getString("userId"),
                rs.getString("firstName"),
                rs.getString("lastName")
        );

        int heartsCount = getHeartsCount(postId, dataSource);
        int commentsCount = getCommentsCount(postId, dataSource);

        return new Post(
                postId,
                rs.getString("content"),
                rs.getString("postDate"),
                user,
                heartsCount,
                commentsCount,
                isHearted,
                isBookmarked
        );
    }

    public List<Post> getHomeFeedPosts(String loggedInUserId) {
        final String sql
                = "SELECT p.postId, p.userId, p.content, "
                + "DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate "
                + "FROM post p "
                + "WHERE p.userId = ? "
                + "OR p.userId IN (SELECT followeeId FROM follow WHERE followerId = ?) "
                + "ORDER BY p.postDate DESC";

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loggedInUserId);
            pstmt.setString(2, loggedInUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String posterId = rs.getString("userId");
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");

                    User poster = getPoster(posterId, dataSource);
                    int heartCount = getHeartsCount(postId, dataSource);
                    int commentCount = getCommentsCount(postId, dataSource);
                    boolean hearted = isHearted(postId, loggedInUserId, dataSource);
                    boolean bookmarked = isBookmarked(postId, loggedInUserId, dataSource);

                    posts.add(new Post(postId, content, postDate, poster,
                            heartCount, commentCount, hearted, bookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    /**
     * Inserts a new post for the given user, then links any #hashtags found.
     * Used by HomeController when the create post form is submitted.
     */
    public boolean createPost(String userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        final String insertPost
                = "INSERT INTO post (userId, content, postDate) VALUES (?, ?, NOW())";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertPost, 1)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, content.trim());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newPostId = keys.getLong(1);
                    linkHashtags(conn, newPostId, content);
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parses #hashtags from post content and inserts a row into the hashtag
     * table (postId, tag) for each one found. Your hashtag table schema:
     * hashtag(postId INT, tag VARCHAR(255))
     */
    private void linkHashtags(Connection conn, long postId, String content)
            throws SQLException {

        final String insertTag
                = "INSERT IGNORE INTO hashtag (postId, tag) VALUES (?, ?)";

        // Split on whitespace, check each word for a leading #
        for (String word : content.split("\\s+")) {
            String cleaned = word.replaceAll("[^#a-zA-Z0-9_]", "").toLowerCase();

            if (cleaned.startsWith("#") && cleaned.length() > 1) {
                try (PreparedStatement ps = conn.prepareStatement(insertTag)) {
                    ps.setLong(1, postId);
                    ps.setString(2, cleaned);
                    ps.executeUpdate();
                }
            }
        }
    }

    public boolean addComment(String postId, String userId, String content) {
        final String sql = "INSERT INTO comment (postId, userId, content, commentDate) "
                + "VALUES "
                + "( "
                + "?, ?, ?, NOW()"
                + " );";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId);
            pstmt.setString(2, userId);
            pstmt.setString(3, content);

            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

// ---------- For new UI part ------------

    public List<Post> getSortedHomePosts(String userId, String sortBy) {
        List<Post> posts = new ArrayList<>();

        String orderByClause;
        switch (sortBy.toLowerCase()) {
            case "newest":
                orderByClause = "p.postDate DESC";
                break;
            case "oldest":
                orderByClause = "p.postDate ASC";
                break;
            case "likes":
                orderByClause = "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.postId) DESC";
                break;
            case "comments":
                orderByClause = "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) DESC";
                break;
            default:
                orderByClause = "p.postDate DESC"; // Default to newest
        }

        final String sql
                = "SELECT p.postId postId, p.userId userId, p.content content, DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate, "
                + "u.firstName firstName, u.lastName lastName "
                + "FROM post p, user u "
                + "WHERE p.userId = u.userId "
                + "AND (p.userId = ? OR p.userId IN ( "
                + "SELECT followeeId FROM follow WHERE followerId = ? "
                + ")"
                + ") "
                + "ORDER BY " + orderByClause;

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String postId = rs.getString("postId");
                    boolean isHearted = isHearted(postId, userId, dataSource);
                    boolean isBookmarked = isBookmarked(postId, userId, dataSource);
                    posts.add(buildPost(rs, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
        return posts;
    }

        public List<Post> getAllSortedPosts(String userId, String sortBy) {
        List<Post> posts = new ArrayList<>();

        String orderByClause;
        if (sortBy == null) {
            orderByClause = "p.postDate DESC";
        } else {
            switch (sortBy.toLowerCase()) {
                case "newest":
                    orderByClause = "p.postDate DESC";
                    break;
                case "oldest":
                    orderByClause = "p.postDate ASC";
                    break;
                case "likes":
                    orderByClause = "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.postId) DESC";
                    break;
                case "comments":
                    orderByClause = "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) DESC";
                    break;
                default:
                    orderByClause = "p.postDate DESC"; // Default to newest
            }
        }

        final String sql
                = "SELECT p.postId postId, p.userId userId, p.content content, DATE_FORMAT(p.postDate, '%b %d, %Y, %I:%i %p') AS postDate, "
                + "u.firstName firstName, u.lastName lastName "
                + "FROM post p, user u "
                + "WHERE p.userId = u.userId "
                + "ORDER BY " + orderByClause;

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String postId = rs.getString("postId");
                    boolean isHearted = isHearted(postId, userId, dataSource);
                    boolean isBookmarked = isBookmarked(postId, userId, dataSource);
                    posts.add(buildPost(rs, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            System.out.println(sql);
            e.printStackTrace();
        }
        return posts;
    }
}
