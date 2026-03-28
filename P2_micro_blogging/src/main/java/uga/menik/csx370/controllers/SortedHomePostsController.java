/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.
 *
 *  *This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/sortedHomePosts")
public class SortedHomePostsController {

    private final PostService postService;
    private final UserService userService;

    @Autowired
    public SortedHomePostsController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * This function handles the /sortedposts URL.
     *
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "sortBy", required = true) String sortBy,
            @RequestParam(name = "error", required = false) String error) {

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("home_page");
        User user = userService.getLoggedInUser();

        String currentUserId = user.getUserId();
        List<Post> posts = postService.getSortedHomePosts(currentUserId, sortBy);
        if (posts == null) {
            String errorMessage = "Something went wrong.\n" + error;
            mv.addObject("errorMessage", errorMessage);
            return mv;
        } else if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        } else {
            mv.addObject("posts", posts);
        }

        // Pass data for maintaining sortBy selection
        mv.addObject("currentSortBy", sortBy);
        mv.addObject("isNewest", "newest".equals(sortBy));
        mv.addObject("isOldest", "oldest".equals(sortBy));
        mv.addObject("isLikes", "likes".equals(sortBy));
        mv.addObject("isComments", "comments".equals(sortBy));

        return mv;
    }

}
