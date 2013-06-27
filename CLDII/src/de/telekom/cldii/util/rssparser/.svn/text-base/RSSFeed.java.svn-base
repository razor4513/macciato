/*
 * Copyright (C) 2010 A. Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.telekom.cldii.util.rssparser;

/**
 * Data about an RSS feed and its RSS items.
 * 
 * @author Mr Horn and Jun Chen, jambit GmbH
 */
public class RSSFeed extends RSSBase {

  private final java.util.List<RSSItem> items;
  private int ttl;
  private android.net.Uri iconUrl;

  RSSFeed() {
    super(/* initial capacity for category names */ (byte) 3);
    items = new java.util.LinkedList<RSSItem>();
  }

  /**
   * Returns an unmodifiable list of RSS items.
   */
  public java.util.List<RSSItem> getItems() {
    return java.util.Collections.unmodifiableList(items);
  }

  void addItem(RSSItem item) {
    items.add(item);
  }
  
  public int getTtl() {
	  return ttl;
  }
  
  void setTtl(int ttl) {
	  this.ttl = ttl;
  }
  
  public android.net.Uri getIconUrl() {
      return iconUrl;
  }
  
  void setUrl(android.net.Uri url) {
      this.iconUrl = url;
  }

}

