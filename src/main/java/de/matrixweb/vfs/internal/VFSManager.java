package de.matrixweb.vfs.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.matrixweb.vfs.VFS;

/**
 * Registers a vfs protocol handler.
 * 
 * @author markusw
 */
public final class VFSManager {

  private static final VFSManager INSTANCE = new VFSManager();

  private final Map<String, VFS> active = new HashMap<String, VFS>();

  /**
   * @param vfs
   * @return Returns a host for use in the given {@link VFS} {@link URL}s.
   */
  public static String register(final VFS vfs) {
    final String host = UUID.randomUUID().toString();
    getInstance().active.put(host, vfs);
    return host;
  }

  /**
   * @param vfsHost
   */
  public static void unregister(final String vfsHost) {
    getInstance().active.remove(vfsHost);
  }

  private static VFSManager getInstance() {
    return INSTANCE;
  }

  private VFSManager() {
    final String key = "java.protocol.handler.pkgs";
    final String pkg = "de.matrixweb.vfs.internal";
    final String current = System.getProperty(key);
    if (current != null) {
      if (!current.contains(pkg)) {
        System.setProperty(key, current + "|" + pkg);
      }
    } else {
      System.setProperty(key, pkg);
    }
  }

  /** */
  public static class VFSURLStreamHandler extends URLStreamHandler {

    /** */
    @Override
    public URLConnection openConnection(final URL u) throws IOException {
      return new URLConnection(u) {

        /**
         * @see java.net.URLConnection#connect()
         */
        @Override
        public void connect() throws IOException {
        }

        /**
         * @see java.net.URLConnection#getInputStream()
         */
        @Override
        public InputStream getInputStream() throws IOException {
          final String host = getURL().getHost();
          final VFS vfs = getInstance().active.get(host);
          if (vfs == null) {
            throw new IOException("Host '" + host
                + "' is not known in the registry.");
          }
          return vfs.find(getURL().getFile()).getInputStream();
        }

      };
    }

  }

}
