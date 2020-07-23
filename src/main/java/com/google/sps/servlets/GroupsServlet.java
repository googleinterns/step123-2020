package com.google.sps.servlets;

import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
    
    /**
     * Renders a HTML template with cards representing the various
     * groups that have already been created.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Hard coded data for now but will use data from datastore
        // as groups are added to datastore
        // Photo by Hybrid on Unsplash 
        // (https://unsplash.com/@artbyhybrid?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
        ImmutableMap<String, String> groupMap = ImmutableMap.of("groupName", "Black Lives Matter",
            "groupImage", "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=" + 
            "rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80",
            "groupDescription", "Group description would go here...");
        ImmutableList<ImmutableMap<String, String>> groups = ImmutableList.of(groupMap);
        
        // Each group has its own map which points to its info and all maps are passed into the template as a list
        // This will make it easier when groups are queried from Datastore
        ImmutableMap<String, ImmutableList<ImmutableMap<String, String>>> groupsData = 
            ImmutableMap.of(GROUPS_KEY, groups);

        String groupsPageHtml = getOutputString(GROUPS_SOY_FILE, GROUPS_PAGE_NAMESPACE, groupsData);
        response.getWriter().println(groupsPageHtml);
    }
}
