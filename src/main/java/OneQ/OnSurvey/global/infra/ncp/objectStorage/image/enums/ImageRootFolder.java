package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums;

public enum ImageRootFolder {
    PUBLIC;

    public String dir() {
        return name().toLowerCase();
    }
}
