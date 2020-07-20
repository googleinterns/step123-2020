package com.google.sps.utils;

import java.io.File;
import com.google.common.collect.ImmutableMap;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;

public final class SoyRendererUtils {
    private static final ClassLoader CLASS_LOADER = SoyRendererUtils.class.getClassLoader();

    private SoyRendererUtils() {
    }

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
