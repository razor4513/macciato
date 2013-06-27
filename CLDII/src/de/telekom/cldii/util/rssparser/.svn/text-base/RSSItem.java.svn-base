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
 * Data about an RSS item.
 * 
 * @author Mr Horn and Jun Chen, jambit GmbH
 */
public class RSSItem extends RSSBase {
    private final java.util.List<MediaThumbnail> thumbnails;
    private String content;
    private String guid;
    private final java.util.List<String> contentImages;

    /* Internal constructor for RSSHandler */
    RSSItem(byte categoryCapacity, byte thumbnailCapacity) {
        super(categoryCapacity);
        thumbnails = new java.util.ArrayList<MediaThumbnail>(thumbnailCapacity);
        contentImages = new java.util.ArrayList<String>();
    }

    /* Internal method for RSSHandler */
    void addThumbnail(MediaThumbnail thumbnail) {
        thumbnails.add(thumbnail);
    }

    /**
     * Returns an unmodifiable list of thumbnails. The return value is never
     * {@code null}. Images are in order of importance.
     */
    public java.util.List<MediaThumbnail> getThumbnails() {
        return java.util.Collections.unmodifiableList(thumbnails);
    }

    /**
     * Returns the values of the unique news item identifier. In case it's not
     * available, the URL is used
     * 
     * @return string value of the guid
     */
    public String getGuid() {
        return this.guid;
    }

    /* Internal method for RSSHandler */
    void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Returns the value of the optional &lt;content:encoded&gt; tag
     * 
     * @return string value of the element data
     */
    public String getContent() {
        return content;
    }

    /* Internal method for RSSHandler */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the list of HTML img links
     * 
     * @return string list of HTML img links
     */
    public java.util.List<String> getContentImages() {
        return this.contentImages;
    }

    /* Internal method for RSSHandler */
    void addContentImage(String image) {
        this.contentImages.add(image);
    }
}
