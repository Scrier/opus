package io.github.scrier.puqe.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import com.hazelcast.nio.ObjectDataOutput;

/**
 * Mock class to check serialize and unSerialize of hazelcast objects.
 * @author Andreas Joelsson
 */
public class ObjectDataOutputMock implements ObjectDataOutput {
	
	private Path tempFile = null;
	private OutputStream out = null;
	private ObjectOutputStream stream = null;
	
	public ObjectDataOutputMock() throws IOException {
		tempFile = Files.createTempFile("test", "file");
		out = new FileOutputStream(tempFile.toFile());
		stream = new ObjectOutputStream(out);
	}
	
	public Path getTempFile() {
		return tempFile;
	}
	
	public void close() throws IOException {
		stream.close();
		out.close();
	}

	@Override
	public void write(int b) throws IOException {
		stream.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		stream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		stream.write(b,off,len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		stream.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		stream.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		stream.writeShort(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		stream.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		stream.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		stream.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		stream.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		stream.writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		stream.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		stream.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		stream.writeUTF(s);
	}

	@Override
	public ByteOrder getByteOrder() {
		return ByteOrder.BIG_ENDIAN;
	}

	@Override
	public byte[] toByteArray() {
		return stream.toString().getBytes();
	}

	@Override
	public void writeCharArray(char[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeChar(arg0[i]);
		}
	}

	@Override
	public void writeDoubleArray(double[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeDouble(arg0[i]);
		}
	}

	@Override
	public void writeFloatArray(float[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeFloat(arg0[i]);
		}
	}

	@Override
	public void writeIntArray(int[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeInt(arg0[i]);
		}
	}

	@Override
	public void writeLongArray(long[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeLong(arg0[i]);
		}
	}

	@Override
	public void writeObject(Object arg0) throws IOException {
		stream.writeObject(arg0);
	}

	@Override
	public void writeShortArray(short[] arg0) throws IOException {
		stream.writeInt(arg0.length);
		for( int i = 0; i < arg0.length; i++ ) {
			stream.writeShort(arg0[i]);
		}
	}

}
