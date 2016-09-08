package ru.skinnyweb.server.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by smalex on 28/05/15.
 */
public class DigestUtils {
  public static String md5Hex(String str) {
    return md5Hex(str.getBytes(Charsets.UTF_8));
  }

  public static String md5Hex(byte[] bytes) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(bytes);
      StringBuilder builder = new StringBuilder();
      for (byte b : md5.digest()) {
        builder.append(String.format("%02x", b & 0xFF));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException ex) {
    }
    return "";
  }

  public static String getMd5sum(byte[] data) {
    String md5sum = "";
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("MD5");
      InputStream inStream = new ByteArrayInputStream(data);
      byte[] buffer = new byte[65536];
      while (true) {
        int bytesRead = inStream.read(buffer);
        if (bytesRead == -1) {
          break;
        }
        digest.update(buffer, 0, bytesRead);
      }
      byte[] hash = digest.digest();

      StringBuffer hashString = new StringBuffer();
      for (int j = 0; j < hash.length; j++) {
        String hexValue = Integer.toHexString(0xFF & hash[j]);
        if (hexValue.length() == 1) {
          hashString.append("0");
        }
        hashString.append(hexValue);
      }

      md5sum = hashString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return md5sum;
  }

  public static String longToRemoteAddrTo(long remoteAddr) {
    StringBuilder sb = new StringBuilder();
    for (int i = 24; i >= 0; i -= 8) {
      long l = (remoteAddr >> i) & 0xff;
      if (sb.length() > 0) {
        sb.append(".");
      }
      sb.append(l);
    }
    return sb.toString();
  }
}

