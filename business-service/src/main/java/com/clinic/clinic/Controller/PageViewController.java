package com.clinic.clinic.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the server-rendered navigation UI for browsing paginated records.
 * This is a plain MVC controller (not a REST controller): it returns a view
 * name resolved by Thymeleaf, while the data itself is fetched client-side
 * from the role-protected REST endpoints using a JWT.
 */
@Controller
public class PageViewController {

    @GetMapping("/ui/records")
    public String records() {
        return "records";
    }
}
