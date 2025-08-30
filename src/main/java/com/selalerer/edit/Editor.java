package com.selalerer.edit;

/***
 * Interface of editor.
 * @param <T> The type of the edited objects.
 */
public interface Editor<T> {

    /***
     * Edits an object and returns the edited copy.
     * @param in The object to edit.
     * @return the edited copy.
     */
    T edit(T in);
}
