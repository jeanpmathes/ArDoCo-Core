package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.List;

public interface IRelation {

	/**
	 * Adds more end points to the relation. Checks if the instance is already
	 * contained.
	 *
	 * @param others list of other end points of this relation
	 */
	void addOtherInstances(List<? extends IInstance> others);

	/**
	 * Returns the end points of this relation as instances.
	 *
	 * @return list of connected instances by this relation
	 */
	List<? extends IInstance> getInstances();

	/**
	 * Returns the determiner of the relation
	 *
	 * @return the type of relation
	 */
	String getType();

	/**
	 * Returns the unique identifier of this relation.
	 *
	 * @return the uid of this relation
	 */
	String getUid();

	@Override
	String toString();

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);

}