package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

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
                    posts.add(buildPost(rs, false, true));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
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
        final String sql = "INSERT INTO bookmark (userId, postId) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(postId));
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean removeBookmark(String userId, String postId) {
        final String sql = "DELETE FROM bookmark WHERE userId = ? AND postId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(postId));
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
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
