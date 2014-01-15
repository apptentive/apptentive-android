package com.apptentive.android.sdk.module.engagement.logic;

/**
 * @author Sky Kelsey
 */
public class Condition<T extends Comparable> {

	public Predicate.Operation operation;
	public T operand;

	public Condition(Predicate.Operation operation, T operand) {
		this.operation = operation;
		this.operand = operand;
	}

}
