package com.google.sps.data;
 
import com.google.common.collect.ImmutableMap;
 
/**
 * This class represents the data needed to send a request
 * to the Perspective API and will be used to turn the data
 * into a JSON formatted string.
 */
public class PerspectiveRequest {
    private static final String TEXT_KEY = "text";
    private ImmutableMap<String, String> comment;
    private ImmutableMap<String, ImmutableMap<String, String>> requestedAttributes;
 
    public PerspectiveRequest(String text, String attribute) {
        this.comment = ImmutableMap.of(TEXT_KEY, text);
        // Each attribute maps to a configuration object. No configurations 
        // are needed, therefore an empty map.
        this.requestedAttributes = ImmutableMap.of(attribute, ImmutableMap.of());
    } 
}
