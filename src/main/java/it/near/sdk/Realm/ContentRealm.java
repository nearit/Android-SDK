package it.near.sdk.Realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import it.near.sdk.Models.Content;
import it.near.sdk.Realm.wrapper.RealmListConverter;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * Create a MatchingRealm object, used to persist on Realm (device persistence)
 *
 */
public class ContentRealm extends RealmObject {

    @PrimaryKey
    String id;

    @Required
    String title;

    String shortDescription;

    @Required
    String longDescription;

    Boolean trashed;

    RealmList<RealmString> photoIds = new RealmList<RealmString>();

    public ContentRealm() {}

    /**
     * Initialize a ContentRealm with data's from a content model
     * @param content
     */
    public ContentRealm(Content content) {
        id = content.getId();
        title = content.getTitle();
        shortDescription = content.getShortDescription();
        longDescription = content.getLongDescription();
        trashed = content.getTrashed();

        for (String id : content.getPhotoIds()) {
            photoIds.add(new RealmString(id));
        }

    }

    /**
     * Convert the ContentRealm to a Content model
     * @return converted content
     */
    public Content convertToModel() {

        Content content = new Content();

        content.setId(id);
        content.setTitle(title);
        content.setShortDescription(shortDescription);
        content.setLongDescription(longDescription);
        content.setTrashed(trashed);
        content.setPhotoIds(RealmListConverter.convertToStringList(photoIds));

        return content;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public RealmList<RealmString> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(RealmList<RealmString> photoIds) {
        this.photoIds = photoIds;
    }
}
