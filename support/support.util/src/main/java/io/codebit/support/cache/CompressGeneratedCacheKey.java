package io.codebit.support.cache;

import javax.cache.annotation.GeneratedCacheKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Default cache key implementation. Keeps a reference to a copy of the entire parameter array from
 * the intercepted invocation and uses {@link Arrays#deepHashCode(Object[])} to
 * implement {@link #hashCode()} and {@link Arrays#deepEquals(Object[], Object[])} to implement
 * {@link #equals(Object)}
 * <p>
 * IMPORTANT: This implementation assumes that the entire object graph passed in as the parameters Object[]
 * is immutable. The value returned by {@link #hashCode()} is calculated in the constructor.
 * </p>
 *
 * @author Eric Dalquist
 * @since 1.0
 */
public class CompressGeneratedCacheKey implements GeneratedCacheKey {

    private static final long serialVersionUID = 1L;

    private final int hashCode;
    private final String base64;
    /**
     * Constructs a default cache key
     *
     * @param parameters the paramters to use
     */
    public CompressGeneratedCacheKey(Object[] parameters) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            //object를 byte array화
            for (Object parameter : parameters) {
                out.writeObject(parameter);
            }
            byte[] compress = compress(bos.toByteArray());
            this.hashCode = Arrays.hashCode(compress);
            base64 = new String(Base64.getEncoder().encode(compress));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode != obj.hashCode())
            return false;
        CompressGeneratedCacheKey other = (CompressGeneratedCacheKey) obj;
        return base64.equals(other.base64);
    }

    private static byte[] compress(byte[] data) throws IOException {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_SPEED);
            deflater.setInput(data);
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer); // returns the generated code... index
                outputStream.write(buffer, 0, count);
            }
            byte[] output = outputStream.toByteArray();
            return output;
        }
    }

    private static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            byte[] output = outputStream.toByteArray();
            return output;
        }
    }
}