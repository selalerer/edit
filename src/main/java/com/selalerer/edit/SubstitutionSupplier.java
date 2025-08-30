package com.selalerer.edit;

import java.util.Optional;

public interface SubstitutionSupplier<MATCHER, DATUM> {

    /***
     * Gets a substitution to be used by the editor when creating the edited copy.
     * @param matcher The matcher that was used to find the datum
     * @param current The found datum
     * @return The replacement
     */
    Optional<DATUM> getSubstitution(MATCHER matcher, DATUM current);
}
