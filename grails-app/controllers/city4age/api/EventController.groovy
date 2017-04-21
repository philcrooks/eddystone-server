package city4age.api

class EventController {

    def index() {
        respond Event.list()
    }

    def save() {
        def json = request.JSON
        def careReceiver = CareReceiver.findByToken(json.token.toString())
        if (careReceiver) {
            // Note that we have heard from the device
            def device = Device.findByUniqueID(json.uuid.toString())
            if (device) {
                device.lastContact = new Date(Long.valueOf(json.timestamp.toString()))
                device.save(flush: true)
            }

            def beacon = Beacon.findByBeaconId(json.beaconId.toString())
            def event = new Event(
                    eventType: json.eventType,
                    timestamp: new Date(Long.valueOf(json.timestamp.toString())),
                    rssi: json.rssi,
                    parameter: (json.eventType == "found") ? json.txPower : json.rssiMax,
                    beacon: beacon,
                    careReceiver: careReceiver,
                    device: device
            ).save()
            respond(event, status: 201)
        }
        else {
            response.sendError(403, "")
        }
    }
}
