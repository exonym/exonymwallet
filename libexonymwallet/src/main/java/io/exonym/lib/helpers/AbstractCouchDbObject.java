package io.exonym.lib.helpers;

public abstract class AbstractCouchDbObject {

    public static final String FIELD_TYPE = "type";

    private String _id;
    private String _rev;
    protected String type;

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type; }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public String toString(){
        return this._id + " " + this._rev + " " + this.getType();

    }
}
