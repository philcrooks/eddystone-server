package org.etive.city4age.repository

import org.etive.city4age.withings.WithingsService

class CareReceiver {

    static private final String startDate = "2017-01-01" // The first date used to retrieve Withings data

    String emailAddress
    String token
    Long logbookId
    String city4ageId = "<unknown>"
    Long withingsId
    String accessKey
    String accessSecret
    String activityRecordsDownloaded = (getStartDate() - 1).format("yyyy-MM-dd") // The day before startDate
    String sleepRecordsDownloaded = (getStartDate() - 1).format("yyyy-MM-dd")
    Date eventsGenerated
    Boolean forTest = false // This Care Receiver's data is for test only and should not be uploaded
    Date dateCreated
    Date lastUpdated

    static hasMany = [device: Device, proximityEvents: ProximityEvent, activityRecords: ActivityRecord, sleepRecords: SleepRecord]

    static constraints = {
        emailAddress blank: false, nullable: false, email: true, unique: true
        token blank: false, nullable: false, unique: true
        proximityEvents nullable: true
        logbookId nullable: false, unique: true
        city4ageId blank: false, nullable: true
        device nullable: true
        withingsId nullable: false
        accessKey blank: false, nullable: false
        accessSecret blank: false, nullable: false
        activityRecordsDownloaded nullable: false, blank: false
        sleepRecordsDownloaded nullable: false, blank: false
        eventsGenerated nullable: true
        forTest nullable: false
    }

    private fetchActivityData(Date startDate, Date endDate = null) {
        if (endDate && (startDate.format("yyyy-MM-dd") == endDate.format("yyyy-MM-dd"))) endDate = null
        return WithingsService.instance.fetchActivityData(this, startDate, endDate)
    }

    private fetchSleepData(Date startDate, Date endDate = null) {
        return WithingsService.instance.fetchSleepData(this, startDate, endDate)
    }

    def fetchWithingsData(Date startDate, Date endDate = null) {
        return [ activity: fetchActivityData(startDate, endDate), sleep: fetchSleepData(startDate + 1, endDate) ]
    }

    def updateWithingsData(Date endDate) {
        def activities = []
        def sleeps = []

        def sEndDate = endDate.format("yyyy-MM-dd")
        def activityDate = Date.parse("yyyy-MM-dd", this.activityRecordsDownloaded) + 1
        def sActivityDate = activityDate.format("yyyy-MM-dd")
        def sleepDate = Date.parse("yyyy-MM-dd", this.sleepRecordsDownloaded) + 1
        def sSleepDate = sleepDate.format("yyyy-MM-dd")

        if (sEndDate >= sActivityDate) activities = fetchActivityData(activityDate, endDate)
        if (sEndDate >= sSleepDate) sleeps = fetchSleepData(sleepDate, endDate)

        return [ activity: activities, sleep: sleeps ]
    }

    static Date getStartDate() {
        return Date.parse("yyyy-MM-dd", startDate)
    }
}