package de.shadowhunt.subversion;

import org.apache.commons.lang3.Validate;

public final class LockToken {

    private final String token;

    public LockToken(final String token) {
        Validate.notBlank(token, "token must not be null or blank");
        this.token = token;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LockToken)) {
            return false;
        }

        final LockToken lockToken = (LockToken) o;

        if (!token.equals(lockToken.token)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return token;
    }
}
