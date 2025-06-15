# 📦 Confluence Drop-Down List Macro

## Description

**Confluence Drop-Down List** is a custom macro for Atlassian Confluence that provides an interactive drop-down menu. Users can select a value which is then stored per page. This macro is designed for managing statuses, ratings, or any other selectable values directly within a Confluence page.

### Features

- Customizable drop-down values (e.g., `Low`, `Medium`, `High` or `1`, `2`, `3`, `5`)
- Colored indicators corresponding to selected values
- Selected value is stored as a Content Property per page
- Unique key generation (automatically or manually defined)
- Multiple macro instances on the same page with independent state
- AJAX-based value update (no page reload)
- Compatible with `excel-macros`

---

## ⚙️ How It Works

1. A unique `propertyKey` is generated automatically using the macro context and its parameters. Alternatively, a custom key can be provided manually via the `key` parameter.

2. When the macro is rendered:
   - It checks for an existing value stored in the Confluence Content Property using the `propertyKey`.
   - If a value is found, it is pre-selected in the drop-down.
   - If no value is stored, the `default` parameter is used.

3. The UI displays a styled drop-down list:
   - Optional label is shown before the drop-down.
   - Values may be rendered with visual indicators (such as color dots or background shades).

4. When a user selects a value:
   - An AJAX POST request is sent to the servlet endpoint at  
     `/plugins/servlet/confluence-drop-down-list/save`.

5. The request includes the following payload:
   - `pageId`: ID of the current Confluence page
   - `propertyKey`: Unique key to identify this macro instance
   - `value`: The selected value

6. The servlet processes the request:
   - Validates the input
   - Stores the new value as a Content Property on the Confluence page
   - Responds to the client

7. Once the response is received, the page is automatically refreshed to reflect the new selection.

8. On future loads, the stored value is re-used, ensuring consistent state across sessions.

> This design ensures each macro instance can store and retrieve user-specific data scoped to the page, making it perfect for building interactive dashboards, decision matrices, or team feedback tools within Confluence.

## 🛰️ Save Servlet (POST API)

Your backend servlet should accept and handle the following POST parameters:

- `pageId` — the numeric ID of the current Confluence page.
- `value` — the selected value from the drop-down.
- `propertyKey` — a unique key to associate the value with a specific drop-down instance.

The servlet should:

1. Retrieve the page using the `pageId`.
2. Use the Content Property API (via `ContentEntityObject` or REST) to set or update the value.
3. Respond with a success status (e.g., HTTP 200).

## 📥 Download

You can download the latest version of the Confluence Drop-Down List plugin here:

👉 [Download Latest Release (.jar)](https://github.com/tyr1k/confluence-drop-down-list/releases/download/v1.1.1/drdwlist-1.0.0.jar)

