package org.etive.city4age.repository

class ProximityEventList {
    // The client waits 1.4 seconds before declaring a beacon found and 8.4 seconds before declaring a beacon lost
    // There are still occasions when a lost beacon is re-found quickly after being lost
    // This could be due to a lot of environmental factors so is impossible to mitigate against inside the phone
    // All we can hope for is that the phone doesn't generate too many events.

    // If a lost event is quickly followed by a found event then the lost and found events can safely be removed since
    // there must be a preceding "found" event and a following "lost" event. A total drop out of 15 seconds wouldn't
    // seem unreasonable.

    // On 22nd October 2017, a Care Receiver generated a large number of POI events whilst inside St. Giles Hospice.
    // In most cases, a POI_EXIT event was followed by a POI_ENTER event for the same location no more than 30 seconds
    // later. As a result, the 15 second threshold should be increased to 30 seconds.

    private static final Integer MAX_DROP_OUT = 30000
    private mList = null
    private Integer mIndex = -1
    private Integer mLength = 0

    ProximityEventList(list) {
        if (list) {
            this.mList = removeDropouts(list).reverse()
            this.mLength = this.mList.size()
        }
    }

    // Public Instance Methods

    def nextFound(beacon) {
        if (isEmpty()) return null
//        mIndex = nextFoundIndex(beacon.beaconId, mIndex + 1)
//        return (mIndex >= mLength) ? null : mList[mIndex]
        def index = nextFoundIndex(beacon.beaconId, mIndex + 1)
        return (index >= mLength) ? null : mList[index]
    }

    def nextLost() {
        if (isEmpty()) return null
        mIndex = nextLostIndex(mIndex + 1)
        return (mIndex >= mLength) ? null : mList[mIndex]
    }

    def isEmpty() {
        return ((mList && mLength > 0) ? (mIndex + 1 >= mLength) : true)
    }

    def size() {
        return mLength
    }

    def getIndex() {
        // For test purposes - not much use otherwise
        return new Integer(mIndex)
    }

    def getEntry(index) {
        // For test purposes - not much use otherwise
        if (!mList || index < 0 || index >= mLength) return null
        return mList[index]
    }

    // Private Instance Methods

    private nextFoundIndex(beaconId, start) {
        nextFoundIndex(beaconId, this.mList, start)
    }

    private nextLostIndex(start) {
        def i = new Integer(start)
        while ((i < mLength) && !isLost(mList[i])) i += 1
        return i
    }

    // Private Static Methods

    private static Boolean isDropout(list, indexA, indexB) {
        def diff = (list[indexA].timestamp.getTime() - list[indexB].timestamp.getTime()).abs()
        return (diff < MAX_DROP_OUT)
    }

    private static Boolean isLost(listEntry) {
        return (listEntry.eventType == 'lost')
    }

    private static Boolean isFound(listEntry) {
        return (listEntry.eventType == 'found')
    }

    private static nextFoundIndex(beaconId, list, start) {
        def i = new Integer(start)
        def length = list.size()
        while ((i < length) && !(isFound(list[i]) && (list[i].beacon.beaconId == beaconId))) i += 1
        return i
    }

    private static removeDropouts(eventList) {
        def list = eventList.clone()
        def i = 0
        while (i < list.size()) {
            if (isLost(list[i])) {
                def j = nextFoundIndex(list[i].beacon.beaconId, list, i + 1)
                // If j is the last item then the list ends with a found event - don't want to delete the final lost event
                if (j < list.size() - 1 && isDropout(list, i, j)) {
                    list.removeAt(i)
                    list.removeAt(j - 1) // allow for the removal of item at index i
                    // The list is two elements shorter
                    // The index of the next item to be processed is unchanged so decrement i - it will be incremented later
                    i -= 1
                }
            }
            i += 1
        }
        return list
    }
}
