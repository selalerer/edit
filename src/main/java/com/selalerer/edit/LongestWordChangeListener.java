package com.selalerer.edit;

public interface LongestWordChangeListener {
    /**
     * Called when the value of the longest word length has changed.
     * @param previousLongestWord The previous length of the longest word.
     * @param newLongestWord The new length of the longest word.
     */
    void longestWordUpdated(int previousLongestWord, int newLongestWord);
}
