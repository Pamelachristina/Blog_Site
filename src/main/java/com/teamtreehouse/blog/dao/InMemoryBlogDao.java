package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;

import java.util.ArrayList;
import java.util.List;

public class InMemoryBlogDao implements BlogDao {
    private List<BlogEntry> blogEntries = new ArrayList<>();

    // Constructor: Add some initial blog entries for testing
    public InMemoryBlogDao() {
        // Add test entries
        blogEntries.add(new BlogEntry("The best day I’ve ever had", "This is my story about the best day ever.", "October 1, 2024 at 1:00"));
        blogEntries.add(new BlogEntry("The absolute worst day I’ve ever had", "This is my story about the worst day ever.", "October 1, 2024 at 1:00"));
        blogEntries.add(new BlogEntry("That time at the mall", "Let me tell you about this interesting time at the mall.", "October 1, 2024 at 1:00"));
        blogEntries.add(new BlogEntry("Dude, where’s my car", "I lost my car at the mall. Here's what happened.", "October 1, 2024 at 1:00"));

        // Generate slugs for the entries
        for (BlogEntry entry : blogEntries) {
            entry.setSlug(entry.getTitle().toLowerCase().replace(" ", "-"));
        }
    }


    @Override
    public boolean addEntry(BlogEntry blogEntry) {
        // Generate the slug if it's not set
        if (blogEntry.getSlug() == null || blogEntry.getSlug().isEmpty()) {
            blogEntry.setSlug(blogEntry.getTitle().toLowerCase().replace(" ", "-"));
        }
        return blogEntries.add(blogEntry);
    }

    @Override
    public List<BlogEntry> findAllEntries() {
        return blogEntries;
    }

    @Override
    public BlogEntry findEntryBySlug(String slug) {
        // Look for the entry by slug
        for (BlogEntry entry : blogEntries) {
            if (entry.getSlug().equals(slug)) {
                return entry;
            }
        }
        return null;
    }

    // Delete a blog entry
    @Override
    public boolean deleteEntry(BlogEntry blogEntry) {
        return blogEntries.remove(blogEntry);
    }
}


