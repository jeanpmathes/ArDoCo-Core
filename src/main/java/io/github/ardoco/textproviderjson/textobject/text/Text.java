/* Licensed under MIT 2021-2022. */
package io.github.ardoco.textproviderjson.textobject.text;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * This interface defines the representation of a text.
 */
public interface Text {

    /**
     * Gets the length of the text (amount of words).
     *
     * @return the length
     */
    default int getLength() {
        return words().size();
    }

    /**
     * Gets all words of the text (ordered).
     *
     * @return the words
     */
    ImmutableList<Word> words();

    /**
     * Returns the sentences of the text, ordered by appearance.
     *
     * @return the sentences of the text, ordered by appearance.
     */
    ImmutableList<Sentence> getSentences();

    /**
     * sets the sentences if the text
     *
     * @param sentences the sentences ´
     */
    void setSentences(ImmutableList<Sentence> sentences);
}
