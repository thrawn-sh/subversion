package de.shadowhunt.subversion.xml;

import org.xml.sax.Attributes;

public interface SaxExpression<V> {

    void end(String nameSpaceUri, String localName, int depth, String text);

    void reset();

    void start(String nameSpaceUri, String localName, int depth, Attributes attributes);

    V getValue();
}
