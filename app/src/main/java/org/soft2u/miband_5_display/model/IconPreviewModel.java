package org.soft2u.miband_5_display.model;

public class IconPreviewModel {
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public IconPreviewModel getNext() {
        return next;
    }

    public void setNext(IconPreviewModel next) {
        this.next = next;
    }

    public IconPreviewModel getPrev() {
        return prev;
    }

    public void setPrev(IconPreviewModel prev) {
        this.prev = prev;
    }

    public IconPreviewModel getEnter() {
        return enter;
    }

    public void setEnter(IconPreviewModel enter) {
        this.enter = enter;
    }

    public IconPreviewModel getBack() {
        return back;
    }

    public void setBack(IconPreviewModel back) {
        this.back = back;
    }

    private String fileName;
	private IconPreviewModel next;
	private IconPreviewModel prev;
	private IconPreviewModel enter;
    private IconPreviewModel back;
}