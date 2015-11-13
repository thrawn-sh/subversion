package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;

public final class QualifiedResource {

    private final Resource base;

    private final Resource resource;

    public QualifiedResource(final Resource resource) {
        this(Resource.ROOT, resource);
    }

    public QualifiedResource(final Resource base, final Resource resource) {
        this.base = base;
        this.resource = resource;
    }

    public QualifiedResource append(final Resource suffix) {
        return new QualifiedResource(base, resource.append(suffix));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QualifiedResource)) {
            return false;
        }
        QualifiedResource other = (QualifiedResource) obj;
        if (base == null) {
            if (other.base != null) {
                return false;
            }
        } else if (!base.equals(other.base)) {
            return false;
        }
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    public Resource getBase() {
        return base;
    }

    public QualifiedResource getParent() {
        return new QualifiedResource(base, resource.getParent());
    }

    public Resource getResource() {
        return resource;
    }

    public String getValue() {
        return base.append(resource).getValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return base.append(resource).toString();
    }

    public QualifiedResource append(final QualifiedResource suffix) {
        assert (suffix.base == Resource.ROOT);
        return new QualifiedResource(base, resource.append(suffix.resource));
    }
}
