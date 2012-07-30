package org.jetbrains.plugins.clojure.repl;

import clojure.lang.PersistentVector;
import clojure.tools.nrepl.Connection;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.NotImplementedException;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author cprice
 */
public class ClojureNReplProcess extends Process {

    private static final Logger LOG = Logger.getInstance(ClojureNReplProcess.class.getName());

    private String lastNamespace = "user";

    private final Connection repl_conn;

    private final Queue<byte[]> resp_queue = new LinkedBlockingQueue<byte[]>();

    private final OutputStream out_buffer = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            final String expr = new String(b, "UTF-8");
            LOG.info("out_buffer.write: '" + expr + "'");
            sendExpression(expr);
        }
    };

    private final InputStream in_buffer = new InputStream() {
        // TODO: this implementation is not thread safe; it assumes
        //  that there will be exactly one thread calling 'available' and 'read'.
        private byte[] nextResponse = null;
        private int nextOffset = 0;

        @Override
        public int read() throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            LOG.info("Read called with off/len: '" + off + "/" + len + "'");
            int numBytes = Math.min(len, nextResponse.length - nextOffset);
            for (int i = 0; i < numBytes; i++) {
                b[off + i] = nextResponse[nextOffset + i];
            }
            nextOffset += numBytes;
            LOG.info("Read returning " + numBytes + " bytes. (nR.l/nextOffset: " + nextResponse.length + "/" + nextOffset + ")");
            if (nextOffset >= nextResponse.length) {
                nextResponse = null;
            }
            return numBytes;
        }

        @Override
        public int available() throws IOException {
            if (nextResponse != null) {
                LOG.info("AVAILABLE: nextResponse is not null.");
                LOG.info("AVAILABLE: nextResponse is not null.");
                return nextResponse.length - nextOffset;
            }

            nextResponse = resp_queue.poll();
            if (nextResponse == null) {
                return 0;
            } else {
                nextOffset = 0;
                return nextResponse.length;
            }
        }
    };

    private final InputStream err_buffer = new InputStream() {
        @Override
        public int read() throws IOException {
            throw new NotImplementedException();
        }
    };


    public ClojureNReplProcess() throws ExecutionException {
        final String url = "nrepl://localhost:54050";
        try {
            repl_conn = new Connection(url);
            sendExpression("(symbol (str \"Clojure \" (clojure-version)))");
        } catch (Exception e) {
            throw new ExecutionException("Unable to connect to repl at url '" + url + "'", e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return out_buffer;
    }

    @Override
    public InputStream getInputStream() {
        return in_buffer;
    }

    @Override
    public InputStream getErrorStream() {
        return err_buffer;
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (true) {
            Thread.sleep(10000);
        }
    }

    @Override
    public int exitValue() {
        throw new NotImplementedException();
    }

    @Override
    public void destroy() {
        throw new NotImplementedException();
    }


    private void sendExpression(String expr) throws IOException {
        Connection.Response resp = repl_conn.send("op", "eval", "code",
                "(do (ns " + lastNamespace + ")\n" + expr + ")");
        LOG.info("combined response: '" + resp.combinedResponse() + "'");
        try {
            resp_queue.add(responseToByteArray(resp));
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }


    private byte[] responseToByteArray(Connection.Response resp) throws ExecutionException {
        final Map<String, Object> responseMap = resp.combinedResponse();
        StringBuffer resp_str = new StringBuffer();
        if (responseMap.containsKey("out")) {
            resp_str.append((String)responseMap.get("out"));
        }
        if (responseMap.containsKey("err")) {
            resp_str.append((String)responseMap.get("err"));
        }
        if (responseMap.containsKey("value")) {
            resp_str.append((String) ((PersistentVector) (responseMap.get("value"))).get(0))
                    .append("\n");
        }
        if (responseMap.containsKey("ns")) {
            lastNamespace = (String) responseMap.get("ns");
        }
        resp_str.append(lastNamespace)
                .append("=>");
//        String resp_str = "Command complete\nuser=>";
        try {
            return resp_str.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ExecutionException("Unable to convert response to UTF-8: (" + resp_str + ")", e);
        }
    }


}
