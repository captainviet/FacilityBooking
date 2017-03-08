# FacilityBooking - A client-server application for managing bookings
## Requirements
* (C)ommunication
    * The application must implement UDP communication protocol (C1)
    * The application must transmit the data in byte stream (C2)
* (S)emantics
    * The application must be able to switch between **at-least-once** & **at-most-once** invocation scheme (S1)
        * Timeout mechanism on clients for resending failed requests
        * Request filter on servers for eliminating duplicates
        * History on servers for storing saved replies
* (O)perations
    * The user must be able to query availability for particular facility & days (O1)
    * The user must be able to book a facility by the facility's name and pair of start-end time (O2a)
    * The server must return a confirmation ID to user upon completing the booking request (O2b)
    * The user must be able to edit an existing booking by the confirmation ID & time offset (either -1||+0.5 hours) (O3a)
    * The server must return a confirmation message to user upon completing the edit request (O3b)
    * The user must be able to receive update (monitor) on a facility during a specified period by the facility's name & start-end time (O4a)
    * The user must not send any other request upon sending the request to monitor a facility (as mentioned above) (O4b)
    * The application must implements another 2 addons operations, of which one is idempotent, the other is non-idempotent (to be discussed) (O5)
* (D)atabase
    * The application must store all facilities available for booking (D1)
    * The application must store all bookings requested by clients (D2)
    * The application must store all clients who send monitoring requests (D3a)
    * The application must remove all clients whose monitoring requests got timeout (D3b)
## Proposed Design
Each headings below correspond to a package within the Java project.
### Server
Classes to be run on server-side
* DOW (Days of Week) (*)
    * Attributes: DOW.MONDAY -&gt; DOW.SUNDAY
    * Methods:
* Facility (*)
    * static Facility getFacilityByName(String facilityName)
* QueryService
    * Attributes: List&lt;Facility&gt; facilities, List&lt;Booking&gt; booking
    * Methods
        * void getAvailableFacilityByDay(DOW day)
        * void getAvailableFacilityByName(String facilityName)
        * void getAvailableFacilityByName(String facilityName, DOW day)
        * long getConfirmationID(String facilityName, Date start, Date end)
        * boolean editBookedConfirmation(long confirmationID, int minutes)
* InterestedSlot: (*)
    * Attributes: Facility
    * Methods:
* MonitorService
    * Attributes: MonitoringClient
    * Methods
        * registerClient(String ipAddr, int port, String facilityName, Date start, Date end)
        * List&lt;Client&gt; getInterestedClient(Facility facility, Date start, Date end)
* Main
    * Communication Protocol
        * {Client IP Address}|{Message Increment}|{Action}|{Parameter lists}
### Client
Classes to be run on client-side
### Shared
Classes to be shared between server & client
* **NOTE**: (*) indicated incompleted class description
## Work Breakdown
* Nhat
    * Implement the communication protocol (C1)
    * Implement the server thread to listen for requests from client (C2)
* Tu
    * Implement database for clients (D3)
    * Implement operations for monitoring (O4), addons (O5)
    * Implement communication semantics switch (S1)
* Vince
    * Implement database for facilities (D1), bookings (D2)
    * Implement operations for querying (O1), booking (O2), editing (O3)