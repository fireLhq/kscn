package top.kscn.exception;

import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.util.DisconnectedClientHelper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Response-write failure only; source-file IO errors must remain server failures. */
public class DownloadTransferException extends IOException {
    public enum Reason { WRITE_TIMEOUT, CONNECTION_LOST }
    private final Reason reason;
    private final long bytesWritten;
    private final long expectedBytes;

    private DownloadTransferException(IOException cause, long bytesWritten, long expectedBytes) {
        super(hasTimeout(cause) ? "下载响应写出超时，可重试或续传" : "下载连接已中断，可重试或续传", cause);
        this.reason = hasTimeout(cause) ? Reason.WRITE_TIMEOUT : Reason.CONNECTION_LOST;
        this.bytesWritten = bytesWritten; this.expectedBytes = expectedBytes;
    }

    public static IOException responseFailure(IOException cause, long written, long expected) {
        if (hasTimeout(cause) || DisconnectedClientHelper.isClientDisconnectedException(cause))
            return new DownloadTransferException(cause, written, expected);
        return cause;
    }

    public static DownloadTransferException find(Throwable cause) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = cause; current != null && seen.add(current); current = current.getCause()) {
            if (current instanceof DownloadTransferException transfer) return transfer;
            if (current instanceof AsyncRequestNotUsableException async) return new DownloadTransferException(async, -1, -1);
        }
        return null;
    }

    private static boolean hasTimeout(Throwable cause) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = cause; current != null && seen.add(current); current = current.getCause())
            if (current instanceof SocketTimeoutException) return true;
        return false;
    }
    public Reason getReason() { return reason; }
    public long getBytesWritten() { return bytesWritten; }
    public long getExpectedBytes() { return expectedBytes; }
}
