package com.google.sps.servlets;
 
import com.google.common.collect.ImmutableMap;
 
/**
 * This class represents the data needed to send a request
 * to the Perspective API and will be used to turn the data
 * into a JSON formatted string.
 */
public class PerspectiveRequest {
    private ImmutableMap<String, String> comment;
    private ImmutableMap<String, ImmutableMap<String, String>> requestedAttributes;
 
    public PerspectiveRequest(String text) {
        this.comment = ImmutableMap.of("text", text);
        this.requestedAttributes = ImmutableMap.of("TOXICITY", ImmutableMap.of());
    } 
}
