/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Needed for

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
        final String sql = "SELECT * " +
        "FROM heart " +
        "WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                int numRows = -1;
                do {
                    numRows += 1;
                } while (rs.next());
                
                return numRows;
            }
        } catch (SQLException e) {
                e.printStackTrace();
        }
        return 0;
    }

    public static int getCommentsCount(String postId, DataSource dataSource) {
        final String sql = "SELECT * " +
        "FROM comment " +
        "WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since postId is unique
                int numRows = -1;
                do {
                    numRows += 1;
                } while (rs.next());
                
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
        // Replace the following line and return the list you created.
        // return Utility.createSampleFollowableUserList();
    }

}
