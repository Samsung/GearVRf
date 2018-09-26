package org.gearvrf.widgetlib.widget.basic;

/**
 * Defines an interface for Checkable Widgets
 */
public interface Checkable {
    /**
     * Interface definition for a callback to be invoked when the Checkable's checked state is changed.
     */
    interface OnCheckChangedListener {
        /**
         * Called on changing the checked state
         * @param checkable checkable whose state has changed.
         * @param checked  The new checked state
         */
        void onCheckChanged(Checkable checkable, boolean checked);
    }

    /**
     * Add {@link OnCheckChangedListener listener}
     * @param listener
     * @return true if the listener has been successfully added
     */
    boolean addOnCheckChangedListener(OnCheckChangedListener listener);

    /**
     * Remove {@link OnCheckChangedListener listener}
     * @param listener
     * @return true if the listener has been successfully removed
     */
    boolean removeOnCheckChangedListener(OnCheckChangedListener listener);

    /**
     * Check if Checkable is checked or not
     * @return The current checked state
     */
    boolean isChecked();

    /**
     * Change the checked state of the Checkable
     * @param checked The new checked state
     */
    void setChecked(boolean checked);

    /**
     * Change the checked state of the Checkable to the inverse of its current state
     */
    void toggle();
}
