package com.hardhand.wedder.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.hardhand.wedder.filter.DateRequired;

@Path("/filtered")
public class FilteredResource {

    @GET
    @DateRequired
    @Path("hello")
    public String sayHello() {
        return "hello";
    }
}
