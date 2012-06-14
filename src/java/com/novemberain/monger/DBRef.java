package com.novemberain.monger;

import clojure.lang.IDeref;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.bson.BSONObject;

/**
 * Exactly as com.mongodb.DBRef but also implements Clojure IDefer for @dereferencing
 */
public class DBRef extends com.mongodb.DBRef implements IDeref {

  /**
   * Creates a DBRef
   * @param db the database
   * @param o a BSON object representing the reference
   */
  public DBRef(DB db, BSONObject o) {
    super(db , o.get("$ref").toString(), o.get("$id"));
  }

  /**
   * Creates a DBRef
   * @param db the database
   * @param ns the namespace where the object is stored
   * @param id the object id
   */
  public DBRef(DB db, String ns, Object id) {
    super(db, ns, id);
  }

  /**
   * Creates a DBRef from a com.mongodb.DBRef instance.
   * @param source The original reference MongoDB Java driver uses
   */
  public DBRef(com.mongodb.DBRef source) {
    this(source.getDB(), source.getRef(), source.getId());
  }

  @Override
  public DBObject deref() {
    return this.fetch();
  }
}
