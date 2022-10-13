/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.Locale;

/**
 * This interface extends {@link EvaluationResults} to capture the metrics Accuracy and Phi Coefficient. These metrics are different from the ones of
 * {@link EvaluationResults} as they need the number of True Negatives.
 */
public interface ExtendedEvaluationResults extends EvaluationResults {

    /**
     * Returns the Phi Coefficient (also known as mean square contingency coefficient (MCC)) based on the true positives, false positives, false negatives, and
     * true negatives in the results.
     * The return value lies between -1 and +1. -1 show perfect disagreement, +1 shows perfect agreement and 0 indicates no relationship.
     * Therefore, good values should be close to +1.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Phi_coefficient">Wikipedia: Phi coefficient</a>
     *
     * @return the value for Phi Coefficient (or MCC)
     */
    double getPhiCoefficient();

    /**
     * Returns the accuracy based on the true positives, false positives, false negatives, and true negatives in this result.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Accuracy_and_precision">Wikipedia: Accuracy and Precision</a>
     * @return the accuracy
     */
    double getAccuracy();

    /**
     * Calculates the specificity, also known as selectivity or true negative rate, based on the number of true negatives and false positives.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Sensitivity_and_specificity">Wikipedia: Sensitivity and specificity</a>
     * @return the specificity
     */
    double getSpecificity();

    @Override
    default String getResultString() {
        String output = String.format(Locale.ENGLISH, "\tPrecision:%7.3f%n\tRecall:%10.3f%n\tF1:%14.3f", getPrecision(), getRecall(), getF1());
        output += String.format(Locale.ENGLISH, "%n\tAccuracy:%8.3f%n\tPhi Coef.:%7.3f", getAccuracy(), getPhiCoefficient());
        return output;
    }

    @Override
    default String getResultStringWithExpected(ExpectedResults expectedResults) {
        String output = String.format(Locale.ENGLISH,
                "\tPrecision:%7.3f (min. expected: %.3f)%n\tRecall:%10.3f (min. expected: %.3f)%n\tF1:%14.3f (min. expected: %.3f)", getPrecision(),
                expectedResults.precision(), getRecall(), expectedResults.recall(), getF1(), expectedResults.f1());
        output += String.format(Locale.ENGLISH,
                "%n\tAccuracy:%8.3f (min. expected: %.3f)%n\tPhi Coef.:%7.3f (min. expected: %.3f)%n\tSpecificity:%5.3f (min. expected: %.3f)", getAccuracy(),
                expectedResults.accuracy(), getPhiCoefficient(), expectedResults.phiCoefficient(), getSpecificity(), expectedResults.specificity());
        return output;
    }

}
