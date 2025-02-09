/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;

public class DependencyImpl {

    private final DependencyTag dependencyType;
    private final long wordId;

    public DependencyImpl(DependencyTag type, long wordId) {
        this.dependencyType = type;
        this.wordId = wordId;
    }

    public long getWordId() {
        return wordId;
    }

    public DependencyTag getDependencyTag() {
        return dependencyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DependencyImpl that))
            return false;
        return wordId == that.wordId && dependencyType == that.dependencyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, wordId);
    }
}
