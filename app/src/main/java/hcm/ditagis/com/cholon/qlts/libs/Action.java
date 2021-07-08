package hcm.ditagis.com.cholon.qlts.libs;

/**
 * Created by NGUYEN HONG on 6/15/2018.
 */

public class Action{
    private Boolean isCreate, isDelete, isEdit, isView = false;

    public Action(Boolean isView,Boolean isCreate,Boolean isEdit, Boolean isDelete ) {
        this.isCreate = isCreate;
        this.isDelete = isDelete;
        this.isEdit = isEdit;
        this.isView = isView;
    }

    public Boolean getCreate() {
        return isCreate;
    }

    public void setCreate(Boolean create) {
        isCreate = create;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public Boolean getEdit() {
        return isEdit;
    }

    public void setEdit(Boolean edit) {
        isEdit = edit;
    }

    public Boolean getView() {
        return isView;
    }

    public void setView(Boolean view) {
        isView = view;
    }
}