/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.ArrayList;

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
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

    @Autowired
    public PostService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Post> getBookmarkedPosts(String currentUserId) {
        List<Post> posts = new ArrayList<>();

        final String sql = """
            SELECT p.postId, p.content, p.postDate,
                   u.userId, u.firstName, u.lastName
            FROM bookmark b
            JOIN post p ON b.postId = p.postId
            JOIN user u ON p.userId = u.userId
            WHERE b.userId = ?
            ORDER BY p.postDate DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT firstName, lastName " +
        "FROM user " +
        "WHERE userId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT COUNT(*) as NumComments " +
        "FROM heart " +
        "WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT COUNT(*) as NumComments " +
        "FROM comment " +
        "WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT * " +
        "FROM heart " +
        "WHERE postId = ? AND userId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT * " +
        "FROM bookmark " +
        "WHERE postId = ? AND userId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final String sql = "SELECT postId, userId, content," +
        "DATE_FORMAT(postDate, '%M %d %Y %H:%i %p') as postDate " +
        "FROM post " +
        "WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

        final String sql = 
            "SELECT commentId, userId, content, commentDate " +
            "FROM comment " + 
            "WHERE postId = ?" +
            "ORDER BY commentDate ASC" 
            ;

        final String sqlUser = "SELECT firstName, lastName " +
        "FROM user " +
        "WHERE userId = ?"
        ;
        final String sqlIsFollowed = "SELECT True as followed " +
        "FROM follow " +
        "WHERE followerId = ? AND followeeId = ?";
        final String sqlDate = "SELECT DATE_FORMAT(postDate, '%M %d %Y %H:%i %p') as postDate from post WHERE userId = ? ORDER BY postDate DESC LIMIT 1;";

        List<Comment> comments = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = rs.getString("commentId");
                    String userId = rs.getString("userId");
                    String content = rs.getString("content");
                    String commentDate = rs.getString("commentDate");

                    PreparedStatement pstmtUser = conn.prepareStatement(sqlUser);
                    pstmtUser.setString(1, userId);
                    ResultSet userSet = pstmtUser.executeQuery();
                    String firstName = "", lastName = "";
                    if (userSet.next()) {
                        firstName = userSet.getString("firstName");
                        lastName = userSet.getString("lastName");
                    }

                    PreparedStatement pstmtFollow = conn.prepareStatement(sqlIsFollowed);
                    pstmtFollow.setString(1, posterId);
                    pstmtFollow.setString(2, userId);
                    ResultSet followSet = pstmtFollow.executeQuery();
                    Boolean isFollowed = false;
                    if (followSet.next()) {
                        // Because a row will only be returned when the Poster's userId
                        // == followerId AND the commenter's user id == followeeId,
                        // if this condition returns true, we can assume isFollow is true
                        isFollowed = true; 
                    }
                    
                    PreparedStatement pstmtDate = conn.prepareStatement(sqlDate);
                    pstmtDate.setString(1, userId);
                    ResultSet dateSet = pstmtDate.executeQuery();
                    String date = "";
                    if (dateSet.next()) {
                        date = dateSet.getString("postDate");
                    }

                    FollowableUser user = new FollowableUser(userId, firstName, lastName, isFollowed, date);

                    comments.add(new Comment(commentId, content, commentDate, user));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return comments;
    }

    public List<Post> searchPostsByHashtags(List<String> hashtags) {
        List<Post> posts = new ArrayList<>();

        if (hashtags == null || hashtags.isEmpty()) {
            return posts;
        }

        String placeholders = String.join(",", Collections.nCopies(hashtags.size(), "?"));

        final String sql = String.format("""
            SELECT p.postId, p.content, p.postDate,
                   u.userId, u.firstName, u.lastName
            FROM post p
            JOIN user u ON p.userId = u.userId
            JOIN hashtag h ON p.postId = h.postId
            WHERE h.tag IN (%s)
            GROUP BY p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName
            HAVING COUNT(DISTINCT h.tag) = ?
            ORDER BY p.postDate DESC
        """, placeholders);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (String hashtag : hashtags) {
                pstmt.setString(index++, hashtag);
            }
            pstmt.setInt(index, hashtags.size());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(buildPost(rs, false, false));
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

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

		pstmt.setInt(1, Integer.parseInt(userId));
        	pstmt.setInt(2, Integer.parseInt(postId));
        	return pstmt.executeUpdate() == 1;

    	} catch (SQLException e) {
        	e.printStackTrace();
        	return false;
    	}
    }

    private Post buildPost(ResultSet rs, boolean isHearted, boolean isBookmarked) throws SQLException {
        User user = new User(
                rs.getString("userId"),
                rs.getString("firstName"),
                rs.getString("lastName")
        );

        return new Post(
                rs.getString("postId"),
                rs.getString("content"),
                formatTimestamp(rs.getTimestamp("postDate")),
                user,
                0,
                0,
                isHearted,
                isBookmarked
        );
    }

    private String formatTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(DISPLAY_DATE_FORMAT);
    }

}
