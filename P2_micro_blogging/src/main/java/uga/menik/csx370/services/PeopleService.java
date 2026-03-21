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

import uga.menik.csx370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {
    
    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.

        // Use the query result to create a list of followable users.
        // See UserService.java to see how to access rows and their attributes
        // from the query result.
        // Check the following createSampleFollowableUserList function to see 
        // how to create a list of FollowableUsers.


        final String sql = 
        "SELECT userId, firstName, lastName FROM user WHERE userId != ?";
        final String sqlIsFollowed = "SELECT True as followed " +
        "FROM follow " +
        "WHERE followerId = ? AND followeeId = ?";
        final String sqlDate = "SELECT DATE_FORMAT(postDate, '%M %d %Y %H:%i %p') as postDate from post WHERE userId = ? ORDER BY postDate DESC LIMIT 1;";

        List<FollowableUser> followableUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    PreparedStatement pstmtDate = conn.prepareStatement(sqlDate);
                    pstmtDate.setString(1, userId);

                    ResultSet postSet = pstmtDate.executeQuery();
                    String date = "Unknown";
                    if (postSet.next()) {
                        date = postSet.getString("postDate");
                    }

                    PreparedStatement pstmtFollow = conn.prepareStatement(sqlIsFollowed);
                    pstmtFollow.setString(1, userIdToExclude);
                    pstmtFollow.setString(2, userId);
                    
                    ResultSet followSet = pstmtFollow.executeQuery();
                    Boolean isFollowed = false;
                    if (followSet.next()) {
                        // Because a row will only be returned when the Poster's userId
                        // == followerId AND the commenter's user id == followeeId,
                        // if this condition returns true, we can assume isFollow is true
                        isFollowed = true; 
                    }
                    followableUsers.add(new FollowableUser(userId, firstName, lastName, isFollowed, date));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return followableUsers;
        // Replace the following line and return the list you created.
        // return Utility.createSampleFollowableUserList();
    }

}
