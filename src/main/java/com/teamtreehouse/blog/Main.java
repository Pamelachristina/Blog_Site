package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.BlogDao;
import com.teamtreehouse.blog.dao.InMemoryBlogDao;
import com.teamtreehouse.blog.model.BlogEntry;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import static spark.Spark.*;

import java.util.*;

public class Main {
    private static BlogDao blogDao = new InMemoryBlogDao();

    public static void main(String[] args) {
        // Set up Handlebars template engine
        HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();

        staticFileLocation("/public");

        // Route for displaying the password page
        get("/password", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "password.hbs");  // Renders the password page
        }, engine);

        // Route for handling password authentication
        post("/password", (req, res) -> {
            String enteredPassword = req.queryParams("password");
            String correctPassword = "yourSecretPassword";

            // Debugging: print the entered password to check it's being sent
            System.out.println("Entered Password: " + enteredPassword);

            if (enteredPassword.equals(correctPassword)) {
                // If password is correct, redirect to the original page (Add/Edit)
                String redirectUrl = req.session().attribute("redirectUrl");

                // Debugging: print the redirect URL to check if it's being set correctly
                System.out.println("Redirect URL from session: " + redirectUrl);

                if (redirectUrl != null) {
                    res.redirect(redirectUrl);  // Redirect to the correct page after password validation
                    return null;
                } else {
                    // If redirectUrl is not set, redirect to the homepage
                    System.out.println("No redirect URL set, redirecting to homepage.");
                    res.redirect("/");
                    return null;
                }
            } else {
                // If password is incorrect, show the password page again with an error message
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Incorrect password. Please try again.");
                return new ModelAndView(errorModel, "password.hbs");  // Renders the password page with error message
            }
        });

        // Route to display the Add Blog Entry page (admin)
        get("/admin", (req, res) -> {
            req.session().attribute("redirectUrl", "/admin");  // Store the URL trying to access in the session
            // Debugging: print that we're redirecting to password page
            System.out.println("Redirecting to password page for /admin.");
            res.redirect("/password");  // Redirect to the password page
            return null;
        });

        // Route to handle the Add form submission (admin)
        post("/add", (req, res) -> {
            String title = req.queryParams("title");
            String body = req.queryParams("body");
            String createdAt = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date());
            BlogEntry entry = new BlogEntry(title, body, createdAt);
            blogDao.addEntry(entry);  // Add the new blog entry using the DAO
            res.redirect("/");
            return null;
        });

        // Route to display the Edit Blog Entry page (admin)
        get("/entry/:slug/edit", (req, res) -> {
            req.session().attribute("redirectUrl", "/entry/" + req.params(":slug") + "/edit");  // Store the URL
            // Debugging: print that we're redirecting to password page
            System.out.println("Redirecting to password page for /entry/:slug/edit.");
            res.redirect("/password");  // Redirect to the password page
            return null;
        });

        // Route to handle the Edit form submission
        post("/entry/:slug/edit", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry != null) {
                entry.setTitle(req.queryParams("title"));
                entry.setBody(req.queryParams("body"));
                res.redirect("/entry/" + slug);  // Redirect to the updated blog entry page
            } else {
                res.status(404);
                return "Blog entry not found!";
            }
            return null;
        });

        // Route to display a blog entry's details
        get("/entry/:slug", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry == null) {
                res.status(404);
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Blog entry not found!");
                return new ModelAndView(errorModel, "error.hbs");  // Render error page
            }

            Map<String, Object> model = new HashMap<>();
            model.put("title", entry.getTitle());
            model.put("slug", slug);
            model.put("body", entry.getBody());
            model.put("createdAt", entry.getCreatedAt());
            model.put("comments", entry.getComments());

            return new ModelAndView(model, "detail.hbs");  // Render detail page
        }, engine);

        // Route to delete a blog entry
        post("/entry/:slug/delete", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry != null) {
                blogDao.deleteEntry(entry);  // Delete the entry from the DAO
                res.redirect("/");  // Redirect to the homepage after deletion
            } else {
                res.status(404);
                return "Blog entry not found!";
            }
            return null;
        });

        // Route to display the homepage with all blog entries
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Blog Homepage");

            List<BlogEntry> blogEntries = blogDao.findAllEntries();
            model.put("blogEntries", blogEntries);

            return new ModelAndView(model, "index.hbs");  // Renders 'index.hbs' template
        }, engine);
    }
}
