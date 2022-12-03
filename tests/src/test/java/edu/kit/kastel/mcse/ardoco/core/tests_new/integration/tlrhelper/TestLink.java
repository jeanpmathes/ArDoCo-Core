/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper;

import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;

import java.util.Comparator;

/**
 * Represents a simple trace link by the id of the model and number of the sentence involved.
 */
public record TestLink(String modelId, int sentenceNr) implements Comparable<TestLink> {

    public TestLink(TraceLink traceLink) {
        this(traceLink.getModelElementUid(), traceLink.getSentenceNumber());
    }

    @Override
    public int compareTo(TestLink o) {
        return Comparator.comparing(TestLink::modelId).thenComparing(TestLink::sentenceNr).compare(this, o);
    }

}
