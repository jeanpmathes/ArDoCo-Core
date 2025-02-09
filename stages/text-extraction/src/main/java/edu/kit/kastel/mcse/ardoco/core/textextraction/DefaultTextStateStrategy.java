/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public abstract class DefaultTextStateStrategy implements TextStateStrategy {

    private TextStateImpl textState;

    public void setTextState(TextStateImpl textExtractionState) {
        textState = textExtractionState;
    }

    public TextStateImpl getTextState() {
        return textState;
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {
        for (NounMapping nounMappingToMerge : nounMappingsToMerge) {

            if (!textState.getNounMappings().contains(nounMappingToMerge)) {

                final NounMapping finalNounMappingToMerge = nounMappingToMerge;
                var fittingNounMappings = textState.getNounMappings().select(nm -> nm.getWords().containsAllIterable(finalNounMappingToMerge.getWords()));
                if (fittingNounMappings.isEmpty()) {
                    continue;
                } else if (fittingNounMappings.size() == 1) {
                    nounMappingToMerge = fittingNounMappings.get(0);
                } else {
                    throw new IllegalStateException();
                }
            }

            assert textState.getNounMappings().contains(nounMappingToMerge);

            var references = nounMapping.getReferenceWords().toList();
            references.addAllIterable(nounMappingToMerge.getReferenceWords());
            textState.mergeNounMappings(nounMapping, nounMappingToMerge, claimant, references.toImmutable());

            var mergedWords = SortedSets.mutable.empty();
            mergedWords.addAllIterable(nounMapping.getWords());
            mergedWords.addAllIterable(nounMappingToMerge.getWords());

            var mergedNounMapping = textState.getNounMappings().select(nm -> nm.getWords().toSortedSet().equals(mergedWords));

            assert (mergedNounMapping.size() == 1);

            nounMapping = mergedNounMapping.get(0);
        }

        return nounMapping;
    }

    protected final Confidence putAllConfidencesTogether(Confidence confidence, Confidence confidence1) {

        Confidence result = confidence.createCopy();
        result.addAllConfidences(confidence1);
        return result;
    }
}
