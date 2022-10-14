package jewelrock.irev.com.jewelrock.model;

public class SettingsItem {
    public String title;
    public boolean isOn;
    public boolean editable = false;

    public SettingsItem(String title, boolean isOn, int editable) {
        this.title = title;
        this.isOn = isOn;
        this.editable = editable == 1;
    }
}
