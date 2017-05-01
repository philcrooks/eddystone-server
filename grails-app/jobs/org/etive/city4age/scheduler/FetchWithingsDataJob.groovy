package org.etive.city4age.scheduler

import org.etive.city4age.repository.CareReceiver

class FetchWithingsDataJob {
    def careReceiverService

    static triggers = {
      cron name: 'withingsTrigger', cronExpression: "0 16 * * * ?"
    }

    def execute() {
        // For all CareReceivers, grab their Withings data for the preceding day
        // Providing we don't have it yet

        def careReceivers = careReceiverService.listCareReceivers()
        for (receiver in careReceivers) {
            def data = receiver.updateWithingsData(new Date() - 1)

            def activities = activityRecordService.bulkCreate(data.activity)
            if (activities) receiver.setActivityDownloadDate(activities.last().date)

            def sleeps = sleepRecordService.bulkCreate(data.sleep)
            if (sleeps) receiver.setSleepDownloadDate(receiver, sleeps.last().date)

            careReceiverService.updateCareReceiver(receiver)
        }
    }
}
