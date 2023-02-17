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

	static final Set<PosixFilePermission> permsDir = PosixFilePermissions.fromString("rwx------");
	static final FileAttribute<Set<PosixFilePermission>> attrDir = PosixFilePermissions.asFileAttribute(permsDir);
	static final Set<PosixFilePermission> permsFiles = PosixFilePermissions.fromString("rwx------");
	static final FileAttribute<Set<PosixFilePermission>> attrFiles = PosixFilePermissions.asFileAttribute(permsFiles);

	public static void createSecureDir(final Path dir, final boolean always) throws IOException {
		if (Files.notExists(dir) || always) {
			Files.createDirectories(dir, attrDir);
		}
	}

	public static void createSecureFile(final Path fileInput) throws IOException {
		final Path file = fileInput.normalize().toAbsolutePath();
		createSecureDir(file.getParent(), false);
		Files.createFile(file, attrFiles);
	}

	public static OutputStream createSecureFileOutputStream(final Path fileInput) throws IOException {
		createSecureFile(fileInput);
		return Files.newOutputStream(fileInput);
	}

	private SecureFileUtils() {
	}

}
