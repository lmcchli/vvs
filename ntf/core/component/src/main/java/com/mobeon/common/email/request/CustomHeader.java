/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.common.email.request;

/**
 * A custom header is simply a header name and a value.
 * Example: X-My-Header: Off vould hava value "X-My-Header" 
 * and value "Off".
 *
 */
public class CustomHeader {
  private String value;
  private String header;
  /**
   * Creates a new <code>CustomHeader</code> instance.
   *
   * @param header a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public CustomHeader(String header, String value) {
    this.value = value; this.header = header;
  }
  /**
   * Gets the header name
   *
   * @return Header name
   */
  public String getHeader() { return header; }
  
  /**
   * Gets the header value
   *
   * @return Header value
   */
  public String getValue() { return value; }
}
