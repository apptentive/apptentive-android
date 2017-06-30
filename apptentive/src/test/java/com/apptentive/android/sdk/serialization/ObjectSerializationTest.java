package com.apptentive.android.sdk.serialization;

import org.junit.Before;
import org.junit.Test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ObjectSerializationTest {

	private File file;

	@Before
	public void setUp() throws IOException {
		file = File.createTempFile("dummy", ".bin");
		file.deleteOnExit();
	}

	@Test
	public void testSerialization() throws IOException {

		Dummy expected = new Dummy("Some value");
		ObjectSerialization.serialize(file, expected);
		Dummy actual = ObjectSerialization.deserialize(file, Dummy.class);
		assertEquals(expected, actual);
	}

	static class Dummy implements SerializableObject {

		private final String value;

		public Dummy(String value) {
			this.value = value;
		}

		public Dummy(DataInput in) throws IOException {
			value = in.readUTF();
		}

		@Override
		public void writeExternal(DataOutput out) throws IOException {
			out.writeUTF(value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Dummy dummy = (Dummy) o;

			return value.equals(dummy.value);

		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}
}