package master.master.domain;

/**
 * Lists the room categories supported by both hotel and meeting-space features.
 */
public enum RoomType {
  STANDARD,
  SUPERIOR,
  JUNIOR_SUITE,
  SINGLE,
  DOUBLE,
  TWIN,
  FAMILY_ROOM,
  FAMILY,
  SUITE,
  DELUXE,
  PRESIDENTIAL_SUITE,
  PENTHOUSE,
  CONFERENCE,
  MEETING,
  OFFICE,
  TRAINING,
  BOARDROOM,
  HUDDLE,
  PHONE_BOOTH,
  LOUNGE,
  COLLABORATION,
  PRESENTATION,
  ROOM,
  EVENT;

  /**
   * Checks whether this type represents a hotel bedroom rather than a workspace.
   */
  public boolean isHotelRoom() {
    return switch (this) {
      case STANDARD,
          SUPERIOR,
          DELUXE,
          JUNIOR_SUITE,
          SUITE,
          PRESIDENTIAL_SUITE,
          FAMILY_ROOM,
          FAMILY,
          TWIN,
          DOUBLE,
          SINGLE,
          PENTHOUSE -> true;
      default -> false;
    };
  }
}
