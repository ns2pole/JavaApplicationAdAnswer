//prac28
public enum Status {
    UNHANDLED("未対応"),
    PROCESSING("処理中"),
    DONE("完了"),
    UNDER_VERIFICATION("検証中");

    private final String displayValue;

    Status(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}