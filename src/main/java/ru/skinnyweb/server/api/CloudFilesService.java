package ru.skinnyweb.server.api;

import java.nio.charset.Charset;
import java.util.Date;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.logging.Logger;

public class CloudFilesService {
  private static final Logger log = Logger.getLogger(CloudFilesService.class.getName());
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public class File {
    private final Key key;
    private final String path;
    private final byte[] bytes;
    private final Date created;

    /**
     * @param key
     * @param path
     * @param bytes
     * @param created
     */
    public File(Key key, String path, byte[] bytes, Date created) {
      this.key = key;
      this.path = path;
      this.bytes = bytes;
      this.created = created;
    }

    /**
     * @return the key
     */
    public Key getKey() {
      return key;
    }

    /**
     * @return the path
     */
    public String getPath() {
      return path;
    }

    /**
     * @return the bytes
     */
    public byte[] getBytes() {
      return bytes;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
      return created;
    }
  }

  public File read(String path) {
    Key key = KeyFactory.createKey("File", path);
    
    try {
      Entity entity = datastore.get(key);
      return new File(key, 
          (String) entity.getProperty("path"), 
          ((Blob) entity.getProperty("content")).getBytes(), 
          (Date) entity.getProperty("created"));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public void write(String path, String content) {
    Key key = KeyFactory.createKey("File", path);
    Entity entity = new Entity(key);
    entity.setProperty("content", new Blob(content.getBytes(Charset.forName("utf-8"))));
    entity.setProperty("path", path);
    entity.setProperty("created", new Date());
    datastore.put(entity);
    log.info("path: " + path + " length: " + content.length());
  }
}
