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

import org.jsoup.nodes.Element;
import org.xml.sax.Attributes;

/**
 * Internal SAX handler to efficiently parse RSS feeds. Only a single thread
 * must use this SAX handler.
 * 
 * Extended to support ATOM feeds
 * 
 * @author Mr Horn and Jun Chen, jambit GmbH
 */
class RSSHandler extends org.xml.sax.helpers.DefaultHandler {

    /**
     * Constant for XML element name which identifies RSS items.
     */
    private static final String RSS_ITEM = "item";

    private static final String ATOM_ITEM = "entry";
    /**
     * Constant symbol table to ensure efficient treatment of handler states.
     */
    private final java.util.Map<String, Setter> setters;

    /**
     * Reference is never {@code null}. Visibility must be package-private to
     * ensure efficiency of inner classes.
     */
    final RSSFeed feed = new RSSFeed();

    /**
     * Reference is {@code null} unless started to parse &lt;item&gt; element.
     * Visibility must be package-private to ensure efficiency of inner classes.
     */
    RSSItem item;

    /**
     * If not {@code null}, then buffer the characters inside an XML text
     * element.
     */
    private StringBuilder buffer;

    /**
     * Dispatcher to set either {@link #feed} or {@link #item} fields.
     */
    private Setter setter;

    /**
     * Interface to store information about RSS elements.
     */
    private static interface Setter {
    }

    /**
     * Closure to change fields in POJOs which store RSS content.
     */
    private static interface ContentSetter extends Setter {

        /**
         * Set the field of an object which represents an RSS element.
         */
        void set(String value);

    }

    /**
     * Closure to change fields in POJOs which store information about RSS
     * elements which have only attributes.
     */
    private static interface AttributeSetter extends Setter {

        /**
         * Set the XML attributes.
         */
        void set(org.xml.sax.Attributes attributes);

    }

    /**
     * Setter for RSS &lt;title&gt; elements inside a &lt;channel&gt; or an
     * &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_TITLE = new ContentSetter() {
        @Override
        public void set(String title) {
            if (item == null) {
                if (feed.getTitle() == null)
                    feed.setTitle(title);
            } else {
                if (item.getTitle() == null)
                    item.setTitle(title);
            }
        }
    };

    /**
     * Setter for RSS &lt;description&gt; elements inside a &lt;channel&gt; or
     * an &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_DESCRIPTION = new ContentSetter() {
        @Override
        public void set(String description) {
            if (item == null) {
                feed.setDescription(description);
            } else {
                item.setDescription(description);
            }
        }
    };

    /**
     * Setter for an RSS &lt;content:encoded&gt; element inside an &lt;item&gt;
     * element.
     */
    private final Setter SET_CONTENT = new ContentSetter() {
        @Override
        public void set(String content) {
            if (item != null) {
                item.setContent(content);
            }
        }
    };

    /**
     * Setter for RSS &lt;link&gt; elements inside a &lt;channel&gt; or an
     * &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_LINK = new ContentSetter() {
        @Override
        public void set(String link) {
            final android.net.Uri uri = android.net.Uri.parse(link);
            if (link.length() > 0) {
                if (item == null) {
                    if (feed.getLink() == null)
                        feed.setLink(uri);
                } else {
                    if (item.getLink() == null)
                        item.setLink(uri);
                }
            }
        }
    };

    /**
     * Setter for RSS &lt;link&gt; elements inside a &lt;channel&gt; or an
     * &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_LINKATTRIBUTE = new AttributeSetter() {
        @Override
        public void set(Attributes attributes) {
            final android.net.Uri uri = android.net.Uri.parse(attributes.getValue("href"));
            if (uri != null && uri.toString().length() > 0) {
                if (item == null) {
                    if (feed.getLink() == null)
                        feed.setLink(uri);
                } else {
                    if (item.getLink() == null)
                        item.setLink(uri);
                }
            }

        }
    };

    /**
     * Setter for RSS &lt;guid&gt; elements inside a &lt;channel&gt; or an
     * &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_GUID = new ContentSetter() {
        @Override
        public void set(String guid) {
            if (item != null)
                item.setGuid(guid);
        }
    };

    /**
     * Setter for RSS &lt;pubDate&gt; elements inside a &lt;channel&gt; or an
     * &lt;item&gt; element. The title of the RSS feed is set only if
     * {@link #item} is {@code null}. Otherwise, the title of the RSS
     * {@link #item} is set.
     */
    private final Setter SET_PUBDATE = new ContentSetter() {
        @Override
        public void set(String pubDate) {
            final java.util.Date date = Dates.parseRfc822(pubDate);
            if (item == null) {
                feed.setPubDate(date);
            } else {
                item.setPubDate(date);
            }
        }
    };

