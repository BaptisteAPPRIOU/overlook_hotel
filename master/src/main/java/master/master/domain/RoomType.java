package master.master.domain;

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
