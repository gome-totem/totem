package org.z.common.io;

/**
 * A {@link RuntimeException} which is thrown when the data the {@link IoBuffer}
 * contains is corrupt.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 *
 */
public class BufferDataException extends RuntimeException {
    private static final long serialVersionUID = -4138189188602563502L;

    public BufferDataException() {
        super();
    }

    public BufferDataException(String message) {
        super(message);
    }

    public BufferDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public BufferDataException(Throwable cause) {
        super(cause);
    }

}