    private final Setter SET_UPDATE = new ContentSetter() {
        @Override
        public void set(String upDate) {
            final java.util.Date date = Dates.parseRfc3339(upDate);
            if (item == null) {
                feed.setPubDate(date);
            } else {
                item.setPubDate(date);
            }
        }
    };

    private final Setter SET_TTL = new ContentSetter() {
        @Override
        public void set(String ttl) {
            feed.setTtl(Integer.valueOf(ttl));
        }
    };

    /**
     * Setter for one or multiple RSS &lt;category&gt; elements inside a
     * &lt;channel&gt; or an &lt;item&gt; element. The title of the RSS feed is
     * set only if {@link #item} is {@code null}. Otherwise, the title of the
     * RSS {@link #item} is set.
     */
    private final Setter ADD_CATEGORY = new ContentSetter() {

        @Override
        public void set(String category) {
            if (item == null) {
                feed.addCategory(category);
            } else {
                item.addCategory(category);
            }
        }
    };

    /**
     * Setter for one or multiple RSS &lt;media:thumbnail&gt; elements inside an
     * &lt;item&gt; element. The thumbnail element has only attributes. Both its
     * height and width are optional. Invalid elements are ignored.
     */
    private final Setter ADD_MEDIA_THUMBNAIL = new AttributeSetter() {

        private static final String MEDIA_THUMBNAIL_HEIGHT = "height";
        private static final String MEDIA_THUMBNAIL_WIDTH = "width";
        private static final String MEDIA_THUMBNAIL_URL = "url";
        private static final int DEFAULT_DIMENSION = -1;

        @Override
        public void set(org.xml.sax.Attributes attributes) {
            if (item == null) {
                // ignore invalid media:thumbnail elements which are not inside
                // item
                // elements
                return;
            }

            final int height = MediaAttributes.intValue(attributes, MEDIA_THUMBNAIL_HEIGHT, DEFAULT_DIMENSION);
            final int width = MediaAttributes.intValue(attributes, MEDIA_THUMBNAIL_WIDTH, DEFAULT_DIMENSION);
            final String url = MediaAttributes.stringValue(attributes, MEDIA_THUMBNAIL_URL);

            if (url == null) {
                // ignore invalid media:thumbnail elements which have no URL.
                return;
            }

            item.addThumbnail(new MediaThumbnail(android.net.Uri.parse(url), height, width));
        }

    };

    /**
     * Use configuration to optimize initial capacities of collections
     */
    private final RSSConfig config;

    /**
     * Instantiate a SAX handler which can parse a subset of RSS 2.0 feeds.
     * 
     * @param config
     *            configuration for the initial capacities of collections
     */
    RSSHandler(RSSConfig config) {
        this.config = config;

        // initialize dispatchers to manage the state of the SAX handler
        setters = new java.util.HashMap<String, Setter>(/* 2^3 */8);
        setters.put("title", SET_TITLE);

        setters.put("link", SET_LINK);
        setters.put("feedburner:origLink", SET_LINK);

        setters.put("description", SET_DESCRIPTION); // RSS
        setters.put("summary", SET_DESCRIPTION); // ATOM

        setters.put("pubDate", SET_PUBDATE); // RSS
        setters.put("updated", SET_UPDATE); // ATOM also published
        setters.put("dc:date", SET_UPDATE);

        setters.put("content:encoded", SET_CONTENT); // RSS
        setters.put("content", SET_CONTENT); // ATOM

        setters.put("guid", SET_GUID); // RSS
        setters.put("id", SET_GUID); // ATOM

        setters.put("ttl", SET_TTL); // RSS ONLY
        setters.put("category", ADD_CATEGORY);

        setters.put("media:thumbnail", ADD_MEDIA_THUMBNAIL);
        setters.put("enclosure", ADD_MEDIA_THUMBNAIL);

    }

    /**
     * Returns the RSS feed after this SAX handler has processed the XML
     * document.
     */
    RSSFeed feed() {
        return feed;
    }

