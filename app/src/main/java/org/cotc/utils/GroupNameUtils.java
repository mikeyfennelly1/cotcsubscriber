package org.cotc.utils;

import org.cotc.exception.InvalidGroupNameException;
import org.springframework.stereotype.Component;

@Component
public class GroupNameUtils {
    public NameBuilder builder(String rootName) {
        return new NameBuilder(rootName);
    }

    public static class NameBuilder {
        private final StringBuilder fullName;

        private NameBuilder(String rootName) {
            this.fullName = new StringBuilder().append(rootName);
        }

        public void addName(String name) throws InvalidGroupNameException {
            InvalidGroupNameException.validate(name);
            fullName.append(".").append(name);
        }

        public String build() {
            return fullName.toString();
        }
    }

    public String getParentName(String name) {
        return name.substring(0, name.indexOf('.'));
    }

    public boolean isRootName(String name) {
        return !name.contains(".");
    }
}
