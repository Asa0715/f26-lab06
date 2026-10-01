package edu.cmu.cs214.booking;

/**
 * Everything needed to create one booking, passed to
 * {@link BookingApi#createBooking(BookingRequest)}.
 *
 * <p>The room and the half-open range {@code [startMinute, endMinute)} are
 * given to the constructor. The waitlist key and the notes are optional and
 * default to null; set them with {@link #withWaitlistKey(String)} and
 * {@link #withNotes(String)}.
 *
 * <p>A request is immutable. Each {@code with} method returns a new request
 * and leaves this one unchanged. A request does not validate its fields; the
 * service does that when the request is submitted.
 */
public final class BookingRequest {

    private final String roomId;
    private final long startMinute;
    private final long endMinute;
    private final String waitlistKey;
    private final String notes;

    /** A request for {@code roomId} over {@code [startMinute, endMinute)}, with no key or notes. */
    public BookingRequest(String roomId, long startMinute, long endMinute) {
        this(roomId, startMinute, endMinute, null, null);
    }

    private BookingRequest(String roomId, long startMinute, long endMinute,
                           String waitlistKey, String notes) {
        this.roomId = roomId;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.waitlistKey = waitlistKey;
        this.notes = notes;
    }

    /** A copy of this request carrying {@code waitlistKey}, or null to decline waitlisting. */
    public BookingRequest withWaitlistKey(String waitlistKey) {
        return new BookingRequest(roomId, startMinute, endMinute, waitlistKey, notes);
    }

    /** A copy of this request carrying {@code notes}, or null for none. */
    public BookingRequest withNotes(String notes) {
        return new BookingRequest(roomId, startMinute, endMinute, waitlistKey, notes);
    }

    public String getRoomId() {
        return roomId;
    }

    public long getStartMinute() {
        return startMinute;
    }

    public long getEndMinute() {
        return endMinute;
    }

    /** The waitlist key, or null if waitlisting is declined. */
    public String getWaitlistKey() {
        return waitlistKey;
    }

    /** The notes, or null if none were given. */
    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return "BookingRequest[" + roomId + " " + startMinute + "-" + endMinute + "]";
    }
}
