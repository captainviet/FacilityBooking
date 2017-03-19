# FacilityBooking - A client-server application for managing bookings #
~~ Let's get rollin' ~~
## Requirements ##
- (**C**)ommunication
    - The application must implement *UDP communication protocol* (C1)
    - The application must transmit the data in *byte stream* (C2)
- (**S**)emantics
    - The application must be able to switch between **at-least-once** & **at-most-once** invocation scheme (S1)
        - *Timeout mechanism* on clients for resending failed requests
        - *Request filter* on servers for eliminating duplicates
        - *History* on servers for storing saved replies
- (**O**)perations
    - The user must be able to *query availability* for particular facility & days (O1)
    - The user must be able to *book a facility* by the facility's name and pair of start-end time (O2a)
    - The server must *return a confirmation ID* to user upon completing the booking request (O2b)
    - The user must be able to *edit an existing booking* by the confirmation ID & time offset (within 1 hour advancement to 30 min postpone) (O3a)
    - The server must *return a confirmation message* to user upon completing the edit request (O3b)
    - The user must be able to *receive update* (monitor) on a facility during a specified period by the facility's name & start-end time (O4a)
    - The user **must not send any other request** upon sending the request to monitor a facility (as mentioned above) (O4b)
    - The application must implements another *2 addons operations*, of which one is **idempotent**, the other is **non-idempotent** (to be discussed) (O5)
- (**D**)atabase
    - The application must store all *facilities* available for booking (D1)
    - The application must store all *bookings* requested by clients (D2)
    - The application must store all *clients who send monitoring requests* (D3a)
    - The application must remove all clients whose monitoring requests got timeout (D3b)
## Proposed Design ##
Each headings below correspond to a package within the Java project.
### Server ###
Classes to be run on server-side
- **`DayOfWeek`**
    - Attributes: 
        - `DOW.MONDAY(0)` -&gt; `DOW.SUNDAY(6)`
        - `DOW.INVALID(7)`
    - Methods:
        - `public List<DayOfWeek> getDaysInbetween(DayOfWeek day)`: return a list of `DayOfWeek` which contains all the days in-between the invoking day & the parameter *day*
        - `public DayOfWeek getOffsetDay(int offset)`: return the `DayOfWeek` instance that is *offset* days from the invoking instance
        - `public static DayOfWeek valueOf(int code)`: return the `DayOfWeek` instance corresponding to its *code*
- **`Time`**
    - Attributes:
        - `int hour`
        - `int minute`
        - `int totalMinutes`
    - Methods:
        - `public static Time getTime(int hour, int minute)`: return the `Time` instance with the specified *hour* & *minute* as parameter
        - `public Time addOffset(int minute)`: return the `Time` instance that is *minute* difference from the invoking `Time` instance
        - `public int compareTo(Time time)`: return -1, 0, 1 if the *time* instance is greater than, equal, or smaller than the invoking instance
- **`DateTime`**
    - Attributes:
        - `DayOfWeek day`
        - `Time time`
    - Methods:
        - `public static DateTime getDateTime(int day, int hour, int minute)`: return the `DateTime` instance with the specified parameter as its internal representation
        - `public int compareTo(DateTime dateTime)`: return -1, 0, 1 if the *dateTime* instance is greater than, equal, or smaller than the invoking instance
- **`FreeSlot`**
    - Attributes:
        - `Time start`
        - `Time end`
    - Methods:
        - `public static FreeSlot getFreeSlot(Time start, Time end)`: return a `FreeSlot` instance with the specified parameters as its internal representation
        - `public boolean isClashed(Booking booking)`: return true if the booking in the parameter clashes with the invoking booking
- **`Facility`**
    - Attributes:
        - `HashMap<DOW, List<Booking>> timetable`
        - `String name`
    - Methods:
        - `public static Facility getFacilityByName(String facilityName)`: return the `Facility` instance with the specified *name* (unique)
        - `public static boolean addFacility(String name)`: add a facility with the specified *name* in the database, returning true if operation succeeds (added), otherwise false (name already exists)
- **`Booking extends FreeSlot`**
    - Attributes:
        - `int confirmationID`
    - Methods:
        - `public static Booking placeBooking(int confirmationID, Time start, Time end)`: create an instance of booking record with the specified parameters
        - `public static Facility getFacilityBookedByID(int confirmationID)`: find the `Facility` to which the booking with the *confirmationID* belongs to
        - `public boolean isConfirmationIDEqual(int confirmationID)`: return true if the *confirmationID* in the parameter equals the booking's internal *confirmationID*
- **`QueryService`**
    - Attributes:
        - `List<Facility> facilities`
    - Methods
        - `public static void setFacilityList(List<Facility> list)`: replace the `QueryService`'s facility list with the instace specified
        - `public static List<Facility> getAllFacility()`: return the list of `Facility` that the `QueryService` instance is holding
        - `public static List<FreeSlot> getAvailableFacilityByName(String facilityName, DOW day)`: return a list of `FreeSlot` indicating the time available for the specified facility in the specified day
        - `public static long getConfirmationID(String facilityName, DateTime start, DateTime end)`: place a booking of the facility during the period specified, returning the confirmationID if the operation succeeds, otherwise return -1
        - `public static boolean editBookedConfirmation(int confirmationID, int minute)`: advance or postpone the booking identified by the *confirmationID* by *minute* offset (limited by -60 <= minute <= 30), returning true if operation succeeds, false otherwise
- **`MonitorService`** (to be discussed)
    - Attributes:
        - `List&lt;MonitoredSLot&gt; client`
    - Methods
        - `public static boolean registerClient(String ipAddr, int port, String facilityName, Time start, Time end)`: register a client with interest in the specified facility within the specified time range, returning true if operation succeeds, false otherwise
- **`Server`**
    - Communication Protocol
        - `<Client IP Address>|<Message Increment>|<Operation>|<Parameter lists>`
### Client ###
Classes to be run on client-side
### Shared ###
Classes to be shared between server & client
- **NOTE**: (*) indicated incompleted class description
## Work Breakdown ##
- Nhat
    - Implement the communication protocol (C1)
    - Implement the server thread to listen for requests from client (C2)
- Tu
    - Implement database for clients (D3)
    - Implement operations for monitoring (O4), addons (O5)
    - Implement communication semantics switch (S1)
- Vince
    - Implement database for facilities (D1), bookings (D2)
    - Implement operations for querying (O1), booking (O2), editing (O3)