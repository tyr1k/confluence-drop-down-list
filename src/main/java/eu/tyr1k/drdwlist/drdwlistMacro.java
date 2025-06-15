package eu.tyr1k.drdwlist;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.component.ComponentLocator;

import java.util.*;

public class DrdwlistMacro implements Macro {

    private static final String PROPERTY_KEY_PREFIX = "drdwlist-stable-";

    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException {
        ContentEntityObject entity = context.getEntity();

        String valuesParam = parameters.getOrDefault("values", "1,2,3");
        String[] values = valuesParam.split(",");
        String defaultValue = parameters.getOrDefault("default", "1");
        String label = parameters.getOrDefault("label", " ");

        Long pageId = entity != null ? entity.getId() : null;
        String propertyKey = parameters.get("propertyKey");

        if (propertyKey == null || propertyKey.trim().isEmpty()) {
            ContentPropertyManager propertyManager = ComponentLocator.getComponent(ContentPropertyManager.class);
            String stableKey = PROPERTY_KEY_PREFIX + pageId + "-" + stableParamsKey(parameters);
            Object uuidProp = (entity != null) ? propertyManager.getStringProperty(entity, stableKey) : null;
            String uuid;
            if (uuidProp != null && !uuidProp.toString().isEmpty()) {
                uuid = uuidProp.toString();
            } else {
                uuid = UUID.randomUUID().toString();
                if (entity != null) {
                    propertyManager.setStringProperty(entity, stableKey, uuid);
                }
            }
            propertyKey = "drdwlist-" + uuid;
        }

        ContentPropertyManager propertyManager = ComponentLocator.getComponent(ContentPropertyManager.class);
        String current = defaultValue;
        if (entity != null && propertyKey != null) {
            Object prop = propertyManager.getStringProperty(entity, propertyKey);
            if (prop != null && !prop.toString().isEmpty()) {
                current = prop.toString();
            }
        }

        PermissionManager permissionManager = ComponentLocator.getComponent(PermissionManager.class);
        ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
        boolean canEdit = entity != null && currentUser != null &&
            permissionManager.hasPermission(currentUser, Permission.EDIT, entity);

        String uniqueId = UUID.randomUUID().toString().replace("-", "");

        StringBuilder out = new StringBuilder();
        out.append("<style>");
        out.append(".dpd-dropdown-").append(uniqueId).append(" { position:relative; display:inline-block; min-width:80px; }");
        out.append(".dpd-current-").append(uniqueId).append(" { border:1px solid #ccc; padding:4px 16px 4px 28px; border-radius:6px; cursor:pointer; position:relative; min-width:60px; user-select:none; background:#fff; }");
        out.append(".dpd-list-").append(uniqueId).append(" { display:none; position:absolute; left:0; background:white; border:1px solid #ccc; z-index:1000; min-width:60px; border-radius:8px; box-shadow:0 2px 12px #0001; }");
        out.append(".dpd-item-").append(uniqueId).append(" { padding:4px 24px 4px 28px; cursor:pointer; position:relative; transition:background 0.2s; }");
        out.append(".dpd-item-").append(uniqueId).append(":hover { background:#f0f7ff; }");
        out.append(".dpd-color-dot-").append(uniqueId).append(" { position:absolute; left:8px; width:14px; height:14px; border-radius:50%; display:inline-block; border:1px solid #ccc; top:5px; }");
        out.append("</style>");

        out.append("<form id=\"drdwlist-form-").append(uniqueId).append("\">");
        out.append("<label for=\"drdwlist-current-").append(uniqueId).append("\">").append(label).append("</label>");

        out.append("<span class=\"drdwlist-hidden-value\" id=\"dpd-hidden-").append(uniqueId).append("\" style=\"display:none\">")
            .append(escapeHtml(current)).append("</span>");

        out.append("<div class=\"dpd-dropdown-").append(uniqueId).append("\" id=\"dropdown-").append(uniqueId).append("\">");
        out.append("  <div class=\"dpd-current-").append(uniqueId).append("\" id=\"dpd-current-").append(uniqueId).append("\" tabindex=\"0\">");
        out.append("    <span class=\"dpd-color-dot-").append(uniqueId).append("\" id=\"dpd-color-dot-").append(uniqueId).append("\"></span>");
        out.append("    <span id=\"dpd-current-text-").append(uniqueId).append("\">").append(escapeHtml(current)).append("</span>");
        if (canEdit) out.append(" <span style='float:right; font-size:10px;'>&#9660;</span>");
        out.append("  </div>");

        Map<String, String> colorMap = new HashMap<>();
        try {
            double[] numericValues = Arrays.stream(values).mapToDouble(v -> Double.parseDouble(v.trim())).toArray();
            double min = Arrays.stream(numericValues).min().orElse(0);
            double max = Arrays.stream(numericValues).max().orElse(1);
            double range = max - min == 0 ? 1 : max - min;

            for (int i = 0; i < values.length; i++) {
                String val = values[i].trim();
                double ratio = (numericValues[i] - min) / range;
                int lightness = (int)(85 - ratio * 45);
                String color = "hsl(135, 60%, " + lightness + "%)";
                colorMap.put(val, color);
            }
        } catch (NumberFormatException ignored) {
            for (String val : values) {
                colorMap.put(val.trim(), "#ccc");
            }
        }

        if (canEdit) {
            out.append("  <div class=\"dpd-list-").append(uniqueId).append("\" id=\"dpd-list-").append(uniqueId).append("\">");
            for (String val : values) {
                val = val.trim();
                String color = colorMap.getOrDefault(val, "#ccc");
                out.append("    <div class=\"dpd-item-").append(uniqueId)
                    .append("\" data-value=\"").append(escapeHtml(val))
                    .append("\" data-color=\"").append(color)
                    .append("\" style=\"position:relative;\">");
                out.append("<span class=\"dpd-color-dot-").append(uniqueId)
                    .append("\" style=\"background:").append(color).append(";\"></span>");
                out.append(escapeHtml(val));
                out.append("</div>");
            }
            out.append("  </div>");
        }

        out.append("</div>");

        if (canEdit) {
            out.append("<input type=\"hidden\" id=\"drdwlist-propertyKey-").append(uniqueId).append("\" value=\"")
                .append(escapeHtml(propertyKey)).append("\" />");
        }

        out.append("</form>");

        out.append("<script>");
        out.append("var dpdCurrent_" + uniqueId + " = document.getElementById('dpd-current-" + uniqueId + "');");
        out.append("var dpdList_" + uniqueId + " = document.getElementById('dpd-list-" + uniqueId + "');");
        out.append("var dpdCurrentText_" + uniqueId + " = document.getElementById('dpd-current-text-" + uniqueId + "');");
        out.append("var dpdColorDot_" + uniqueId + " = document.getElementById('dpd-color-dot-" + uniqueId + "');");
        out.append("var dpdDropdown_" + uniqueId + " = document.getElementById('dropdown-" + uniqueId + "');");
        out.append("var propertyKey_" + uniqueId + " = document.getElementById('drdwlist-propertyKey-" + uniqueId + "').value;");
        out.append("dpdCurrent_" + uniqueId + ".onclick = function(){ ");
        out.append("  dpdList_" + uniqueId + ".style.display = (dpdList_" + uniqueId + ".style.display=='block'?'none':'block');");
        out.append("};");
        out.append("document.addEventListener('click', function(e){ ");
        out.append("if(!dpdDropdown_" + uniqueId + ".contains(e.target)) dpdList_" + uniqueId + ".style.display='none';");
        out.append("});");
        out.append("Array.from(dpdList_" + uniqueId + ".children).forEach(function(item){");
        out.append("  item.onclick = function(){");
        out.append("    var val = item.getAttribute('data-value');");
        out.append("    var color = item.getAttribute('data-color');");
        out.append("    dpdCurrentText_" + uniqueId + ".textContent = val;");
        out.append("    dpdColorDot_" + uniqueId + ".style.background = color;");
        out.append("    var hiddenElem = document.getElementById('dpd-hidden-" + uniqueId + "');");
        out.append("    if (hiddenElem) hiddenElem.textContent = val;");
        out.append("    dpdList_" + uniqueId + ".style.display='none';");
        out.append("    fetch('/plugins/servlet/drdwlist/save?pageId=" + pageId + "&value='+encodeURIComponent(val)+'&propertyKey='+encodeURIComponent(propertyKey_" + uniqueId + "),{method:'POST',headers:{'X-Atlassian-Token':'no-check'}}).then(function(){location.reload();});");
        out.append("  };");
        out.append("});");

        String initialColor = colorMap.getOrDefault(current, "#ccc");
        out.append("dpdColorDot_" + uniqueId + ".style.background = '").append(initialColor).append("';");

        out.append("dpdCurrent_" + uniqueId + ".onkeydown = function(e){ if(e.key==='Escape'||e.key==='Tab'){dpdList_" + uniqueId + ".style.display='none';} };");
        out.append("</script>");

        return out.toString();
    }

    private String stableParamsKey(Map<String, String> parameters) {
        return parameters.entrySet().stream()
            .filter(e -> !"propertyKey".equals(e.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + escapeHtml(e.getValue()))
            .reduce((a, b) -> a + ";" + b).orElse("");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.BLOCK;
    }
}


