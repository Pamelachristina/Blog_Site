package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.BlogDao;
import com.teamtreehouse.blog.dao.InMemoryBlogDao;
import com.teamtreehouse.blog.model.BlogEntry;

import com.teamtreehouse.blog.model.Comment;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import static spark.Spark.*;

import java.util.*;

public class Main {
    private static BlogDao blogDao = new InMemoryBlogDao();

    public static void main(String[] args) {
        // Set up Handlebars template engine
        HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();

        staticFileLocation("/css");

        // Route for displaying the password page
        get("/password", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "password.hbs");  // Renders the password page
        }, engine);

// Route for handling password authentication
        post("/password", (req, res) -> {
            String enteredPassword = req.queryParams("password");
            String correctPassword = "mySecretPassword";

            System.out.println("Entered Password: " + enteredPassword);

            if (enteredPassword.equals(correctPassword)) {
                res.cookie("password", correctPassword); // Store password as a cookie
                String redirectUrl = req.session().attribute("redirectUrl");

                System.out.println("Redirect URL from session: " + redirectUrl);

                if (redirectUrl != null) {
                    res.redirect(redirectUrl);  // Redirect to the page saved in the session
                    return null;
                } else {
                    res.redirect("/");  // Default to homepage
                    return null;
                }
            } else {
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Incorrect password. Please try again.");
                return new ModelAndView(errorModel, "password.hbs");
            }
        }, engine);

// Route to display the Add Blog Entry page (admin)
        get("/admin", (req, res) -> {
            String correctPassword = "mySecretPassword";
            String passwordCookie = req.cookie("password");

            if (correctPassword.equals(passwordCookie)) {
                // Password is correct; render the admin page
                return new ModelAndView(new HashMap<>(), "edit.hbs");
            } else {
                // Password not valid, save redirect URL and go to password page
                req.session().attribute("redirectUrl", "/admin");
                System.out.println("Redirecting to password page for /admin.");
                res.redirect("/password");
                return null;
            }
        }, engine);

// New Route: Display the New Blog Entry page
        get("/new", (req, res) -> {
            String correctPassword = "mySecretPassword";
            String passwordCookie = req.cookie("password");

            if (correctPassword.equals(passwordCookie)) {
                return new ModelAndView(new HashMap<>(), "new.hbs"); // Renders the new blog page
            } else {
                req.session().attribute("redirectUrl", "/new");
                res.redirect("/password"); // Redirect to password page
                return null;
            }
        }, engine);


        // Route to handle the Add form submission (new blog entry)
        post("/add", (req, res) -> {
            String title = req.queryParams("title");
            String body = req.queryParams("body");

            // Clean the title for slug generation
            String slug = title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                    .replaceAll("\\s+", "-");      // Replace spaces with hyphens

            String createdAt = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date());
            BlogEntry entry = new BlogEntry(title, body, createdAt);
            blogDao.addEntry(entry);
            res.redirect("/");
            return null;
        });

        // Route to display the Edit Blog Entry page
        get("/entry/:slug/edit", (req, res) -> {
            String slug = req.params(":slug");
            System.out.println("Slug received: " + slug);  // Debugging: Verify slug

            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry != null) {
                Map<String, Object> model = new HashMap<>();
                model.put("slug", slug);           // Pass the slug to the template
                model.put("title", entry.getTitle());
                model.put("body", entry.getBody());
                return new ModelAndView(model, "edit.hbs");
            } else {
                res.status(404);
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Blog entry not found!");
                return new ModelAndView(errorModel, "error.hbs"); // Error template
            }
        }, engine);

        // Route to handle the Edit form submission
        post("/entry/:slug/edit", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry != null) {
                // Update the blog entry with the new data
                String updatedTitle = req.queryParams("title");
                String updatedBody = req.queryParams("body");

                // Recalculate the slug based on the updated title
                String newSlug = updatedTitle.toLowerCase()
                        .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                        .replaceAll("\\s+", "-");      // Replace spaces with hyphens

                // Update the entry
                entry.setTitle(updatedTitle);
                entry.setBody(updatedBody);
                entry.setSlug(newSlug);

                // Redirect to the detail page with the new slug
                res.redirect("/entry/" + newSlug);
                return null;
            } else {
                res.status(404);
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Blog entry not found!");
                return new ModelAndView(errorModel, "error.hbs");
            }
        });



        // Route to display a blog entry's details
        get("/entry/:slug", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry == null) {
                res.status(404);
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Blog entry not found!");
                return new ModelAndView(errorModel, "error.hbs");
            }

            Map<String, Object> model = new HashMap<>();
            model.put("title", entry.getTitle());
            model.put("slug", slug);
            model.put("body", entry.getBody());
            model.put("createdAt", entry.getCreatedAt());
            model.put("comments", entry.getComments()); // Pass comments to the template

            return new ModelAndView(model, "detail.hbs");
        }, engine);


        // Route to delete a blog entry
        post("/entry/:slug/delete", (req, res) -> {
            String slug = req.params(":slug");
            System.out.println("Deleting slug: " + slug); // Debugging: Verify slug

            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry != null) {
                blogDao.deleteEntry(entry);
                res.redirect("/");  // Redirect to homepage after deletion
            } else {
                res.status(404);
                return "Blog entry not found!";
            }
            return null;
        });

        post("/entry/:slug/comment", (req, res) -> {
            String slug = req.params(":slug");
            BlogEntry entry = blogDao.findEntryBySlug(slug);

            if (entry == null) {
                res.status(404);
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Blog entry not found!");
                return new ModelAndView(errorModel, "error.hbs");
            }

            // Retrieve form inputs
            String name = req.queryParams("name");
            String body = req.queryParams("body");

            // Validate input
            if (name == null || name.trim().isEmpty() || body == null || body.trim().isEmpty()) {
                Map<String, Object> errorModel = new HashMap<>();
                errorModel.put("errorMessage", "Name and Comment body are required.");
                errorModel.put("slug", slug);
                errorModel.put("title", entry.getTitle());
                errorModel.put("body", entry.getBody());
                errorModel.put("createdAt", entry.getCreatedAt());
                errorModel.put("comments", entry.getComments());
                return new ModelAndView(errorModel, "detail.hbs");
            }

            // Add the comment
            Comment comment = new Comment(name, body);
            entry.addComment(comment);

            // Redirect back to the blog entry detail page
            res.redirect("/entry/" + slug);
            return null;
        });



        // Route to display the homepage with all blog entries
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Blog Homepage");

            List<BlogEntry> blogEntries = blogDao.findAllEntries();
            model.put("blogEntries", blogEntries);

            return new ModelAndView(model, "index.hbs");
        }, engine);
    }
}
