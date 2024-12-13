package com.teamtreehouse.blog;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import spark.ModelAndView;
import spark.TemplateEngine;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HandlebarsTemplateEngine extends TemplateEngine {
    private final Handlebars handlebars;

    public HandlebarsTemplateEngine() {
        this.handlebars = new Handlebars();
    }

    @Override
    public String render(ModelAndView modelAndView) {
        Map<String, Object> model = (Map<String, Object>) modelAndView.getModel();
        String templateName = modelAndView.getViewName();
        try {
            // Specify the correct path to load templates
            Template template = handlebars.compile("templates/" + templateName);
            return template.apply(model);
        } catch (IOException e) {
            throw new RuntimeException("Error rendering template " + templateName, e);
        }
    }
}

