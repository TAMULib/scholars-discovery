package edu.tamu.scholars.middleware.utility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtility {

    private ZipUtility() {

    }

    public static long zipFile(ZipOutputStream zos, File file) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));

        long bytesRead = 0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
        }
        zos.closeEntry();

        return bytesRead;
    }

}
