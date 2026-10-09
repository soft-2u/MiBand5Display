package org.soft2u.miband_5_display.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblThemeUserMaking")
public class ThemeUserMaking {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "base_theme_id")
    private int base_theme_id;

    @ColumnInfo(name = "theme_json")
    private String theme_json;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBase_theme_id() {
        return base_theme_id;
    }

    public void setBase_theme_id(int base_theme_id) {
        this.base_theme_id = base_theme_id;
    }

    public String getTheme_json() {
        return theme_json;
    }

    public void setTheme_json(String theme_json) {
        this.theme_json = theme_json;
    }
}