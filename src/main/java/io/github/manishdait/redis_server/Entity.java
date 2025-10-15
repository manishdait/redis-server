package io.github.manishdait.redis_server;

import java.time.Instant;

public class Entity {
  private String key;
  private Object value;
  private Instant expiration;
  public Entity(String key, Object value, Instant expiration) {
    this.key = key;
    this.value = value;
    this.expiration = expiration;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Instant getExpiration() {
    return expiration;
  }

  public void setExpiration(Instant expiration) {
    this.expiration = expiration;
  }

  @Override
  public String toString() {
    return "Entity [key=" + key + ", value=" + value + ", expiration=" + expiration + "]";
  }
}