    /**
     * Identify the appropriate dispatcher which should be used to store XML
     * data in a POJO. Unsupported RSS 2.0 elements are currently ignored.
     */
    @Override
    public void startElement(String nsURI, String localName, String qname, org.xml.sax.Attributes attributes) {
        // Lookup dispatcher in hash table
        setter = setters.get(qname);
        if (qname.equals("link")) {
            String href = attributes.getValue("href");
            if (href != null)
                setter = SET_LINKATTRIBUTE;
        }

        if (setter == null) {
            if (RSS_ITEM.equals(qname) || ATOM_ITEM.equals(qname)) {
                item = new RSSItem(config.categoryAvg, config.thumbnailAvg);
            }
        } else if (setter instanceof AttributeSetter) {
            ((AttributeSetter) setter).set(attributes);
        } else {
            // Buffer supported RSS content data
            buffer = new StringBuilder();
        }
    }

    @Override
    public void endElement(String nsURI, String localName, String qname) {
        if (isBuffering()) {
            // set field of an RSS feed or RSS item
            if (qname.equals("content:encoded") || qname.equals("content") || qname.equals("summary")
                    || qname.equals("description")) {
                // remove images
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(buffer.toString());
                org.jsoup.nodes.Element firstImg = null;
                org.jsoup.select.Elements bodyChildren = doc.body().children();
                try {
                    if (bodyChildren != null) {
                        org.jsoup.nodes.Element imgParent = doc.body().children().first();
                        if (imgParent != null) {
                            org.jsoup.select.Elements selection;
                            if (!(selection = imgParent.select("img[src]")).isEmpty())
                                firstImg = selection.first();
                        }
                    }
                    if (firstImg != null && item != null
                            && (item.getThumbnails() == null || item.getThumbnails().size() < 1)) {
                        android.net.Uri url = android.net.Uri.parse(firstImg.attr("abs:src"));
                        String height = firstImg.attr("height");
                        int heightInt = -1;
                        String width = firstImg.attr("width");
                        int widthInt = -1;
                        if (height.length() > 0)
                            heightInt = Integer.valueOf(height).intValue();
                        if (width.length() > 0)
                            widthInt = Integer.valueOf(width).intValue();

                        if (heightInt > de.telekom.cldii.ApplicationConstants.MIN_IGNOREHEIGHT
                                && widthInt > de.telekom.cldii.ApplicationConstants.MIN_IGNOREHEIGHT
                                || (heightInt + widthInt) < -1) {
                            MediaThumbnail thumbnail = new MediaThumbnail(url, heightInt, widthInt);
                            item.addThumbnail(thumbnail);
                        }
                    }
                } catch (NullPointerException e) {
                    android.util.Log.w("Handler", "There was something strange in the feed content!");
                }

                // remove all images
                doc.select("img").remove();

                // start from the end and remove all space, break or empty tags
                Element element = doc.getAllElements().last();
                while ((element.tagName().equals("br") || element.tagName().equals("a") || element.tagName()
                        .equals("p")) && element.childNodes().isEmpty()) {
                    element.unwrap();
                    element = doc.getAllElements().last();
                }

                // unwrap a top p tag
                if (doc.select("p").first() != null)
                    doc.select("p").unwrap();

                // android.util.Log.w("Handler", "html: " + doc.toString());

                // for (Element ele : doc.getAllElements()) {
                // android.util.Log.e("Handler", "Element: " + ele.tagName() +
                // ": " + ele.toString() + " | " + ele.children());
                // }
                element = null;
                ((ContentSetter) setter).set(doc.toString());
            } else
                ((ContentSetter) setter).set(buffer.toString());

            // clear buffer
            buffer = null;
        } else if (RSS_ITEM.equals(qname) || ATOM_ITEM.equals(qname)) {
            feed.addItem(item);

            // (re)enter <channel> scope
            item = null;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (isBuffering()) {
            buffer.append(ch, start, length);
        }
    }

    /**
     * Determines if the SAX parser is ready to receive data inside an XML
     * element such as &lt;title&gt; or &lt;description&gt;.
     * 
     * @return boolean {@code true} if the SAX handler parses data inside an XML
     *         element, {@code false} otherwise
     */
    boolean isBuffering() {
        return buffer != null && setter != null;
    }

}
