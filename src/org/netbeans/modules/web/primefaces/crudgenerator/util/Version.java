/*
 * This software is distributied under the GPL Version 2 license. The license
 * text is accompanied with this module.
 */

package org.netbeans.modules.web.primefaces.crudgenerator.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a String-based Version that can be compared to other versions.
 * Version cannot contain any characters or [NumberFormatException] will be
 * thrown.
 *
 * @author Kay Wrobel
 * @version 1.0
 */
public final class Version implements Comparable<Version> {

    // private static final String ACCEPTED_VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*";
    private static final String ACCEPTED_VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*(\\.[\\w]+)*";
    private static final String DEFAULT_DELIMITER = ".";
    private String version;
    private String delimiter;
    private List<Integer> versionArtifacts;

    /**
     * Returns a list of version integers. This is being used for comparisons
     * by the compareTo method.
     * @return
     */
    public List<Integer> getVersionArtifacts() {
        return versionArtifacts;
    }

    private void setVersionArtifacts() {
        versionArtifacts = new ArrayList<Integer>();
        for (String artifact : this.version.split(this.delimiter)) {
            try {
                int parseInt = Integer.parseInt(artifact);
                versionArtifacts.add(parseInt);
            } catch (NumberFormatException ex) {
                break;
            }
        }
    }

    /**
     * Returns the delimiter currently being used by this Version.
     * @return
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the delimiter to be used by this Version for traversing during
     * comparisons. Also internally initializes a version artifact ArrayList.
     * @param delimiter
     */
    public void setDelimiter(String delimiter) {
        // Add escape sequence in case it was not provided. This is important
        // for the regex matching to work properly.
        if (delimiter.contains("\\")) {
            this.delimiter = delimiter;
        } else {
            this.delimiter = "\\".concat(delimiter);
        }
        setVersionArtifacts(); // Turn version string into parsable Integers
    }

    @Override
    public String toString() {
        return this.version;
    }

    /**
     * Instantiates a Version with a given version string and sets a default
     * delimiter to be used for traversing versions in comparisons.
     * @param version
     * @throws IllegalArgumentException
     */
    public Version(String version) throws IllegalArgumentException {
        if (version == null) {
            throw new IllegalArgumentException("Version can not be null");
        }
        if (!version.matches(ACCEPTED_VERSION_PATTERN)) {
            throw new IllegalArgumentException("Invalid version format");
        }
        this.version = version;
        setDelimiter(DEFAULT_DELIMITER);
    }

    @Override
    public int compareTo(Version that) {
        if (versionArtifacts.isEmpty()) {
            throw new NumberFormatException("Source Version did not contain any parsable artifacts. Check your delimiter.");
        }
        if (that.getVersionArtifacts().isEmpty()) {
            throw new NumberFormatException("Target Version did not contain any parsable artifacts. Check your delimiter.");
        }

        int thisSize = versionArtifacts.size();
        int thatSize = that.getVersionArtifacts().size();
        int maxSize = Math.max(versionArtifacts.size(), that.getVersionArtifacts().size());
        for (int i = 0; i < maxSize; i++) {
            Integer thisArtifact = i < thisSize ? versionArtifacts.get(i) : 0;
            Integer thatArtifact = i < thatSize ? that.getVersionArtifacts().get(i) : 0;
            if (thisArtifact < thatArtifact) {
                return -1;
            } //Source version is smaller
            if (thisArtifact > thatArtifact) {
                return +1;
            } //Source version is larger
        }
        return 0;
    }
    
    public int compareTo(String that) {
        return compareTo(new Version(that));
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (this.getClass() != that.getClass()) {
            return false;
        }
        return (this.compareTo((Version) that) == 0);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 53 * hash + (this.delimiter != null ? this.delimiter.hashCode() : 0);
        return hash;
    }
}
