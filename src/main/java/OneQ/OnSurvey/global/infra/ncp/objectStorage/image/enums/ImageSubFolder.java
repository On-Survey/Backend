package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums;

public enum ImageSubFolder {
    MEMBER,
    SURVEY;

    public String dir() {
        return name().toLowerCase();
    }
}
