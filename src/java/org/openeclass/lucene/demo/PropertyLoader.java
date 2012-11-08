/**
 * Copyright (c) 2012 by Thanos Kyritsis
 *
 * This file is part of eclass-javasearch.
 *
 * eclass-javasearch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * eclass-javasearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with eclass-javasearch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */
package org.openeclass.lucene.demo;

import java.io.InputStream;
import java.util.Properties;

public abstract class PropertyLoader {

	public static Properties loadProperties(String name, ClassLoader loader) {
        if (name == null)
            throw new IllegalArgumentException("null input: name");
        
        if (name.startsWith("/"))
            name = name.substring(1);
            
        if (name.endsWith (SUFFIX))
            name = name.substring(0, name.length() - SUFFIX.length());
        
        Properties result = null;
        
        InputStream in = null;
        try {
            if (loader == null) loader = ClassLoader.getSystemClassLoader();

            name = name.replace('.', '/');

            if (! name.endsWith(SUFFIX))
                name = name.concat(SUFFIX);

            // returns null on lookup failures:
            in = loader.getResourceAsStream(name);
            if (in != null) {
                result = new Properties();
                result.loadFromXML(in); // can throw IOException
            }
        }
        catch (Exception e) {
            result = null;
        }
        finally {
            if (in != null) try { in.close(); } catch (Throwable ignore) {}
        }
        
        if (THROW_ON_LOAD_FAILURE && (result == null)) {
            throw new IllegalArgumentException("could not load [" + name + "]" + " as a classloader resource");
        }
        
        return result;
    }


    public static Properties loadProperties(final String name) {
        return loadProperties(name, Thread.currentThread().getContextClassLoader());
    }
    
    
    private PropertyLoader() {}
    
    
    private static final boolean THROW_ON_LOAD_FAILURE = true;
    private static final String SUFFIX = ".xml";

}
