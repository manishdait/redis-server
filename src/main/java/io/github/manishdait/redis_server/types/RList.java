package io.github.manishdait.redis_server.types;

public class RList implements RType {
  private class Node {
    private String value;
    private Node next;

    public Node(String value) {
      this.value = value;
    }
  }

  private Node head;
  private Node tail;

  public RList () {
    this.head = null;
    this.tail = null;
  }


  public void leftPush(String value) {
    Node node = new Node(value);
    node.next = this.head;
    this.head = node;

    if (tail == null) {
      this.tail = this.head;
    }
  }

  public void rigthPush(String value) {
    if (this.tail == null) {
      leftPush(value);
      return;
    }

    Node node = new Node(value);
    this.tail.next = node;
    this.tail = node;
  }

  public String getValue(long index) {
    long count = 0;
    Node tmp = this.head;

    while (tmp != null) {
      if (count == index) {
        return tmp.value;
      }

      tmp = tmp.next;
      count++;
    }

    throw new RuntimeException("Index out of bound");
  }
  
  public String[] getValues(long sIndex, long eIndex) {
    long max = size();

    if (eIndex >= max) {
      eIndex = max - 1;
    }

    if(sIndex >= max) {
      return new String [0]; 
    }

    if (sIndex < 0) {
      if ((sIndex * -1) > max) {
        sIndex = 0;
      } else {
        sIndex = max + sIndex;
      }
    }

    if (eIndex < 0) {
      if ((eIndex * -1) > max) {
        eIndex = 0;
      } else {
        eIndex = max + eIndex;
      }
    }

    long count = 0;
    Node tmp = this.head;

    while (tmp != null) {
      if (count == sIndex) {
        break;
      }
      tmp = tmp.next;
      count++;
    }

    try {
      String[] values = new String[(int) eIndex - (int) sIndex + 1];
      System.out.println(sIndex);
      System.out.println(eIndex);

      for (long i = 0; i < values.length; i++) {
        values[(int) i] = tmp.value;
        tmp = tmp.next;
      }

      return values;
    } catch (Exception e) {
      return new String [0];
    } 
  }
  
  private long size() {
    long size = 0;
    Node tmp = this.head;

    while (tmp != null) {
      size++;
      tmp = tmp.next;
    }

    return size;
  }
}
