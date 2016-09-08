package ru.skinnyweb.server.utils;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleSaxErrorHandler implements ErrorHandler {
  private final Logger logger;

  /**
   * Create a new SimpleSaxErrorHandler for the given
   * Commons Logging logger instance.
   */
  public SimpleSaxErrorHandler(Logger logger) {
    this.logger = logger;
  }

  public void warning(SAXParseException ex) throws SAXException {
    logger.log(Level.WARNING, "Ignored XML validation warning", ex);
  }

  public void error(SAXParseException ex) throws SAXException {
    throw ex;
  }

  public void fatalError(SAXParseException ex) throws SAXException {
    throw ex;
  }
}
