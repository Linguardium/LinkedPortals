package mod.linguardium.linkedportals.exceptions;

public class PortalSearchExceptions extends Exception {
    public static class PortalControllerNotFoundException extends PortalSearchExceptions {}
    public static class WorldNotFoundException extends PortalSearchExceptions {}
    public static class CannotFindAnotherPortalException extends PortalSearchExceptions {}
    public static class PortalNotFoundException extends PortalSearchExceptions {}
    public static class InvalidEntryInPortalsList extends PortalSearchExceptions {}
}
