package org.etive.city4age.repository

class ProximityEventController {
    def proximityEventService
    def deviceService

    def index() {
        def receiver = CareReceiver.findById(params.receiverId)
        def list = proximityEventService.listProximityEvents(receiver)
        respond(list, status: 200)
    }

    def save() {
        def json = request.JSON
        def careReceiver = CareReceiver.findByToken(json.token as String)
        def device = Device.findByUniqueId(json.uuid as String)
        if (careReceiver && device && device.careReceiver.id == careReceiver.id) {
            def beacon = Beacon.findByBeaconId(json.beaconId as String)
            if (beacon) {
                def event = proximityEventService.createProximityEvent(careReceiver, beacon, device, json)
                // Note that we have heard from the device
                deviceService.updateLastContact(json.uuid as String, json.timestamp as Long)
                respond(event, status: 201)
            }
            else {
                response.sendError(409, "")
            }
        }
        else {
            response.sendError(403, "")
        }
    }
}
