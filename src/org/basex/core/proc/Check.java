package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.core.Commands;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;

/**
 * Evaluates the 'check' command. Checks if the specified XML document is in
 * memory; if negative, the database file is opened; if it doesn't exist, a
 * new database instance is created.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Check extends Proc {
  @Override
  protected boolean exec() {
    // file in memory?
    final Data data = context.data();
    final String db = Create.chopPath(cmd.arg(0));
    if(data != null && Create.chopPath(data.meta.filename).equals(db)) {
      return Prop.info ? info(DBINMEM) : true;
    }

    // streaming mode - create new database instance in main memory
    if(Prop.onthefly) return exec(Commands.CREATEXML);
    // open or create new database
    return exec(Commands.OPEN) || exec(Commands.CREATEXML);
  }
  
  /**
   * Static command for getting a database reference.
   * No warnings are thrown; instead, an empty reference is returned.
   * @param path file path
   * @return data instance
   */
  public static Data check(final String path) {
    final String db = Create.chopPath(path);
    return MetaData.found(path, db) ? Open.open(path) : Create.xml(db, path);
  }
}

