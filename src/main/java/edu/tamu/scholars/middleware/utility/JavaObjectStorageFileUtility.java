package edu.tamu.scholars.middleware.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class JavaObjectStorageFileUtility {

    private JavaObjectStorageFileUtility() {

    }

    public static <T extends Serializable> void writeObject(T object, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);
            oos.flush();

            channel.write(ByteBuffer.wrap(baos.toByteArray()));
        }
    }

    public static <T extends Serializable> T readObject(String fileName) throws IOException, ClassNotFoundException {
        Path path = Paths.get(fileName);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (channel.read(buffer) > 0) {
                buffer.flip();
                baos.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                @SuppressWarnings("unchecked")
                T object = (T) ois.readObject();
                return object;
            }
        }
    }

}
