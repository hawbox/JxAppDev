package com.jersey.example;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

@Path("hello")
public class HelloWorld {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getMessage() {
        return "Hello World 2";
    }
}
