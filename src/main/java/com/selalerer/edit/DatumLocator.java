package com.selalerer.edit;

import java.util.Optional;

/***
 * An interface for locators of data to edit.
 * @param <INPUT> The type of the input object to search in.
 * @param <LOCATION> The type of location is the searched objects.
 * @param <DATUM> The type of found datum.
 * @param <MATCHER> The type of matcher used to find matching datum.
 */
public interface DatumLocator<INPUT, LOCATION, DATUM, MATCHER> {

    /***
     * Result of findNext() method.
     * @param location The location where the datum was found
     * @param datum The found datum
     * @param matcher The matcher used to find this datum
     */
    record Result<LOCATION, DATUM, MATCHER>(LOCATION location, DATUM datum, MATCHER matcher) {
        @Override
        public String toString() {
            return "{ location: " + Utils.toString(location) +
                    ", datum: " + Utils.toString(datum) +
                    ", matcher: " + Utils.toString(matcher) + " }";
        }
    }

    /***
     * Finds the next location in the input that matches a matcher.
     * @param in The input.
     * @param fromLocation Location in the input to start the search at.
     * @return A result containing the matched location, the matcher that matched the found datum and the datum itself.
     */
    Optional<Result<LOCATION, DATUM, MATCHER>> findNext(INPUT in, LOCATION fromLocation);
}
