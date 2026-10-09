package org.soft2u.miband_5_display.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.soft2u.miband_5_display.utils.DateConverter;

import java.util.Date;

@Entity(tableName = "tblThemeHistory")
public class ThemeHistory {
    @PrimaryKey
    @ColumnInfo(name = "themeid")
    private int themeid;

    @ColumnInfo(name = "date_add")
    @TypeConverters({DateConverter.class})
    private Date date_add;

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
}