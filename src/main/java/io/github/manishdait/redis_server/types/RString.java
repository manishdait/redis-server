package io.github.manishdait.redis_server.types;

import java.time.Instant;

public class RString implements RType {
  private String value;
  private Instant expiration;

  public RString(String value) {
    this(value, null);
  }

  public RString(String value, Instant expiration) {
    this.value = value;
    this.expiration = expiration;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Instant getExpiration() {
    return this.expiration;
  }

  public void setExpiration(Instant expiration) {
    this.expiration = expiration;
  }

  public void plusSeconds(long seconds) {
    setExpiration(Instant.now().plusSeconds(seconds));
  }

  public void plusMilliSeconds(long millis) {
    setExpiration(Instant.now().plusMillis(millis));
  }

  public void setExpirationSeconds(long seconds) {
    setExpiration(Instant.ofEpochSecond(seconds));
  }

  public void setExpirationMills(long millis) {
    setExpiration(Instant.ofEpochMilli(millis));
  }

  public boolean hasExpire() {
    if (this.getExpiration() == null){
      return false;
    }
    
    return this.getExpiration().isBefore(Instant.now());
  }

  @Override
  public String toString() {
    return "RString [value=" + value + ", expiration=" + expiration + "]";
  }
}
