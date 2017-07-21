package com.apptentive.android.sdk.serialization;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Only the identity of the class of an SerializableObject instance
 * is written in the serialization stream and it is the responsibility
 * of the class to save and restore the contents of its instances.
 * The writeExternal and a single arg constructor of the SerializableObject
 * interface are implemented by a class to give the class complete control
 * over the format and contents of the stream for an object and its
 * supertypes. These methods must explicitly coordinate with the supertype
 * to save its state.
 */
public interface SerializableObject {

	/**
	 * The object should have a public single arg constructor accepting
	 * DataInput to restore its contents by calling the methods
	 * of DataInput for primitive types. The constructor must read the
	 * values in the same sequence and with the same types as were written
	 * by writeExternal.
	 */
	/* SerializableObject(DataInput in) throws IOException; */

	/**
	 * The object implements the writeExternal method to save its contents
	 * by calling the methods of DataOutput for its primitive values.
	 */
	void writeExternal(DataOutput out) throws IOException;
}
