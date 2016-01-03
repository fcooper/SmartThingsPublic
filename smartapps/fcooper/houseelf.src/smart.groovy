/**
 *  PassiveLiving
 *
 *  Copyright 2016 Franklin Cooper
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "PassiveLiving",
    namespace: "fcooper27",
    author: "Franklin Cooper",
    description: "System to automate the lives of elderly people.",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
    // TODO: subscribe to attributes, devices, locations, etc.
    section("Title") {
    
            input "bedroom_light", "capability.switch", required: true, multiple: false
            input "bedroom_sleep", "capability.motionSensor", required: true, multiple: false
            input "bathroom_light", "capability.switch", required: true, multiple: false
            input "hallway_light", "capability.switch", required: true, multiple: false
            input "kitchen_light", "capability.switch", required: true, multiple: false
            input "living_light", "capability.switch", required: true, multiple: false
            input "front_status", "capability.contactSensor", title: "front door contact", required: true
            input "front_lock", "capability.lock", title:"front door lock", required: true, multiple: false
    }

}

def installed() {

    initialize()
}

def updated() {

    /*
    log.debug "Updated with settings3: ${settings}"
    state.r = [1, 2, 3] as LinkedList
    
    def attrs = bedroom_light.supportedAttributes
    attrs.each {
        log.debug "${bedroom_light.displayName}, attribute ${it.name}, values: ${it.values}"
        log.debug "${bedroom_light.displayName}, attribute ${it.name}, dataType: ${it.dataType}"
    }
    */
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Initalizing"
    subscribe(bedroom_sleep, "motion.active", sleepAwake)
    subscribe(bedroom_sleep, "motion.inactive", sleepSleeping)
}


def bedTime() {
    log.debug "Its bed time!"
    bathroom_light.off()
    
    hallway_light.off()
    kitchen_light.off()
    living_light.off()

    //garage_door.close()
    
    def issue = false

    
    if (front_status.currentState("contact").value == "open") {
        // Save status to indicate something is wrong with front door
        issue = true
    }
    else {
        front_lock.lock()
    }

    if (back_status.currentState("contact").value == "open") {
        // Save status to indicate something is wrong with front door
        issue = true
    }
    else {
        back_lock.lock()
    }

    if(issue == true) {
        log.debug "Oops there is a problem. Something is still on"
        log.debug "Turn back on the lights"
        // Reminder saying what needs to be done before going to bed
        issue = true
        bedroom_light.on()
    }
    else {
        bedroom_light.off()

    }
}
def sleepAwake(evt) {
    log.debug "Person is awake"
    bedroom_light.on()
    unschedule("bedTime")
}

def sleepSleeping(evt) {
    state.PersonStatus = "Sleep"
    log.debug "Person went to sleep"

    log.debug "User is asleep enter bedtime mode in 5 secs if still sleep"
    runIn(5, bedTime)
}