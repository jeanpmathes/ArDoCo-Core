/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

public interface Phrase {
    int getSentenceNo();

    String getText();

    PhraseType getPhraseType();

    ImmutableList<Word> getContainedWords();

    ImmutableList<Phrase> getSubPhrases();

    boolean isSuperPhraseOf(Phrase other);

    boolean isSubPhraseOf(Phrase other);

    ImmutableSortedMap<Word, Integer> getPhraseVector();
}
