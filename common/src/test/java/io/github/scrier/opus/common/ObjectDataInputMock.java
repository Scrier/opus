/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteOrder;
import java.nio.file.Path;

import com.hazelcast.nio.ObjectDataInput;

public class ObjectDataInputMock implements ObjectDataInput {
	
	private ObjectInputStream stream = null;
	private InputStream is = null;
	private Path tempFile = null;
	
	public ObjectDataInputMock(Path file) throws IOException {
		tempFile = file;
		is = new FileInputStream(tempFile.toFile());
		stream = new ObjectInputStream(is);
	}
	
	public void close() throws IOException {
		stream.close();
		is.close();
	}
	
	public boolean remove() {
		return tempFile.toFile().delete();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		stream.read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		stream.read(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return stream.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return stream.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return stream.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return stream.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return stream.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return stream.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return stream.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return stream.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return stream.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return stream.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return stream.readDouble();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String readLine() throws IOException {
		return stream.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return stream.readUTF();
	}

	@Override
	public ByteOrder getByteOrder() {
		return ByteOrder.BIG_ENDIAN;
	}

	@Override
	public ClassLoader getClassLoader() {
		return null;
	}

	@Override
	public char[] readCharArray() throws IOException {
		char[] retValue = new char[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readChar();
		}
		return retValue;
	}

	@Override
	public double[] readDoubleArray() throws IOException {
		double[] retValue = new double[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readDouble();
		}
		return retValue;
	}

	@Override
	public float[] readFloatArray() throws IOException {
		float[] retValue = new float[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readFloat();
		}
		return retValue;
	}

	@Override
	public int[] readIntArray() throws IOException {
		int[] retValue = new int[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readInt();
		}
		return retValue;
	}

	@Override
	public long[] readLongArray() throws IOException {
		long[] retValue = new long[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readLong();
		}
		return retValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readObject() throws IOException {
		try {
			return (T)stream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public short[] readShortArray() throws IOException {
		short[] retValue = new short[stream.readInt()];
		for( int i = 0; i < retValue.length; i++ ) {
			retValue[i] = stream.readShort();
		}
		return retValue;
	}

	

}
