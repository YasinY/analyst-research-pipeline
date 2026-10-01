package com.assignment.research.adapter.output;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

final class ProtectedDirectory implements AutoCloseable {

    private static final String READ_ONLY = "r-x------";
    private static final String WRITABLE = "rwx------";
    private static final String USER_NAME = "user.name";

    private final Path directory;
    private final List<AclEntry> originalAcl;

    private ProtectedDirectory(Path directory, List<AclEntry> originalAcl) {
        this.directory = directory;
        this.originalAcl = originalAcl;
    }

    static ProtectedDirectory protect(Path directory) {
        try {
            var posix = Files.getFileAttributeView(directory, PosixFileAttributeView.class);
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString(READ_ONLY));
                return new ProtectedDirectory(directory, List.of());
            }
            var acl = Files.getFileAttributeView(directory, AclFileAttributeView.class);
            var original = acl.getAcl();
            var user = FileSystems.getDefault().getUserPrincipalLookupService()
                    .lookupPrincipalByName(System.getProperty(USER_NAME));
            var denyNewEntries = AclEntry.newBuilder().setType(AclEntryType.DENY).setPrincipal(user)
                    .setPermissions(AclEntryPermission.ADD_SUBDIRECTORY, AclEntryPermission.ADD_FILE).build();
            var restricted = new ArrayList<AclEntry>();
            restricted.add(denyNewEntries);
            restricted.addAll(original);
            acl.setAcl(restricted);
            return new ProtectedDirectory(directory, original);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    boolean isEffective() {
        return !Files.isWritable(directory);
    }

    @Override
    public void close() {
        try {
            var posix = Files.getFileAttributeView(directory, PosixFileAttributeView.class);
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString(WRITABLE));
                return;
            }
            Files.getFileAttributeView(directory, AclFileAttributeView.class).setAcl(originalAcl);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }
}
