package org.basex.build.fs.metadata;

import java.io.IOException;
import org.basex.BaseX;

/**
 * MetaData Exception.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Alexander Holupirek
 */
public class MetaDataException extends IOException {
  /**
   * Constructor.
   * @param message message
   */
  public MetaDataException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   * @param s message
   * @param e message extension
   */
  public MetaDataException(final String s, final Object... e) {
    super(BaseX.info(s, e));
  }
}
