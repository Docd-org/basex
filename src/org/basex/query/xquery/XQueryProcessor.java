package org.basex.query.xquery;

import java.io.File;
import org.basex.query.QueryProcessor;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;

/**
 * This is the main class of the XQuery Processor.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQueryProcessor extends QueryProcessor {
  /** XQuery context reference. */
  public XQContext ctx = new XQContext();
  
  /**
   * XQuery Constructor.
   * @param qu query
   */
  public XQueryProcessor(final String qu) {
    super(qu);
  }
  
  /**
   * XQuery Constructor.
   * @param qu query
   * @param f query file reference
   */
  public XQueryProcessor(final String qu, final File f) {
    this(qu);
    ctx.file = f;
  }

  @Override
  public QueryContext create() throws QueryException {
    parse(query);
    return ctx;
  }

  /**
   * Parses the specified input.
   * @param in input to be parsed
   * @throws QueryException query exception
   */
  public void parse(final byte[] in) throws QueryException {
    new XQParser(ctx).parse(in);
  }

  /**
   * Adds a module reference.
   * @param ns module namespace
   * @param file file name
   */
  public void module(final String ns, final String file) {
    ctx.modules.add(ns);
    ctx.modules.add(file);
  }
}
