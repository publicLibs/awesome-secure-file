/**
 *
 */
package com.github.publicLibs.awesome.securefile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * @author freedom1b2830
 * @date 2023-февраля-17 15:54:20
 */
public final class SecureFileUtils {
	static abstract class SecureData {
		public final FileAttribute<Set<PosixFilePermission>> getAttrs() {
			return PosixFilePermissions.asFileAttribute(getPerms());
		}

		public final Set<PosixFilePermission> getPerms() {
			return PosixFilePermissions.fromString(getPermString());
		}

		public abstract String getPermString();
	}

	static class SecureDirData extends SecureData {
		private final static SecureDirData instance = new SecureDirData();

		public static SecureDirData getInstance() {
			return instance;
		}

		public @Override String getPermString() {
			return "rwx------";
		}
	}

	static class SecureFileData extends SecureData {
		private static final SecureFileData instance = new SecureFileData();

		public static SecureFileData getInstance() {
			return instance;
		}

		public @Override String getPermString() {
			return "rw-------";
		}
	}

	public static void createOrFixSecureDir(final Path dir, final boolean always) throws IOException {
		if (Files.notExists(dir) || always) {
			Files.createDirectories(dir, SecureDirData.getInstance().getAttrs());
		} else {
			Files.setPosixFilePermissions(dir, SecureDirData.getInstance().getPerms());
		}
	}

	public @Deprecated static void createOrFixSecureFile(final Path fileInput) throws IOException {
		final Path file = fileInput.normalize().toAbsolutePath();
		createSecureDir(file.getParent(), false);
		if (Files.notExists(file)) {
			Files.createFile(file, SecureFileData.getInstance().getAttrs());
		} else {
			Files.setPosixFilePermissions(file, SecureFileData.getInstance().getPerms());
		}
	}

	public static void createOrFixSecureFile(Path dirInput, Path fileInput) throws IOException {
		dirInput = dirInput.normalize().toAbsolutePath();
		fileInput = fileInput.normalize().toAbsolutePath();
		if (dirInput.startsWith(fileInput)) {
			throw new IllegalArgumentException(String.format("%s is not dir for %s", dirInput, fileInput));
		}
		createOrFixSecureDir(dirInput, false);
		int index = dirInput.getNameCount();
		Path tmpPath = dirInput.resolve(fileInput.getName(index));
		final int fileNameCount = fileInput.getNameCount();

		while (true) {
			final int dirNameCount = tmpPath.getNameCount();

			if (dirNameCount == fileNameCount) {
				if (Files.isSameFile(tmpPath, fileInput)) {
					if (Files.notExists(tmpPath)) {
						Files.createFile(tmpPath, SecureFileData.getInstance().getAttrs());
					} else {
						Files.setPosixFilePermissions(tmpPath, SecureFileData.getInstance().getPerms());
					}
					break;
				}
			}

			createSecureDir(tmpPath, true);
			index++;
			tmpPath = tmpPath.resolve(fileInput.getName(index));
		}
	}

	public static void createSecureDir(final Path dir, final boolean always) throws IOException {
		if (Files.notExists(dir) || always) {
			Files.createDirectories(dir, SecureDirData.getInstance().getAttrs());
		}
	}

	public static void createSecureFile(final Path fileInput) throws IOException {
		final Path file = fileInput.normalize().toAbsolutePath();
		createSecureDir(file.getParent(), false);
		Files.createFile(file, SecureFileData.getInstance().getAttrs());
	}

	public static OutputStream createSecureFileOutputStream(final Path fileInput) throws IOException {
		createSecureFile(fileInput);
		return Files.newOutputStream(fileInput);
	}

	private SecureFileUtils() {
	}

}
