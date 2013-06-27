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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Internal helper class for date conversions.
 * 
 * @author Mr Horn and Jun Chen, jambit GmbH
 */
final class Dates {

    /**
     * @see <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC 822</a>
     */
    /*
     * private static final SimpleDateFormat RFC822 = new SimpleDateFormat(
     * "EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
     */

    private static final SimpleDateFormat[] RFC3339 = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ") };
    
    private static final SimpleDateFormat RFC822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US);

    /* Hide constructor */
    private Dates() {
    }

    /**
     * Parses string as an RFC 822 date/time.
     * 
     * @throws RSSFault
     *             if the string is not a valid RFC 822 date/time
     */
    static synchronized java.util.Date parseRfc822(String date) {
        try {
            return RFC822.parse(date);
        } catch (ParseException e) {
            throw new RSSFault(e);
        }
    }

    static synchronized java.util.Date parseRfc3339(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            if (date.endsWith("Z")) { // ...Z)
                if (date.length() == 20) // without milliseconds
                    dateFormat = RFC3339[0];
                else
                    dateFormat = RFC3339[1];
            } else if (date.endsWith("00")) { // ...+01:00
                if (date.length() == 25) // without milliseconds
                    dateFormat = RFC3339[2];
                else
                    dateFormat = RFC3339[3];
            } else
                throw new RSSFault("DateFormat Parsing Problem");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RSSFault(e);
        }
    }

}
