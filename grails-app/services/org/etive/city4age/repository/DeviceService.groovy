package org.etive.city4age.repository

import grails.transaction.Transactional

@Transactional
class DeviceService {

    def createDevice(json) {
        def careReceiver = CareReceiver.findByToken(json.token.toString())
        if (!careReceiver) return null //throw 403

        def device = Device.findByUniqueId(json.uuid.toString())
        if (device) {
            device.careReceiver = careReceiver
            device.osVersion = json.osVersion.toString()
            device.lastContact = new Date(Long.valueOf(json.timestamp.toString()))
        } else {
            device = new Device(
                    operatingSystem: json.os,
                    osVersion: json.osVersion,
                    model: json.model,
                    uniqueId: json.uuid,
                    careReceiver: careReceiver,
                    lastContact: new Date(Long.valueOf(json.timestamp.toString())),
            )
        }
        device.save()
        return device
    }

    def updateLastContact(json) {
        def careReceiver = CareReceiver.findByToken(json.token.toString())
        if (!careReceiver) return null //throw 403

        def device = Device.findByUniqueId(params.uuid.toString())
        if (device) {
            device.lastContact = new Date(Long.valueOf(json.timestamp.toString()))
            device.save()
        }
        return device
    }
}