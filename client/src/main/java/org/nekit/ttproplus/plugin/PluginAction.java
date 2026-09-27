package org.nekit.ttproplus.plugin;

import android.content.Context;

/**
 * Interface representing a custom UI action/button registered by a plugin.
 */
public interface PluginAction {
    /**
     * Unique identifier for this action.
     */
    String getId();

    /**
     * User-visible title for the action button or menu entry.
     */
    String getTitle();

    /**
     * Called when the user clicks this action.
     */
    void onActionClick(Context context);
}
