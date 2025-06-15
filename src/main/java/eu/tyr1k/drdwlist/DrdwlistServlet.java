package eu.tyr1k.drdwlist;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.Modification;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DrdwlistServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pageId = req.getParameter("pageId");
        String value = req.getParameter("value");
        String propertyKey = req.getParameter("propertyKey");

        if (pageId == null || value == null || propertyKey == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters");
            return;
        }

        try {
            PageManager pageManager = ComponentLocator.getComponent(PageManager.class);
            ContentPropertyManager propertyManager = ComponentLocator.getComponent(ContentPropertyManager.class);
            ContentEntityManager contentEntityManager = ComponentLocator.getComponent(ContentEntityManager.class);

            ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();

            final Page page = pageManager.getPage(Long.parseLong(pageId));
            if (page == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Page not found");
                return;
            }

            String oldValue = propertyManager.getStringProperty(page, propertyKey);

            propertyManager.setStringProperty(page, propertyKey, value);

            String macro = "<ac:structured-macro ac:name=\"platform-value\">" +
                    "<ac:parameter ac:name=\"value\">" + escapeXml(value) + "</ac:parameter>" +
                    "</ac:structured-macro>";

            String body = page.getBodyAsString();

            if (body.contains("<ac:structured-macro ac:name=\"platform-value\">")) {
                body = body.replaceAll("(?s)<ac:structured-macro ac:name=\"platform-value\">.*?</ac:structured-macro>", macro);
            } else {
                body += "<p style=\"display:none\">" + macro + "</p>";
            }

            page.setBodyAsString(body);
            page.setVersion(page.getVersion() + 1);

            page.setVersionComment("Modified value in dropdown '" + propertyKey + "' from '" +
                    (oldValue != null ? oldValue : "null") + "' to '" + value + "'");

            Modification<Page> modification = new Modification<Page>() {
                public void modify(Page p) {
                    p.setLastModifier(currentUser);
                }
            };

            contentEntityManager.saveNewVersion(page, modification);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("ok");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}

