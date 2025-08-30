package com.selalerer.edit;

import java.util.Arrays;
import java.util.Optional;

public interface DatumLocator<INPUT, LOCATION, DATUM, MATCHER> {

    /**
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

    Optional<Result<LOCATION, DATUM, MATCHER>> findNext(INPUT in, LOCATION fromLocation);
}
