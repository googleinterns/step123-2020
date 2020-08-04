package com.google.sps.utils;

import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;

public final class SoyRendererUtils {
    private static final ClassLoader CLASS_LOADER = SoyRendererUtils.class.getClassLoader();

    /**
     * Returns the output string for the response. In other words,
     * it sets up the soy template with the passed in data.
     */
    public static String getOutputString(String templateFile, String templateNamespace, 
        ImmutableMap templateData) {
        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File(CLASS_LOADER.getResource(templateFile).getFile()))
            .build();
        SoyTofu tofu = sfs.compileToTofu();

        return tofu.newRenderer(templateNamespace).setData(templateData).render();
    }
}
