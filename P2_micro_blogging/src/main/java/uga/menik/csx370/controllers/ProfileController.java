/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.services.ProfileService;
import uga.menik.csx370.services.UserService;

/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    // UserService is used to get the logged in user's ID.
    private final UserService userService;

    // ProfileService is used to get posts for a specific user from the database.
    private final ProfileService profileService;

    // Spring automatically injects UserService and ProfileService instances via @Autowired.
    @Autowired
    public ProfileController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId(), null);
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     * I tried matching the format in PeopleController.java as that was provided already
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId, @RequestParam(name = "error", required = false) String error) {
        System.out.println("User is attempting to view profile: " + userId);

        // posts_page is the HTML template that will display the posts.
        ModelAndView mv = new ModelAndView("posts_page");

        // Use ProfileService to get real posts for this user from the database.
        // Replaces the hardcoded Utility.createSamplePostsListWithoutComments() call.
        List<Post> posts = profileService.getPostsForUser(userId);
        mv.addObject("posts", posts);

        // Pass error message to template if one was provided in the URL.
        mv.addObject("errorMessage", error);

        // If the post list is empty, show a no content message.
        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        return mv;
    }
    
}
