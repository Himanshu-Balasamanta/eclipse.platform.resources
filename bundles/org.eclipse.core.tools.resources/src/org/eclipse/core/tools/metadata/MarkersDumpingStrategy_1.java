/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A strategy for reading .markers files version 1. Layout:
 * <pre>
 * SAVE_FILE -> VERSION_ID RESOURCE+
 * VERSION_ID -> 
 * RESOURCE -> RESOURCE_PATH MARKERS_SIZE MARKER*
 * RESOURCE_PATH -> String
 * MARKERS_SIZE -> int
 * MARKER -> MARKER_ID TYPE ATTRIBUTES_SIZE ATTRIBUTE*
 * MARKER_ID -> long
 * TYPE -> INDEX | QNAME
 * INDEX -> int int
 * QNAME -> int String
 * ATTRIBUTES_SIZE -> int
 * ATTRIBUTE -> ATTRIBUTE_KEY ATTRIBUTE_VALUE
 * ATTRIBUTE_KEY -> String
 * ATTRIBUTE_VALUE -> INTEGER_VALUE | BOOLEAN_VALUE | STRING_VALUE | NULL_VALUE
 * INTEGER_VALUE -> int int
 * BOOLEAN_VALUE -> int boolean
 * STRING_VALUE -> int String
 * NULL_VALUE -> int
 * </pre>
 */
public class MarkersDumpingStrategy_1 implements IStringDumpingStrategy {

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#dumpStringContents(DataInputStream)
	 */
	public String dumpStringContents(DataInputStream dataInput) throws Exception {
		StringBuffer contents = new StringBuffer();
		List markerTypes = new ArrayList();
		while (dataInput.available() > 0) {
			String resourceName = dataInput.readUTF();
			contents.append("Resource: "); //$NON-NLS-1$
			contents.append(resourceName);
			contents.append('\n');
			dumpMarkers(dataInput, contents, markerTypes);
			contents.append('\n');
		}
		return contents.toString();
	}

	private void dumpMarkers(DataInputStream input, StringBuffer contents, List markerTypes) throws IOException, DumpException {
		int markersSize = input.readInt();
		contents.append("Markers ["); //$NON-NLS-1$
		contents.append(markersSize);
		contents.append("]:"); //$NON-NLS-1$
		contents.append('\n');
		for (int i = 0; i < markersSize; i++) {
			contents.append("ID: "); //$NON-NLS-1$
			contents.append(input.readLong());
			contents.append('\n');
			dumpMarkerType(input, contents, markerTypes);
			dumpAttributes(input, contents);
			contents.append('\n');
		}
	}

	private void dumpAttributes(DataInputStream input, StringBuffer contents) throws IOException, DumpException {
		int attributesSize = input.readInt();
		contents.append("Attributes ["); //$NON-NLS-1$
		contents.append(attributesSize);
		contents.append("]:"); //$NON-NLS-1$
		contents.append('\n');
		for (int j = 0; j < attributesSize; j++) {
			contents.append(input.readUTF());
			int type = input.readInt();
			Object value = null;
			switch (type) {
				case MarkersDumper.ATTRIBUTE_INTEGER :
					value = new Integer(input.readInt());
					break;
				case MarkersDumper.ATTRIBUTE_BOOLEAN :
					value = input.readBoolean() ? Boolean.TRUE : Boolean.FALSE;
					break;
				case MarkersDumper.ATTRIBUTE_STRING :
					value = "\"" + input.readUTF() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case MarkersDumper.ATTRIBUTE_NULL :
					break;
				default :
					throw new DumpException("Invalid marker attribute type found: " + type); //$NON-NLS-1$
			}
			contents.append('=');
			contents.append(value);
			contents.append('\n');
		}
	}

	private void dumpMarkerType(DataInputStream input, StringBuffer contents, List markerTypes) throws IOException, DumpException {
		String markerType;
		int constant = input.readInt();
		switch (constant) {
			case MarkersDumper.QNAME :
				markerType = input.readUTF();
				markerTypes.add(markerType);
				break;
			case MarkersDumper.INDEX :
				markerType = (String) markerTypes.get(input.readInt());
				break;
			default :
				throw new DumpException("Invalid marker type constant found: " + constant); //$NON-NLS-1$
		}
		contents.append("Marker Type: "); //$NON-NLS-1$
		contents.append(markerType);
		contents.append('\n');
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IStringDumpingStrategy#getFormatDescription()
	 */
	public String getFormatDescription() {
		return "Markers snapshot file version 1"; //$NON-NLS-1$
	}
}