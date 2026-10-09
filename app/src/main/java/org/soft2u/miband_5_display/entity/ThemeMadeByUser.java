package org.soft2u.miband_5_display.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.soft2u.miband_5_display.utils.DateConverter;

import java.util.Date;

@Entity(tableName = "tblThemeMadeByUser")
public class ThemeMadeByUser {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "publish_id")
    private String publish_id;

    @ColumnInfo(name = "date_add")
    @TypeConverters({DateConverter.class})
    private Date date_add;

    @ColumnInfo(name = "cover_url")
    private String cover_url;

    @ColumnInfo(name = "public_status_code")
    private int public_status_code;

    public String getPublish_id() {
        return publish_id;
    }

    public void setPublish_id(String publish_id) {
        this.publish_id = publish_id;
    }

    public Date getDate_add() {
        return date_add;
    }

    public void setDate_add(Date date_add) {
        this.date_add = date_add;
    }

    public String getCover_url() {
        return cover_url;
    }

    public void setCover_url(String cover_url) {
        this.cover_url = cover_url;
    }

    public int getPublic_status_code() {
        return public_status_code;
    }

    public void setPublic_status_code(int public_status_code) {
        this.public_status_code = public_status_code;
    }
}