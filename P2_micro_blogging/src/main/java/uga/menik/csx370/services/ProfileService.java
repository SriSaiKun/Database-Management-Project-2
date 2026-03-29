package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

// Spring boot annotation to mark this class as a service component. Deals with business logic/backend information related to user profiles.
@Service
public class ProfileService {

    // dataSource is needed to run SQL queries to get profile related information from the database.
    // the datasource was configured in the application.properties file. 
    // You can use it to run SQL queries to get profile related information from the database.
    private final DataSource dataSource;

    @Autowired
    public ProfileService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Selects all posts made by the specified user by joining the post and user tables on userId.
    // Retrieves the post details (postId, content, postDate) and the user details (userId, firstName, lastName).
    // Filters results to only include posts by the specified user, ordered most recent first.
    public List<Post> getPostsForUser(String userId, String loggedInUserId) {
        final String sql = "SELECT p.postId, p.content, DATE_FORMAT(p.postDate, '%b %d, %Y %h:%i %p') as postDate," +
                           "u.userId, u.firstName, u.lastName " +
                           "FROM post p JOIN user u ON p.userId = u.userId " +
                           "WHERE p.userId = ? " +
                           "ORDER BY p.postDate DESC";

        // Empty list to hold the posts that will be created in the below while
        List<Post> posts = new ArrayList<>();

        // Prepares a connection to the database and executes the SQL query to retrieve posts for the specified user.
        // PreparedStatement is used to execute parameterized SQL queries. It allows for setting parameters (in this case, the userId) in the query and helps prevent SQL injection attacks.
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            // Executes the query and processes the result set to create Post objects, which are then added to the list of posts.
            try (ResultSet rs = pstmt.executeQuery()) {
                // rs.next() moves the cursor to the next row of the result set. It returns true if there is a next row, and false if there are no more rows.
                // Inside the while loop, retrieve the values of the columns for the current row using rs.getString("columnName") and create a new Post object with those values. We then add the Post object to the list of posts.
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    User user = new User(userId, firstName, lastName);
                    int heartsCount = PostService.getHeartsCount(postId, dataSource);
                    int commentCount = PostService.getCommentsCount(postId, dataSource);
                    Boolean isHearted = PostService.isHearted(postId, loggedInUserId, dataSource);
                    Boolean isBookmarked = PostService.isBookmarked(postId, loggedInUserId, dataSource);
                    posts.add(new Post(postId, content, postDate, user, heartsCount, commentCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }
}
