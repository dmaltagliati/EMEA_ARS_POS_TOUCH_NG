package com.ncr;

import java.io.*;
import java.util.*;

/*******************************************************************
 *
 * Access to local data files (record data is based on 8-bit oem codepages) (record size is fixed) (record separator is
 * hex 0d0A)
 *
 *******************************************************************/
public class DatIo extends LinIo {
	/**
	 * size of access key or record header (depending on file type)
	 **/
	public int fixSize;
	/**
	 * byte array holding oem data after read
	 **/
	public byte record[];
	/**
	 * abstract path/file name
	 **/
	public File pathfile;
	/**
	 * reference to underlying io service object
	 **/
	public RandomAccessFile file;

	public static File local(String path, String name) {

		File f = new File(path, name);

		return new File(f.getAbsolutePath());
	}

	/***************************************************************************
	 * Constructor
	 *
	 * @param id
	 *            String (3 chars) used as unique identification
	 * @param fixSize
	 *            size of access key or record header
	 * @param recSize
	 *            record size in bytes including separators CR/LF
	 ***************************************************************************/
	public DatIo(String id, int fixSize, int recSize) {
		super(id, 0, recSize - 2);
		this.fixSize = fixSize;
		record = new byte[recSize];
	}

	/***************************************************************************
	 * read data record and prepare subsequent parse functions (unicode string in parse buffer pb, index to zero)
	 *
	 * @param rec
	 *            relative record number (1 <= rec <= records in file)
	 * @return record size - 2 (0 = end of file)
	 ***************************************************************************/
	public int read(int rec) {
		if (file == null)
			return 0;
		while (true) {
			try {
				int len = record.length;
				file.seek((rec - 1l) * len);
				file.readFully(record);
				if (record[--len] == 0x0a)
					if (record[--len] == 0x0d) {
						pb = new String(record, index = 0, len, oem);
						return len;
					}
				error(new IOException("record " + rec + ": size"), true);
			} catch (EOFException e) {
				return 0;
			} catch (IOException e) {
				error(e, false);
			}
		}
	}

	/***************************************************************************
	 * convert unicode buffer (0 to index) to oem data and overwrite in file
	 *
	 * @param rec
	 *            relative record number (1 <= rec <= records in file)
	 * @param off
	 *            target offset within record on file
	 ***************************************************************************/
	public void rewrite(int rec, int off) {
		while (true) {
			try {
				file.seek((rec - 1l) * record.length + off);
				file.write(toString(0, index).getBytes(oem));
				break;
			} catch (IOException e) {
				error(e, false);
			}
		}
	}

	/***************************************************************************
	 * convert unicode buffer (0 to index) to oem data and append complete record (data size = index) plus CR/LF to file
	 ***************************************************************************/
	public void write() {
		if (index != dataLen()) {
			error(new IOException("data size = " + index), true);
		}
		rewrite(getSize() + 1, 0);
		try {
			file.write(new byte[] { 0x0d, 0x0a });
		} catch (IOException e) {
			error(e, true);
		}
	}

	/***************************************************************************
	 * synchronize file size and data buffers with underlying device
	 ***************************************************************************/
	public void sync() {
		while (true) {
			try {
				file.getFD().sync();
				break;
			} catch (IOException e) {
				error(e, false);
			}
		}
	}

	/***************************************************************************
	 * open local data file
	 *
	 * @param path
	 *            relative or absolute path
	 * @param name
	 *            file name
	 * @param mode
	 *            open mode (0=read only, 1=read/write, 2=new)
	 ***************************************************************************/
	public void open(String path, String name, int mode) {
		open(path, name, mode, false);
	}

	public void open(String path, String name, int mode, boolean mandatory) {
		pathfile = localFile(path, name);
		if (mode > 1) {
			pathfile.delete();
			if (pathfile.length() > 0)
				error(new IOException("deletion failed"), true);
		}
		try {
			file = new RandomAccessFile(pathfile, mode > 0 ? "rw" : "r");
			if (file.length() % record.length > 0)
				error(new IOException("file size"), true);
		} catch (IOException e) {
			if (mode < 1 && !mandatory)
				file = null;
			else
				error(e, true);
		}
	}

	/***************************************************************************
	 * close local data file
	 ***************************************************************************/
	public void close() {
		if (file == null)
			return;
		try {
			file.close();
			file = null;
		} catch (IOException e) {
			error(e, false);
		}
	}

	/***************************************************************************
	 * get file size
	 *
	 * @return number of records in file
	 ***************************************************************************/
	public int getSize() {
		int rec = 0;

		if (file != null)
			try {
				rec = (int) (file.length() / record.length);
			} catch (IOException e) {
				error(e, true);
			}
		return rec;
	}
}


