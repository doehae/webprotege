package edu.stanford.bmir.protege.web.server.form;

import com.google.common.base.Objects;
import edu.stanford.bmir.protege.web.shared.form.CollectionId;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import org.mongodb.morphia.annotations.*;

import javax.annotation.Nonnull;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 13 Jul 2017
 *
 * Stores details about a WebProtege collection.
 */
@Entity(noClassnameStored = true, value = "CollectionDetails")
@Indexes({
        @Index(fields = {@Field("projectId"), @Field("collectionId")},
               options = @IndexOptions(unique = true))
         })
public class CollectionDetails {

    @Nonnull
    private ProjectId projectId;

    @Nonnull
    private CollectionId collectionId;

    @Nonnull
    private String description;

    /**
     * Creates a {@link CollectionDetails}.
     * @param projectId The project to which the collection belongs.
     * @param collectionId The collection id.  The collection id is a human readable name for the collection e.g.
     *                     "Boeing Aircraft" or "Amino Acids" or "747 Models".
     * @param description An optional description of the collection.  Use the empty string to indicate no name
     */
    public CollectionDetails(@Nonnull ProjectId projectId,
                             @Nonnull CollectionId collectionId,
                             @Nonnull String description) {
        this.projectId = checkNotNull(projectId);
        this.collectionId = checkNotNull(collectionId);
        this.description = checkNotNull(description);
    }

    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    @Nonnull
    public CollectionId getCollectionId() {
        return collectionId;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, collectionId, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CollectionDetails)) {
            return false;
        }
        CollectionDetails other = (CollectionDetails) obj;
        return this.projectId.equals(other.projectId)
                && this.collectionId.equals(other.collectionId)
                && this.description.equals(other.description);
    }


    @Override
    public String toString() {
        return toStringHelper("CollectionDetails")
                .addValue(projectId)
                .addValue(collectionId)
                .add("description", description)
                .toString();
    }
}
