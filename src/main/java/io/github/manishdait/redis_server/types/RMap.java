package io.github.manishdait.redis_server.types;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class RMap implements Serializable {
  private final ConcurrentHashMap<String, RType> MAP;

  public RMap() {
    this.MAP = new ConcurrentHashMap<>();
  }

  public void put(String key, RType value) {
    this.MAP.put(key, value);
  }

  public RType get(String key) {
    return this.MAP.get(key);
  }

  public void remove(String key) {
    this.MAP.remove(key);
  }

  public boolean contains(String key) {
    return this.MAP.containsKey(key);
  }

  public RString getString(String key) {
    RType value = this.MAP.get(key);
    
    if (value instanceof RString) {
      return (RString) value;
    }

    throw new RuntimeException("WRONG-TYPE");
  }

  public RList getList(String key) {
    RType value = this.MAP.get(key);
    
    if (value instanceof RList) {
      return (RList) value;
    }

    throw new RuntimeException("WRONG-TYPE");
  }

  public KeySetView<String, RType> keys() {
    return MAP.keySet();
  }
}
