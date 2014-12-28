package com.gmail.br45entei.supercmds;

// TODO try making a function that reads all the lines of a file (like readAllLines(Path path, Charset cs)
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

/** Hosts functions and methods that are used in many different plugins; this
 * class is primarily for ease of access to the filesystem of the host machine.
 * 
 * @since 0.1
 * @author <a
 *         href="http://enteisislandsurvival.no-ip.org/about/author.html">Brian_Entei
 *         </a> */
public class FileMgmt {
	public static FileMgmt	plugin;
	
	public static boolean WriteToFile(File file, String message, boolean wipeOnWrite) {
		boolean success = false;
		try {
			if(!(file.exists())) {
				file.createNewFile();
			} else {
				if(wipeOnWrite == true) {
					//saveTo.delete(); This is a temporary workaround until I learn how to truncate the file rather than to delete it...
					PrintWriter writer = new PrintWriter(file);
					writer.print("");
					writer.close();
				}
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(message);
			pw.flush();
			pw.close();
			success = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	public static boolean WriteToFile(String filename, String message, boolean wipeOnWrite, String folder, String dataFolderName) {
		boolean writeSuccess = false;
		try {
			File dataFolder = FileMgmt.getPluginFolder(dataFolderName);
			if(!(dataFolder.exists())) {
				dataFolder.mkdir();
			}
			File newFolder = null;
			if(folder.equals("") == false) {
				newFolder = new File(dataFolder, folder);
				if(newFolder.exists() != true) {
					newFolder.mkdirs();
				}
			} else {
				newFolder = dataFolder;
			}
			
			newFolder.mkdirs();
			
			File saveTo = null;
			if(filename.contains(".")) {
				saveTo = new File(newFolder, filename);
			} else {
				saveTo = new File(newFolder, (filename + ".txt"));
			}
			if(Main.showDebugMsgs) {
				Main.console.sendMessage(Main.formatColorCodes("&aDEBUG: saveTo.getAbsolutePath(): \"" + saveTo.getAbsolutePath() + "\"..."));
			}
			if(!(saveTo.exists())) {
				saveTo.createNewFile();
			} else {
				if(wipeOnWrite == true) {
					//saveTo.delete(); This is a temporary workaround until I learn how to truncate the file rather than to delete it...
					PrintWriter writer = new PrintWriter(saveTo);
					writer.print("");
					writer.close();
				}
				saveTo.createNewFile();
			}
			FileWriter fw = new FileWriter(saveTo, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(message);
			pw.flush();
			pw.close();
			writeSuccess = true;
		} catch(IOException e) {
			Main.console.sendMessage(Main.formatColorCodes("&cAn error occurred while attempting to perform the following function: WriteToFile(String filename(\"" + filename + "\"), String message(\"" + message + "\"), boolean wipeOnWrite(\"" + wipeOnWrite + "\"), String folder(\"" + folder + "\"), String dataFolderName(\"" + dataFolderName + "\"))"));
			e.printStackTrace();/*WriteToFile("crash-reports", "--------------------------", false, "");WriteToFile("crash-reports", e.getMessage(), false, "");*/
		}// <--That makes an infinite loop of errors, because you are calling a function inside of itself...
		return writeSuccess;
	}
	
	public static String ReadFromFile(File filetoread, String dataFolderName) {
		//MainChatClass.broadcastMsg("Testing ReadFromFile() in /nick...", true, "Operators");
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(filetoread);
			BufferedReader br = new BufferedReader(new InputStreamReader(fs));
			String line = br.readLine();
			//MainChatClass.broadcastMsg("The result: " + line, true, "Operators");
			fs.close();
			return line;
		} catch(IOException x) {
		}
		if(fs != null) {
			try {
				fs.close();
			} catch(IOException e) {
				FileMgmt.LogCrash(e, "ReadFromFile()", "Unable to close resource; was it ever open?", false, dataFolderName);
			}
		}
		return "";
	}
	
	public static boolean LogCrash(Exception e, String functionName, String msg, boolean printStackTrace, String dataFolderName) {
		if(msg == null || msg.equals(null)) {
			msg = "";
		}
		FileMgmt.WriteToFile("crash-reports.txt", "-------[[ " + Main.getSystemTime(false) + " ]]-------", false, "", dataFolderName);
		FileMgmt.WriteToFile("crash-reports.txt", "An error occurred when processing function '" + functionName + "'. Exception thrown:  \"" + e.getClass().getName() + "\"; Caused by: \"" + e.getMessage(), false, "", dataFolderName);
		FileMgmt.WriteToFile("crash-reports.txt", "\"; Message sent when error occurred: \"" + msg + "\"; Please contact Brian_Entei at br45entei@gmail.com if you would like to personally tell him about this error, so that he may fix it!", false, "", dataFolderName);
		if(printStackTrace) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static File getPluginFolder(String folderName) {
		File dataFolder = new File(folderName);
		if(!(dataFolder.exists())) {
			dataFolder.mkdir();
		}
		return dataFolder;
	}
	
	/*public static File getFile(String path, String FileName) {
		File getFile = null;
		if(FileName.equals("")) return getFile;
		if(path.equals("")) {
			getFile = new File(MainChatClass.dataFolderName, FileName);
		} else {
			getFile = new File(path, FileName);
		}
		if(!(getFile.exists())) {
			try {
				getFile.createNewFile();
			} catch (IOException e) {
				FileMgmt.LogCrash(e, "getFile(\"" + path + "\", \"" + FileName + "\")", "Failed to create new file \"" + FileName + "\" in the directory: \"" + path + "\"", true);
			}
		}
		return getFile;
	}*/
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while(true) {
			int readCount = in.read(buffer);
			if(readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}
	
	public static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			FileMgmt.copy(in, out);
		} finally {
			in.close();
		}
	}
	
	public static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			FileMgmt.copy(in, out);
		} finally {
			out.close();
		}
	}
	
	public static void copy(File from, File to) throws IOException {
		InputStream in = new FileInputStream(from);
		OutputStream out = new FileOutputStream(to);
		try {
			FileMgmt.copy(in, out);
		} finally {
			in.close();
			out.close();
		}
	}
	
	/** @param filename
	 * @param folder
	 * @param dataFolderName
	 * @param loadIfNoFile
	 * @return The read data.
	 * @throws Exception */
	public static String ReadFromFile(String filename, String folder, String dataFolderName, boolean loadIfNoFile) throws Exception {
		String rtrn = "";
		try {
			File dataFolder = FileMgmt.getPluginFolder(dataFolderName);
			if(!(dataFolder.exists())) {
				dataFolder.mkdir();
			}
			File newFolder = null;
			if(folder.equals("") == false) {
				newFolder = new File(dataFolder, folder);
				if(newFolder.exists() != true) {
					newFolder.mkdir();
				}
			} else {
				newFolder = dataFolder;
			}
			File saveTo = null;
			if(filename.contains(".")) {
				saveTo = new File(newFolder, filename);
			} else {
				saveTo = new File(newFolder, (filename + ".txt"));
			}
			if(loadIfNoFile == true) {
				saveTo.createNewFile();
			} else {
				if(saveTo.exists() == false) {
					throw new Exception("File \"" + filename + "\" was not found.");
				}
				saveTo.createNewFile();
			}
			rtrn = FileMgmt.ReadFromFile(saveTo, dataFolderName);
		} catch(IOException e) {
			FileMgmt.LogCrash(e, "ReadFromFile(fileName, folder, dataFolderName)", "An error occurred when attempting to read the file. Check the crash-reports.txt file for more info.", true, dataFolderName);
		}
		return rtrn;
	}
	
	public static String ReadFromFile(String filename, String folder, String dataFolderName) {
		String rtrn = "";
		try {
			File dataFolder = FileMgmt.getPluginFolder(dataFolderName);
			if(!(dataFolder.exists())) {
				dataFolder.mkdir();
			}
			File newFolder = null;
			if(folder.equals("") == false) {
				newFolder = new File(dataFolder, folder);
				if(newFolder.exists() != true) {
					newFolder.mkdir();
				}
			} else {
				newFolder = dataFolder;
			}
			File saveTo = null;
			if(filename.contains(".")) {
				saveTo = new File(newFolder, filename);
			} else {
				saveTo = new File(newFolder, (filename + ".txt"));
			}
			saveTo.createNewFile();
			rtrn = FileMgmt.ReadFromFile(saveTo, dataFolderName);
		} catch(IOException e) {
			FileMgmt.LogCrash(e, "ReadFromFile(fileName, folder, dataFolderName)", "An error occurred when attempting to read the file. Check the crash-reports.txt file for more info.", true, dataFolderName);
		}// <--That makes an infinite loop of errors, because you are calling a function inside of itself...
		return rtrn;
	}
	
	public static boolean logToFile(String filename, String message, String folder, String dataFolderName) {
		boolean writeSuccess = false;
		try {
			File dataFolder = FileMgmt.getPluginFolder(dataFolderName);
			if(!(dataFolder.exists())) {
				dataFolder.mkdir();
			}
			File newFolder = null;
			if(folder.equals("") == false) {
				newFolder = new File(dataFolder, folder);
				if(newFolder.exists() != true) {
					newFolder.mkdir();
				}
			} else {
				newFolder = dataFolder;
			}
			File saveTo = null;
			if(filename.contains(".") == false) {
				filename = filename + ".html";
			}
			saveTo = new File(newFolder, filename);
			saveTo.createNewFile();
			if(saveTo.length() >= 2500000) {//2.5 KiloBytes
			
				int num = 1;
				File newFile = new File(filename + "_" + num);
				while(newFile.exists()) {
					num++;
					newFile = new File(filename + "_" + num);
				}
				boolean success = saveTo.renameTo(newFile);
				if(!success) {/*File was not successfully renamed*/
				}
				saveTo = new File(newFolder, filename);
				saveTo.createNewFile();
			}
			FileWriter fw = new FileWriter(saveTo, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(message);
			pw.flush();
			pw.close();
			writeSuccess = true;
		} catch(IOException e) {
			e.printStackTrace();/*WriteToFile("crash-reports", "--------------------------", false, "");WriteToFile("crash-reports", e.getMessage(), false, "");*/
		}// <--That makes an infinite loop of errors, because you are calling a function inside of itself...
		return writeSuccess;
	}
	
	public static String makeNewDirectoryPath(String filePath, String from, String to) {
		return filePath.replace(from, to);
	}
	
	public static void copyDirectory(final File from, File to) {
		//print("File copier - Written by Brian_Entei");
		//print("For questions, comments, or any issues, please contact me at: br45entei@gmail.com");
		try {
			Thread.sleep(2000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		File getNumDir = new File(from.getAbsolutePath());
		
		//Let's get the total number of files that we're going to copy, to make a percentage:
		Deque<File> getNumQueue = new LinkedList<>();
		getNumQueue.push(getNumDir);
		int numOfFilesAndFolders = 0;
		
		@SuppressWarnings("unused")
		int numOfFolders = 0;
		@SuppressWarnings("unused")
		int numOfFiles = 0;
		
		//print("Scanning source directory \"" + from.getAbsolutePath() + "\" for files and folders to archive...");
		while(!getNumQueue.isEmpty()) {
			getNumDir = getNumQueue.pop();
			if(getNumDir != null) {
				for(File getNumFile : getNumDir.listFiles()) {
					numOfFilesAndFolders++;
					if(getNumFile.isDirectory()) {
						numOfFolders++;
						getNumQueue.push(getNumFile);//Keeps the queue looking down the folder path, adding the rest of the file counts
					} else {
						numOfFiles++;
					}
					//print("Found " + numOfFolders + " folders and " + numOfFiles + " files so far.");
				}
			}
		}
		//
		
		//print("Total files found: " + numOfFiles + ";\nTotal folders found: " + numOfFolders + ";\nGrand total: " + numOfFilesAndFolders);
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e1) {
			
		}
		if(numOfFilesAndFolders == 0) {
			return;// numOfFilesAndFolders;
		}
		
		URI base = from.toURI();
		Deque<File> queue = new LinkedList<>();
		queue.push(from);
		try {
			if(!to.exists()) {
				to.mkdirs();
			}
			String fromStr = from.getAbsolutePath();
			String toStr = to.getAbsolutePath();
			int copyCount = 0;
			while(!queue.isEmpty()) {
				@SuppressWarnings("boxing")
				float count = Float.valueOf(copyCount + ".0f");
				@SuppressWarnings("boxing")
				float total = Float.valueOf(numOfFilesAndFolders + ".0f");
				
				@SuppressWarnings("unused")
				String percentile = Main.limitStringToNumOfChars("" + (((count / total) * 1000.0f) / 10.0f), 6) + "% Complete: ";
				File directory = queue.pop();
				for(File kid : directory.listFiles()) {
					copyCount++;
					@SuppressWarnings("unused")
					String name = base.relativize(kid.toURI()).getPath();
					if(kid.isDirectory()) {
						queue.push(kid);
						//print(percentile + "New directory found: \"" + kid.getAbsolutePath() + "\"");
						try {
							File newKid = new File(FileMgmt.makeNewDirectoryPath(kid.getAbsolutePath(), fromStr, toStr));
							newKid.mkdirs();
							//print(percentile + "New directory created: \"" + newKid.getAbsolutePath() + "\"...");
							//sleep(10000);
						} catch(Exception e) {
							e.printStackTrace();
						}
					} else if(kid.getName().equalsIgnoreCase("thumbs.db") == false) {
						//print(percentile + "New file found: \"" + kid.getAbsolutePath() + "\"");
						try {
							File newKid = new File(FileMgmt.makeNewDirectoryPath(kid.getAbsolutePath(), fromStr, toStr));
							newKid.createNewFile();
							FileMgmt.copy(kid, newKid);
							//print(percentile + "New file created: \"" + newKid.getAbsolutePath() + "\"...");
							//sleep(1000);
						} catch(Exception e) {
							e.printStackTrace();
						}
					} else {
						//print(percentile + "SKIPPING file \"" + kid.getAbsolutePath() + "\"\n    because it is a \"Thumbs.db\" file and causes read/write access errors...");
						//sleep(1000);
					}
				}
			}
		} finally {
			//print("100.00% Complete.");
		}
	}
	
	public static final void zipFile(final File from, File zipFile) throws IOException {
		if(from == null || !from.exists()) {
			return;
		}
		@SuppressWarnings("resource")
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		String zipEntryPath = FilenameUtils.getPath(from.getAbsolutePath().replace(from.getParentFile().getAbsolutePath(), "")) + FilenameUtils.getName(from.getAbsolutePath());
		byte[] tmpBuf = new byte[1024];
		@SuppressWarnings("resource")
		FileInputStream in = new FileInputStream(from);
		out.putNextEntry(new ZipEntry(zipEntryPath));
		int len;
		while((len = in.read(tmpBuf)) > 0) {
			out.write(tmpBuf, 0, len);
		}
		out.closeEntry();
		try {
			in.close();
		} catch(Throwable ignored) {
		}
		try {
			out.close();
		} catch(Throwable ignored) {
		}
	}
	
	public static final void zipDir(final File from, File to) throws IOException {
		@SuppressWarnings("resource")
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(to));
		Deque<File> queue = new LinkedList<>();
		queue.push(from);
		try {
			while(!queue.isEmpty()) {
				File directory = queue.pop();
				if(directory != null) {
					for(File kid : directory.listFiles()) {
						//System.out.println("Kid: \"" + kid.getAbsolutePath() + "\";...");
						String zipEntryPath = FilenameUtils.getPath(kid.getAbsolutePath().replace(from.getParentFile().getAbsolutePath(), "")) + FilenameUtils.getName(kid.getAbsolutePath());
						//System.out.println("Zip entry path: \"" + zipEntryPath + "\";...");
						if(kid.isDirectory()) {
							queue.push(kid);
						}
						try {
							byte[] tmpBuf = new byte[1024];
							@SuppressWarnings("resource")
							FileInputStream in = new FileInputStream(kid);
							//System.out.println("RootDir: \"" + from.getParentFile().getAbsolutePath() + "\";...");
							out.putNextEntry(new ZipEntry(zipEntryPath));
							int len;
							while((len = in.read(tmpBuf)) > 0) {
								out.write(tmpBuf, 0, len);
							}
							out.closeEntry();
							try {
								in.close();
							} catch(Throwable ignored) {
							}
						} catch(Throwable ignored) {
						}
					}
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch(Throwable ignored) {
		}
	}
	
}