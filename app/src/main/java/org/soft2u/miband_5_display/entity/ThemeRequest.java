package org.soft2u.miband_5_display.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.soft2u.miband_5_display.utils.DateConverter;

import java.util.Date;

@Entity(tableName = "tblThemeRequest")
public class ThemeRequest {
    @PrimaryKey
    @ColumnInfo(name = "themeid")
    private int themeid;

    @ColumnInfo(name = "date_add")
    @TypeConverters({DateConverter.class})
    private Date date_add;

    @ColumnInfo(name = "request_id")
    private String request_id;

    @ColumnInfo(name = "request_content")
    private String request_content;

    @ColumnInfo(name = "request_status_code")
    private int request_status_code;



    public int getThemeid() {
        return themeid;
    }

    public void setThemeid(int themeid) {
        this.themeid = themeid;
    }

    public Date getDate_add() {
        return date_add;
    }

    public void setDate_add(Date date_add) {
        this.date_add = date_add;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getRequest_content() {
        return request_content;
    }

    public void setRequest_content(String request_content) {
        this.request_content = request_content;
    }

    public int getRequest_status_code() {
        return request_status_code;
    }

    public void setRequest_status_code(int request_status_code) {
        this.request_status_code = request_status_code;
    }
}